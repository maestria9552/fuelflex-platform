package com.fuelflex.platform.pump.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.pump.dto.request.PumpRequest;
import com.fuelflex.platform.pump.dto.response.PumpResponse;

public interface PumpService {

    PumpResponse create(
            UUID organizationId,
            UUID stationId,
            PumpRequest request
    );

    List<PumpResponse> findAllByStation(
            UUID organizationId,
            UUID stationId
    );

    List<PumpResponse> findActiveByStation(
            UUID organizationId,
            UUID stationId
    );

    PumpResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID pumpId
    );

    PumpResponse update(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            PumpRequest request
    );

    void delete(
            UUID organizationId,
            UUID stationId,
            UUID pumpId
    );
}