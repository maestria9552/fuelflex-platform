package com.fuelflex.platform.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fuelflex.platform.auth.dto.RegisterRequest;
import com.fuelflex.platform.auth.dto.RegisterResponse;
import com.fuelflex.platform.auth.dto.VerifyEmailRequest;
import com.fuelflex.platform.auth.service.AuthService;
import com.fuelflex.platform.auth.dto.ResendVerificationCodeRequest;
import com.fuelflex.platform.auth.dto.LoginRequest;
import com.fuelflex.platform.auth.dto.LoginResponse;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new supervisor account.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return authService.register(request);
    }

    /**
     * Verify email using the OTP code.
     */
    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public String verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {

        authService.verifyEmail(request);

        return "Email verified successfully.";
    }
    @PostMapping("/resend-code")
    @ResponseStatus(HttpStatus.OK)
    public String resendVerificationCode(
            @Valid @RequestBody ResendVerificationCodeRequest request
    ) {

        authService.resendVerificationCode(request);

        return "A new verification code has been sent to your email address.";
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

}