package com.fuelflex.platform.product.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.product.dto.request.ProductRequest;
import com.fuelflex.platform.product.dto.response.ProductResponse;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.product.entity.ProductCategory;
import com.fuelflex.platform.product.mapper.ProductMapper;
import com.fuelflex.platform.product.repository.ProductCategoryRepository;
import com.fuelflex.platform.product.repository.ProductRepository;
import com.fuelflex.platform.product.service.ProductService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductCategoryRepository categoryRepository;

    private final ProductMapper productMapper;

    private final UserRepository userRepository;

    @Override
    public ProductResponse create(
            UUID organizationId,
            ProductRequest request
    ) {
        validateRequest(request);

        Organization organization =
                getAuthorizedOrganization(organizationId);

        ProductCategory category =
                getCategory(
                        organization.getId(),
                        request.getCategoryId()
                );

        validateActiveCategory(category);

        String normalizedCode =
                normalizeCode(request.getCode());

        String normalizedName =
                normalizeRequiredText(request.getName());

        String normalizedBarcode =
                normalizeNullableText(request.getBarcode());

        validateUniqueFields(
                organization.getId(),
                normalizedCode,
                normalizedName,
                normalizedBarcode,
                null
        );

        Product product =
                productMapper.toEntity(request);

        product.setOrganization(organization);
        product.setCategory(category);
        product.setCode(normalizedCode);
        product.setName(normalizedName);
        product.setShortName(
                normalizeNullableText(request.getShortName())
        );
        product.setDescription(
                normalizeNullableText(request.getDescription())
        );
        product.setBarcode(normalizedBarcode);
        product.setColor(
                normalizeNullableText(request.getColor())
        );

        if (request.getDisplayOrder() == null) {
            product.setDisplayOrder(1);
        }

        Product savedProduct =
                productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAllByOrganization(
            UUID organizationId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        return productRepository
                .findByOrganizationIdOrderByDisplayOrderAscNameAsc(
                        organization.getId()
                )
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findActiveByOrganization(
            UUID organizationId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        return productRepository
                .findByOrganizationIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
                        organization.getId()
                )
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategory(
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

        return productRepository
                .findByOrganizationIdAndCategoryIdOrderByDisplayOrderAscNameAsc(
                        organization.getId(),
                        category.getId()
                )
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(
            UUID organizationId,
            UUID productId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        Product product =
                getProduct(
                        organization.getId(),
                        productId
                );

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse update(
            UUID organizationId,
            UUID productId,
            ProductRequest request
    ) {
        validateRequest(request);

        Organization organization =
                getAuthorizedOrganization(organizationId);

        Product product =
                getProduct(
                        organization.getId(),
                        productId
                );

        ProductCategory category =
                getCategory(
                        organization.getId(),
                        request.getCategoryId()
                );

        validateActiveCategory(category);

        String normalizedCode =
                normalizeCode(request.getCode());

        String normalizedName =
                normalizeRequiredText(request.getName());

        String normalizedBarcode =
                normalizeNullableText(request.getBarcode());

        validateUniqueFields(
                organization.getId(),
                normalizedCode,
                normalizedName,
                normalizedBarcode,
                product.getId()
        );

        productMapper.updateEntity(product, request);

        product.setCategory(category);
        product.setCode(normalizedCode);
        product.setName(normalizedName);
        product.setShortName(
                normalizeNullableText(request.getShortName())
        );
        product.setDescription(
                normalizeNullableText(request.getDescription())
        );
        product.setBarcode(normalizedBarcode);
        product.setColor(
                normalizeNullableText(request.getColor())
        );

        if (request.getDisplayOrder() == null) {
            product.setDisplayOrder(1);
        }

        Product updatedProduct =
                productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    public void delete(
            UUID organizationId,
            UUID productId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        Product product =
                getProduct(
                        organization.getId(),
                        productId
                );

        /*
         * Suppression logique afin de préserver
         * les futures références de stock et de vente.
         */
        product.setActive(false);

        productRepository.save(product);
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
                    "Vous n’êtes pas autorisé à accéder aux produits de cette organisation."
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

        return userRepository
                .findByEmailIgnoreCase(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Utilisateur authentifié introuvable."
                        )
                );
    }

    private Product getProduct(
            UUID organizationId,
            UUID productId
    ) {
        if (productId == null) {
            throw new BusinessException(
                    "L’identifiant du produit est obligatoire."
            );
        }

        return productRepository
                .findByIdAndOrganizationId(
                        productId,
                        organizationId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Produit introuvable."
                        )
                );
    }

    private ProductCategory getCategory(
            UUID organizationId,
            UUID categoryId
    ) {
        if (categoryId == null) {
            throw new BusinessException(
                    "La catégorie du produit est obligatoire."
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

    private void validateActiveCategory(
            ProductCategory category
    ) {
        if (!category.isActive()) {
            throw new BusinessException(
                    "La catégorie sélectionnée est désactivée."
            );
        }
    }

    private void validateRequest(
            ProductRequest request
    ) {
        if (request == null) {
            throw new BusinessException(
                    "Les informations du produit sont obligatoires."
            );
        }

        if (request.getCategoryId() == null) {
            throw new BusinessException(
                    "La catégorie du produit est obligatoire."
            );
        }

        if (request.getCode() == null
                || request.getCode().isBlank()) {

            throw new BusinessException(
                    "Le code du produit est obligatoire."
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new BusinessException(
                    "Le nom du produit est obligatoire."
            );
        }

        if (request.getUnit() == null) {
            throw new BusinessException(
                    "L’unité du produit est obligatoire."
            );
        }

        if (request.getDisplayOrder() != null
                && request.getDisplayOrder() < 1) {

            throw new BusinessException(
                    "L’ordre d’affichage doit être supérieur ou égal à 1."
            );
        }
    }

    private void validateUniqueFields(
            UUID organizationId,
            String code,
            String name,
            String barcode,
            UUID currentProductId
    ) {
        boolean codeExists;
        boolean nameExists;
        boolean barcodeExists = false;

        if (currentProductId == null) {
            codeExists =
                    productRepository
                            .existsByOrganizationIdAndCodeIgnoreCase(
                                    organizationId,
                                    code
                            );

            nameExists =
                    productRepository
                            .existsByOrganizationIdAndNameIgnoreCase(
                                    organizationId,
                                    name
                            );

            if (barcode != null) {
                barcodeExists =
                        productRepository
                                .existsByOrganizationIdAndBarcode(
                                        organizationId,
                                        barcode
                                );
            }
        } else {
            codeExists =
                    productRepository
                            .existsByOrganizationIdAndCodeIgnoreCaseAndIdNot(
                                    organizationId,
                                    code,
                                    currentProductId
                            );

            nameExists =
                    productRepository
                            .existsByOrganizationIdAndNameIgnoreCaseAndIdNot(
                                    organizationId,
                                    name,
                                    currentProductId
                            );

            if (barcode != null) {
                barcodeExists =
                        productRepository
                                .existsByOrganizationIdAndBarcodeAndIdNot(
                                        organizationId,
                                        barcode,
                                        currentProductId
                                );
            }
        }

        if (codeExists) {
            throw new BusinessException(
                    "Un produit utilise déjà ce code dans votre organisation."
            );
        }

        if (nameExists) {
            throw new BusinessException(
                    "Un produit portant ce nom existe déjà dans votre organisation."
            );
        }

        if (barcodeExists) {
            throw new BusinessException(
                    "Un produit utilise déjà ce code-barres dans votre organisation."
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

    private String normalizeNullableText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim().replaceAll("\\s+", " ");

        return normalized.isBlank()
                ? null
                : normalized;
    }
}