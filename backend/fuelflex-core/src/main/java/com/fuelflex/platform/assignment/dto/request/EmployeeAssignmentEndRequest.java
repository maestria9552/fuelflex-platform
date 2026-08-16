package com.fuelflex.platform.assignment.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeAssignmentEndRequest {

    private OffsetDateTime validUntil;

    @Size(max = 500, message = "Reason must not exceed 500 characters.")
    private String reason;
}
