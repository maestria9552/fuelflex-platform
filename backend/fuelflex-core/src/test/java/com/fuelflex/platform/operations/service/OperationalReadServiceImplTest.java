package com.fuelflex.platform.operations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fuelflex.platform.assignment.entity.UserStationAssignment;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.fuelmeter.repository.FuelMeterRepository;
import com.fuelflex.platform.operations.entity.OperationalDay;
import com.fuelflex.platform.operations.entity.OperationalStatus;
import com.fuelflex.platform.operations.repository.OperationalDayRepository;
import com.fuelflex.platform.operations.repository.PumpShiftAssignmentRepository;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.pump.entity.MeteringLevel;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.entity.PumpStatus;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.mapper.StationMapper;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;

@ExtendWith(MockitoExtension.class)
class OperationalReadServiceImplTest {

    @Mock AuthorizationService authorizationService;
    @Mock StationAccessService stationAccessService;
    @Mock StationRepository stationRepository;
    @Mock UserStationAssignmentRepository administrativeAssignments;
    @Mock OperationalDayRepository operationalDayRepository;
    @Mock PumpShiftAssignmentRepository shiftAssignmentRepository;
    @Mock FuelMeterRepository fuelMeterRepository;
    @Mock AssignmentMeterValidator assignmentMeterValidator;

    OperationalReadServiceImpl service;
    Organization organization;
    User manager;
    Station station;

    @BeforeEach
    void setUp() {
        service = new OperationalReadServiceImpl(
                authorizationService,
                stationAccessService,
                stationRepository,
                new StationMapper(),
                administrativeAssignments,
                operationalDayRepository,
                shiftAssignmentRepository,
                fuelMeterRepository,
                assignmentMeterValidator
        );
        organization = organization();
        manager = user(organization, "MANAGER", true, "Manager", "One");
        station = station(organization, "Station A");
        when(authorizationService.getAuthenticatedUser()).thenReturn(manager);
    }

    @Test
    void managerSeesOnlyStationsReturnedByStationAccessService() {
        when(stationAccessService.getAccessibleStationIds(manager))
                .thenReturn(Set.of(station.getId()));
        when(stationRepository.findByOrganizationIdAndIdInOrderByDisplayOrderAscNameAsc(
                organization.getId(),
                Set.of(station.getId())
        )).thenReturn(List.of(station));

        assertThat(service.accessibleStations())
                .extracting(response -> response.getId())
                .containsExactly(station.getId());
        verify(stationRepository).findByOrganizationIdAndIdInOrderByDisplayOrderAscNameAsc(
                organization.getId(),
                Set.of(station.getId())
        );
    }

    @Test
    void attendantsAreFilteredByStationOrganizationEnabledRoleAndOpenShift() {
        User eligible = user(organization, "PUMP_ATTENDANT", true, "Amina", "Zulu");
        eligible.setOperationalCode("PMP-000001");
        User disabled = user(organization, "PUMP_ATTENDANT", false, "Disabled", "User");
        User wrongRole = user(organization, "ACCOUNTANT", true, "Wrong", "Role");
        User otherOrganization = user(
                organization(), "PUMP_ATTENDANT", true, "Other", "Tenant"
        );
        User alreadyOpen = user(
                organization, "PUMP_ATTENDANT", true, "Open", "Attendant"
        );

        when(stationRepository.findByIdAndOrganizationId(station.getId(), organization.getId()))
                .thenReturn(Optional.of(station));
        when(administrativeAssignments
                .findAllByStationIdAndOrganizationIdAndValidUntilIsNull(
                        station.getId(), organization.getId()
                ))
                .thenReturn(List.of(
                        assignment(eligible, station, organization),
                        assignment(disabled, station, organization),
                        assignment(wrongRole, station, organization),
                        assignment(otherOrganization, station, organization),
                        assignment(alreadyOpen, station, organization)
                ));
        when(shiftAssignmentRepository
                .findPumpAttendantIdsByStatusAndPumpAttendantIdIn(
                        OperationalStatus.OPEN,
                        Set.of(eligible.getId(), alreadyOpen.getId())
                ))
                .thenReturn(Set.of(alreadyOpen.getId()));

        assertThat(service.eligiblePumpAttendants(station.getId()))
                .extracting(response -> response.id())
                .containsExactly(eligible.getId());
    }

    @Test
    void managerCannotReadAttendantsFromUnauthorizedStation() {
        when(stationRepository.findByIdAndOrganizationId(station.getId(), organization.getId()))
                .thenReturn(Optional.of(station));
        doThrow(new ForbiddenException("denied"))
                .when(stationAccessService).checkStationAccess(manager, station.getId());

        assertThatThrownBy(() -> service.eligiblePumpAttendants(station.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void crossOrganizationStationIdIsNotResolved() {
        UUID externalStationId = UUID.randomUUID();
        when(stationRepository.findByIdAndOrganizationId(
                externalStationId,
                organization.getId()
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eligiblePumpAttendants(externalStationId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void openAndInvalidMetersAreNotProposed() {
        OperationalDay day = new OperationalDay();
        day.setId(UUID.randomUUID());
        day.setOrganization(organization);
        day.setStation(station);
        day.setStatus(OperationalStatus.OPEN);
        FuelMeter available = meter(station, "Available");
        FuelMeter occupied = meter(station, "Occupied");
        FuelMeter inconsistent = meter(station, "Inconsistent");

        when(operationalDayRepository.findByIdAndOrganizationId(
                day.getId(), organization.getId()
        )).thenReturn(Optional.of(day));
        when(fuelMeterRepository.findActiveByStationId(
                station.getId(), FuelMeterStatus.ACTIVE
        )).thenReturn(List.of(available, occupied, inconsistent));
        when(shiftAssignmentRepository.findFuelMeterIdsByStatusAndFuelMeterIdIn(
                OperationalStatus.OPEN,
                Set.of(available.getId(), occupied.getId(), inconsistent.getId())
        )).thenReturn(Set.of(occupied.getId()));
        when(assignmentMeterValidator.validate(available, station))
                .thenReturn(available.getPump());
        when(assignmentMeterValidator.validate(inconsistent, station))
                .thenThrow(new BusinessException("invalid configuration"));

        assertThat(service.availableFuelMeters(day.getId()))
                .extracting(response -> response.id())
                .containsExactly(available.getId());
    }

    private Organization organization() {
        Organization value = new Organization();
        value.setId(UUID.randomUUID());
        return value;
    }

    private Station station(Organization owner, String name) {
        Station value = new Station();
        value.setId(UUID.randomUUID());
        value.setOrganization(owner);
        value.setName(name);
        value.setDisplayOrder(1);
        value.setActive(true);
        return value;
    }

    private User user(
            Organization owner,
            String roleCode,
            boolean enabled,
            String firstName,
            String lastName
    ) {
        Role role = new Role();
        role.setCode(roleCode);
        role.setActive(true);
        User value = new User();
        value.setId(UUID.randomUUID());
        value.setOrganization(owner);
        value.setEnabled(enabled);
        value.setFirstName(firstName);
        value.setLastName(lastName);
        value.setRoles(Set.of(role));
        return value;
    }

    private UserStationAssignment assignment(
            User user,
            Station assignedStation,
            Organization owner
    ) {
        UserStationAssignment value = new UserStationAssignment();
        value.setId(UUID.randomUUID());
        value.setUser(user);
        value.setStation(assignedStation);
        value.setOrganization(owner);
        return value;
    }

    private FuelMeter meter(Station owner, String name) {
        Pump pump = new Pump();
        pump.setId(UUID.randomUUID());
        pump.setName("Pump " + name);
        pump.setStation(owner);
        pump.setMeteringLevel(MeteringLevel.PUMP);
        pump.setStatus(PumpStatus.ACTIVE);
        pump.setActive(true);
        FuelMeter meter = new FuelMeter();
        meter.setId(UUID.randomUUID());
        meter.setCode(name.toUpperCase());
        meter.setName(name);
        meter.setCurrentIndex(new BigDecimal("100.000"));
        meter.setStatus(FuelMeterStatus.ACTIVE);
        meter.setActive(true);
        meter.setPump(pump);
        return meter;
    }
}
