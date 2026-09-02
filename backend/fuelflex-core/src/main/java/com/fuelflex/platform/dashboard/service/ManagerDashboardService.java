package com.fuelflex.platform.dashboard.service;

import static com.fuelflex.platform.dashboard.dto.ManagerDashboardDtos.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.dashboard.repository.ManagerDashboardRepository;
import com.fuelflex.platform.dashboard.repository.ManagerDashboardRepository.DailyProjection;
import com.fuelflex.platform.reception.repository.ReceptionStockBalanceRepository;
import com.fuelflex.platform.reception.repository.ReceptionStockBalanceRepository.BalanceProjection;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerDashboardService {
    private static final BigDecimal ZERO = new BigDecimal("0.000");
    private final AuthorizationService authorization;
    private final StationAccessService stationAccess;
    private final StationRepository stations;
    private final ManagerDashboardRepository dashboard;
    private final ReceptionStockBalanceRepository stockBalances;

    @Transactional(readOnly = true)
    public Response get(UUID stationId) {
        User actor = authorization.getAuthenticatedUser();
        if (actor == null || actor.getOrganization() == null || actor.getRoles().stream()
                .noneMatch(role -> role.isActive() && "MANAGER".equalsIgnoreCase(role.getCode()))) {
            throw new ForbiddenException("Rôle MANAGER requis.");
        }
        Set<UUID> accessible = stationAccess.getAccessibleStationIds(actor);
        if (!accessible.contains(stationId)) {
            throw new ResourceNotFoundException("Station introuvable.");
        }
        UUID organizationId = actor.getOrganization().getId();
        Station station = stations.findByIdAndOrganizationId(stationId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station introuvable."));
        LocalDate currentEnd = LocalDate.now();
        LocalDate currentStart = currentEnd.minusDays(29);
        LocalDate previousEnd = currentStart.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(29);
        List<DailyProjection> rows = dashboard.findDailySnapshots(
                organizationId, stationId, previousStart, currentEnd);
        List<DailyProjection> currentRows = rows.stream()
                .filter(row -> !row.getBusinessDate().isBefore(currentStart)).toList();
        List<DailyProjection> previousRows = rows.stream()
                .filter(row -> row.getBusinessDate().isBefore(currentStart)).toList();
        return new Response(
                station.getId(), station.getName(), actor.getOrganization().getDefaultCurrency(),
                currentStart, currentEnd, previousStart, previousEnd,
                totals(currentRows), totals(previousRows), series(currentStart, currentEnd, currentRows),
                products(stockBalances.findDashboardBalances(Set.of(stationId))));
    }

    private PeriodTotals totals(List<DailyProjection> rows) {
        BigDecimal cash = sum(rows, DailyProjection::getCash);
        BigDecimal credit = sum(rows, DailyProjection::getCredit);
        BigDecimal expenses = sum(rows, DailyProjection::getExpenses);
        return new PeriodTotals(
                scale(cash.add(credit)), cash, credit, expenses,
                sum(rows, DailyProjection::getInternalVolume),
                sum(rows, DailyProjection::getInternalAmount),
                scale(cash.subtract(expenses)));
    }

    private List<DailySales> series(LocalDate start, LocalDate end, List<DailyProjection> rows) {
        Map<LocalDate, DailyProjection> byDate = rows.stream()
                .collect(Collectors.toMap(DailyProjection::getBusinessDate, Function.identity()));
        List<DailySales> result = new ArrayList<>(30);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DailyProjection row = byDate.get(date);
            result.add(new DailySales(date, row == null ? ZERO : scale(row.getCash()),
                    row == null ? ZERO : scale(row.getCredit())));
        }
        return List.copyOf(result);
    }

    private List<ProductStock> products(List<BalanceProjection> rows) {
        return rows.stream().collect(Collectors.groupingBy(
                BalanceProjection::getProductId, java.util.LinkedHashMap::new, Collectors.toList()))
                .values().stream().map(productRows -> {
                    BalanceProjection first = productRows.getFirst();
                    List<TankStock> tanks = productRows.stream().map(row -> {
                        BigDecimal capacity = positive(row.getCapacity());
                        BigDecimal stock = positive(row.getCurrentStock());
                        return new TankStock(row.getTankId(), row.getTankName(), stock, capacity,
                                percentage(stock, capacity));
                    }).toList();
                    BigDecimal stock = tanks.stream().map(TankStock::stockQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal capacity = tanks.stream().map(TankStock::capacity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ProductStock(first.getProductId(), first.getProductName(), scale(stock),
                            scale(capacity), percentage(stock, capacity), tanks);
                }).toList();
    }

    private BigDecimal sum(List<DailyProjection> rows, Function<DailyProjection, BigDecimal> value) {
        return scale(rows.stream().map(value).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal positive(BigDecimal value) {
        return scale(value == null ? BigDecimal.ZERO : value.max(BigDecimal.ZERO));
    }

    private BigDecimal percentage(BigDecimal stock, BigDecimal capacity) {
        return capacity.signum() == 0 ? BigDecimal.ZERO
                : stock.multiply(BigDecimal.valueOf(100)).divide(capacity, 1, RoundingMode.HALF_UP)
                        .min(BigDecimal.valueOf(100));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}
