package com.fuelflex.platform.user.service;

import java.util.List;
import java.util.Set;

public final class EmployeeRolePolicy {

    public static final List<String> ASSIGNABLE_ROLE_CODES = List.of(
            "MANAGER",
            "PUMP_ATTENDANT",
            "ACCOUNTANT",
            "AUDITOR"
    );

    public static final Set<String> VISIBLE_ROLE_CODES = Set.copyOf(ASSIGNABLE_ROLE_CODES);

    private EmployeeRolePolicy() {
    }

    public static boolean isAssignable(String roleCode) {
        return ASSIGNABLE_ROLE_CODES.contains(roleCode);
    }
}
