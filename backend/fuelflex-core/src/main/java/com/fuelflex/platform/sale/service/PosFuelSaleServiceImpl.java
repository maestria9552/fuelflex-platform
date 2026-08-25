package com.fuelflex.platform.sale.service;

import static com.fuelflex.platform.sale.dto.PosSaleDtos.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.operations.entity.OperationalStatus;
import com.fuelflex.platform.operations.entity.PumpShiftAssignment;
import com.fuelflex.platform.operations.repository.PumpShiftAssignmentRepository;
import com.fuelflex.platform.reception.repository.ReceptionStockBalanceRepository;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.sale.dto.PosSaleDtos.CreateSaleRequest;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleResponse;
import com.fuelflex.platform.sale.entity.FuelSale;
import com.fuelflex.platform.sale.entity.SaleStockMovement;
import com.fuelflex.platform.sale.entity.VehicleType;
import com.fuelflex.platform.sale.repository.FuelSaleRepository;
import com.fuelflex.platform.sale.repository.SaleNumberRepository;
import com.fuelflex.platform.sale.repository.SaleStockMovementRepository;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.repository.TankRepository;
import com.fuelflex.platform.user.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PosFuelSaleServiceImpl implements PosFuelSaleService {
    private final AuthorizationService authorization;
    private final PumpShiftAssignmentRepository shifts;
    private final UserStationAssignmentRepository administrativeAssignments;
    private final PosConfigurationResolver resolver;
    private final TankRepository tanks;
    private final ReceptionStockBalanceRepository inboundStock;
    private final SaleStockMovementRepository outboundStock;
    private final FuelSaleRepository sales;
    private final SaleNumberRepository numbers;

    @Override
    public SaleResponse create(CreateSaleRequest request) {
        User attendant = authenticatedPumpAttendant();
        PumpShiftAssignment assignment = shifts.lockOpenByPumpAttendantId(attendant.getId(), OperationalStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Le pompiste n’est actuellement affecté à aucun poste ouvert."));
        validateAssignment(attendant, assignment);
        ResolvedPosContext context = resolver.resolve(assignment);
        Tank lockedTank = tanks.lockById(context.tank().getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuve du contexte POS est introuvable."));
        if (!lockedTank.getId().equals(context.tank().getId())) {
            throw new ConflictException("La configuration de la cuve POS a changé.");
        }

        BigDecimal quantity = quantity(request.quantity());
        String licensePlate = normalizeLicensePlate(request.vehicleType(), request.licensePlate());
        BigDecimal availableStock = availableStock(lockedTank.getId());
        if (quantity.compareTo(availableStock) > 0) {
            throw new BusinessException("Stock insuffisant dans la cuve. Stock disponible: "
                    + availableStock.toPlainString() + " L.");
        }

        BigDecimal unitPrice = context.cashUnitPrice();
        BigDecimal total = quantity.multiply(unitPrice).setScale(3, RoundingMode.HALF_UP);
        FuelSale sale = new FuelSale();
        sale.setSaleNumber("SALE-" + OffsetDateTime.now().getYear() + "-" + String.format("%06d", numbers.nextValue()));
        sale.setOrganization(assignment.getOperationalDay().getOrganization());
        sale.setStation(assignment.getOperationalDay().getStation());
        sale.setOperationalDay(assignment.getOperationalDay());
        sale.setShiftAssignment(assignment);
        sale.setPumpAttendant(attendant);
        sale.setFuelMeter(context.fuelMeter());
        sale.setDispensingPoint(context.dispensingPoint());
        sale.setTank(lockedTank);
        sale.setProduct(context.product());
        sale.setTariffCategory(context.cashTariff());
        sale.setQuantity(quantity);
        sale.setUnitPrice(unitPrice);
        sale.setTotalAmount(total);
        sale.setVehicleType(request.vehicleType());
        sale.setLicensePlate(licensePlate);
        sale.setSoldAt(OffsetDateTime.now());
        sale = sales.saveAndFlush(sale);

        if (outboundStock.existsBySaleId(sale.getId())) {
            throw new ConflictException("Le mouvement stock de cette vente existe déjà.");
        }
        SaleStockMovement movement = new SaleStockMovement();
        movement.setSale(sale);
        movement.setStation(sale.getStation());
        movement.setTank(lockedTank);
        movement.setProduct(context.product());
        movement.setQuantity(quantity);
        try {
            outboundStock.saveAndFlush(movement);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Le mouvement stock de cette vente existe déjà.");
        }
        return response(sale, context.pump().getId(), context.pump().getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findCurrentOperationalDaySales() {
        User attendant = authenticatedPumpAttendant();
        ResolvedPosContext context = currentContext(attendant);
        return sales.findByPumpAttendantIdAndOperationalDayIdOrderBySoldAtDesc(
                        attendant.getId(), context.assignment().getOperationalDay().getId())
                .stream().map(this::response).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse findById(UUID saleId) {
        User attendant = authenticatedPumpAttendant();
        ResolvedPosContext context = currentContext(attendant);
        var day = context.assignment().getOperationalDay();
        FuelSale sale = sales.findByIdAndPumpAttendantIdAndOperationalDayIdAndOrganizationIdAndStationId(
                        saleId, attendant.getId(), day.getId(), day.getOrganization().getId(), day.getStation().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vente POS introuvable."));
        return response(sale);
    }

    private ResolvedPosContext currentContext(User attendant) {
        PumpShiftAssignment assignment = shifts
                .findFirstByPumpAttendantIdAndStatusOrderByOpenedAtDesc(attendant.getId(), OperationalStatus.OPEN)
                .orElseThrow(() -> new BusinessException("Le pompiste n’est actuellement affecté à aucun poste ouvert."));
        validateAssignment(attendant, assignment);
        return resolver.resolve(assignment);
    }

    private void validateAssignment(User attendant, PumpShiftAssignment assignment) {
        if (!assignment.getPumpAttendant().getId().equals(attendant.getId())) {
            throw new ForbiddenException("Cette affectation appartient à un autre pompiste.");
        }
        if (assignment.getStatus() != OperationalStatus.OPEN) {
            throw new ConflictException("L’affectation pompiste est fermée.");
        }
        if (assignment.getOperationalDay().getStatus() != OperationalStatus.OPEN) {
            throw new ConflictException("La journée opérationnelle est fermée.");
        }
        if (!assignment.getOperationalDay().getOrganization().getId().equals(attendant.getOrganization().getId())) {
            throw new ForbiddenException("L’affectation POS appartient à une autre organisation.");
        }
        if (!administrativeAssignments.existsByUserIdAndStationIdAndOrganizationIdAndValidUntilIsNull(
                attendant.getId(), assignment.getOperationalDay().getStation().getId(), attendant.getOrganization().getId())) {
            throw new ForbiddenException("Le pompiste n’est plus affecté administrativement à cette station.");
        }
    }

    private BigDecimal availableStock(UUID tankId) {
        BigDecimal inbound = defaultZero(inboundStock.sumInboundByTankId(tankId));
        BigDecimal outbound = defaultZero(outboundStock.sumOutboundByTankId(tankId));
        BigDecimal available = inbound.subtract(outbound).setScale(3, RoundingMode.UNNECESSARY);
        if (available.signum() < 0) {
            throw new ConflictException("Le stock calculé de la cuve est négatif; la vente est bloquée.");
        }
        return available;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(3) : value.setScale(3, RoundingMode.UNNECESSARY);
    }

    private BigDecimal quantity(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new BusinessException("La quantité vendue doit être strictement positive.");
        }
        try {
            return value.setScale(3, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BusinessException("La quantité vendue accepte au maximum trois décimales.");
        }
    }

    private String normalizeLicensePlate(VehicleType vehicleType, String value) {
        String normalized = value == null ? null : value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (normalized != null && normalized.isBlank()) normalized = null;
        if (vehicleType != VehicleType.MOTORCYCLE && normalized == null) {
            throw new BusinessException("La plaque d’immatriculation est obligatoire pour ce type de véhicule.");
        }
        return normalized;
    }

    private User authenticatedPumpAttendant() {
        User user = authorization.getAuthenticatedUser();
        if (user == null || !user.isEnabled() || user.getOrganization() == null
                || user.getRoles().stream().filter(Role::isActive)
                .noneMatch(role -> "PUMP_ATTENDANT".equalsIgnoreCase(role.getCode()))) {
            throw new ForbiddenException("Un compte pompiste actif est requis.");
        }
        return user;
    }

    private SaleResponse response(FuelSale sale) {
        var point = sale.getDispensingPoint();
        var pump = point == null ? sale.getFuelMeter().getPump() : point.getPump();
        return response(sale, pump.getId(), pump.getName());
    }

    private SaleResponse response(FuelSale sale, UUID pumpId, String pumpName) {
        return new SaleResponse(sale.getId(), sale.getSaleNumber(), sale.getOrganization().getId(),
                new ReferenceSummary(sale.getStation().getId(), sale.getStation().getName()),
                sale.getOperationalDay().getId(), sale.getShiftAssignment().getId(),
                new PumpAttendantSummary(sale.getPumpAttendant().getId(), sale.getPumpAttendant().getFirstName(),
                        sale.getPumpAttendant().getLastName(), sale.getPumpAttendant().getOperationalCode()),
                new ReferenceSummary(pumpId, pumpName),
                sale.getDispensingPoint() == null ? null : new ReferenceSummary(sale.getDispensingPoint().getId(), sale.getDispensingPoint().getName()),
                new ReferenceSummary(sale.getFuelMeter().getId(), sale.getFuelMeter().getName()),
                new ReferenceSummary(sale.getTank().getId(), sale.getTank().getName()),
                new ReferenceSummary(sale.getProduct().getId(), sale.getProduct().getName()),
                new ReferenceSummary(sale.getTariffCategory().getId(), sale.getTariffCategory().getName()),
                sale.getQuantity(), sale.getUnitPrice(), sale.getTotalAmount(), sale.getVehicleType(),
                sale.getLicensePlate(), sale.getSoldAt());
    }
}
