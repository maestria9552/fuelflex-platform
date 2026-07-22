package com.fuelflex.platform.organization.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.organization.dto.request.OrganizationRequest;
import com.fuelflex.platform.organization.dto.response.OrganizationResponse;

public interface OrganizationService {

    OrganizationResponse create(OrganizationRequest request);

    OrganizationResponse findById(UUID id);

    List<OrganizationResponse> findAll();

    OrganizationResponse update(
            UUID id,
            OrganizationRequest request
    );

    OrganizationResponse suspend(UUID id);

    OrganizationResponse activate(UUID id);
}