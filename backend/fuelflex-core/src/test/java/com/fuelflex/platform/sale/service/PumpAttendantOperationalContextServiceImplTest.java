package com.fuelflex.platform.sale.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.operations.entity.*;
import com.fuelflex.platform.operations.repository.PumpShiftAssignmentRepository;
import com.fuelflex.platform.operations.service.PumpAttendantOperationalContextServiceImpl;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.user.entity.User;

@ExtendWith(MockitoExtension.class)
class PumpAttendantOperationalContextServiceImplTest {
    @Mock AuthorizationService auth; @Mock PumpShiftAssignmentRepository shifts;
    @Mock UserStationAssignmentRepository admin; @Mock PosConfigurationResolver resolver;
    PumpAttendantOperationalContextServiceImpl service; User attendant; PumpShiftAssignment assignment; ResolvedPosContext resolved;

    @BeforeEach void setup() {
        service = new PumpAttendantOperationalContextServiceImpl(auth, shifts, admin, resolver);
        Organization org = new Organization(); org.setId(UUID.randomUUID());
        attendant = new User(); attendant.setId(UUID.randomUUID()); attendant.setFirstName("Jean"); attendant.setLastName("K"); attendant.setOperationalCode("PMP-000001"); attendant.setOrganization(org); attendant.setEnabled(true);
        Role role = new Role(); role.setCode("PUMP_ATTENDANT"); role.setActive(true); attendant.setRoles(Set.of(role));
        Station station = new Station(); station.setId(UUID.randomUUID()); station.setName("Gombe"); station.setOrganization(org);
        OperationalDay day = new OperationalDay(); day.setId(UUID.randomUUID()); day.setOrganization(org); day.setStation(station); day.setStatus(OperationalStatus.OPEN);
        FuelMeter meter = new FuelMeter(); meter.setId(UUID.randomUUID()); meter.setName("M1");
        assignment = new PumpShiftAssignment(); assignment.setId(UUID.randomUUID()); assignment.setPumpAttendant(attendant); assignment.setOperationalDay(day); assignment.setFuelMeter(meter); assignment.setOpeningIndex(new BigDecimal("100.000")); assignment.setStatus(OperationalStatus.OPEN);
        Pump pump = new Pump(); pump.setId(UUID.randomUUID()); pump.setName("P1");
        Tank tank = new Tank(); tank.setId(UUID.randomUUID()); tank.setName("T1");
        Product product = new Product(); product.setId(UUID.randomUUID()); product.setName("Diesel");
        TariffCategory cash = new TariffCategory(); cash.setId(UUID.randomUUID());
        resolved = new ResolvedPosContext(assignment, pump, null, tank, product, null, cash, new BigDecimal("2.500"));
        when(auth.getAuthenticatedUser()).thenReturn(attendant);
    }

    @Test void attendantWithoutOpenAssignmentIsRejected() {
        when(shifts.findFirstByPumpAttendantIdAndStatusOrderByOpenedAtDesc(attendant.getId(), OperationalStatus.OPEN)).thenReturn(Optional.empty());
        assertThatThrownBy(service::requireCurrentContext).isInstanceOf(BusinessException.class).hasMessageContaining("aucun poste ouvert");
    }

    @Test void openAssignmentProducesDerivedContext() {
        when(shifts.findFirstByPumpAttendantIdAndStatusOrderByOpenedAtDesc(attendant.getId(), OperationalStatus.OPEN)).thenReturn(Optional.of(assignment));
        when(admin.existsByUserIdAndStationIdAndOrganizationIdAndValidUntilIsNull(attendant.getId(), assignment.getOperationalDay().getStation().getId(), attendant.getOrganization().getId())).thenReturn(true);
        when(resolver.resolve(assignment)).thenReturn(resolved);
        var context = service.requireCurrentContext();
        assertThat(context.pumpAttendant().id()).isEqualTo(attendant.getId());
        assertThat(context.operationalCode()).isEqualTo("PMP-000001");
        assertThat(context.stationId()).isEqualTo(assignment.getOperationalDay().getStation().getId());
        assertThat(context.pumpId()).isEqualTo(resolved.pump().getId());
        assertThat(context.fuelMeterId()).isEqualTo(assignment.getFuelMeter().getId());
        assertThat(context.tankId()).isEqualTo(resolved.tank().getId());
        assertThat(context.productId()).isEqualTo(resolved.product().getId());
        assertThat(context.cashUnitPrice()).isEqualByComparingTo("2.500");
    }
}
