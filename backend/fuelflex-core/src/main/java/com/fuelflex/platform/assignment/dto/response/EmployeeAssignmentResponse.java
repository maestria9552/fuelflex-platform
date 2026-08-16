package com.fuelflex.platform.assignment.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeAssignmentResponse {
    private UUID id;
    private UUID employeeId;
    private UUID stationId;
    private String stationName;
    private String stationCode;
    private OffsetDateTime validFrom;
    private OffsetDateTime validUntil;
    private boolean active;
    private UUID createdById;
    private OffsetDateTime createdAt;
    private UUID endedById;
    private OffsetDateTime endedAt;
    private String reason;
}
