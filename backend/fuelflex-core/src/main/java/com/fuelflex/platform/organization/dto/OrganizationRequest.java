package com.fuelflex.platform.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {

    private String code;

    private String name;

    private String legalName;

    private String tradeName;

    private String registrationNumber;

    private String nationalId;

    private String taxNumber;

    private String email;

    private String phone;

    private String website;

    private String logoUrl;

    private String country;

    private String province;

    private String city;

    private String address;

    private String defaultCurrency;

    private String timezone;

    private String defaultLanguage;

    private String primaryColor;

    private String secondaryColor;
}