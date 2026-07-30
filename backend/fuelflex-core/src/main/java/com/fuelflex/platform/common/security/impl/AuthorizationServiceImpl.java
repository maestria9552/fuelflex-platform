package com.fuelflex.platform.common.security.impl;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UserRepository userRepository;

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || authentication.getName() == null
                        || authentication.getName().isBlank()
        ) {
            throw new BusinessException(
                    "Utilisateur non authentifié."
            );
        }

        return userRepository
                .findByEmailIgnoreCase(
                        authentication.getName()
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Utilisateur authentifié introuvable."
                        )
                );
    }

    @Override
    public void checkOrganizationAccess(
            UUID organizationId
    ) {
        if (organizationId == null) {
            throw new BusinessException(
                    "L’identifiant de l’organisation est obligatoire."
            );
        }

        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser.getOrganization() == null) {
            throw new BusinessException(
                    "L’utilisateur authentifié n’est rattaché à aucune organisation."
            );
        }

        if (
                !authenticatedUser
                        .getOrganization()
                        .getId()
                        .equals(organizationId)
        ) {
            throw new BusinessException(
                    "Vous n’êtes pas autorisé à accéder à cette organisation."
            );
        }
    }
}