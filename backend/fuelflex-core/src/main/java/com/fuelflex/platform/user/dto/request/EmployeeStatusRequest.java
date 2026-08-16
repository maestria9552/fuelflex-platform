package com.fuelflex.platform.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeStatusRequest {

    @NotNull(message = "Enabled status is required.")
    private Boolean enabled;
}
