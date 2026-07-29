package com.fuelflex.platform.tank.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.tank.dto.request.TankRequest;
import com.fuelflex.platform.tank.dto.response.TankResponse;

public interface TankService {

    TankResponse create(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            TankRequest request
    );

    List<TankResponse> findAllByDepot(
            UUID organizationId,
            UUID stationId,
            UUID depotId
    );

    List<TankResponse> findActiveByDepot(
            UUID organizationId,
            UUID stationId,
            UUID depotId
    );

    TankResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            UUID tankId
    );

    TankResponse update(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            UUID tankId,
            TankRequest request
    );

    void delete(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            UUID tankId
    );
}