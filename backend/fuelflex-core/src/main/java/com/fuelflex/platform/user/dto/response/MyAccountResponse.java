package com.fuelflex.platform.user.dto.response;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyAccountResponse {

    private String email;

    private boolean emailVerified;

    private OffsetDateTime lastLoginAt;

    private OffsetDateTime passwordChangedAt;
}
