package com.fuelflex.platform.user.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.user.dto.request.EmployeeCreateRequest;
import com.fuelflex.platform.user.dto.request.EmployeeStatusRequest;
import com.fuelflex.platform.user.dto.request.EmployeeUpdateRequest;
import com.fuelflex.platform.user.dto.response.AssignableEmployeeRoleResponse;
import com.fuelflex.platform.user.dto.response.EmployeePageResponse;
import com.fuelflex.platform.user.dto.response.EmployeeResponse;

public interface EmployeeService {

    EmployeePageResponse findAll(
            int page,
            int size,
            String search,
            String roleCode,
            Boolean enabled
    );

    EmployeeResponse findById(UUID employeeId);

    EmployeeResponse create(EmployeeCreateRequest request);

    EmployeeResponse update(UUID employeeId, EmployeeUpdateRequest request);

    EmployeeResponse updateStatus(UUID employeeId, EmployeeStatusRequest request);

    List<AssignableEmployeeRoleResponse> findAssignableRoles();

    com.fuelflex.platform.user.dto.response.EmployeeInvitationResponse resendInvitation(UUID employeeId);
}
