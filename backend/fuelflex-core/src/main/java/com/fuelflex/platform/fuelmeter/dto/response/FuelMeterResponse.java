package com.fuelflex.platform.fuelmeter.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.fuelmeter.entity.MeterTechnology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuelMeterResponse {

    private UUID id;

    private UUID pumpId;

    private UUID dispensingPointId;

    private String code;

    private String name;

    private MeterTechnology technology;

    private BigDecimal currentIndex;

    private FuelMeterStatus status;

    private Integer displayOrder;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
