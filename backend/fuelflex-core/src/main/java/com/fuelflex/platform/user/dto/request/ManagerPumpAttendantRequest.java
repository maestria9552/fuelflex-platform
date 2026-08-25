package com.fuelflex.platform.user.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.fuelflex.platform.user.model.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManagerPumpAttendantRequest {

    @NotBlank(message = "First name is required.")
    @Size(max = 100, message = "First name must not exceed 100 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(max = 100, message = "Last name must not exceed 100 characters.")
    private String lastName;

    @NotBlank(message = "Post-name is required.")
    @Size(max = 100, message = "Post-name must not exceed 100 characters.")
    private String postName;

    @NotNull(message = "Gender is required.")
    private Gender gender;

    @NotBlank(message = "Birth place is required.")
    @Size(max = 150, message = "Birth place must not exceed 150 characters.")
    private String birthPlace;

    @NotNull(message = "Birth date is required.")
    @Past(message = "Birth date must be in the past.")
    private LocalDate birthDate;

    @NotBlank(message = "Address is required.")
    @Size(max = 500, message = "Address must not exceed 500 characters.")
    private String address;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    @Size(max = 180, message = "Email must not exceed 180 characters.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Size(max = 30, message = "Phone number must not exceed 30 characters.")
    private String phoneNumber;

    @NotNull(message = "Station id is required.")
    private UUID stationId;
}
