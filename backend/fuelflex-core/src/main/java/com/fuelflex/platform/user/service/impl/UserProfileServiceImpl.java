package com.fuelflex.platform.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.user.dto.request.UpdateMyProfileRequest;
import com.fuelflex.platform.user.dto.response.MyProfileResponse;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.user.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(String authenticatedEmail) {
        return toResponse(getCurrentUser(authenticatedEmail));
    }

    @Override
    public MyProfileResponse updateMyProfile(
            String authenticatedEmail,
            UpdateMyProfileRequest request
    ) {
        User user = getCurrentUser(authenticatedEmail);
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        userRepository.findByPhoneNumber(phoneNumber)
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new BusinessException(
                            "This phone number is already registered."
                    );
                });

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhoneNumber(phoneNumber);

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

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber
                .trim()
                .replaceAll("[\\s()\\-]", "");
    }

    private MyProfileResponse toResponse(User user) {
        return MyProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
