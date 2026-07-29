package com.fuelflex.platform.station.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.station.dto.request.StationRequest;
import com.fuelflex.platform.station.dto.response.StationResponse;

public interface StationService {

    StationResponse create(
            UUID organizationId,
            StationRequest request
    );

    List<StationResponse> findAllByOrganization(
            UUID organizationId
    );

    List<StationResponse> findActiveByOrganization(
            UUID organizationId
    );

    StationResponse findById(
            UUID organizationId,
            UUID stationId
    );

    StationResponse update(
            UUID organizationId,
            UUID stationId,
            StationRequest request
    );

    void delete(
            UUID organizationId,
            UUID stationId
    );
}