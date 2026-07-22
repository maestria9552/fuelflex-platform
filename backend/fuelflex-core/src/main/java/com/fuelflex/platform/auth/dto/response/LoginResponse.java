package com.fuelflex.platform.auth.dto.response;

import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private long expiresIn;

    private UUID userId;

    private String firstName;

    private String lastName;

    private String email;

    private Set<String> roles;

    private Set<String> permissions;
}