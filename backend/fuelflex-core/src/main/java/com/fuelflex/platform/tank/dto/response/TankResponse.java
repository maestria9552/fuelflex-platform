package com.fuelflex.platform.tank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.tank.entity.TankStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TankResponse {

    private UUID id;

    private UUID organizationId;

    private UUID stationId;

    private String stationCode;

    private String stationName;

    private UUID depotId;

    private String depotCode;

    private String depotName;

    private UUID productId;

    private String productCode;

    private String productName;

    private String code;

    private String name;

    private BigDecimal capacityLiters;

    private BigDecimal minimumLevelLiters;

    private BigDecimal maximumLevelLiters;

    private TankStatus status;

    private String location;

    private Integer displayOrder;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}