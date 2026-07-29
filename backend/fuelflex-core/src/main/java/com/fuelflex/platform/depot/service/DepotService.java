package com.fuelflex.platform.depot.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.depot.dto.request.DepotRequest;
import com.fuelflex.platform.depot.dto.response.DepotResponse;

public interface DepotService {

    DepotResponse create(
            UUID organizationId,
            UUID stationId,
            DepotRequest request
    );

    List<DepotResponse> findAllByStation(
            UUID organizationId,
            UUID stationId
    );

    List<DepotResponse> findActiveByStation(
            UUID organizationId,
            UUID stationId
    );

    DepotResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID depotId
    );

    DepotResponse update(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            DepotRequest request
    );

    void delete(
            UUID organizationId,
            UUID stationId,
            UUID depotId
    );
}