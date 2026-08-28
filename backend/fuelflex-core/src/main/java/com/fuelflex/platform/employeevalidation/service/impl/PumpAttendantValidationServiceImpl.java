package com.fuelflex.platform.employeevalidation.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.assignment.entity.UserStationAssignment;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentService;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.CandidateResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ApprovalResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.CreateRequest;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.HistoryResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ItemResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.PageResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.PosCredentialResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.Response;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ReviewRequest;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.StationSummary;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.UserSummary;
import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationHistory;
import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationItem;
import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationRequest;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationAction;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.employeevalidation.repository.PumpAttendantValidationHistoryRepository;
import com.fuelflex.platform.employeevalidation.repository.PumpAttendantValidationItemRepository;
import com.fuelflex.platform.employeevalidation.repository.PumpAttendantValidationNumberRepository;
import com.fuelflex.platform.employeevalidation.repository.PumpAttendantValidationRequestRepository;
import com.fuelflex.platform.employeevalidation.service.PumpAttendantValidationService;
import com.fuelflex.platform.notification.dto.request.CreateNotificationCommand;
import com.fuelflex.platform.notification.entity.NotificationCategory;
import com.fuelflex.platform.notification.service.NotificationService;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.user.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PumpAttendantValidationServiceImpl
        implements PumpAttendantValidationService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String RESOURCE_TYPE =
            "PUMP_ATTENDANT_VALIDATION_REQUEST";

    private final PumpAttendantValidationRequestRepository requests;
    private final PumpAttendantValidationItemRepository items;
    private final PumpAttendantValidationHistoryRepository history;
    private final PumpAttendantValidationNumberRepository numbers;
    private final UserRepository users;
    private final UserStationAssignmentRepository stationAssignments;
    private final StationRepository stations;
    private final EmployeeService employeeService;
    private final EmployeeAssignmentService employeeAssignments;
    private final StationAccessService stationAccess;
    private final NotificationService notifications;
    private final AuthorizationService authorization;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CandidateResponse> findManagerCandidates(
            int page,
            int size,
            String search,
            PumpAttendantValidationStatus status
    ) {
        User manager = current("MANAGER");
        PageRequest pageable = pageable(page, size);
        Page<User> result = users.findPreparedPumpAttendants(
                manager.getOrganization().getId(),
                manager.getId(),
                status,
                clean(search),
                pageable
        );
        Map<UUID, PumpAttendantValidationItem> itemByEmployee = new HashMap<>();
        if (!result.isEmpty()) {
            items.findByPumpAttendantIdIn(
                            result.getContent().stream().map(User::getId).toList())
                    .forEach(item -> itemByEmployee.put(
                            item.getPumpAttendant().getId(), item));
        }
        Map<UUID, UserStationAssignment> assignmentByEmployee =
                activeAssignments(result.getContent(),
                        manager.getOrganization().getId());
        return PageResponse.from(result.map(user -> candidate(
                user, itemByEmployee.get(user.getId()),
                assignmentByEmployee.get(user.getId()))));
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResponse findManagerCandidate(UUID pumpAttendantId) {
        User manager = current("MANAGER");
        User employee = managerCandidate(pumpAttendantId, manager, false);
        return candidate(employee,
                items.findByPumpAttendantId(employee.getId()).orElse(null),
                activeAssignment(employee, manager.getOrganization().getId()));
    }

    @Override
    public Response create(CreateRequest request) {
        User manager = current("MANAGER");
        Station station = station(request.stationId(), manager);
        List<UUID> candidateIds = request.pumpAttendantIds().stream()
                .distinct()
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        if (candidateIds.isEmpty()) {
            throw new BusinessException(
                    "At least one pump attendant is required.");
        }

        List<User> candidates = candidateIds.stream()
                .map(id -> managerCandidate(id, manager, true))
                .toList();
        candidates.forEach(candidate -> {
            if (candidate.getPumpAttendantValidationStatus()
                    != PumpAttendantValidationStatus.PREPARATION) {
                throw new ConflictException(
                        "Only pump attendants in preparation can enter a new request.");
            }
            if (items.findByPumpAttendantId(candidate.getId()).isPresent()) {
                throw new ConflictException(
                        "A pump attendant already belongs to a validation request.");
            }
            UserStationAssignment assignment = activeAssignment(
                    candidate, manager.getOrganization().getId());
            if (assignment == null
                    || !station.getId().equals(assignment.getStation().getId())) {
                throw new ConflictException(
                        "All pump attendants must be assigned to the request station.");
            }
        });

        PumpAttendantValidationRequest validationRequest =
                new PumpAttendantValidationRequest();
        validationRequest.setRequestNumber(
                "EMP-VAL-" + OffsetDateTime.now().getYear() + "-"
                        + String.format("%06d", numbers.nextValue()));
        validationRequest.setOrganization(manager.getOrganization());
        validationRequest.setStation(station);
        validationRequest.setCreatedBy(manager);
        validationRequest.setStatus(
                PumpAttendantValidationRequestStatus.DRAFT);
        requests.saveAndFlush(validationRequest);

        List<PumpAttendantValidationItem> requestItems = candidates.stream()
                .map(candidate -> validationItem(validationRequest, candidate))
                .toList();
        items.saveAll(requestItems);
        record(validationRequest, null,
                PumpAttendantValidationRequestStatus.DRAFT,
                PumpAttendantValidationAction.CREATED, manager, null);
        return response(validationRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Response> findManagerRequests(
            int page,
            int size,
            PumpAttendantValidationRequestStatus status
    ) {
        User manager = current("MANAGER");
        Page<PumpAttendantValidationRequest> result = requests.findManagerRequests(
                manager.getOrganization().getId(), manager.getId(), status,
                pageable(page, size));
        return PageResponse.from(result.map(this::response));
    }

    @Override
    @Transactional(readOnly = true)
    public Response findManagerRequest(UUID requestId) {
        User manager = current("MANAGER");
        return response(managerRequest(requestId, manager, false));
    }

    @Override
    public Response submit(UUID requestId) {
        User manager = current("MANAGER");
        PumpAttendantValidationRequest request =
                managerRequest(requestId, manager, true);
        boolean resubmission = request.getStatus()
                == PumpAttendantValidationRequestStatus.RETURNED_FOR_CORRECTION;
        if (request.getStatus() != PumpAttendantValidationRequestStatus.DRAFT
                && !resubmission) {
            throw transition(request);
        }
        List<PumpAttendantValidationItem> requestItems = items.findByRequestId(
                request.getId());
        if (requestItems.isEmpty()) {
            throw new ConflictException(
                    "Validation request must contain at least one pump attendant.");
        }

        PumpAttendantValidationStatus expected = resubmission
                ? PumpAttendantValidationStatus.RETURNED_FOR_CORRECTION
                : PumpAttendantValidationStatus.PREPARATION;
        List<User> attendants = lockAttendants(requestItems, request);
        request.setStation(commonAssignmentStation(attendants, manager));
        attendants.forEach(attendant -> {
            if (attendant.getPumpAttendantValidationStatus() != expected) {
                throw new ConflictException(
                        "A pump attendant is not ready for submission.");
            }
            attendant.setPumpAttendantValidationStatus(
                    PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL);
        });
        requestItems.forEach(PumpAttendantValidationItem::refreshSnapshot);
        users.saveAll(attendants);
        items.saveAll(requestItems);

        PumpAttendantValidationRequestStatus old = request.getStatus();
        request.setStatus(
                PumpAttendantValidationRequestStatus.PENDING_SUPERVISOR_APPROVAL);
        request.setSubmittedAt(OffsetDateTime.now());
        request.setReviewedBy(null);
        request.setReviewedAt(null);
        request.setReviewComment(null);
        record(request, old, request.getStatus(),
                resubmission
                        ? PumpAttendantValidationAction.RESUBMITTED
                        : PumpAttendantValidationAction.SUBMITTED,
                manager, null);
        if (resubmission) {
            resolveRequiredActions(request, manager);
        }
        notifySupervisors(request, manager,
                resubmission
                        ? "PUMP_ATTENDANT_VALIDATION_RESUBMITTED"
                        : "PUMP_ATTENDANT_VALIDATION_SUBMITTED");
        return response(request);
    }

    @Override
    public Response cancel(UUID requestId, ReviewRequest review) {
        User manager = current("MANAGER");
        PumpAttendantValidationRequest request =
                managerRequest(requestId, manager, true);
        if (request.getStatus() != PumpAttendantValidationRequestStatus.DRAFT
                && request.getStatus()
                        != PumpAttendantValidationRequestStatus.RETURNED_FOR_CORRECTION) {
            throw transition(request);
        }
        String reason = requiredComment(review);
        boolean returned = request.getStatus()
                == PumpAttendantValidationRequestStatus.RETURNED_FOR_CORRECTION;
        PumpAttendantValidationRequestStatus old = request.getStatus();
        List<User> attendants = lockAttendants(
                items.findByRequestId(request.getId()), request);
        attendants.forEach(attendant -> attendant
                .setPumpAttendantValidationStatus(
                        PumpAttendantValidationStatus.CANCELLED));
        attendants.forEach(attendant -> employeeAssignments.endAllForEmployee(
                attendant, manager, OffsetDateTime.now(),
                "PUMP_ATTENDANT_VALIDATION_CANCELLED"));
        users.saveAll(attendants);
        request.setStatus(PumpAttendantValidationRequestStatus.CANCELLED);
        request.setReviewComment(reason);
        record(request, old, request.getStatus(),
                PumpAttendantValidationAction.CANCELLED, manager, reason);
        if (returned) {
            resolveRequiredActions(request, manager);
        }
        notifySupervisorsInformation(request, manager,
                "PUMP_ATTENDANT_VALIDATION_CANCELLED",
                "notifications:events.pumpValidationCancelled.title",
                "notifications:events.pumpValidationCancelled.message");
        return response(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Response> findSupervisorRequests(
            int page,
            int size,
            PumpAttendantValidationRequestStatus status
    ) {
        User supervisor = current("SUPERVISOR");
        Set<UUID> stationIds = stationAccess.getAccessibleStationIds(supervisor);
        PageRequest pageable = pageable(page, size);
        if (stationIds.isEmpty()) {
            return PageResponse.from(Page.empty(pageable));
        }
        return PageResponse.from(requests.findSupervisorRequests(
                supervisor.getOrganization().getId(), stationIds, status,
                pageable).map(this::response));
    }

    @Override
    @Transactional(readOnly = true)
    public Response findSupervisorRequest(UUID requestId) {
        User supervisor = current("SUPERVISOR");
        return response(supervisorRequest(requestId, supervisor, false));
    }

    @Override
    public ApprovalResponse approve(UUID requestId, ReviewRequest review) {
        User supervisor = current("SUPERVISOR");
        PumpAttendantValidationRequest request =
                supervisorRequest(requestId, supervisor, true);
        requirePending(request);
        List<User> attendants = lockAttendants(
                items.findByRequestId(request.getId()), request);
        attendants.forEach(employeeService::validatePreparedPumpAttendant);
        List<PosCredentialResponse> credentials = attendants.stream()
                .map(attendant -> new PosCredentialResponse(
                        attendant.getId(), attendant.getOperationalCode(),
                        employeeService.issuePosCredential(attendant)))
                .toList();
        completeReview(request, supervisor,
                PumpAttendantValidationRequestStatus.VALIDATED,
                PumpAttendantValidationAction.VALIDATED,
                clean(review == null ? null : review.comment()));
        resolveRequiredActions(request, supervisor);
        notifyManagerInformation(request, supervisor,
                "PUMP_ATTENDANT_VALIDATION_APPROVED",
                "notifications:events.pumpValidationApproved.title",
                "notifications:events.pumpValidationApproved.message");
        return new ApprovalResponse(response(request), credentials);
    }

    @Override
    public Response returnForCorrection(UUID requestId, ReviewRequest review) {
        User supervisor = current("SUPERVISOR");
        PumpAttendantValidationRequest request =
                supervisorRequest(requestId, supervisor, true);
        requirePending(request);
        String reason = requiredComment(review);
        List<User> attendants = lockAttendants(
                items.findByRequestId(request.getId()), request);
        attendants.forEach(attendant -> attendant
                .setPumpAttendantValidationStatus(
                        PumpAttendantValidationStatus.RETURNED_FOR_CORRECTION));
        users.saveAll(attendants);
        completeReview(request, supervisor,
                PumpAttendantValidationRequestStatus.RETURNED_FOR_CORRECTION,
                PumpAttendantValidationAction.RETURNED_FOR_CORRECTION,
                reason);
        resolveRequiredActions(request, supervisor);
        notifyManagerAction(request, supervisor,
                "PUMP_ATTENDANT_VALIDATION_RETURNED",
                "notifications:events.pumpValidationReturned.title",
                "notifications:events.pumpValidationReturned.message");
        return response(request);
    }

    @Override
    public Response reject(UUID requestId, ReviewRequest review) {
        User supervisor = current("SUPERVISOR");
        PumpAttendantValidationRequest request =
                supervisorRequest(requestId, supervisor, true);
        requirePending(request);
        String reason = requiredComment(review);
        List<User> attendants = lockAttendants(
                items.findByRequestId(request.getId()), request);
        attendants.forEach(attendant -> attendant
                .setPumpAttendantValidationStatus(
                        PumpAttendantValidationStatus.REJECTED));
        attendants.forEach(attendant -> employeeAssignments.endAllForEmployee(
                attendant, supervisor, OffsetDateTime.now(),
                "PUMP_ATTENDANT_VALIDATION_REJECTED"));
        users.saveAll(attendants);
        completeReview(request, supervisor,
                PumpAttendantValidationRequestStatus.REJECTED,
                PumpAttendantValidationAction.REJECTED, reason);
        resolveRequiredActions(request, supervisor);
        notifyManagerInformation(request, supervisor,
                "PUMP_ATTENDANT_VALIDATION_REJECTED",
                "notifications:events.pumpValidationRejected.title",
                "notifications:events.pumpValidationRejected.message");
        return response(request);
    }

    private void completeReview(
            PumpAttendantValidationRequest request,
            User reviewer,
            PumpAttendantValidationRequestStatus status,
            PumpAttendantValidationAction action,
            String comment
    ) {
        PumpAttendantValidationRequestStatus old = request.getStatus();
        request.setStatus(status);
        request.setReviewedBy(reviewer);
        request.setReviewedAt(OffsetDateTime.now());
        request.setReviewComment(comment);
        record(request, old, status, action, reviewer, comment);
    }

    private List<User> lockAttendants(
            List<PumpAttendantValidationItem> requestItems,
            PumpAttendantValidationRequest request
    ) {
        List<User> attendants = new ArrayList<>();
        requestItems.stream()
                .map(item -> item.getPumpAttendant().getId())
                .sorted(Comparator.comparing(UUID::toString))
                .forEach(id -> {
                    User attendant = users.lockByIdAndOrganizationId(
                                    id, request.getOrganization().getId())
                            .filter(this::isPumpAttendant)
                            .filter(value -> value.getPreparedBy() != null
                                    && request.getCreatedBy().getId().equals(
                                            value.getPreparedBy().getId()))
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Pump attendant was not found."));
                    attendants.add(attendant);
                });
        return attendants;
    }

    private PumpAttendantValidationRequest managerRequest(
            UUID id, User manager, boolean lock) {
        PumpAttendantValidationRequest request = tenantRequest(
                id, manager, lock);
        if (!manager.getId().equals(request.getCreatedBy().getId())
                || !stationAccess.canAccessStation(
                        manager, request.getStation().getId())) {
            throw missingRequest();
        }
        return request;
    }

    private PumpAttendantValidationRequest supervisorRequest(
            UUID id, User supervisor, boolean lock) {
        PumpAttendantValidationRequest request = tenantRequest(
                id, supervisor, lock);
        if (!stationAccess.canAccessStation(
                supervisor, request.getStation().getId())) {
            throw missingRequest();
        }
        return request;
    }

    private PumpAttendantValidationRequest tenantRequest(
            UUID id, User actor, boolean lock) {
        if (id == null) {
            throw missingRequest();
        }
        UUID organizationId = actor.getOrganization().getId();
        return (lock
                ? requests.lockByIdAndOrganizationId(id, organizationId)
                : requests.findByIdAndOrganizationId(id, organizationId))
                .orElseThrow(this::missingRequest);
    }

    private User managerCandidate(UUID id, User manager, boolean lock) {
        if (id == null) {
            throw new ResourceNotFoundException(
                    "Prepared pump attendant was not found.");
        }
        User candidate = (lock
                ? users.lockByIdAndOrganizationId(
                        id, manager.getOrganization().getId())
                : users.findByIdAndOrganizationId(
                        id, manager.getOrganization().getId()))
                .filter(this::isPumpAttendant)
                .filter(value -> value.getPreparedBy() != null
                        && manager.getId().equals(value.getPreparedBy().getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prepared pump attendant was not found."));
        return candidate;
    }

    private Map<UUID, UserStationAssignment> activeAssignments(
            List<User> employees, UUID organizationId) {
        if (employees.isEmpty()) {
            return Map.of();
        }
        Map<UUID, UserStationAssignment> result = new HashMap<>();
        stationAssignments
                .findAllByUserIdInAndOrganizationIdAndValidUntilIsNull(
                        employees.stream().map(User::getId).toList(),
                        organizationId)
                .forEach(assignment -> {
                    UUID employeeId = assignment.getUser().getId();
                    if (result.put(employeeId, assignment) != null) {
                        throw new ConflictException(
                                "Pump attendant has incompatible active assignments.");
                    }
                });
        return result;
    }

    private UserStationAssignment activeAssignment(
            User employee, UUID organizationId) {
        return activeAssignments(List.of(employee), organizationId)
                .get(employee.getId());
    }

    private Station commonAssignmentStation(
            List<User> attendants, User manager) {
        Map<UUID, UserStationAssignment> active = activeAssignments(
                attendants, manager.getOrganization().getId());
        if (active.size() != attendants.size()) {
            throw new ConflictException(
                    "Every pump attendant must have an active station assignment.");
        }
        Set<UUID> stationIds = active.values().stream()
                .map(assignment -> assignment.getStation().getId())
                .collect(java.util.stream.Collectors.toSet());
        if (stationIds.size() != 1) {
            throw new ConflictException(
                    "All pump attendants in a request must share one station.");
        }
        return station(stationIds.iterator().next(), manager);
    }

    private Station station(UUID stationId, User manager) {
        Station station = stations.findByIdAndOrganizationId(
                        stationId, manager.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Station was not found."));
        stationAccess.checkStationAccess(manager, station.getId());
        if (!station.isActive()) {
            throw new ConflictException("Station is inactive.");
        }
        return station;
    }

    private PumpAttendantValidationItem validationItem(
            PumpAttendantValidationRequest request,
            User candidate
    ) {
        PumpAttendantValidationItem item = new PumpAttendantValidationItem();
        item.setRequest(request);
        item.setPumpAttendant(candidate);
        item.refreshSnapshot();
        return item;
    }

    private void record(
            PumpAttendantValidationRequest request,
            PumpAttendantValidationRequestStatus oldStatus,
            PumpAttendantValidationRequestStatus newStatus,
            PumpAttendantValidationAction action,
            User actor,
            String comment
    ) {
        PumpAttendantValidationHistory event =
                new PumpAttendantValidationHistory();
        event.setRequest(request);
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setAction(action);
        event.setPerformedBy(actor);
        event.setComment(comment);
        history.save(event);
    }

    private void notifySupervisors(
            PumpAttendantValidationRequest request,
            User actor,
            String eventType
    ) {
        users.findEnabledByOrganizationIdAndRoleCode(
                        request.getOrganization().getId(), "SUPERVISOR")
                .stream()
                .filter(recipient -> stationAccess.canAccessStation(
                        recipient, request.getStation().getId()))
                .forEach(recipient -> notify(recipient, request, actor,
                        eventType, NotificationCategory.ACTION_REQUIRED,
                        "notifications:events.pumpValidationSubmitted.title",
                        "notifications:events.pumpValidationSubmitted.message"));
    }

    private void notifyManagerAction(
            PumpAttendantValidationRequest request,
            User actor,
            String eventType,
            String title,
            String message
    ) {
        notify(request.getCreatedBy(), request, actor, eventType,
                NotificationCategory.ACTION_REQUIRED, title, message);
    }

    private void notifyManagerInformation(
            PumpAttendantValidationRequest request,
            User actor,
            String eventType,
            String title,
            String message
    ) {
        notify(request.getCreatedBy(), request, actor, eventType,
                NotificationCategory.INFORMATION, title, message);
    }

    private void notifySupervisorsInformation(
            PumpAttendantValidationRequest request,
            User actor,
            String eventType,
            String title,
            String message
    ) {
        users.findEnabledByOrganizationIdAndRoleCode(
                        request.getOrganization().getId(), "SUPERVISOR")
                .stream()
                .filter(recipient -> stationAccess.canAccessStation(
                        recipient, request.getStation().getId()))
                .forEach(recipient -> notify(recipient, request, actor,
                        eventType, NotificationCategory.INFORMATION,
                        title, message));
    }

    private void notify(
            User recipient,
            PumpAttendantValidationRequest request,
            User actor,
            String eventType,
            NotificationCategory category,
            String title,
            String message
    ) {
        notifications.create(CreateNotificationCommand.builder()
                .recipientId(recipient.getId())
                .organizationId(request.getOrganization().getId())
                .stationId(request.getStation().getId())
                .actorId(actor.getId())
                .eventType(eventType)
                .category(category)
                .titleKey(title)
                .messageKey(message)
                .resourceType(RESOURCE_TYPE)
                .resourceId(request.getId())
                .requiresAction(category == NotificationCategory.ACTION_REQUIRED)
                .build());
    }

    private void resolveRequiredActions(
            PumpAttendantValidationRequest request,
            User actor
    ) {
        notifications.resolveRequiredActions(
                request.getOrganization().getId(),
                RESOURCE_TYPE,
                request.getId(),
                actor.getId());
    }

    private Response response(PumpAttendantValidationRequest request) {
        boolean useSnapshots = request.getStatus()
                != PumpAttendantValidationRequestStatus.DRAFT
                && request.getStatus()
                        != PumpAttendantValidationRequestStatus.RETURNED_FOR_CORRECTION;
        List<PumpAttendantValidationItem> requestItems = items.findByRequestId(
                request.getId());
        Map<UUID, UserStationAssignment> assignmentByEmployee =
                activeAssignments(
                        requestItems.stream()
                                .map(PumpAttendantValidationItem::getPumpAttendant)
                                .toList(),
                        request.getOrganization().getId());
        List<ItemResponse> attendantResponses = requestItems.stream()
                .map(item -> item(item, useSnapshots, assignmentByEmployee.get(
                        item.getPumpAttendant().getId())))
                .toList();
        List<HistoryResponse> timeline = history
                .findByRequestIdOrderByPerformedAtAscIdAsc(request.getId())
                .stream()
                .map(event -> new HistoryResponse(
                        event.getId(), event.getAction(), event.getOldStatus(),
                        event.getNewStatus(), user(event.getPerformedBy()),
                        event.getPerformedAt(), event.getComment()))
                .toList();
        return new Response(
                request.getId(),
                request.getRequestNumber(),
                request.getOrganization().getId(),
                new StationSummary(
                        request.getStation().getId(),
                        request.getStation().getCode(),
                        request.getStation().getName()),
                request.getStatus(),
                user(request.getCreatedBy()),
                request.getSubmittedAt(),
                user(request.getReviewedBy()),
                request.getReviewedAt(),
                request.getReviewComment(),
                attendantResponses,
                timeline,
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getVersion()
        );
    }

    private ItemResponse item(
            PumpAttendantValidationItem item,
            boolean useSnapshot,
            UserStationAssignment assignment
    ) {
        User employee = item.getPumpAttendant();
        return new ItemResponse(
                item.getId(),
                employee.getId(),
                useSnapshot ? item.getFirstNameSnapshot() : employee.getFirstName(),
                useSnapshot ? item.getLastNameSnapshot() : employee.getLastName(),
                useSnapshot ? item.getPostNameSnapshot() : employee.getPostName(),
                useSnapshot ? item.getGenderSnapshot() : employee.getGender(),
                useSnapshot ? item.getBirthPlaceSnapshot() : employee.getBirthPlace(),
                useSnapshot ? item.getBirthDateSnapshot() : employee.getBirthDate(),
                useSnapshot ? item.getAddressSnapshot() : employee.getAddress(),
                useSnapshot ? item.getEmailSnapshot() : employee.getEmail(),
                useSnapshot ? item.getPhoneNumberSnapshot() : employee.getPhoneNumber(),
                useSnapshot
                        ? item.getOperationalCodeSnapshot()
                        : employee.getOperationalCode(),
                assignment == null ? null : new StationSummary(
                        assignment.getStation().getId(),
                        assignment.getStation().getCode(),
                        assignment.getStation().getName()),
                employee.getPumpAttendantValidationStatus(),
                employee.getPumpAttendantValidationStatus()
                        == PumpAttendantValidationStatus.VALIDATED
                        && !employee.isEnabled()
                        && !employee.isEmailVerified()
        );
    }

    private CandidateResponse candidate(
            User employee,
            PumpAttendantValidationItem item,
            UserStationAssignment assignment
    ) {
        PumpAttendantValidationRequest request = item == null
                ? null : item.getRequest();
        return new CandidateResponse(
                employee.getId(), employee.getFirstName(), employee.getLastName(),
                employee.getPostName(), employee.getGender(),
                employee.getBirthPlace(), employee.getBirthDate(),
                employee.getAddress(), employee.getEmail(),
                employee.getPhoneNumber(), employee.getOperationalCode(),
                assignment == null ? null : new StationSummary(
                        assignment.getStation().getId(),
                        assignment.getStation().getCode(),
                        assignment.getStation().getName()),
                employee.getPumpAttendantValidationStatus(),
                request == null ? null : request.getId(),
                request == null ? null : request.getRequestNumber(),
                request == null ? null : request.getStatus(),
                employee.getCreatedAt(), employee.getUpdatedAt());
    }

    private UserSummary user(User user) {
        return user == null ? null : new UserSummary(
                user.getId(), user.getFirstName(), user.getLastName());
    }

    private User current(String roleCode) {
        User actor = authorization.getAuthenticatedUser();
        if (actor == null || !actor.isEnabled()
                || actor.getRoles().stream()
                        .noneMatch(role -> role.isActive()
                                && roleCode.equalsIgnoreCase(role.getCode()))) {
            throw new ForbiddenException("Role required: " + roleCode + ".");
        }
        if (actor.getOrganization() == null
                || actor.getOrganization().getId() == null) {
            throw new ForbiddenException("Organization is required.");
        }
        return actor;
    }

    private boolean isPumpAttendant(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.isActive()
                        && "PUMP_ATTENDANT".equalsIgnoreCase(role.getCode()));
    }

    private void requirePending(PumpAttendantValidationRequest request) {
        if (request.getStatus()
                != PumpAttendantValidationRequestStatus.PENDING_SUPERVISOR_APPROVAL) {
            throw transition(request);
        }
    }

    private ConflictException transition(
            PumpAttendantValidationRequest request) {
        return new ConflictException(
                "Transition is not allowed from " + request.getStatus() + ".");
    }

    private String requiredComment(ReviewRequest request) {
        String comment = clean(request == null ? null : request.comment());
        if (comment == null) {
            throw new BusinessException("A reason is required.");
        }
        return comment;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private PageRequest pageable(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Page must be zero or greater.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    "Page size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }
        return PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id")));
    }

    private ResourceNotFoundException missingRequest() {
        return new ResourceNotFoundException(
                "Pump attendant validation request was not found.");
    }
}
