package com.fuelflex.platform.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.product.entity.ProductCategory;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, UUID> {

    List<ProductCategory> findByOrganizationIdOrderByNameAsc(
            UUID organizationId
    );

    Optional<ProductCategory> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    boolean existsByOrganizationIdAndCodeIgnoreCase(
            UUID organizationId,
            String code
    );

    boolean existsByOrganizationIdAndNameIgnoreCase(
            UUID organizationId,
            String name
    );

    boolean existsByOrganizationIdAndCodeIgnoreCaseAndIdNot(
            UUID organizationId,
            String code,
            UUID id
    );

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(
            UUID organizationId,
            String name,
            UUID id
    );
}