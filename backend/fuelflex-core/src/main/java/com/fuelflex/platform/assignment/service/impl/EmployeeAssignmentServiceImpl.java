package com.fuelflex.platform.assignment.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.assignment.dto.request.EmployeeAssignmentCreateRequest;
import com.fuelflex.platform.assignment.dto.request.EmployeeAssignmentEndRequest;
import com.fuelflex.platform.assignment.dto.request.EmployeeTransferRequest;
import com.fuelflex.platform.assignment.dto.response.EmployeeAssignmentPageResponse;
import com.fuelflex.platform.assignment.dto.response.EmployeeAssignmentResponse;
import com.fuelflex.platform.assignment.dto.response.EmployeeTransferResponse;
import com.fuelflex.platform.assignment.entity.EmployeeStationTransfer;
import com.fuelflex.platform.assignment.entity.UserStationAssignment;
import com.fuelflex.platform.assignment.mapper.EmployeeAssignmentMapper;
import com.fuelflex.platform.assignment.model.AssignmentStatus;
import com.fuelflex.platform.assignment.repository.EmployeeStationTransferRepository;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentPolicy;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentService;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeAssignmentServiceImpl implements EmployeeAssignmentService {
    private static final int MAX_PAGE_SIZE = 100;

    private final UserStationAssignmentRepository assignmentRepository;
    private final EmployeeStationTransferRepository transferRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final AuthorizationService authorizationService;
    private final EmployeeAssignmentPolicy assignmentPolicy;
    private final EmployeeAssignmentMapper assignmentMapper;

    @Override
    public EmployeeAssignmentResponse create(
            UUID employeeId, EmployeeAssignmentCreateRequest request) {
        Context context = lockedContext(employeeId);
        requireEnabled(context.employee());
        Station station = getActiveStation(request.getStationId(), context.organization().getId());
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime validFrom = request.getValidFrom() == null ? now : request.getValidFrom();
        rejectFuture(validFrom, now, "Assignment start date cannot be in the future.");
        UserStationAssignment assignment = createAssignment(
                context.employee(), station, context.actor(), validFrom, request.getReason());
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeAssignmentPageResponse findAll(
            UUID employeeId, AssignmentStatus status, int page, int size) {
        Context context = context(employeeId);
        PageRequest pageable = PageRequest.of(validatePage(page), validateSize(size),
                Sort.by(Sort.Direction.DESC, "validFrom")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"))
                        .and(Sort.by(Sort.Direction.DESC, "id")));
        AssignmentStatus effectiveStatus = status == null ? AssignmentStatus.ALL : status;
        Page<UserStationAssignment> assignments = switch (effectiveStatus) {
            case ACTIVE -> assignmentRepository
                    .findByUserIdAndOrganizationIdAndValidUntilIsNull(
                            employeeId, context.organization().getId(), pageable);
            case ENDED -> assignmentRepository
                    .findByUserIdAndOrganizationIdAndValidUntilIsNotNull(
                            employeeId, context.organization().getId(), pageable);
            case ALL -> assignmentRepository.findByUserIdAndOrganizationId(
                    employeeId, context.organization().getId(), pageable);
        };
        return EmployeeAssignmentPageResponse.from(assignments.map(assignmentMapper::toResponse));
    }

    @Override
    public EmployeeAssignmentResponse end(
            UUID employeeId, UUID assignmentId, EmployeeAssignmentEndRequest request) {
        Context context = context(employeeId);
        UserStationAssignment assignment = lockActive(
                assignmentId, employeeId, context.organization().getId());
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime effectiveAt = request.getValidUntil() == null ? now : request.getValidUntil();
        endAssignment(assignment, context.actor(), effectiveAt, now, request.getReason());
        return assignmentMapper.toResponse(assignmentRepository.saveAndFlush(assignment));
    }

    @Override
    public EmployeeTransferResponse transfer(UUID employeeId, EmployeeTransferRequest request) {
        Context context = lockedContext(employeeId);
        requireEnabled(context.employee());
        UserStationAssignment source = lockActive(
                request.getSourceAssignmentId(), employeeId, context.organization().getId());
        Station destination = getActiveStation(
                request.getDestinationStationId(), context.organization().getId());
        if (source.getStation().getId().equals(destination.getId())) {
            throw new ConflictException("Destination station must differ from source station.");
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime effectiveAt = request.getEffectiveAt() == null ? now : request.getEffectiveAt();
        endAssignment(source, context.actor(), effectiveAt, now, request.getReason());
        assignmentRepository.saveAndFlush(source);

        UserStationAssignment destinationAssignment = createAssignment(
                context.employee(), destination, context.actor(), effectiveAt, request.getReason());
        EmployeeStationTransfer transfer = new EmployeeStationTransfer();
        transfer.setOrganization(context.organization());
        transfer.setEmployee(context.employee());
        transfer.setSourceAssignment(source);
        transfer.setDestinationAssignment(destinationAssignment);
        transfer.setTransferredBy(context.actor());
        transfer.setTransferredAt(now);
        transfer.setEffectiveAt(effectiveAt);
        transfer.setReason(normalizeReason(request.getReason()));
        transfer = transferRepository.saveAndFlush(transfer);
        return EmployeeTransferResponse.builder()
                .id(transfer.getId())
                .sourceAssignment(assignmentMapper.toResponse(source))
                .destinationAssignment(assignmentMapper.toResponse(destinationAssignment))
                .transferredById(context.actor().getId())
                .transferredAt(now)
                .effectiveAt(effectiveAt)
                .reason(transfer.getReason())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveAssignments(UUID employeeId, UUID organizationId) {
        return assignmentRepository.countByUserIdAndOrganizationIdAndValidUntilIsNull(
                employeeId, organizationId);
    }

    @Override
    public void endAllForEmployee(
            User employee, User actor, OffsetDateTime effectiveAt, String reason) {
        List<UserStationAssignment> assignments = assignmentRepository
                .findAllByUserIdAndOrganizationIdAndValidUntilIsNull(
                        employee.getId(), employee.getOrganization().getId());
        endAll(assignments, actor, effectiveAt, reason);
    }

    @Override
    public void endAllForStation(
            Station station, User actor, OffsetDateTime effectiveAt, String reason) {
        List<UserStationAssignment> assignments = assignmentRepository
                .findAllByStationIdAndOrganizationIdAndValidUntilIsNull(
                        station.getId(), station.getOrganization().getId());
        endAll(assignments, actor, effectiveAt, reason);
    }

    private void endAll(
            List<UserStationAssignment> assignments,
            User actor,
            OffsetDateTime effectiveAt,
            String reason) {
        OffsetDateTime now = OffsetDateTime.now();
        assignments.forEach(assignment ->
                endAssignment(assignment, actor, effectiveAt, now, reason));
        assignmentRepository.saveAll(assignments);
    }

    private UserStationAssignment createAssignment(
            User employee, Station station, User actor,
            OffsetDateTime validFrom, String reason) {
        UUID organizationId = employee.getOrganization().getId();
        if (assignmentRepository.existsByUserIdAndStationIdAndOrganizationIdAndValidUntilIsNull(
                employee.getId(), station.getId(), organizationId)) {
            throw new ConflictException("An active assignment already exists for this station.");
        }
        assignmentPolicy.checkNewAssignment(employee,
                assignmentRepository.countByUserIdAndOrganizationIdAndValidUntilIsNull(
                        employee.getId(), organizationId));
        UserStationAssignment assignment = new UserStationAssignment();
        assignment.setOrganization(employee.getOrganization());
        assignment.setUser(employee);
        assignment.setStation(station);
        assignment.setValidFrom(validFrom);
        assignment.setCreatedBy(actor);
        assignment.setCreatedAt(OffsetDateTime.now());
        assignment.setReason(normalizeReason(reason));
        try {
            return assignmentRepository.saveAndFlush(assignment);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("The assignment conflicts with an active assignment.");
        }
    }

    private void endAssignment(
            UserStationAssignment assignment, User actor,
            OffsetDateTime effectiveAt, OffsetDateTime endedAt, String reason) {
        rejectFuture(effectiveAt, endedAt, "Assignment end date cannot be in the future.");
        if (effectiveAt.isBefore(assignment.getValidFrom())) {
            throw new BusinessException("Assignment end date must not precede its start date.");
        }
        assignment.setValidUntil(effectiveAt);
        assignment.setEndedBy(actor);
        assignment.setEndedAt(endedAt);
        assignment.setReason(normalizeReason(reason));
    }

    private UserStationAssignment lockActive(UUID assignmentId, UUID employeeId, UUID organizationId) {
        return assignmentRepository.lockActive(assignmentId, employeeId, organizationId)
                .orElseGet(() -> {
                    if (assignmentRepository.findByIdAndOrganizationId(assignmentId, organizationId)
                            .filter(a -> a.getUser().getId().equals(employeeId)).isPresent()) {
                        throw new ConflictException("Assignment is already ended.");
                    }
                    throw new ResourceNotFoundException("Assignment was not found.");
                });
    }

    private Context lockedContext(UUID employeeId) {
        User actor = authorizationService.getAuthenticatedUser();
        if (actor.getOrganization() == null || actor.getOrganization().getId() == null) {
            throw new ForbiddenException("Authenticated supervisor has no organization.");
        }
        User employee = userRepository.lockByIdAndOrganizationId(
                        employeeId, actor.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee was not found."));
        assignmentPolicy.requireAssignableRole(employee);
        return new Context(actor, actor.getOrganization(), employee);
    }

    private Context context(UUID employeeId) {
        User actor = authorizationService.getAuthenticatedUser();
        if (actor.getOrganization() == null || actor.getOrganization().getId() == null) {
            throw new ForbiddenException("Authenticated supervisor has no organization.");
        }
        User employee = userRepository.findByIdAndOrganizationId(
                        employeeId, actor.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee was not found."));
        assignmentPolicy.requireAssignableRole(employee);
        return new Context(actor, actor.getOrganization(), employee);
    }

    private void requireEnabled(User employee) {
        if (!employee.isEnabled()) {
            throw new ConflictException("A disabled employee cannot receive station assignments.");
        }
    }

    private Station getActiveStation(UUID stationId, UUID organizationId) {
        Station station = stationRepository.findByIdAndOrganizationId(stationId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station was not found."));
        if (!station.isActive()) {
            throw new ConflictException("An inactive station cannot receive assignments.");
        }
        return station;
    }

    private void rejectFuture(OffsetDateTime value, OffsetDateTime now, String message) {
        if (value.isAfter(now)) {
            throw new BusinessException(message);
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim().replaceAll("\\s+", " ");
    }

    private int validatePage(int page) {
        if (page < 0) throw new BusinessException("Page must be zero or greater.");
        return page;
    }

    private int validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException("Page size must be between 1 and 100.");
        }
        return size;
    }

    private record Context(User actor, Organization organization, User employee) {
    }
}
