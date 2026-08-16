package com.fuelflex.platform.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.user.dto.request.ChangeMyPasswordRequest;
import com.fuelflex.platform.user.dto.response.MyAccountResponse;
import com.fuelflex.platform.user.service.UserAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    @GetMapping("/account")
    public MyAccountResponse getMyAccount(Authentication authentication) {
        return userAccountService.getMyAccount(authentication.getName());
    }

    @PutMapping("/password")
    public MyAccountResponse changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangeMyPasswordRequest request
    ) {
        return userAccountService.changeMyPassword(
                authentication.getName(),
                request
        );
    }
}
