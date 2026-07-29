package com.fuelflex.platform.product.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.product.entity.ProductUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {

    private UUID id;

    private UUID organizationId;

    private UUID categoryId;

    private String categoryCode;

    private String categoryName;

    private String code;

    private String name;

    private String shortName;

    private String description;

    private ProductUnit unit;

    private String barcode;

    private String color;

    private Integer displayOrder;

    private boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}