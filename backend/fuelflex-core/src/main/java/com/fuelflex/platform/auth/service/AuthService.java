package com.fuelflex.platform.auth.service;

import com.fuelflex.platform.auth.dto.LoginRequest;
import com.fuelflex.platform.auth.dto.LoginResponse;
import com.fuelflex.platform.auth.dto.RegisterRequest;
import com.fuelflex.platform.auth.dto.RegisterResponse;
import com.fuelflex.platform.auth.dto.ResendVerificationCodeRequest;
import com.fuelflex.platform.auth.dto.VerifyEmailRequest;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerificationCode(
            ResendVerificationCodeRequest request
    );

    LoginResponse login(LoginRequest request);
}