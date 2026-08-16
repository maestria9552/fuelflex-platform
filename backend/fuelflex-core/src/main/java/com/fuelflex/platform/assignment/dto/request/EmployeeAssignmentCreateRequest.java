package com.fuelflex.platform.assignment.dto.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeAssignmentCreateRequest {

    @NotNull(message = "Station id is required.")
    private UUID stationId;

    private OffsetDateTime validFrom;

    @Size(max = 500, message = "Reason must not exceed 500 characters.")
    private String reason;
}
