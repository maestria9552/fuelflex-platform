package com.fuelflex.platform.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.fuelflex.platform.user.dto.request.EmployeeCreateRequest;
import com.fuelflex.platform.user.dto.request.EmployeeStatusRequest;
import com.fuelflex.platform.user.dto.request.EmployeeUpdateRequest;

class EmployeeControllerSecurityTest {

    @Test
    void endpointsRequireSupervisorAndExpectedBackendPermission() throws Exception {
        assertSecurity("findAll", "user:view", int.class, int.class, String.class, String.class, Boolean.class);
        assertSecurity("findById", "user:view", UUID.class);
        assertSecurity("findAssignableRoles", "user:view");
        assertSecurity("create", "user:create", EmployeeCreateRequest.class);
        assertSecurity("update", "user:update", UUID.class, EmployeeUpdateRequest.class);
        assertSecurity("updateStatus", "user:disable", UUID.class, EmployeeStatusRequest.class);
    }

    private void assertSecurity(String methodName, String permission, Class<?>... parameterTypes)
            throws Exception {
        Method method = EmployeeController.class.getMethod(methodName, parameterTypes);
        String expression = method.getAnnotation(PreAuthorize.class).value();
        assertThat(expression).contains("SUPERVISOR", permission);
    }
}
