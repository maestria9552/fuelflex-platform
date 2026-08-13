package com.fuelflex.platform.organization.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fuelflex.platform.organization.dto.request.OrganizationRequest;
import com.fuelflex.platform.organization.dto.response.OrganizationResponse;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.organization.entity.OrganizationStatus;
import com.fuelflex.platform.organization.mapper.OrganizationMapper;
import com.fuelflex.platform.organization.repository.OrganizationRepository;
import com.fuelflex.platform.organization.service.OrganizationService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.storage.service.OrganizationLogoStorageService;
import com.fuelflex.platform.tariffcategory.service.DefaultTariffCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private static final String ORGANIZATION_CODE_PREFIX = "ORG-";

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final UserRepository userRepository;
    private final OrganizationLogoStorageService organizationLogoStorageService;
    private final DefaultTariffCategoryService defaultTariffCategoryService;

    @Override
    public OrganizationResponse create(
            OrganizationRequest request
    ) {
        validateRequest(request);

        User currentUser = getCurrentUser();

        if (currentUser.getOrganization() != null) {
            throw new IllegalStateException(
                    "Votre organisation est déjà configurée"
            );
        }

        validateUniqueFields(request, null);

        Organization organization =
                organizationMapper.toEntity(request);

        /*
         * Le code est généré automatiquement par le système.
         */
        organization.setCode(generateUniqueCode());

        organization.setName(
                request.getName().trim()
        );

        organization.setTradeName(
                normalizeNullableValue(
                        request.getTradeName()
                )
        );

        organization.setStatus(
                OrganizationStatus.ACTIVE
        );

        organization.setActive(true);

        Organization savedOrganization =
                organizationRepository.save(organization);

        defaultTariffCategoryService.ensureDefaults(savedOrganization);

        /*
         * Le créateur du compte est automatiquement rattaché
         * à l'organisation qu'il vient de créer.
         */
        currentUser.setOrganization(savedOrganization);

        userRepository.save(currentUser);

        return organizationMapper.toResponse(
                savedOrganization
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse findById(UUID id) {
        Organization organization =
                getOrganization(id);

        return organizationMapper.toResponse(
                organization
        );
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

        Organization organization =
                getOrganization(id);

        validateUniqueFields(request, id);

        /*
         * Le code technique n'est jamais modifiable.
         */
        organization.setName(
                request.getName().trim()
        );

        /*
         * Le nom commercial reste facultatif.
         * Une valeur vide est enregistrée comme null.
         */
        organization.setTradeName(
                normalizeNullableValue(
                        request.getTradeName()
                )
        );

        organization.setRegistrationNumber(
                normalizeNullableValue(
                        request.getRegistrationNumber()
                )
        );

        organization.setNationalId(
                normalizeNullableValue(
                        request.getNationalId()
                )
        );

        organization.setTaxNumber(
                normalizeNullableValue(
                        request.getTaxNumber()
                )
        );

        organization.setEmail(
                normalizeNullableValue(
                        request.getEmail()
                )
        );

        organization.setPhone(
                normalizeNullableValue(
                        request.getPhone()
                )
        );

        organization.setWebsite(
                normalizeNullableValue(
                        request.getWebsite()
                )
        );

        organization.setLogoUrl(
                normalizeNullableValue(
                        request.getLogoUrl()
                )
        );

        organization.setCountry(
                normalizeNullableValue(
                        request.getCountry()
                )
        );

        organization.setProvince(
                normalizeNullableValue(
                        request.getProvince()
                )
        );

        organization.setCity(
                normalizeNullableValue(
                        request.getCity()
                )
        );

        organization.setAddress(
                normalizeNullableValue(
                        request.getAddress()
                )
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
                normalizeNullableValue(
                        request.getTimezone()
                );

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

        Organization updatedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toResponse(
                updatedOrganization
        );
    }

    @Override
public OrganizationResponse uploadLogo(
        UUID id,
        MultipartFile file
) {
    Organization organization =
            getOrganization(id);

    String previousLogoUrl =
            organization.getLogoUrl();

    String newLogoUrl =
            organizationLogoStorageService.store(
                    id,
                    file
            );

    organization.setLogoUrl(newLogoUrl);

    Organization updatedOrganization;

    try {
        updatedOrganization =
                organizationRepository.save(organization);
    } catch (RuntimeException exception) {
        organizationLogoStorageService
                .deleteByPublicUrl(newLogoUrl);

        throw exception;
    }

    if (previousLogoUrl != null
            && !previousLogoUrl.equals(newLogoUrl)) {

        organizationLogoStorageService
                .deleteByPublicUrl(previousLogoUrl);
    }

    return organizationMapper.toResponse(
            updatedOrganization
    );
}

    @Override
    public OrganizationResponse suspend(UUID id) {
        Organization organization =
                getOrganization(id);

        organization.setStatus(
                OrganizationStatus.SUSPENDED
        );

        organization.setActive(false);

        Organization suspendedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toResponse(
                suspendedOrganization
        );
    }

    @Override
    public OrganizationResponse activate(UUID id) {
        Organization organization =
                getOrganization(id);

        organization.setStatus(
                OrganizationStatus.ACTIVE
        );

        organization.setActive(true);

        Organization activatedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toResponse(
                activatedOrganization
        );
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Utilisateur non authentifié"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Utilisateur authentifié introuvable"
                        )
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

    private String generateUniqueCode() {
        String generatedCode;

        do {
            String randomPart = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);

            generatedCode =
                    ORGANIZATION_CODE_PREFIX + randomPart;

        } while (
                organizationRepository
                        .findByCodeIgnoreCase(generatedCode)
                        .isPresent()
        );

        return generatedCode;
    }

    private void validateRequest(
            OrganizationRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations de l’organisation sont obligatoires"
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
        if (request.getEmail() != null
                && !request.getEmail().isBlank()) {

            organizationRepository
                    .findByEmailIgnoreCase(
                            request.getEmail().trim()
                    )
                    .filter(organization ->
                            currentOrganizationId == null
                                    || !organization.getId()
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
                                    .trim()
                    )
                    .filter(organization ->
                            currentOrganizationId == null
                                    || !organization.getId()
                                            .equals(currentOrganizationId)
                    )
                    .ifPresent(organization -> {
                        throw new IllegalStateException(
                                "Ce numéro RCCM existe déjà"
                        );
                    });
        }
    }

    private String normalizeNullableValue(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
