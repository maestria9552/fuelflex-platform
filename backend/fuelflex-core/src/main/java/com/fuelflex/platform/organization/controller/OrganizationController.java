package com.fuelflex.platform.organization.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fuelflex.platform.organization.dto.request.OrganizationRequest;
import com.fuelflex.platform.organization.dto.response.OrganizationResponse;
import com.fuelflex.platform.organization.service.OrganizationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse create(
            @RequestBody OrganizationRequest request
    ) {
        return organizationService.create(request);
    }

    @GetMapping
    public List<OrganizationResponse> findAll() {
        return organizationService.findAll();
    }

    @GetMapping("/{id}")
    public OrganizationResponse findById(
            @PathVariable UUID id
    ) {
        return organizationService.findById(id);
    }

    @PutMapping("/{id}")
    public OrganizationResponse update(
            @PathVariable UUID id,
            @RequestBody OrganizationRequest request
    ) {
        return organizationService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public OrganizationResponse activate(
            @PathVariable UUID id
    ) {
        return organizationService.activate(id);
    }

    @PatchMapping("/{id}/suspend")
    public OrganizationResponse suspend(
            @PathVariable UUID id
    ) {
        return organizationService.suspend(id);
    }
}