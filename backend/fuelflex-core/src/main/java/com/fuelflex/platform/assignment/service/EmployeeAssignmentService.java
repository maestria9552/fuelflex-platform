package com.fuelflex.platform.assignment.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.assignment.dto.request.EmployeeAssignmentCreateRequest;
import com.fuelflex.platform.assignment.dto.request.EmployeeAssignmentEndRequest;
import com.fuelflex.platform.assignment.dto.request.EmployeeTransferRequest;
import com.fuelflex.platform.assignment.dto.response.EmployeeAssignmentPageResponse;
import com.fuelflex.platform.assignment.dto.response.EmployeeAssignmentResponse;
import com.fuelflex.platform.assignment.dto.response.EmployeeTransferResponse;
import com.fuelflex.platform.assignment.model.AssignmentStatus;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.user.entity.User;

public interface EmployeeAssignmentService {
    EmployeeAssignmentResponse create(UUID employeeId, EmployeeAssignmentCreateRequest request);
    EmployeeAssignmentResponse assignForPumpAttendantOnboarding(
            User employee, UUID stationId, User actor, String reason);
    EmployeeAssignmentPageResponse findAll(UUID employeeId, AssignmentStatus status, int page, int size);
    EmployeeAssignmentResponse end(UUID employeeId, UUID assignmentId, EmployeeAssignmentEndRequest request);
    EmployeeTransferResponse transfer(UUID employeeId, EmployeeTransferRequest request);
    long countActiveAssignments(UUID employeeId, UUID organizationId);
    void endAllForEmployee(User employee, User actor, OffsetDateTime effectiveAt, String reason);
    void endAllForStation(Station station, User actor, OffsetDateTime effectiveAt, String reason);
}
