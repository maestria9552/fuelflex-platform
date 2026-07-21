package com.fuelflex.platform.email.service;

public interface EmailService {

    void sendVerificationCode(
            String email,
            String firstName,
            String verificationCode
    );

}