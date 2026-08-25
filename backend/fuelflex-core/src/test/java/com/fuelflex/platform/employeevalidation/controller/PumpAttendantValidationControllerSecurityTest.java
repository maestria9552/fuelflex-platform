package com.fuelflex.platform.employeevalidation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.CreateRequest;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ReviewRequest;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.user.dto.request.ManagerPumpAttendantRequest;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;

class PumpAttendantValidationControllerSecurityTest {

    @Test
    void managerEndpointsRequireManagerAndWorkflowPermissions()
            throws Exception {
        assertSecurity(ManagerPumpAttendantValidationController.class,
                "findPumpAttendants", "MANAGER", "pump-attendant:prepare",
                int.class, int.class, String.class,
                PumpAttendantValidationStatus.class);
        assertSecurity(ManagerPumpAttendantValidationController.class,
                "createPumpAttendant", "MANAGER", "pump-attendant:prepare",
                ManagerPumpAttendantRequest.class);
        assertSecurity(ManagerPumpAttendantValidationController.class,
                "updatePumpAttendant", "MANAGER", "pump-attendant:prepare",
                UUID.class, ManagerPumpAttendantRequest.class);
        assertSecurity(ManagerPumpAttendantValidationController.class,
                "createRequest", "MANAGER",
                "pump-attendant-validation:create", CreateRequest.class);
        assertSecurity(ManagerPumpAttendantValidationController.class,
                "submit", "MANAGER", "pump-attendant-validation:submit",
                UUID.class);
    }

    @Test
    void supervisorEndpointsRequireSupervisorAndReviewPermission()
            throws Exception {
        assertSecurity(SupervisorPumpAttendantValidationController.class,
                "findRequests", "SUPERVISOR",
                "pump-attendant-validation:view",
                int.class, int.class,
                PumpAttendantValidationRequestStatus.class);
        assertSecurity(SupervisorPumpAttendantValidationController.class,
                "approve", "SUPERVISOR",
                "pump-attendant-validation:review",
                UUID.class, ReviewRequest.class);
        assertSecurity(SupervisorPumpAttendantValidationController.class,
                "returnForCorrection", "SUPERVISOR",
                "pump-attendant-validation:review",
                UUID.class, ReviewRequest.class);
        assertSecurity(SupervisorPumpAttendantValidationController.class,
                "reject", "SUPERVISOR",
                "pump-attendant-validation:review",
                UUID.class, ReviewRequest.class);
    }

    private void assertSecurity(
            Class<?> controller,
            String methodName,
            String role,
            String permission,
            Class<?>... parameterTypes
    ) throws Exception {
        Method method = controller.getMethod(methodName, parameterTypes);
        String expression = method.getAnnotation(PreAuthorize.class).value();
        assertThat(expression).contains(role, permission);
    }
}
