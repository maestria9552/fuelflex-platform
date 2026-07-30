package com.fuelflex.platform.pump.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.pump.entity.PumpStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PumpResponse {

    private UUID id;

    private UUID stationId;

    private String stationCode;

    private String stationName;

    private String code;

    private String name;

    private Integer pumpNumber;

    private String manufacturer;

    private String model;

    private String serialNumber;

    private PumpStatus status;

    private String location;

    private Integer displayOrder;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}