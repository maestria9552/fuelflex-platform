package com.fuelflex.platform.user.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean enabled;
    private boolean invitationSent;
    private boolean invitationPending;
    private String roleCode;
    private UUID organizationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
