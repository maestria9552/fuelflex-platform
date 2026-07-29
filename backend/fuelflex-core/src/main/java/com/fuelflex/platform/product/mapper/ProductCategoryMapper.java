package com.fuelflex.platform.product.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.product.dto.request.ProductCategoryRequest;
import com.fuelflex.platform.product.dto.response.ProductCategoryResponse;
import com.fuelflex.platform.product.entity.ProductCategory;

@Component
public class ProductCategoryMapper {

    public ProductCategory toEntity(
            ProductCategoryRequest request
    ) {

        if (request == null) {
            return null;
        }

        ProductCategory category = new ProductCategory();

        updateEntity(category, request);

        return category;
    }

    public void updateEntity(
            ProductCategory category,
            ProductCategoryRequest request
    ) {

        if (category == null || request == null) {
            return;
        }

        category.setCode(
                normalizeRequiredValue(request.getCode())
        );

        category.setName(
                normalizeRequiredValue(request.getName())
        );

        category.setDescription(
                normalizeNullableValue(
                        request.getDescription()
                )
        );

        if (request.getActive() != null) {
            category.setActive(
                    request.getActive()
            );
        }
    }

    public ProductCategoryResponse toResponse(
            ProductCategory category
    ) {

        if (category == null) {
            return null;
        }

        return ProductCategoryResponse.builder()
                .id(category.getId())
                .organizationId(
                        category.getOrganization().getId()
                )
                .code(category.getCode())
                .name(category.getName())
                .description(
                        category.getDescription()
                )
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
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

}