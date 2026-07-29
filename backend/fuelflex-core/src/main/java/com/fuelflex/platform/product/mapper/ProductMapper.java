package com.fuelflex.platform.product.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.product.dto.request.ProductRequest;
import com.fuelflex.platform.product.dto.response.ProductResponse;
import com.fuelflex.platform.product.entity.Product;

@Component
public class ProductMapper {

    public Product toEntity(
            ProductRequest request
    ) {
        Product product = new Product();

        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setShortName(request.getShortName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setBarcode(request.getBarcode());
        product.setColor(request.getColor());
        product.setDisplayOrder(request.getDisplayOrder());

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        return product;
    }

    public void updateEntity(
            Product product,
            ProductRequest request
    ) {
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setShortName(request.getShortName());
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setBarcode(request.getBarcode());
        product.setColor(request.getColor());
        product.setDisplayOrder(request.getDisplayOrder());

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
    }

    public ProductResponse toResponse(
            Product product
    ) {
        return ProductResponse.builder()
                .id(product.getId())
                .organizationId(
                        product.getOrganization().getId()
                )
                .categoryId(
                        product.getCategory().getId()
                )
                .categoryCode(
                        product.getCategory().getCode()
                )
                .categoryName(
                        product.getCategory().getName()
                )
                .code(product.getCode())
                .name(product.getName())
                .shortName(product.getShortName())
                .description(product.getDescription())
                .unit(product.getUnit())
                .barcode(product.getBarcode())
                .color(product.getColor())
                .displayOrder(product.getDisplayOrder())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}