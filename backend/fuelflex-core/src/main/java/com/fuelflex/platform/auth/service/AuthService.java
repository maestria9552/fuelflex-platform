package com.fuelflex.platform.auth.service;

import com.fuelflex.platform.auth.dto.request.LoginRequest;
import com.fuelflex.platform.auth.dto.request.RegisterRequest;
import com.fuelflex.platform.auth.dto.request.ResendVerificationCodeRequest;
import com.fuelflex.platform.auth.dto.request.VerifyEmailRequest;
import com.fuelflex.platform.auth.dto.response.LoginResponse;
import com.fuelflex.platform.auth.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerificationCode(
            ResendVerificationCodeRequest request
    );

    LoginResponse login(LoginRequest request);
}