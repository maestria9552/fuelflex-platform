package com.fuelflex.platform.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.fuelflex.platform.user.dto.request.EmployeeCreateRequest;
import com.fuelflex.platform.user.dto.request.EmployeeStatusRequest;
import com.fuelflex.platform.user.dto.request.EmployeeUpdateRequest;
import com.fuelflex.platform.user.dto.response.AssignableEmployeeRoleResponse;
import com.fuelflex.platform.user.dto.response.EmployeePageResponse;
import com.fuelflex.platform.user.dto.response.EmployeeResponse;
import com.fuelflex.platform.user.dto.response.EmployeeInvitationResponse;
import com.fuelflex.platform.user.dto.response.PumpAttendantCreationResponse;
import com.fuelflex.platform.user.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('user:view')")
    public EmployeePageResponse findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Boolean enabled
    ) {
        return employeeService.findAll(page, size, search, roleCode, enabled);
    }

    @GetMapping("/assignable-roles")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('user:view')")
    public List<AssignableEmployeeRoleResponse> findAssignableRoles() {
        return employeeService.findAssignableRoles();
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('user:view')")
    public EmployeeResponse findById(@PathVariable UUID employeeId) {
        return employeeService.findById(employeeId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('user:create')")
    public EmployeeResponse create(@Valid @RequestBody EmployeeCreateRequest request) {
        return employeeService.create(request);
    }

    @PostMapping("/pump-attendants")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('user:create')")
    public PumpAttendantCreationResponse createPumpAttendant(
            @Valid @RequestBody EmployeeCreateRequest request) {
        return employeeService.createPumpAttendant(request);
    }

    @PostMapping("/{employeeId}/resend-invitation")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('user:update')")
    public EmployeeInvitationResponse resendInvitation(@PathVariable UUID employeeId) {
        return employeeService.resendInvitation(employeeId);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('user:update')")
    public EmployeeResponse update(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        return employeeService.update(employeeId, request);
    }

    @PutMapping("/{employeeId}/status")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('user:disable')")
    public EmployeeResponse updateStatus(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeStatusRequest request
    ) {
        return employeeService.updateStatus(employeeId, request);
    }
}
