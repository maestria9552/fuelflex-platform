package com.fuelflex.platform.assignment.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeTransferRequest {

    @NotNull(message = "Source assignment id is required.")
    private UUID sourceAssignmentId;

    @NotNull(message = "Destination station id is required.")
    private UUID destinationStationId;

    private OffsetDateTime effectiveAt;

    @Size(max = 500, message = "Reason must not exceed 500 characters.")
    private String reason;
}
