package com.fuelflex.platform.employeevalidation.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.PageResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.Response;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ApprovalResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ReviewRequest;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.employeevalidation.service.PumpAttendantValidationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/supervisor/pump-attendant-validation-requests")
@RequiredArgsConstructor
public class SupervisorPumpAttendantValidationController {

    private final PumpAttendantValidationService validationService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('pump-attendant-validation:view')")
    public PageResponse<Response> findRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)
            PumpAttendantValidationRequestStatus status
    ) {
        return validationService.findSupervisorRequests(page, size, status);
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('pump-attendant-validation:view')")
    public Response findRequest(@PathVariable UUID requestId) {
        return validationService.findSupervisorRequest(requestId);
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('pump-attendant-validation:review')")
    public ApprovalResponse approve(
            @PathVariable UUID requestId,
            @Valid @RequestBody(required = false) ReviewRequest request
    ) {
        return validationService.approve(requestId, request);
    }

    @PostMapping("/{requestId}/return")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('pump-attendant-validation:review')")
    public Response returnForCorrection(
            @PathVariable UUID requestId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return validationService.returnForCorrection(requestId, request);
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('pump-attendant-validation:review')")
    public Response reject(
            @PathVariable UUID requestId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return validationService.reject(requestId, request);
    }
}
