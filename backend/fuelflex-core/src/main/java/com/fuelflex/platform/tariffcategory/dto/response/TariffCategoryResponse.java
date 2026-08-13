package com.fuelflex.platform.tariffcategory.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TariffCategoryResponse {
    private UUID id;
    private UUID organizationId;
    private String code;
    private String name;
    private String description;
    private boolean system;
    private Integer displayOrder;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
