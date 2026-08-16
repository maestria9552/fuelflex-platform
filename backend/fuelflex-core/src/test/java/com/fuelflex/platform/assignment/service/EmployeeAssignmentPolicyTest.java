package com.fuelflex.platform.assignment.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.user.entity.User;

class EmployeeAssignmentPolicyTest {
    private final EmployeeAssignmentPolicy policy = new EmployeeAssignmentPolicy();

    @Test
    void managerCanHaveSeveralStations() {
        assertThatCode(() -> policy.checkNewAssignment(user("MANAGER"), 3)).doesNotThrowAnyException();
    }

    @Test
    void pumpAttendantIsLimitedToOneActiveStation() {
        assertThatThrownBy(() -> policy.checkNewAssignment(user("PUMP_ATTENDANT"), 1))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void accountantAndAuditorAssignmentsRemainOptional() {
        assertThatCode(() -> policy.checkNewAssignment(user("ACCOUNTANT"), 0)).doesNotThrowAnyException();
        assertThatCode(() -> policy.checkNewAssignment(user("AUDITOR"), 0)).doesNotThrowAnyException();
    }

    @Test
    void supervisorAndExternalUsersAreRejected() {
        assertThatThrownBy(() -> policy.requireAssignableRole(user("SUPERVISOR")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> policy.requireAssignableRole(user("SUPPLIER_USER")))
                .isInstanceOf(BusinessException.class);
    }

    private User user(String code) {
        Role role = new Role();
        role.setCode(code);
        role.setActive(true);
        User user = new User();
        user.setRoles(Set.of(role));
        return user;
    }
}
