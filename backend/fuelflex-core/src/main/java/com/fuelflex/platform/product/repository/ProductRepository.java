package com.fuelflex.platform.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.product.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {

    List<Product> findByOrganizationIdOrderByDisplayOrderAscNameAsc(
            UUID organizationId
    );

    Optional<Product> findByIdAndOrganizationId(
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

    boolean existsByOrganizationIdAndBarcode(
            UUID organizationId,
            String barcode
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

    boolean existsByOrganizationIdAndBarcodeAndIdNot(
            UUID organizationId,
            String barcode,
            UUID id
    );

    List<Product> findByOrganizationIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
            UUID organizationId
    );

    List<Product> findByOrganizationIdAndCategoryIdOrderByDisplayOrderAscNameAsc(
            UUID organizationId,
            UUID categoryId
    );
}