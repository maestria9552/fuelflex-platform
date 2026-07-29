package com.fuelflex.platform.depot.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepotResponse {

    private UUID id;

    private UUID organizationId;

    private UUID stationId;

    private String stationCode;

    private String stationName;

    private String code;

    private String name;

    private String description;

    private String location;

    private Integer displayOrder;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}