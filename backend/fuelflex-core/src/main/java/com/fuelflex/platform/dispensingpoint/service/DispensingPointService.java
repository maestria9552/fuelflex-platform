package com.fuelflex.platform.dispensingpoint.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.dispensingpoint.dto.request.DispensingPointRequest;
import com.fuelflex.platform.dispensingpoint.dto.response.DispensingPointResponse;

public interface DispensingPointService {

    DispensingPointResponse create(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            DispensingPointRequest request
    );

    List<DispensingPointResponse> findAllByPump(
            UUID organizationId,
            UUID stationId,
            UUID pumpId
    );

    List<DispensingPointResponse> findActiveByPump(
            UUID organizationId,
            UUID stationId,
            UUID pumpId
    );

    DispensingPointResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    );

    DispensingPointResponse update(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            DispensingPointRequest request
    );

    void delete(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    );
}