package com.fuelflex.platform.stationproductprice.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceRequest;
import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceUpdateRequest;
import com.fuelflex.platform.stationproductprice.dto.response.StationProductPriceResponse;
import com.fuelflex.platform.stationproductprice.entity.StationProductPrice;

@Component
public class StationProductPriceMapper {
    public StationProductPrice toEntity(StationProductPriceRequest request) {
        if (request == null) return null;
        return StationProductPrice.builder()
                .price(request.getPrice())
                .active(true)
                .build();
    }

    public void updateEntity(StationProductPrice entity, StationProductPriceUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setPrice(request.getPrice());
        if (request.getActive() != null) entity.setActive(request.getActive());
    }

    public StationProductPriceResponse toResponse(StationProductPrice entity) {
        if (entity == null) return null;
        StationProduct stationProduct = entity.getStationProduct();
        return StationProductPriceResponse.builder()
                .id(entity.getId())
                .organizationId(stationProduct.getStation().getOrganization().getId())
                .stationId(stationProduct.getStation().getId())
                .stationProductId(stationProduct.getId())
                .productId(stationProduct.getProduct().getId())
                .productCode(stationProduct.getProduct().getCode())
                .productName(stationProduct.getProduct().getName())
                .productShortName(stationProduct.getProduct().getShortName())
                .unit(stationProduct.getProduct().getUnit())
                .tariffCategoryId(entity.getTariffCategory().getId())
                .tariffCategoryCode(entity.getTariffCategory().getCode())
                .tariffCategoryName(entity.getTariffCategory().getName())
                .tariffCategorySystem(entity.getTariffCategory().isSystem())
                .price(entity.getPrice())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
