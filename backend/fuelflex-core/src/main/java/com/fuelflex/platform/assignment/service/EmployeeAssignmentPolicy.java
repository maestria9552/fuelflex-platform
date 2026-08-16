package com.fuelflex.platform.assignment.service;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.service.EmployeeRolePolicy;

@Component
public class EmployeeAssignmentPolicy {
    public String requireAssignableRole(User employee) {
        var roles = employee.getRoles().stream().filter(Role::isActive).toList();
        if (roles.size() != 1 || !EmployeeRolePolicy.isAssignable(roles.getFirst().getCode())) {
            throw new BusinessException("User is outside the employee assignment scope.");
        }
        return roles.getFirst().getCode();
    }

    public void checkNewAssignment(User employee, long activeCount) {
        if ("PUMP_ATTENDANT".equals(requireAssignableRole(employee)) && activeCount > 0) {
            throw new ConflictException(
                    "A pump attendant can have only one active administrative station assignment.");
        }
    }
}
