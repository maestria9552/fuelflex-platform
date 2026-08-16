package com.fuelflex.platform.assignment.controller;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.fuelflex.platform.assignment.dto.request.*;
import com.fuelflex.platform.assignment.dto.response.*;
import com.fuelflex.platform.assignment.model.AssignmentStatus;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}")
@RequiredArgsConstructor
public class EmployeeAssignmentController {
    private final EmployeeAssignmentService assignmentService;

    @PostMapping("/assignments")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('assignment:create')")
    public EmployeeAssignmentResponse create(@PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeAssignmentCreateRequest request) {
        return assignmentService.create(employeeId, request);
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('assignment:view')")
    public EmployeeAssignmentPageResponse findAll(@PathVariable UUID employeeId,
            @RequestParam(defaultValue = "ALL") AssignmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return assignmentService.findAll(employeeId, status, page, size);
    }

    @PutMapping("/assignments/{assignmentId}/end")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('assignment:end')")
    public EmployeeAssignmentResponse end(@PathVariable UUID employeeId,
            @PathVariable UUID assignmentId,
            @Valid @RequestBody EmployeeAssignmentEndRequest request) {
        return assignmentService.end(employeeId, assignmentId, request);
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('assignment:transfer')")
    public EmployeeTransferResponse transfer(@PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeTransferRequest request) {
        return assignmentService.transfer(employeeId, request);
    }
}
