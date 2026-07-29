package com.fuelflex.platform.product.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.product.dto.request.ProductCategoryRequest;
import com.fuelflex.platform.product.dto.response.ProductCategoryResponse;

public interface ProductCategoryService {

    ProductCategoryResponse create(
            UUID organizationId,
            ProductCategoryRequest request
    );

    List<ProductCategoryResponse> findAllByOrganization(
            UUID organizationId
    );

    ProductCategoryResponse findById(
            UUID organizationId,
            UUID categoryId
    );

    ProductCategoryResponse update(
            UUID organizationId,
            UUID categoryId,
            ProductCategoryRequest request
    );

    void delete(
            UUID organizationId,
            UUID categoryId
    );
}