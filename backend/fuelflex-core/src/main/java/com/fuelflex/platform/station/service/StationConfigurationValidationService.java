package com.fuelflex.platform.station.service;

import java.util.UUID;

import com.fuelflex.platform.station.dto.response.StationConfigurationValidationResponse;

public interface StationConfigurationValidationService {

    StationConfigurationValidationResponse validate(
            UUID organizationId,
            UUID stationId
    );
}
