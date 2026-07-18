package com.fuelflex.platform.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fuelflex.platform.auth.dto.RegisterRequest;
import com.fuelflex.platform.auth.dto.RegisterResponse;
import com.fuelflex.platform.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }
}