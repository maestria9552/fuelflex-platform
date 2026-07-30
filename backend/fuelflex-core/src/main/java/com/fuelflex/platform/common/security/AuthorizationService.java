package com.fuelflex.platform.common.security;

import java.util.UUID;

import com.fuelflex.platform.user.entity.User;

public interface AuthorizationService {

    User getAuthenticatedUser();

    void checkOrganizationAccess(
            UUID organizationId
    );
}