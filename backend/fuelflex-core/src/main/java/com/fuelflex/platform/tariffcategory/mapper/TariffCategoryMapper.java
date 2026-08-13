package com.fuelflex.platform.tariffcategory.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.tariffcategory.dto.request.TariffCategoryRequest;
import com.fuelflex.platform.tariffcategory.dto.response.TariffCategoryResponse;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;

@Component
public class TariffCategoryMapper {
    public TariffCategory toEntity(TariffCategoryRequest request) {
        if (request == null) return null;
        return TariffCategory.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive() == null || request.getActive())
                .system(false)
                .build();
    }

    public void updateEntity(TariffCategory entity, TariffCategoryRequest request) {
        if (entity == null || request == null) return;
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        if (!entity.isSystem()) entity.setCode(request.getCode());
        if (request.getDisplayOrder() != null) entity.setDisplayOrder(request.getDisplayOrder());
        if (request.getActive() != null) entity.setActive(request.getActive());
    }

    public TariffCategoryResponse toResponse(TariffCategory entity) {
        if (entity == null) return null;
        return TariffCategoryResponse.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganization().getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .system(entity.isSystem())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
