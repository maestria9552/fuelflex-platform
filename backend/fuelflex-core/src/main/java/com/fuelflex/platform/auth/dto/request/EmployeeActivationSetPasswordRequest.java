package com.fuelflex.platform.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeActivationSetPasswordRequest {
    @NotBlank @Email private String email;
    @NotBlank private String code;
    @NotBlank @Size(min = 8, message = "Password must contain at least 8 characters.") private String password;
    @NotBlank private String confirmPassword;
}
