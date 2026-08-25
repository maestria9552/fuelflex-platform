package com.fuelflex.platform.employeevalidation.controller;

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

import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.CandidateResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.CreateRequest;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.PageResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.Response;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ReviewRequest;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.employeevalidation.service.PumpAttendantValidationService;
import com.fuelflex.platform.user.dto.request.ManagerPumpAttendantRequest;
import com.fuelflex.platform.user.dto.response.EmployeeResponse;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;
import com.fuelflex.platform.user.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
public class ManagerPumpAttendantValidationController {

    private final EmployeeService employeeService;
    private final PumpAttendantValidationService validationService;

    @GetMapping("/pump-attendants")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant:prepare')")
    public PageResponse<CandidateResponse> findPumpAttendants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PumpAttendantValidationStatus status
    ) {
        return validationService.findManagerCandidates(
                page, size, search, status);
    }

    @GetMapping("/pump-attendants/{pumpAttendantId}")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant:prepare')")
    public CandidateResponse findPumpAttendant(
            @PathVariable UUID pumpAttendantId) {
        return validationService.findManagerCandidate(pumpAttendantId);
    }

    @PostMapping("/pump-attendants")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant:prepare')")
    public CandidateResponse createPumpAttendant(
            @Valid @RequestBody ManagerPumpAttendantRequest request) {
        EmployeeResponse created = employeeService
                .createPumpAttendantDraft(request);
        return validationService.findManagerCandidate(created.getId());
    }

    @PutMapping("/pump-attendants/{pumpAttendantId}")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant:prepare')")
    public CandidateResponse updatePumpAttendant(
            @PathVariable UUID pumpAttendantId,
            @Valid @RequestBody ManagerPumpAttendantRequest request
    ) {
        employeeService.updatePumpAttendantDraft(pumpAttendantId, request);
        return validationService.findManagerCandidate(pumpAttendantId);
    }

    @GetMapping("/pump-attendant-validation-requests")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant-validation:view')")
    public PageResponse<Response> findRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)
            PumpAttendantValidationRequestStatus status
    ) {
        return validationService.findManagerRequests(page, size, status);
    }

    @GetMapping("/pump-attendant-validation-requests/{requestId}")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant-validation:view')")
    public Response findRequest(@PathVariable UUID requestId) {
        return validationService.findManagerRequest(requestId);
    }

    @PostMapping("/pump-attendant-validation-requests")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant-validation:create')")
    public Response createRequest(@Valid @RequestBody CreateRequest request) {
        return validationService.create(request);
    }

    @PostMapping("/pump-attendant-validation-requests/{requestId}/submit")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant-validation:submit')")
    public Response submit(@PathVariable UUID requestId) {
        return validationService.submit(requestId);
    }

    @PostMapping("/pump-attendant-validation-requests/{requestId}/cancel")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('pump-attendant-validation:submit')")
    public Response cancel(
            @PathVariable UUID requestId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return validationService.cancel(requestId, request);
    }
}
