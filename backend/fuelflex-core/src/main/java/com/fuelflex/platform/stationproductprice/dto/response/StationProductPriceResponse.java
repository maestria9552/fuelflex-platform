package com.fuelflex.platform.stationproductprice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.product.entity.ProductUnit;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StationProductPriceResponse {
    private UUID id;
    private UUID organizationId;
    private UUID stationId;
    private UUID stationProductId;
    private UUID productId;
    private String productCode;
    private String productName;
    private String productShortName;
    private ProductUnit unit;
    private UUID tariffCategoryId;
    private String tariffCategoryCode;
    private String tariffCategoryName;
    private boolean tariffCategorySystem;
    private BigDecimal price;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
