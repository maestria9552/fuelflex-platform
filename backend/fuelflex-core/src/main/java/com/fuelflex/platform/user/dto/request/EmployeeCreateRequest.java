package com.fuelflex.platform.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeCreateRequest {

    @NotBlank(message = "First name is required.")
    @Size(max = 100, message = "First name must not exceed 100 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(max = 100, message = "Last name must not exceed 100 characters.")
    private String lastName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    @Size(max = 180, message = "Email must not exceed 180 characters.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Size(max = 30, message = "Phone number must not exceed 30 characters.")
    private String phoneNumber;

    @NotBlank(message = "Role code is required.")
    @Size(max = 100, message = "Role code must not exceed 100 characters.")
    private String roleCode;
}
