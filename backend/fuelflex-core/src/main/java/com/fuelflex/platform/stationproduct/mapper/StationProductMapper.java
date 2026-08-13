package com.fuelflex.platform.stationproduct.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.stationproduct.dto.request.StationProductRequest;
import com.fuelflex.platform.stationproduct.dto.response.StationProductResponse;
import com.fuelflex.platform.stationproduct.entity.StationProduct;

@Component
public class StationProductMapper {
    public StationProduct toEntity(StationProductRequest request) {
        if (request == null) {
            return null;
        }
        return StationProduct.builder()
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive() == null || request.getActive())
                .build();
    }

    public void updateEntity(StationProduct entity, StationProductRequest request) {
        if (entity == null || request == null) {
            return;
        }
        if (request.getDisplayOrder() != null) {
            entity.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
    }

    public StationProductResponse toResponse(StationProduct entity) {
        if (entity == null) {
            return null;
        }
        return StationProductResponse.builder()
                .id(entity.getId())
                .stationId(entity.getStation().getId())
                .productId(entity.getProduct().getId())
                .productCode(entity.getProduct().getCode())
                .productName(entity.getProduct().getName())
                .productShortName(entity.getProduct().getShortName())
                .unit(entity.getProduct().getUnit())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
