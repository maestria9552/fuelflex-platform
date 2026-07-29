package com.fuelflex.platform.product.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.product.dto.request.ProductRequest;
import com.fuelflex.platform.product.dto.response.ProductResponse;

public interface ProductService {

    ProductResponse create(
            UUID organizationId,
            ProductRequest request
    );

    List<ProductResponse> findAllByOrganization(
            UUID organizationId
    );

    List<ProductResponse> findActiveByOrganization(
            UUID organizationId
    );

    List<ProductResponse> findByCategory(
            UUID organizationId,
            UUID categoryId
    );

    ProductResponse findById(
            UUID organizationId,
            UUID productId
    );

    ProductResponse update(
            UUID organizationId,
            UUID productId,
            ProductRequest request
    );

    void delete(
            UUID organizationId,
            UUID productId
    );
}