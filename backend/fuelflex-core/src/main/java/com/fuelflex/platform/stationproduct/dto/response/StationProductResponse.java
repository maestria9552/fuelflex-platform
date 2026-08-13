package com.fuelflex.platform.stationproduct.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.product.entity.ProductUnit;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StationProductResponse {
    private UUID id;
    private UUID stationId;
    private UUID productId;
    private String productCode;
    private String productName;
    private String productShortName;
    private ProductUnit unit;
    private Integer displayOrder;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
