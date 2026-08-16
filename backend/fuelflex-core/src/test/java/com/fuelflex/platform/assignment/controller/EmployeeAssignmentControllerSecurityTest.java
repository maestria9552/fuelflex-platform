package com.fuelflex.platform.assignment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class EmployeeAssignmentControllerSecurityTest {
    @Test
    void endpointsRequireDedicatedPermissions() {
        Map<String, String> expected = Map.of(
                "create", "assignment:create",
                "findAll", "assignment:view",
                "end", "assignment:end",
                "transfer", "assignment:transfer");
        for (Method method : EmployeeAssignmentController.class.getDeclaredMethods()) {
            if (expected.containsKey(method.getName())) {
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).contains("SUPERVISOR", expected.get(method.getName()));
            }
        }
    }
}
