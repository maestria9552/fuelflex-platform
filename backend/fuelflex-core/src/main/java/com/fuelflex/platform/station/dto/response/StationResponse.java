package com.fuelflex.platform.station.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.station.entity.StationStatus;
import com.fuelflex.platform.station.entity.StationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StationResponse {

    private UUID id;

    private UUID organizationId;

    private String code;

    private String name;

    private String shortName;

    private StationType type;

    private StationStatus status;

    private String address;

    private String city;

    private String province;

    private String country;

    private String phoneNumber;

    private String email;

    private String latitude;

    private String longitude;

    private Integer displayOrder;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
