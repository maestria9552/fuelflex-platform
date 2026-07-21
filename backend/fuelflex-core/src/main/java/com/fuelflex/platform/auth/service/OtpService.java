package com.fuelflex.platform.auth.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final int OTP_MIN = 100000;
    private static final int OTP_RANGE = 900000;
    private static final int EXPIRATION_MINUTES = 30;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateCode() {
        int number = OTP_MIN + secureRandom.nextInt(OTP_RANGE);
        return String.valueOf(number);
    }

    public OffsetDateTime expirationDate() {
        return OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

    public boolean isExpired(OffsetDateTime expirationDate) {
        return expirationDate == null
                || OffsetDateTime.now().isAfter(expirationDate);
    }
}