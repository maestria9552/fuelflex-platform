package com.fuelflex.platform.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    private String email;

    @NotBlank(message = "Verification code is required.")
    @Pattern(
            regexp = "\\d{6}",
            message = "Verification code must contain exactly 6 digits."
    )
    private String verificationCode;
}