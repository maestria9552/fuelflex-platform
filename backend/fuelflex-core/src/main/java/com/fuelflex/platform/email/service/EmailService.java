package com.fuelflex.platform.email.service;

public interface EmailService {

    void sendVerificationCode(
            String email,
            String firstName,
            String verificationCode
    );

    void sendEmployeeInvitation(String email, String firstName, String lastName,
            String activationCode, java.time.OffsetDateTime expiresAt);

}
