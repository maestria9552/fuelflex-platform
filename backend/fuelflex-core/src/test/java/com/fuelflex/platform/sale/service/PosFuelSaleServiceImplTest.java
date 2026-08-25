package com.fuelflex.platform.sale.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.*;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.operations.entity.*;
import com.fuelflex.platform.operations.repository.PumpShiftAssignmentRepository;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.reception.repository.ReceptionStockBalanceRepository;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.sale.dto.PosSaleDtos.CreateSaleRequest;
import com.fuelflex.platform.sale.entity.*;
import com.fuelflex.platform.sale.repository.*;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.repository.TankRepository;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.user.entity.User;

@ExtendWith(MockitoExtension.class)
class PosFuelSaleServiceImplTest {
    @Mock AuthorizationService auth; @Mock PumpShiftAssignmentRepository shifts; @Mock UserStationAssignmentRepository admin;
    @Mock PosConfigurationResolver resolver; @Mock TankRepository tanks; @Mock ReceptionStockBalanceRepository inbound;
    @Mock SaleStockMovementRepository outbound; @Mock FuelSaleRepository sales; @Mock SaleNumberRepository numbers;
    PosFuelSaleServiceImpl service; User attendant; PumpShiftAssignment assignment; FuelMeter meter; Tank tank; ResolvedPosContext context;

    @BeforeEach void setup() {
        service = new PosFuelSaleServiceImpl(auth, shifts, admin, resolver, tanks, inbound, outbound, sales, numbers);
        Organization org = new Organization(); org.setId(UUID.randomUUID());
        Station station = new Station(); station.setId(UUID.randomUUID()); station.setName("Station"); station.setOrganization(org);
        attendant = user(org, "PUMP_ATTENDANT"); attendant.setOperationalCode("PMP-000001");
        OperationalDay day = new OperationalDay(); day.setId(UUID.randomUUID()); day.setOrganization(org); day.setStation(station); day.setStatus(OperationalStatus.OPEN);
        meter = new FuelMeter(); meter.setId(UUID.randomUUID()); meter.setName("Meter"); meter.setCurrentIndex(new BigDecimal("5000.000"));
        assignment = new PumpShiftAssignment(); assignment.setId(UUID.randomUUID()); assignment.setPumpAttendant(attendant); assignment.setOperationalDay(day); assignment.setFuelMeter(meter); assignment.setStatus(OperationalStatus.OPEN); assignment.setOpeningIndex(new BigDecimal("5000.000"));
        Pump pump = new Pump(); pump.setId(UUID.randomUUID()); pump.setName("Pump"); meter.setPump(pump);
        Product product = new Product(); product.setId(UUID.randomUUID()); product.setName("Diesel");
        tank = new Tank(); tank.setId(UUID.randomUUID()); tank.setName("Tank"); tank.setProduct(product);
        TariffCategory cash = new TariffCategory(); cash.setId(UUID.randomUUID()); cash.setName("Cash");
        StationProduct stationProduct = new StationProduct(); stationProduct.setId(UUID.randomUUID());
        context = new ResolvedPosContext(assignment, pump, null, tank, product, stationProduct, cash, new BigDecimal("2.500"));
        lenient().when(auth.getAuthenticatedUser()).thenReturn(attendant);
        lenient().when(shifts.lockOpenByPumpAttendantId(attendant.getId(), OperationalStatus.OPEN)).thenReturn(Optional.of(assignment));
        lenient().when(admin.existsByUserIdAndStationIdAndOrganizationIdAndValidUntilIsNull(attendant.getId(), station.getId(), org.getId())).thenReturn(true);
        lenient().when(resolver.resolve(assignment)).thenReturn(context);
        lenient().when(tanks.lockById(tank.getId())).thenReturn(Optional.of(tank));
        lenient().when(inbound.sumInboundByTankId(tank.getId())).thenReturn(new BigDecimal("1000.000"));
        lenient().when(outbound.sumOutboundByTankId(tank.getId())).thenReturn(BigDecimal.ZERO);
        lenient().when(numbers.nextValue()).thenReturn(1L);
        lenient().when(sales.saveAndFlush(any())).thenAnswer(invocation -> { FuelSale sale = invocation.getArgument(0); sale.setId(UUID.randomUUID()); return sale; });
    }

    @Test void createsCashSaleWithServerPriceTotalNormalizedPlateAndOneMovement() {
        var result = service.create(request("100.000", VehicleType.CAR, "  abc   123 "));
        assertThat(result.unitPrice()).isEqualByComparingTo("2.500");
        assertThat(result.totalAmount()).isEqualByComparingTo("250.000");
        assertThat(result.licensePlate()).isEqualTo("ABC 123");
        ArgumentCaptor<SaleStockMovement> movement = ArgumentCaptor.forClass(SaleStockMovement.class);
        verify(outbound, times(1)).saveAndFlush(movement.capture());
        assertThat(movement.getValue().getQuantity()).isEqualByComparingTo("100.000");
    }

    @Test void priceIsSnapshottedAndNotAffectedByLaterConfigurationChange() {
        var result = service.create(request("10", VehicleType.CAR, "A1"));
        context = new ResolvedPosContext(assignment, context.pump(), null, tank, context.product(), context.stationProduct(), context.cashTariff(), new BigDecimal("3.000"));
        assertThat(result.unitPrice()).isEqualByComparingTo("2.500");
    }

    @Test void motorcycleMayOmitLicensePlate() { assertThat(service.create(request("1", VehicleType.MOTORCYCLE, null)).licensePlate()).isNull(); }
    @Test void otherVehiclesRequireLicensePlate() { assertThatThrownBy(() -> service.create(request("1", VehicleType.TRUCK, " "))).isInstanceOf(BusinessException.class).hasMessageContaining("plaque"); }
    @Test void nonPositiveQuantityIsRejected() { assertThatThrownBy(() -> service.create(request("0", VehicleType.CAR, "A1"))).isInstanceOf(BusinessException.class).hasMessageContaining("quantité"); }

    @Test void closedShiftIsRejected() {
        assignment.setStatus(OperationalStatus.CLOSED);
        assertThatThrownBy(() -> service.create(request("1", VehicleType.CAR, "A1"))).isInstanceOf(ConflictException.class).hasMessageContaining("affectation");
    }
    @Test void closedOperationalDayIsRejected() {
        assignment.getOperationalDay().setStatus(OperationalStatus.CLOSED);
        assertThatThrownBy(() -> service.create(request("1", VehicleType.CAR, "A1"))).isInstanceOf(ConflictException.class).hasMessageContaining("journée");
    }
    @Test void assignmentOfAnotherAttendantIsRejected() {
        assignment.setPumpAttendant(user(attendant.getOrganization(), "PUMP_ATTENDANT"));
        assertThatThrownBy(() -> service.create(request("1", VehicleType.CAR, "A1"))).isInstanceOf(ForbiddenException.class);
    }

    @Test void reception1000ThenSale100LeavesDashboardStock900() {
        service.create(request("100", VehicleType.CAR, "A1"));
        ArgumentCaptor<SaleStockMovement> movement = ArgumentCaptor.forClass(SaleStockMovement.class);
        verify(outbound).saveAndFlush(movement.capture());
        assertThat(new BigDecimal("1000.000").subtract(movement.getValue().getQuantity())).isEqualByComparingTo("900.000");
    }

    @Test void saleAboveStockIsRejectedWithoutSaleOrMovement() {
        assertThatThrownBy(() -> service.create(request("1000.001", VehicleType.CAR, "A1"))).isInstanceOf(BusinessException.class).hasMessageContaining("Stock insuffisant");
        verify(sales, never()).saveAndFlush(any()); verify(outbound, never()).saveAndFlush(any());
    }

    @Test void priorConcurrentSalesAreIncludedAfterTankLock() {
        when(outbound.sumOutboundByTankId(tank.getId())).thenReturn(new BigDecimal("900.000"));
        assertThatThrownBy(() -> service.create(request("100.001", VehicleType.CAR, "A1"))).isInstanceOf(BusinessException.class);
        verify(tanks).lockById(tank.getId()); verify(sales, never()).saveAndFlush(any());
    }

    @Test void duplicateMovementIsRejected() {
        when(outbound.existsBySaleId(any())).thenReturn(true);
        assertThatThrownBy(() -> service.create(request("1", VehicleType.CAR, "A1"))).isInstanceOf(ConflictException.class).hasMessageContaining("mouvement stock");
        verify(outbound, never()).saveAndFlush(any());
    }

    @Test void fuelMeterIndexNeverChangesDuringSale() {
        BigDecimal before = meter.getCurrentIndex(); service.create(request("50", VehicleType.CAR, "A1"));
        assertThat(meter.getCurrentIndex()).isEqualByComparingTo(before);
    }

    private CreateSaleRequest request(String quantity, VehicleType type, String plate) { return new CreateSaleRequest(new BigDecimal(quantity), type, plate); }
    private User user(Organization org, String code) { User user = new User(); user.setId(UUID.randomUUID()); user.setFirstName("Pump"); user.setLastName("Attendant"); user.setOrganization(org); user.setEnabled(true); Role role = new Role(); role.setCode(code); role.setActive(true); user.setRoles(Set.of(role)); return user; }
}
