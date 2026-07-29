package com.fuelflex.platform.organization.mapper;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.organization.dto.request.OrganizationRequest;
import com.fuelflex.platform.organization.dto.response.OrganizationResponse;
import com.fuelflex.platform.organization.entity.Organization;

@Component
public class OrganizationMapper {

    public Organization toEntity(
            OrganizationRequest request
    ) {
        if (request == null) {
            return null;
        }

        Organization organization = new Organization();

        organization.setName(
                normalizeRequiredValue(request.getName())
        );

        organization.setTradeName(
                normalizeNullableValue(request.getTradeName())
        );

        organization.setRegistrationNumber(
                normalizeNullableValue(
                        request.getRegistrationNumber()
                )
        );

        organization.setNationalId(
                normalizeNullableValue(request.getNationalId())
        );

        organization.setTaxNumber(
                normalizeNullableValue(request.getTaxNumber())
        );

        organization.setEmail(
                normalizeEmail(request.getEmail())
        );

        organization.setPhone(
                normalizeNullableValue(request.getPhone())
        );

        organization.setWebsite(
                normalizeNullableValue(request.getWebsite())
        );

        organization.setLogoUrl(
                normalizeNullableValue(request.getLogoUrl())
        );

        organization.setCountry(
                normalizeNullableValue(request.getCountry())
        );

        organization.setProvince(
                normalizeNullableValue(request.getProvince())
        );

        organization.setCity(
                normalizeNullableValue(request.getCity())
        );

        organization.setAddress(
                normalizeNullableValue(request.getAddress())
        );

        String defaultCurrency =
                normalizeNullableValue(
                        request.getDefaultCurrency()
                );

        if (defaultCurrency != null) {
            organization.setDefaultCurrency(
                    defaultCurrency.toUpperCase(Locale.ROOT)
            );
        }

        String timezone =
                normalizeNullableValue(request.getTimezone());

        if (timezone != null) {
            organization.setTimezone(timezone);
        }

        String defaultLanguage =
                normalizeNullableValue(
                        request.getDefaultLanguage()
                );

        if (defaultLanguage != null) {
            organization.setDefaultLanguage(
                    defaultLanguage.toLowerCase(Locale.ROOT)
            );
        }

        organization.setPrimaryColor(
                normalizeNullableValue(
                        request.getPrimaryColor()
                )
        );

        organization.setSecondaryColor(
                normalizeNullableValue(
                        request.getSecondaryColor()
                )
        );

        return organization;
    }

    public OrganizationResponse toResponse(
            Organization organization
    ) {
        if (organization == null) {
            return null;
        }

        return OrganizationResponse.builder()
                .id(organization.getId())
                .code(organization.getCode())
                .name(organization.getName())
                .tradeName(organization.getTradeName())
                .registrationNumber(
                        organization.getRegistrationNumber()
                )
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
                .defaultCurrency(
                        organization.getDefaultCurrency()
                )
                .timezone(organization.getTimezone())
                .defaultLanguage(
                        organization.getDefaultLanguage()
                )
                .primaryColor(organization.getPrimaryColor())
                .secondaryColor(
                        organization.getSecondaryColor()
                )
                .status(organization.getStatus())
                .active(organization.isActive())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }

    private String normalizeRequiredValue(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private String normalizeNullableValue(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }
}