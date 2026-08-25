package com.fuelflex.platform.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fuelflex.platform.user.dto.request.EmployeeCreateRequest;
import com.fuelflex.platform.user.dto.request.ManagerPumpAttendantRequest;
import com.fuelflex.platform.user.model.Gender;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class PumpAttendantIdentityRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void supervisorPumpAttendantRequiresIdentityAndStation() {
        EmployeeCreateRequest request = baseEmployee("PUMP_ATTENDANT");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("pumpAttendantProfileComplete");

        request.setPostName("Kabeya");
        request.setGender(Gender.FEMALE);
        request.setBirthPlace("Kinshasa");
        request.setBirthDate(LocalDate.of(1995, 4, 3));
        request.setAddress("Avenue Centrale 10");
        request.setStationId(UUID.randomUUID());

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void otherEmployeeRoleDoesNotRequirePumpAttendantProfile() {
        assertThat(validator.validate(baseEmployee("ACCOUNTANT"))).isEmpty();
    }

    @Test
    void managerContractIsPumpAttendantOnlyAndRequiresTheSameProfile() {
        ManagerPumpAttendantRequest request = new ManagerPumpAttendantRequest();
        request.setFirstName("Jean");
        request.setLastName("Mukendi");
        request.setEmail("jean@fuelflex.test");
        request.setPhoneNumber("+243810000001");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("postName", "gender", "birthPlace", "birthDate",
                        "address", "stationId");
        assertThat(ManagerPumpAttendantRequest.class.getDeclaredFields())
                .extracting(field -> field.getName())
                .doesNotContain("roleCode", "role", "roles");
    }

    private EmployeeCreateRequest baseEmployee(String roleCode) {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setFirstName("Jean");
        request.setLastName("Mukendi");
        request.setEmail("jean@fuelflex.test");
        request.setPhoneNumber("+243810000001");
        request.setRoleCode(roleCode);
        return request;
    }
}
