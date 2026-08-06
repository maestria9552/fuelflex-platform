package com.fuelflex.platform.fuelmeter.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.fuelmeter.dto.request.FuelMeterRequest;
import com.fuelflex.platform.fuelmeter.dto.response.FuelMeterResponse;

public interface FuelMeterService {

    FuelMeterResponse create(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            FuelMeterRequest request
    );

    List<FuelMeterResponse> findAll(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    );

    List<FuelMeterResponse> findActive(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    );

    FuelMeterResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            UUID fuelMeterId
    );

    FuelMeterResponse update(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            UUID fuelMeterId,
            FuelMeterRequest request
    );

    void delete(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            UUID fuelMeterId
    );
}
