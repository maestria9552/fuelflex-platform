package com.fuelflex.platform.operations.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.operations.dto.OperationalDtos.CloseDayRequest;
import com.fuelflex.platform.operations.entity.OperationalDay;
import com.fuelflex.platform.operations.entity.OperationalDaySummary;
import com.fuelflex.platform.operations.entity.OperationalStatus;
import com.fuelflex.platform.operations.entity.ShiftReconciliation;
import com.fuelflex.platform.operations.entity.TankGaugeReading;
import com.fuelflex.platform.operations.repository.DailyExpenseRepository;
import com.fuelflex.platform.operations.repository.InternalConsumptionStockMovementRepository;
import com.fuelflex.platform.operations.repository.MeteredStockMovementRepository;
import com.fuelflex.platform.operations.repository.OperationalDaySummaryRepository;
import com.fuelflex.platform.operations.repository.PumpShiftAssignmentRepository;
import com.fuelflex.platform.operations.repository.ShiftReconciliationRepository;
import com.fuelflex.platform.operations.repository.TankGaugeReadingRepository;
import com.fuelflex.platform.operations.repository.TankReturnSourceMovementRepository;
import com.fuelflex.platform.operations.repository.TankReturnStockMovementRepository;
import com.fuelflex.platform.reception.repository.ReceptionStockBalanceRepository;
import com.fuelflex.platform.sale.repository.SaleStockMovementRepository;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.repository.TankRepository;
import com.fuelflex.platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class OperationalDayClosingService {
    private final PumpShiftAssignmentRepository shifts;
    private final ShiftReconciliationRepository reconciliations;
    private final TankGaugeReadingRepository gauges;
    private final TankRepository tanks;
    private final DailyExpenseRepository expenses;
    private final ReceptionStockBalanceRepository inbound;
    private final SaleStockMovementRepository outbound;
    private final MeteredStockMovementRepository meteredStock;
    private final TankReturnSourceMovementRepository returnSourceStock;
    private final TankReturnStockMovementRepository returnedStock;
    private final OperationalDaySummaryRepository summaries;
    private final InternalConsumptionStockMovementRepository internalStock;
    private final SupervisorOperationalNotifier notifier;

    public OperationalDayClosingService(
            PumpShiftAssignmentRepository shifts,
            ShiftReconciliationRepository reconciliations,
            TankGaugeReadingRepository gauges,
            TankRepository tanks,
            DailyExpenseRepository expenses,
            ReceptionStockBalanceRepository inbound,
            SaleStockMovementRepository outbound,
            MeteredStockMovementRepository meteredStock,
            TankReturnSourceMovementRepository returnSourceStock,
            TankReturnStockMovementRepository returnedStock,
            OperationalDaySummaryRepository summaries,
            SupervisorOperationalNotifier notifier
    ) {
        this(shifts, reconciliations, gauges, tanks, expenses, inbound, outbound,
                meteredStock, returnSourceStock, returnedStock, summaries, null, notifier);
    }

    public OperationalDaySummary prepare(OperationalDay day, User actor, CloseDayRequest request) {
        long open = shifts.countByOperationalDayIdAndStatus(day.getId(), OperationalStatus.OPEN);
        if (open > 0) {
            throw new ConflictException("Impossible de clôturer la journée : " + open + " affectation(s) restent ouvertes.");
        }
        long shiftCount = shifts.countByOperationalDayId(day.getId());
        if (reconciliations.countByShiftAssignmentOperationalDayId(day.getId()) != shiftCount) {
            throw new ConflictException("Tous les rapprochements doivent être calculés avant la clôture.");
        }

        List<Tank> activeTanks = tanks.findByDepotStationIdAndActiveTrueOrderByDisplayOrderAscNameAsc(day.getStation().getId());
        List<TankGaugeReading> readings = gauges.findByOperationalDayIdOrderByTankNameAsc(day.getId());
        Set<UUID> gaugedTankIds = new HashSet<>();
        readings.forEach(reading -> gaugedTankIds.add(reading.getTank().getId()));
        List<String> missing = activeTanks.stream()
                .filter(tank -> !gaugedTankIds.contains(tank.getId()))
                .map(Tank::getName)
                .toList();
        if (!missing.isEmpty()) {
            throw new ConflictException("Jauges manquantes pour les cuves: " + String.join(", ", missing) + ".");
        }
        readings.forEach(reading -> {
            BigDecimal current = stock(reading.getTank().getId());
            reading.setTheoreticalStock(current);
            reading.setStockVariance(scale(reading.getPhysicalStock().subtract(current)));
        });
        gauges.saveAll(readings);

        List<ShiftReconciliation> recs = reconciliations.findByShiftAssignmentOperationalDayIdOrderByShiftAssignmentOpenedAtAsc(day.getId());
        BigDecimal cashVolume = sum(recs, ShiftReconciliation::getCashVolume);
        BigDecimal creditVolume = sum(recs, ShiftReconciliation::getCreditVolume);
        BigDecimal cashGross = sum(recs, ShiftReconciliation::getCashAmount);
        BigDecimal creditAmount = sum(recs, ShiftReconciliation::getCreditAmount);
        BigDecimal metered = sum(recs, ShiftReconciliation::getMeteredVolume);
        BigDecimal returned = sum(recs, ShiftReconciliation::getTankReturnVolume);
        BigDecimal internalVolume = sum(recs, ShiftReconciliation::getInternalConsumptionVolume);
        BigDecimal internalAmount = sum(recs, ShiftReconciliation::getInternalConsumptionAmount);
        BigDecimal sold = scale(cashVolume.add(creditVolume));
        BigDecimal accounted = scale(sold.add(returned).add(internalVolume));
        BigDecimal disbursedExpenses = scale(expenses.sumByDayId(day.getId()));
        BigDecimal cashNet = scale(cashGross.subtract(disbursedExpenses));
        BigDecimal theoretical = scale(readings.stream().map(TankGaugeReading::getTheoreticalStock).reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal physical = scale(readings.stream().map(TankGaugeReading::getPhysicalStock).reduce(BigDecimal.ZERO, BigDecimal::add));

        String referenceCurrency = referenceCurrency(day);
        BigDecimal physicalReference = scale(request.physicalReferenceAmount());
        BigDecimal physicalUsd = scale(request.physicalUsdAmount());
        BigDecimal exchangeRate = exchangeRate(request.usdExchangeRate());
        if ("USD".equals(referenceCurrency) && physicalUsd.signum() != 0) {
            throw new BusinessException("USD ne peut pas être une devise complémentaire lorsque la devise de référence est USD.");
        }
        BigDecimal convertedUsd = "USD".equals(referenceCurrency)
                ? BigDecimal.ZERO.setScale(3)
                : monetary(physicalUsd.multiply(exchangeRate));
        BigDecimal observedCash = scale(physicalReference.add(convertedUsd));
        BigDecimal cashVariance = scale(observedCash.subtract(cashNet));

        OperationalDaySummary summary = new OperationalDaySummary();
        summary.setOperationalDay(day);
        summary.setCashVolume(cashVolume);
        summary.setCashAmount(cashGross);
        summary.setCreditVolume(creditVolume);
        summary.setCreditAmount(creditAmount);
        summary.setMeteredVolume(metered);
        summary.setSoldVolume(sold);
        summary.setTankReturnVolume(returned);
        summary.setInternalConsumptionVolume(internalVolume);
        summary.setInternalConsumptionAmount(internalAmount);
        summary.setAccountedVolume(accounted);
        summary.setVolumeVariance(scale(metered.subtract(accounted)));
        summary.setExpenseAmount(disbursedExpenses);
        summary.setExpectedCash(cashGross);
        summary.setExpectedNetCash(cashNet);
        summary.setReferenceCurrency(referenceCurrency);
        summary.setCashGrossExpected(cashGross);
        summary.setDisbursedExpenseAmount(disbursedExpenses);
        summary.setCashNetExpected(cashNet);
        summary.setCashReconciliationAvailable(true);
        summary.setPhysicalReferenceAmount(physicalReference);
        summary.setPhysicalUsdAmount(physicalUsd);
        summary.setUsdExchangeRate(exchangeRate);
        summary.setConvertedUsdAmount(convertedUsd);
        summary.setObservedCashAmount(observedCash);
        summary.setCashVariance(cashVariance);
        summary.setTheoreticalStock(theoretical);
        summary.setPhysicalStock(physical);
        summary.setStockVariance(scale(physical.subtract(theoretical)));
        summary.setClosedBy(actor);
        summary.setClosedAt(OffsetDateTime.now());
        summary = summaries.save(summary);
        notifier.recordOperationalDayActivity(actor, day, OperationalDayActivityType.OPERATIONAL_DAY_CLOSED);
        return summary;
    }

    private BigDecimal stock(UUID tankId) {
        BigDecimal value = scale(inbound.sumInboundByTankId(tankId))
                .subtract(scale(outbound.sumOutboundByTankId(tankId)))
                .subtract(scale(meteredStock.sumOutboundByTankId(tankId)))
                .subtract(scale(returnSourceStock.sumOpenOutboundByTankId(tankId)))
                .subtract(scale(internalStock == null ? BigDecimal.ZERO : internalStock.sumOpenOutboundByTankId(tankId)))
                .add(scale(returnedStock.sumInboundByTankId(tankId)));
        if (value.signum() < 0) {
            throw new ConflictException("Le stock théorique d’une cuve est négatif.");
        }
        return scale(value);
    }

    private BigDecimal sum(List<ShiftReconciliation> values, Function<ShiftReconciliation, BigDecimal> field) {
        return scale(values.stream().map(field).map(value -> value == null ? BigDecimal.ZERO : value).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private String referenceCurrency(OperationalDay day) {
        String value = day.getOrganization() == null ? null : day.getOrganization().getDefaultCurrency();
        if (value == null || value.isBlank()) {
            throw new BusinessException("La devise de référence de l’organisation est requise.");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal exchangeRate(BigDecimal value) {
        try {
            return value.setScale(6, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BusinessException("Le taux de change accepte au maximum six décimales.");
        }
    }

    private BigDecimal monetary(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(3, RoundingMode.UNNECESSARY);
    }
}
