package com.fuelflex.platform.assignment.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeTransferResponse {
    private UUID id;
    private EmployeeAssignmentResponse sourceAssignment;
    private EmployeeAssignmentResponse destinationAssignment;
    private UUID transferredById;
    private OffsetDateTime transferredAt;
    private OffsetDateTime effectiveAt;
    private String reason;
}
