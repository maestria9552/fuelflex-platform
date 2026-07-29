package com.fuelflex.platform.product.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.product.dto.request.ProductCategoryRequest;
import com.fuelflex.platform.product.dto.response.ProductCategoryResponse;
import com.fuelflex.platform.product.entity.ProductCategory;
import com.fuelflex.platform.product.mapper.ProductCategoryMapper;
import com.fuelflex.platform.product.repository.ProductCategoryRepository;
import com.fuelflex.platform.product.service.ProductCategoryService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryServiceImpl
        implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    private final ProductCategoryMapper categoryMapper;

    private final UserRepository userRepository;

    @Override
    public ProductCategoryResponse create(
            UUID organizationId,
            ProductCategoryRequest request
    ) {
        validateRequest(request);

        Organization organization =
                getAuthorizedOrganization(organizationId);

        String normalizedCode =
                normalizeCode(request.getCode());

        String normalizedName =
                normalizeRequiredText(request.getName());

        validateUniqueFields(
                organization.getId(),
                normalizedCode,
                normalizedName,
                null
        );

        ProductCategory category =
                categoryMapper.toEntity(request);

        category.setOrganization(organization);
        category.setCode(normalizedCode);
        category.setName(normalizedName);

        if (request.getActive() == null) {
            category.setActive(true);
        }

        ProductCategory savedCategory =
                categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> findAllByOrganization(
            UUID organizationId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        return categoryRepository
                .findByOrganizationIdOrderByNameAsc(
                        organization.getId()
                )
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryResponse findById(
            UUID organizationId,
            UUID categoryId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        ProductCategory category =
                getCategory(
                        organization.getId(),
                        categoryId
                );

        return categoryMapper.toResponse(category);
    }

    @Override
    public ProductCategoryResponse update(
            UUID organizationId,
            UUID categoryId,
            ProductCategoryRequest request
    ) {
        validateRequest(request);

        Organization organization =
                getAuthorizedOrganization(organizationId);

        ProductCategory category =
                getCategory(
                        organization.getId(),
                        categoryId
                );

        String normalizedCode =
                normalizeCode(request.getCode());

        String normalizedName =
                normalizeRequiredText(request.getName());

        validateUniqueFields(
                organization.getId(),
                normalizedCode,
                normalizedName,
                category.getId()
        );

        categoryMapper.updateEntity(category, request);

        category.setCode(normalizedCode);
        category.setName(normalizedName);

        ProductCategory updatedCategory =
                categoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public void delete(
            UUID organizationId,
            UUID categoryId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        ProductCategory category =
                getCategory(
                        organization.getId(),
                        categoryId
                );

        /*
         * Suppression logique :
         * la catégorie reste conservée pour l’historique,
         * mais elle ne pourra plus être utilisée.
         */
        category.setActive(false);

        categoryRepository.save(category);
    }

    private Organization getAuthorizedOrganization(
            UUID organizationId
    ) {
        if (organizationId == null) {
            throw new BusinessException(
                    "L’identifiant de l’organisation est obligatoire."
            );
        }

        User currentUser = getCurrentUser();

        Organization organization =
                currentUser.getOrganization();

        if (organization == null) {
            throw new BusinessException(
                    "Aucune organisation n’est associée à votre compte."
            );
        }

        if (!organization.getId().equals(organizationId)) {
            throw new BusinessException(
                    "Vous n’êtes pas autorisé à accéder aux catégories de cette organisation."
            );
        }

        return organization;
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new BusinessException(
                    "Utilisateur non authentifié."
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new BusinessException(
                                "Utilisateur authentifié introuvable."
                        )
                );
    }

    private ProductCategory getCategory(
            UUID organizationId,
            UUID categoryId
    ) {
        if (categoryId == null) {
            throw new BusinessException(
                    "L’identifiant de la catégorie est obligatoire."
            );
        }

        return categoryRepository
                .findByIdAndOrganizationId(
                        categoryId,
                        organizationId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Catégorie de produit introuvable."
                        )
                );
    }

    private void validateRequest(
            ProductCategoryRequest request
    ) {
        if (request == null) {
            throw new BusinessException(
                    "Les informations de la catégorie sont obligatoires."
            );
        }

        if (request.getCode() == null
                || request.getCode().isBlank()) {

            throw new BusinessException(
                    "Le code de la catégorie est obligatoire."
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new BusinessException(
                    "Le nom de la catégorie est obligatoire."
            );
        }
    }

    private void validateUniqueFields(
            UUID organizationId,
            String code,
            String name,
            UUID currentCategoryId
    ) {
        boolean codeAlreadyExists;
        boolean nameAlreadyExists;

        if (currentCategoryId == null) {
            codeAlreadyExists =
                    categoryRepository
                            .existsByOrganizationIdAndCodeIgnoreCase(
                                    organizationId,
                                    code
                            );

            nameAlreadyExists =
                    categoryRepository
                            .existsByOrganizationIdAndNameIgnoreCase(
                                    organizationId,
                                    name
                            );
        } else {
            codeAlreadyExists =
                    categoryRepository
                            .existsByOrganizationIdAndCodeIgnoreCaseAndIdNot(
                                    organizationId,
                                    code,
                                    currentCategoryId
                            );

            nameAlreadyExists =
                    categoryRepository
                            .existsByOrganizationIdAndNameIgnoreCaseAndIdNot(
                                    organizationId,
                                    name,
                                    currentCategoryId
                            );
        }

        if (codeAlreadyExists) {
            throw new BusinessException(
                    "Une catégorie utilise déjà ce code dans votre organisation."
            );
        }

        if (nameAlreadyExists) {
            throw new BusinessException(
                    "Une catégorie portant ce nom existe déjà dans votre organisation."
            );
        }
    }

    private String normalizeCode(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String normalizeRequiredText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }
}