package com.fuelflex.platform.organization.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.organization.dto.request.OrganizationRequest;
import com.fuelflex.platform.organization.dto.response.OrganizationResponse;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.organization.entity.OrganizationStatus;
import com.fuelflex.platform.organization.mapper.OrganizationMapper;
import com.fuelflex.platform.organization.repository.OrganizationRepository;
import com.fuelflex.platform.organization.service.OrganizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    public OrganizationResponse create(OrganizationRequest request) {

        validateRequest(request);
        validateUniqueFields(request, null);

        Organization organization =
                organizationMapper.toEntity(request);

        organization.setStatus(OrganizationStatus.ACTIVE);
        organization.setActive(true);

        Organization savedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toResponse(savedOrganization);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse findById(UUID id) {

        Organization organization = getOrganization(id);

        return organizationMapper.toResponse(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> findAll() {

        return organizationRepository.findAll()
                .stream()
                .map(organizationMapper::toResponse)
                .toList();
    }

    @Override
    public OrganizationResponse update(
            UUID id,
            OrganizationRequest request
    ) {

        validateRequest(request);

        Organization organization = getOrganization(id);

        validateUniqueFields(request, id);

        organization.setCode(request.getCode());
        organization.setName(request.getName());
        organization.setLegalName(request.getLegalName());
        organization.setTradeName(request.getTradeName());
        organization.setRegistrationNumber(
                request.getRegistrationNumber()
        );
        organization.setNationalId(request.getNationalId());
        organization.setTaxNumber(request.getTaxNumber());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setWebsite(request.getWebsite());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setCountry(request.getCountry());
        organization.setProvince(request.getProvince());
        organization.setCity(request.getCity());
        organization.setAddress(request.getAddress());
        organization.setDefaultCurrency(
                request.getDefaultCurrency()
        );
        organization.setTimezone(request.getTimezone());
        organization.setDefaultLanguage(
                request.getDefaultLanguage()
        );
        organization.setPrimaryColor(request.getPrimaryColor());
        organization.setSecondaryColor(
                request.getSecondaryColor()
        );

        Organization updatedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toResponse(updatedOrganization);
    }

    @Override
    public OrganizationResponse suspend(UUID id) {

        Organization organization = getOrganization(id);

        organization.setStatus(OrganizationStatus.SUSPENDED);
        organization.setActive(false);

        Organization suspendedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toResponse(
                suspendedOrganization
        );
    }

    @Override
    public OrganizationResponse activate(UUID id) {

        Organization organization = getOrganization(id);

        organization.setStatus(OrganizationStatus.ACTIVE);
        organization.setActive(true);

        Organization activatedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toResponse(
                activatedOrganization
        );
    }

    private Organization getOrganization(UUID id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "L’identifiant de l’organisation est obligatoire"
            );
        }

        return organizationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Organisation introuvable : " + id
                        )
                );
    }

    private void validateRequest(OrganizationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations de l’organisation sont obligatoires"
            );
        }

        if (request.getCode() == null
                || request.getCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Le code de l’organisation est obligatoire"
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Le nom de l’organisation est obligatoire"
            );
        }
    }

    private void validateUniqueFields(
            OrganizationRequest request,
            UUID currentOrganizationId
    ) {

        organizationRepository
                .findByCodeIgnoreCase(request.getCode())
                .filter(organization ->
                        !organization.getId()
                                .equals(currentOrganizationId)
                )
                .ifPresent(organization -> {
                    throw new IllegalStateException(
                            "Ce code d’organisation existe déjà"
                    );
                });

        if (request.getEmail() != null
                && !request.getEmail().isBlank()) {

            organizationRepository
                    .findByEmailIgnoreCase(request.getEmail())
                    .filter(organization ->
                            !organization.getId()
                                    .equals(currentOrganizationId)
                    )
                    .ifPresent(organization -> {
                        throw new IllegalStateException(
                                "Cette adresse e-mail est déjà utilisée"
                        );
                    });
        }

        if (request.getRegistrationNumber() != null
                && !request.getRegistrationNumber().isBlank()) {

            organizationRepository
                    .findByRegistrationNumberIgnoreCase(
                            request.getRegistrationNumber()
                    )
                    .filter(organization ->
                            !organization.getId()
                                    .equals(currentOrganizationId)
                    )
                    .ifPresent(organization -> {
                        throw new IllegalStateException(
                                "Ce numéro d’enregistrement existe déjà"
                        );
                    });
        }
    }
}