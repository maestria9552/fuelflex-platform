package com.fuelflex.platform.user.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.fuelflex.platform.user.model.Gender;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeUpdateRequest {

    @NotBlank(message = "First name is required.")
    @Size(max = 100, message = "First name must not exceed 100 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(max = 100, message = "Last name must not exceed 100 characters.")
    private String lastName;

    @Size(max = 100, message = "Post-name must not exceed 100 characters.")
    private String postName;

    private Gender gender;

    @Size(max = 150, message = "Birth place must not exceed 150 characters.")
    private String birthPlace;

    @Past(message = "Birth date must be in the past.")
    private LocalDate birthDate;

    @Size(max = 500, message = "Address must not exceed 500 characters.")
    private String address;

    @NotBlank(message = "Phone number is required.")
    @Size(max = 30, message = "Phone number must not exceed 30 characters.")
    private String phoneNumber;

    @NotBlank(message = "Role code is required.")
    @Size(max = 100, message = "Role code must not exceed 100 characters.")
    private String roleCode;

    private UUID stationId;

    @AssertTrue(message = "Pump attendant identity is required.")
    public boolean isPumpAttendantProfileComplete() {
        if (roleCode == null
                || !"PUMP_ATTENDANT".equalsIgnoreCase(roleCode.trim())) {
            return true;
        }
        return hasText(postName) && gender != null && hasText(birthPlace)
                && birthDate != null && hasText(address);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
