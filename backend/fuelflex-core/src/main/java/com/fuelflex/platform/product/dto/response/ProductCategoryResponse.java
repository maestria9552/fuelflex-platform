package com.fuelflex.platform.product.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductCategoryResponse {

    private UUID id;

    private UUID organizationId;

    private String code;

    private String name;

    private String description;

    private boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}