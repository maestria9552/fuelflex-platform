package com.fuelflex.platform.assignment.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import com.fuelflex.platform.assignment.dto.request.EmployeeAssignmentCreateRequest;
import com.fuelflex.platform.assignment.dto.request.EmployeeAssignmentEndRequest;
import com.fuelflex.platform.assignment.dto.request.EmployeeTransferRequest;
import com.fuelflex.platform.assignment.entity.EmployeeStationTransfer;
import com.fuelflex.platform.assignment.entity.UserStationAssignment;
import com.fuelflex.platform.assignment.mapper.EmployeeAssignmentMapper;
import com.fuelflex.platform.assignment.model.AssignmentStatus;
import com.fuelflex.platform.assignment.repository.EmployeeStationTransferRepository;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentPolicy;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeAssignmentServiceImplTest {
    @Mock UserStationAssignmentRepository assignmentRepository;
    @Mock EmployeeStationTransferRepository transferRepository;
    @Mock UserRepository userRepository;
    @Mock StationRepository stationRepository;
    @Mock AuthorizationService authorizationService;
    EmployeeAssignmentServiceImpl service;
    Organization organization;
    User supervisor;

    @BeforeEach void setUp() {
        service = new EmployeeAssignmentServiceImpl(assignmentRepository, transferRepository,
                userRepository, stationRepository, authorizationService,
                new EmployeeAssignmentPolicy(), new EmployeeAssignmentMapper());
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        supervisor = user("SUPERVISOR", true);
        when(authorizationService.getAuthenticatedUser()).thenReturn(supervisor);
    }

    @Test void managerCanReceiveSeveralStationsAndReassignmentAfterHistory() {
        User manager = user("MANAGER", true);
        Station station = station(true);
        arrangeLocked(manager, station);
        when(assignmentRepository.countByUserIdAndOrganizationIdAndValidUntilIsNull(
                manager.getId(), organization.getId())).thenReturn(2L);
        saveAssignments();

        var response = service.create(manager.getId(), createRequest(station.getId()));

        assertThat(response.isActive()).isTrue();
        assertThat(response.getStationId()).isEqualTo(station.getId());
    }

    @Test void pumpAttendantCannotReceiveSecondActiveStation() {
        User pumpAttendant = user("PUMP_ATTENDANT", true);
        Station station = station(true);
        arrangeLocked(pumpAttendant, station);
        when(assignmentRepository.countByUserIdAndOrganizationIdAndValidUntilIsNull(
                pumpAttendant.getId(), organization.getId())).thenReturn(1L);

        assertThatThrownBy(() -> service.create(
                pumpAttendant.getId(), createRequest(station.getId())))
                .isInstanceOf(ConflictException.class);
    }

    @Test void disabledEmployeeAndInactiveStationAreRejected() {
        User disabled = user("MANAGER", false);
        when(userRepository.lockByIdAndOrganizationId(disabled.getId(), organization.getId()))
                .thenReturn(Optional.of(disabled));
        assertThatThrownBy(() -> service.create(
                disabled.getId(), createRequest(UUID.randomUUID())))
                .isInstanceOf(ConflictException.class);

        User manager = user("MANAGER", true);
        Station inactive = station(false);
        arrangeLocked(manager, inactive);
        assertThatThrownBy(() -> service.create(manager.getId(), createRequest(inactive.getId())))
                .isInstanceOf(ConflictException.class);
    }

    @Test void crossTenantEmployeeIsNotRevealed() {
        UUID employeeId = UUID.randomUUID();
        when(userRepository.lockByIdAndOrganizationId(employeeId, organization.getId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(employeeId, createRequest(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test void transferEndsOnlySourceAndCreatesCorrelatedDestination() {
        User manager = user("MANAGER", true);
        Station sourceStation = station(true);
        Station destination = station(true);
        UserStationAssignment source = assignment(manager, sourceStation);
        when(userRepository.lockByIdAndOrganizationId(manager.getId(), organization.getId()))
                .thenReturn(Optional.of(manager));
        when(assignmentRepository.lockActive(source.getId(), manager.getId(), organization.getId()))
                .thenReturn(Optional.of(source));
        when(stationRepository.findByIdAndOrganizationId(destination.getId(), organization.getId()))
                .thenReturn(Optional.of(destination));
        saveAssignments();
        when(transferRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            EmployeeStationTransfer transfer = invocation.getArgument(0);
            transfer.setId(UUID.randomUUID());
            return transfer;
        });
        EmployeeTransferRequest request = new EmployeeTransferRequest();
        request.setSourceAssignmentId(source.getId());
        request.setDestinationStationId(destination.getId());
        request.setReason("Operational transfer");

        var response = service.transfer(manager.getId(), request);

        assertThat(source.getValidUntil()).isNotNull();
        assertThat(response.getDestinationAssignment().getStationId()).isEqualTo(destination.getId());
        verify(assignmentRepository, never()).findAllByUserIdAndOrganizationIdAndValidUntilIsNull(any(), any());
    }

    @Test void secondEndIsAConflict() {
        User manager = user("MANAGER", true);
        UserStationAssignment ended = assignment(manager, station(true));
        ended.setValidUntil(ended.getValidFrom());
        when(userRepository.findByIdAndOrganizationId(manager.getId(), organization.getId()))
                .thenReturn(Optional.of(manager));
        when(assignmentRepository.lockActive(ended.getId(), manager.getId(), organization.getId()))
                .thenReturn(Optional.empty());
        when(assignmentRepository.findByIdAndOrganizationId(ended.getId(), organization.getId()))
                .thenReturn(Optional.of(ended));
        assertThatThrownBy(() -> service.end(manager.getId(), ended.getId(),
                new EmployeeAssignmentEndRequest())).isInstanceOf(ConflictException.class);
    }

    @Test
    void disabledEmployeeHistoryIsReadable() {
        User disabled = user("MANAGER", false);
        UserStationAssignment historical = assignment(disabled, station(true));
        historical.setValidUntil(historical.getValidFrom().plusHours(8));
        historical.setEndedAt(historical.getValidUntil());
        historical.setEndedBy(supervisor);
        when(userRepository.findByIdAndOrganizationId(disabled.getId(), organization.getId()))
                .thenReturn(Optional.of(disabled));
        when(assignmentRepository.findByUserIdAndOrganizationId(
                eq(disabled.getId()), eq(organization.getId()), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(historical)));
        var page = service.findAll(disabled.getId(), AssignmentStatus.ALL, 0, 20);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().isActive()).isFalse();
        verify(userRepository, never()).lockByIdAndOrganizationId(any(), any());
    }

    @Test
    void disabledEmployeeCannotReceiveAssignment() {
        User disabled = user("MANAGER", false);
        when(userRepository.lockByIdAndOrganizationId(disabled.getId(), organization.getId()))
                .thenReturn(Optional.of(disabled));
        assertThatThrownBy(() -> service.create(
                disabled.getId(), createRequest(UUID.randomUUID())))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(stationRepository);
    }

    @Test
    void disabledEmployeeCannotBeTransferred() {
        User disabled = user("MANAGER", false);
        when(userRepository.lockByIdAndOrganizationId(disabled.getId(), organization.getId()))
                .thenReturn(Optional.of(disabled));
        EmployeeTransferRequest request = new EmployeeTransferRequest();
        request.setSourceAssignmentId(UUID.randomUUID());
        request.setDestinationStationId(UUID.randomUUID());
        assertThatThrownBy(() -> service.transfer(disabled.getId(), request))
                .isInstanceOf(ConflictException.class);
        verify(assignmentRepository, never()).lockActive(any(), any(), any());
    }

    @Test
    void crossTenantDisabledEmployeeHistoryIsNotRevealed() {
        UUID disabledEmployeeId = UUID.randomUUID();
        when(userRepository.findByIdAndOrganizationId(disabledEmployeeId, organization.getId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findAll(
                disabledEmployeeId, AssignmentStatus.ALL, 0, 20))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(assignmentRepository);
    }

    private void arrangeLocked(User employee, Station station) {
        when(userRepository.lockByIdAndOrganizationId(employee.getId(), organization.getId()))
                .thenReturn(Optional.of(employee));
        when(stationRepository.findByIdAndOrganizationId(station.getId(), organization.getId()))
                .thenReturn(Optional.of(station));
    }

    private void saveAssignments() {
        when(assignmentRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            UserStationAssignment assignment = invocation.getArgument(0);
            if (assignment.getId() == null) assignment.setId(UUID.randomUUID());
            return assignment;
        });
    }

    private EmployeeAssignmentCreateRequest createRequest(UUID stationId) {
        EmployeeAssignmentCreateRequest request = new EmployeeAssignmentCreateRequest();
        request.setStationId(stationId);
        return request;
    }

    private User user(String roleCode, boolean enabled) {
        Role role = new Role(); role.setCode(roleCode); role.setActive(true);
        User user = new User(); user.setId(UUID.randomUUID()); user.setEnabled(enabled);
        user.setOrganization(organization); user.setRoles(Set.of(role));
        return user;
    }

    private Station station(boolean active) {
        Station station = new Station(); station.setId(UUID.randomUUID()); station.setActive(active);
        station.setOrganization(organization); station.setCode("ST-" + station.getId());
        station.setName("Station " + station.getId()); return station;
    }

    private UserStationAssignment assignment(User employee, Station station) {
        UserStationAssignment assignment = new UserStationAssignment();
        assignment.setId(UUID.randomUUID()); assignment.setOrganization(organization);
        assignment.setUser(employee); assignment.setStation(station);
        assignment.setValidFrom(java.time.OffsetDateTime.now().minusDays(1));
        assignment.setCreatedAt(java.time.OffsetDateTime.now().minusDays(1));
        assignment.setCreatedBy(supervisor); return assignment;
    }
}
