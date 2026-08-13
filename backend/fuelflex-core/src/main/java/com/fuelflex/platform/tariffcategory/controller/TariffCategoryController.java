package com.fuelflex.platform.tariffcategory.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fuelflex.platform.tariffcategory.dto.request.TariffCategoryRequest;
import com.fuelflex.platform.tariffcategory.dto.response.TariffCategoryResponse;
import com.fuelflex.platform.tariffcategory.service.TariffCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/tariff-categories")
@RequiredArgsConstructor
public class TariffCategoryController {
    private final TariffCategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TariffCategoryResponse create(@PathVariable UUID organizationId,
            @Valid @RequestBody TariffCategoryRequest request) {
        return service.create(organizationId, request);
    }

    @GetMapping
    public List<TariffCategoryResponse> findAll(@PathVariable UUID organizationId) {
        return service.findAllByOrganization(organizationId);
    }

    @GetMapping("/active")
    public List<TariffCategoryResponse> findActive(@PathVariable UUID organizationId) {
        return service.findActiveByOrganization(organizationId);
    }

    @GetMapping("/{tariffCategoryId}")
    public TariffCategoryResponse findById(@PathVariable UUID organizationId,
            @PathVariable UUID tariffCategoryId) {
        return service.findById(organizationId, tariffCategoryId);
    }

    @PutMapping("/{tariffCategoryId}")
    public TariffCategoryResponse update(@PathVariable UUID organizationId,
            @PathVariable UUID tariffCategoryId, @Valid @RequestBody TariffCategoryRequest request) {
        return service.update(organizationId, tariffCategoryId, request);
    }

    @DeleteMapping("/{tariffCategoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID organizationId, @PathVariable UUID tariffCategoryId) {
        service.delete(organizationId, tariffCategoryId);
    }
}
