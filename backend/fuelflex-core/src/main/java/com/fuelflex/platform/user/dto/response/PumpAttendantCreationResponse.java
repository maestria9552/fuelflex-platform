package com.fuelflex.platform.user.dto.response;

public record PumpAttendantCreationResponse(
        EmployeeResponse employee,
        String posCredential
) {
}
