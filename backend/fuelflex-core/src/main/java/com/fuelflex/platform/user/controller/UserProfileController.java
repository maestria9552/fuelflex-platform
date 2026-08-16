package com.fuelflex.platform.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.user.dto.request.UpdateMyProfileRequest;
import com.fuelflex.platform.user.dto.response.MyProfileResponse;
import com.fuelflex.platform.user.service.UserProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public MyProfileResponse getMyProfile(Authentication authentication) {
        return userProfileService.getMyProfile(authentication.getName());
    }

    @PutMapping
    public MyProfileResponse updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        return userProfileService.updateMyProfile(
                authentication.getName(),
                request
        );
    }
}
