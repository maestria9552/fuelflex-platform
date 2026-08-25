package com.fuelflex.platform.user.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.user.model.Gender;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String postName;
    private Gender gender;
    private String birthPlace;
    private LocalDate birthDate;
    private String address;
    private String email;
    private String phoneNumber;
    private String operationalCode;
    private boolean enabled;
    private boolean invitationSent;
    private boolean invitationPending;
    private String roleCode;
    private UUID organizationId;
    private PumpAttendantValidationStatus pumpAttendantValidationStatus;
    private UUID preparedById;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
