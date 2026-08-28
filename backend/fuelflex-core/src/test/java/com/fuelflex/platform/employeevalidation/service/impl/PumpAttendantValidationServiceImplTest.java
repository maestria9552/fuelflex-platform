package com.fuelflex.platform.employeevalidation.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fuelflex.platform.assignment.entity.UserStationAssignment;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentService;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ReviewRequest;
import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationItem;
import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationRequest;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.employeevalidation.repository.PumpAttendantValidationHistoryRepository;
import com.fuelflex.platform.employeevalidation.repository.PumpAttendantValidationItemRepository;
import com.fuelflex.platform.employeevalidation.repository.PumpAttendantValidationNumberRepository;
import com.fuelflex.platform.employeevalidation.repository.PumpAttendantValidationRequestRepository;
import com.fuelflex.platform.notification.dto.request.CreateNotificationCommand;
import com.fuelflex.platform.notification.entity.NotificationCategory;
import com.fuelflex.platform.notification.service.NotificationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.model.Gender;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.user.service.EmployeeService;

@ExtendWith(MockitoExtension.class)
class PumpAttendantValidationServiceImplTest {

    @Mock
    private PumpAttendantValidationRequestRepository requests;
    @Mock
    private PumpAttendantValidationItemRepository items;
    @Mock
    private PumpAttendantValidationHistoryRepository history;
    @Mock
    private PumpAttendantValidationNumberRepository numbers;
    @Mock
    private UserRepository users;
    @Mock
    private UserStationAssignmentRepository stationAssignments;
    @Mock
    private StationRepository stations;
    @Mock
    private EmployeeService employees;
    @Mock
    private EmployeeAssignmentService employeeAssignments;
    @Mock
    private StationAccessService stationAccess;
    @Mock
    private NotificationService notifications;
    @Mock
    private AuthorizationService authorization;

    private PumpAttendantValidationServiceImpl service;
    private Organization organization;
    private Station station;
    private User manager;
    private User supervisor;
    private final List<User> knownUsers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        knownUsers.clear();
        service = new PumpAttendantValidationServiceImpl(
                requests, items, history, numbers, users, stationAssignments,
                stations, employees, employeeAssignments, stationAccess,
                notifications, authorization);
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        station = new Station();
        station.setId(UUID.randomUUID());
        station.setCode("ST-01");
        station.setName("Station One");
        station.setOrganization(organization);
        station.setActive(true);
        manager = user("MANAGER");
        supervisor = user("SUPERVISOR");
        org.mockito.Mockito.lenient().when(stationAssignments
                .findAllByUserIdInAndOrganizationIdAndValidUntilIsNull(
                        anyList(), eq(organization.getId())))
                .thenAnswer(invocation -> {
                    List<UUID> requestedIds = invocation.getArgument(0);
                    return knownUsers.stream()
                            .filter(user -> requestedIds.contains(user.getId()))
                            .map(this::assignment)
                            .toList();
                });
    }

    @Test
    void managerCreatesOneDraftDocumentForSeveralPreparedPumpAttendants() {
        when(authorization.getAuthenticatedUser()).thenReturn(manager);
        when(stations.findByIdAndOrganizationId(
                station.getId(), organization.getId()))
                .thenReturn(Optional.of(station));
        when(numbers.nextValue()).thenReturn(7L);
        List<User> candidates = java.util.stream.IntStream.range(0, 3)
                .mapToObj(index -> {
                    User candidate = user("PUMP_ATTENDANT");
                    candidate.setPreparedBy(manager);
                    candidate.setOperationalCode(
                            "PMP-" + String.format("%06d", index + 10));
                    candidate.setPumpAttendantValidationStatus(
                            PumpAttendantValidationStatus.PREPARATION);
                    when(users.lockByIdAndOrganizationId(
                            candidate.getId(), organization.getId()))
                            .thenReturn(Optional.of(candidate));
                    return candidate;
                }).toList();
        when(requests.saveAndFlush(
                any(PumpAttendantValidationRequest.class)))
                .thenAnswer(invocation -> {
                    PumpAttendantValidationRequest saved =
                            invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        var response = service.create(
                new com.fuelflex.platform.employeevalidation.dto.
                        PumpAttendantValidationDtos.CreateRequest(
                                station.getId(),
                                candidates.stream().map(User::getId)
                                        .collect(java.util.stream.Collectors.toSet())));

        assertThat(response.status())
                .isEqualTo(PumpAttendantValidationRequestStatus.DRAFT);
        assertThat(response.requestNumber()).isEqualTo("EMP-VAL-2026-000007");
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Iterable> savedItems =
                ArgumentCaptor.forClass(Iterable.class);
        verify(items).saveAll(savedItems.capture());
        assertThat(savedItems.getValue()).hasSize(3);
        verify(requests, times(1)).saveAndFlush(any());
        verify(notifications, never()).create(any());
    }

    @Test
    void submissionGroupsSeveralPumpAttendantsIntoOneSupervisorAction() {
        Workflow workflow = workflow(
                PumpAttendantValidationRequestStatus.DRAFT,
                PumpAttendantValidationStatus.PREPARATION, 3);
        arrangeManager(workflow);
        when(users.findEnabledByOrganizationIdAndRoleCode(
                organization.getId(), "SUPERVISOR"))
                .thenReturn(List.of(supervisor));

        var response = service.submit(workflow.request().getId());

        assertThat(response.status()).isEqualTo(
                PumpAttendantValidationRequestStatus.PENDING_SUPERVISOR_APPROVAL);
        assertThat(workflow.attendants())
                .extracting(User::getPumpAttendantValidationStatus)
                .containsOnly(
                        PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL);
        ArgumentCaptor<CreateNotificationCommand> notification =
                ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(notifications, times(1)).create(notification.capture());
        verify(employees, never()).issuePosCredential(any(User.class));
        assertThat(notification.getValue().getCategory())
                .isEqualTo(NotificationCategory.ACTION_REQUIRED);
        assertThat(notification.getValue().getResourceId())
                .isEqualTo(workflow.request().getId());
        assertThat(notification.getValue().getResourceType())
                .isEqualTo("PUMP_ATTENDANT_VALIDATION_REQUEST");
    }

    @Test
    void validationGetResponseContractContainsNoPosCredential() {
        assertThat(com.fuelflex.platform.employeevalidation.dto.
                PumpAttendantValidationDtos.Response.class.getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("credential", "posCredential", "posCredentials");
        assertThat(com.fuelflex.platform.employeevalidation.dto.
                PumpAttendantValidationDtos.ItemResponse.class.getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("credential", "posCredential", "posCredentials");
    }

    @Test
    void approvalValidatesEveryPumpAttendantAndResolvesTheAction() {
        Workflow workflow = workflow(
                PumpAttendantValidationRequestStatus.PENDING_SUPERVISOR_APPROVAL,
                PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL, 2);
        arrangeSupervisor(workflow);
        doAnswer(invocation -> {
            User attendant = invocation.getArgument(0);
            attendant.setPumpAttendantValidationStatus(
                    PumpAttendantValidationStatus.VALIDATED);
            return true;
        }).when(employees).validatePreparedPumpAttendant(any(User.class));
        when(employees.issuePosCredential(any(User.class)))
                .thenReturn("pos-secret-one", "pos-secret-two");

        var response = service.approve(
                workflow.request().getId(), new ReviewRequest("Approved"));

        assertThat(response.request().status())
                .isEqualTo(PumpAttendantValidationRequestStatus.VALIDATED);
        assertThat(workflow.attendants())
                .extracting(User::getPumpAttendantValidationStatus)
                .containsOnly(PumpAttendantValidationStatus.VALIDATED);
        assertThat(response.request().pumpAttendants())
                .extracting(item -> item.postName())
                .containsOnly("Kabeya");
        assertThat(response.request().pumpAttendants())
                .extracting(item -> item.operationalCode())
                .containsExactly("PMP-000001", "PMP-000002");
        assertThat(response.request().pumpAttendants())
                .extracting(item -> item.station().id())
                .containsOnly(station.getId());
        assertThat(response.posCredentials())
                .extracting(value -> value.credential())
                .containsExactly("pos-secret-one", "pos-secret-two");
        assertThat(response.posCredentials())
                .extracting(value -> value.operationalCode())
                .containsExactlyInAnyOrder("PMP-000001", "PMP-000002");
        verify(employees, times(2))
                .validatePreparedPumpAttendant(any(User.class));
        verify(employees, times(2)).issuePosCredential(any(User.class));
        verify(notifications).resolveRequiredActions(
                organization.getId(),
                "PUMP_ATTENDANT_VALIDATION_REQUEST",
                workflow.request().getId(), supervisor.getId());
    }

    @Test
    void returnAllowsCorrectionAndResubmissionCreatesANewAction() {
        Workflow workflow = workflow(
                PumpAttendantValidationRequestStatus.PENDING_SUPERVISOR_APPROVAL,
                PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL, 2);
        arrangeSupervisor(workflow);

        service.returnForCorrection(
                workflow.request().getId(),
                new ReviewRequest("Correct phone numbers"));

        assertThat(workflow.request().getStatus()).isEqualTo(
                PumpAttendantValidationRequestStatus.RETURNED_FOR_CORRECTION);
        assertThat(workflow.attendants())
                .extracting(User::getPumpAttendantValidationStatus)
                .containsOnly(
                        PumpAttendantValidationStatus.RETURNED_FOR_CORRECTION);

        arrangeManager(workflow);
        when(users.findEnabledByOrganizationIdAndRoleCode(
                organization.getId(), "SUPERVISOR"))
                .thenReturn(List.of(supervisor));
        service.submit(workflow.request().getId());

        assertThat(workflow.request().getStatus()).isEqualTo(
                PumpAttendantValidationRequestStatus.PENDING_SUPERVISOR_APPROVAL);
        verify(notifications).resolveRequiredActions(
                organization.getId(),
                "PUMP_ATTENDANT_VALIDATION_REQUEST",
                workflow.request().getId(), supervisor.getId());
        verify(notifications).resolveRequiredActions(
                organization.getId(),
                "PUMP_ATTENDANT_VALIDATION_REQUEST",
                workflow.request().getId(), manager.getId());
        verify(notifications, times(2)).create(any(CreateNotificationCommand.class));
    }

    @Test
    void managerCancelsReturnedDocumentWithoutValidatingPumpAttendants() {
        Workflow workflow = workflow(
                PumpAttendantValidationRequestStatus.RETURNED_FOR_CORRECTION,
                PumpAttendantValidationStatus.RETURNED_FOR_CORRECTION, 2);
        arrangeManager(workflow);

        service.cancel(workflow.request().getId(),
                new ReviewRequest("No longer needed"));

        assertThat(workflow.request().getStatus())
                .isEqualTo(PumpAttendantValidationRequestStatus.CANCELLED);
        assertThat(workflow.attendants())
                .extracting(User::getPumpAttendantValidationStatus)
                .containsOnly(PumpAttendantValidationStatus.CANCELLED);
        verify(employees, never())
                .validatePreparedPumpAttendant(any(User.class));
        verify(notifications).resolveRequiredActions(
                organization.getId(),
                "PUMP_ATTENDANT_VALIDATION_REQUEST",
                workflow.request().getId(), manager.getId());
    }

    @Test
    void rejectionIsTerminalAndDoesNotValidatePumpAttendants() {
        Workflow workflow = workflow(
                PumpAttendantValidationRequestStatus.PENDING_SUPERVISOR_APPROVAL,
                PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL, 2);
        arrangeSupervisor(workflow);

        service.reject(workflow.request().getId(),
                new ReviewRequest("Invalid documents"));

        assertThat(workflow.request().getStatus())
                .isEqualTo(PumpAttendantValidationRequestStatus.REJECTED);
        assertThat(workflow.attendants())
                .extracting(User::getPumpAttendantValidationStatus)
                .containsOnly(PumpAttendantValidationStatus.REJECTED);
        verify(employees, never())
                .validatePreparedPumpAttendant(any(User.class));
        verify(notifications).resolveRequiredActions(
                organization.getId(),
                "PUMP_ATTENDANT_VALIDATION_REQUEST",
                workflow.request().getId(), supervisor.getId());
    }

    @Test
    void crossOrganizationDocumentIsNotRevealed() {
        when(authorization.getAuthenticatedUser()).thenReturn(supervisor);
        UUID externalId = UUID.randomUUID();
        when(requests.lockByIdAndOrganizationId(
                externalId, organization.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(
                externalId, new ReviewRequest(null)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(employees, never())
                .validatePreparedPumpAttendant(any(User.class));
    }

    @Test
    void inaccessibleStationDocumentIsNotRevealedToSupervisor() {
        Workflow workflow = workflow(
                PumpAttendantValidationRequestStatus.PENDING_SUPERVISOR_APPROVAL,
                PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL, 1);
        when(authorization.getAuthenticatedUser()).thenReturn(supervisor);
        when(requests.lockByIdAndOrganizationId(
                workflow.request().getId(), organization.getId()))
                .thenReturn(Optional.of(workflow.request()));
        when(stationAccess.canAccessStation(
                supervisor, station.getId())).thenReturn(false);

        assertThatThrownBy(() -> service.approve(
                workflow.request().getId(), new ReviewRequest(null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Workflow workflow(
            PumpAttendantValidationRequestStatus requestStatus,
            PumpAttendantValidationStatus attendantStatus,
            int count
    ) {
        PumpAttendantValidationRequest request =
                new PumpAttendantValidationRequest();
        request.setId(UUID.randomUUID());
        request.setRequestNumber("EMP-VAL-2026-000001");
        request.setOrganization(organization);
        request.setStation(station);
        request.setCreatedBy(manager);
        request.setStatus(requestStatus);
        List<User> attendants = new ArrayList<>();
        List<PumpAttendantValidationItem> requestItems = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            User attendant = user("PUMP_ATTENDANT");
            attendant.setPreparedBy(manager);
            attendant.setOperationalCode(
                    "PMP-" + String.format("%06d", index + 1));
            attendant.setPumpAttendantValidationStatus(attendantStatus);
            attendants.add(attendant);
            PumpAttendantValidationItem item =
                    new PumpAttendantValidationItem();
            item.setId(UUID.randomUUID());
            item.setRequest(request);
            item.setPumpAttendant(attendant);
            item.refreshSnapshot();
            requestItems.add(item);
        }
        org.mockito.Mockito.lenient().when(items.findByRequestId(request.getId()))
                .thenReturn(requestItems);
        org.mockito.Mockito.lenient().when(history.findByRequestIdOrderByPerformedAtAscIdAsc(request.getId()))
                .thenReturn(List.of());
        attendants.forEach(attendant -> org.mockito.Mockito.lenient().when(users.lockByIdAndOrganizationId(
                attendant.getId(), organization.getId()))
                .thenReturn(Optional.of(attendant)));
        return new Workflow(request, attendants);
    }

    private void arrangeManager(Workflow workflow) {
        when(authorization.getAuthenticatedUser()).thenReturn(manager);
        when(requests.lockByIdAndOrganizationId(
                workflow.request().getId(), organization.getId()))
                .thenReturn(Optional.of(workflow.request()));
        when(stationAccess.canAccessStation(manager, station.getId()))
                .thenReturn(true);
        org.mockito.Mockito.lenient().when(stations.findByIdAndOrganizationId(
                station.getId(), organization.getId()))
                .thenReturn(Optional.of(station));
        org.mockito.Mockito.lenient().when(stationAccess.canAccessStation(
                supervisor, station.getId())).thenReturn(true);
    }

    private void arrangeSupervisor(Workflow workflow) {
        when(authorization.getAuthenticatedUser()).thenReturn(supervisor);
        when(requests.lockByIdAndOrganizationId(
                workflow.request().getId(), organization.getId()))
                .thenReturn(Optional.of(workflow.request()));
        when(stationAccess.canAccessStation(supervisor, station.getId()))
                .thenReturn(true);
    }

    private User user(String roleCode) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("First");
        user.setLastName(roleCode);
        user.setEmail(UUID.randomUUID() + "@fuelflex.test");
        user.setPhoneNumber("+243" + Math.abs(user.getId().hashCode()));
        user.setPasswordHash("hash");
        user.setOrganization(organization);
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(role(roleCode))));
        if ("PUMP_ATTENDANT".equals(roleCode)) {
            user.setPostName("Kabeya");
            user.setGender(Gender.FEMALE);
            user.setBirthPlace("Kinshasa");
            user.setBirthDate(LocalDate.of(1995, 4, 3));
            user.setAddress("Avenue Centrale 10");
        }
        knownUsers.add(user);
        return user;
    }

    private UserStationAssignment assignment(User employee) {
        UserStationAssignment assignment = new UserStationAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setOrganization(organization);
        assignment.setUser(employee);
        assignment.setStation(station);
        assignment.setValidFrom(java.time.OffsetDateTime.now());
        return assignment;
    }

    private Role role(String code) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(code);
        role.setName(code);
        role.setActive(true);
        return role;
    }

    private record Workflow(
            PumpAttendantValidationRequest request,
            List<User> attendants
    ) {
    }
}
