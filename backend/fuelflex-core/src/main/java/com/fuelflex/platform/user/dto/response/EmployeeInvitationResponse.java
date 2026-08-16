package com.fuelflex.platform.user.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeInvitationResponse {
    private UUID employeeId;
    private boolean invitationSent;
}
