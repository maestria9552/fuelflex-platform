package com.fuelflex.platform.user.service.impl;

import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.user.dto.request.ChangeMyPasswordRequest;
import com.fuelflex.platform.user.dto.response.MyAccountResponse;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.user.service.UserAccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public MyAccountResponse getMyAccount(String authenticatedEmail) {
        return toResponse(getCurrentUser(authenticatedEmail));
    }

    @Override
    public MyAccountResponse changeMyPassword(
            String authenticatedEmail,
            ChangeMyPasswordRequest request
    ) {
        User user = getCurrentUser(authenticatedEmail);
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException(
                    "New password must be different from the current password."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(OffsetDateTime.now());

        return toResponse(userRepository.save(user));
    }

    private User getCurrentUser(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new BusinessException("Authenticated user is unavailable.");
        }

        return userRepository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() ->
                        new BusinessException("Authenticated user was not found.")
                );
    }

    private MyAccountResponse toResponse(User user) {
        return MyAccountResponse.builder()
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .build();
    }
}
