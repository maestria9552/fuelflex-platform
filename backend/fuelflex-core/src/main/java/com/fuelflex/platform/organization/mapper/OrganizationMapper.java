package com.fuelflex.platform.organization.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.organization.dto.OrganizationRequest;
import com.fuelflex.platform.organization.dto.OrganizationResponse;
import com.fuelflex.platform.organization.entity.Organization;

@Component
public class OrganizationMapper {

            public Organization toEntity(OrganizationRequest request) {

            if (request == null) {
                return null;
            }

            Organization organization = new Organization();

            organization.setCode(request.getCode());
            organization.setName(request.getName());
            organization.setLegalName(request.getLegalName());
            organization.setTradeName(request.getTradeName());
            organization.setRegistrationNumber(request.getRegistrationNumber());
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
            organization.setDefaultCurrency(request.getDefaultCurrency());
            organization.setTimezone(request.getTimezone());
            organization.setDefaultLanguage(request.getDefaultLanguage());
            organization.setPrimaryColor(request.getPrimaryColor());
            organization.setSecondaryColor(request.getSecondaryColor());

            return organization;
        }

    public OrganizationResponse toResponse(Organization organization) {

        if (organization == null) {
            return null;
        }

        return OrganizationResponse.builder()
                .id(organization.getId())
                .code(organization.getCode())
                .name(organization.getName())
                .legalName(organization.getLegalName())
                .tradeName(organization.getTradeName())
                .registrationNumber(organization.getRegistrationNumber())
                .nationalId(organization.getNationalId())
                .taxNumber(organization.getTaxNumber())
                .email(organization.getEmail())
                .phone(organization.getPhone())
                .website(organization.getWebsite())
                .logoUrl(organization.getLogoUrl())
                .country(organization.getCountry())
                .province(organization.getProvince())
                .city(organization.getCity())
                .address(organization.getAddress())
                .defaultCurrency(organization.getDefaultCurrency())
                .timezone(organization.getTimezone())
                .defaultLanguage(organization.getDefaultLanguage())
                .primaryColor(organization.getPrimaryColor())
                .secondaryColor(organization.getSecondaryColor())
                .status(organization.getStatus())
                .active(organization.isActive())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }
}