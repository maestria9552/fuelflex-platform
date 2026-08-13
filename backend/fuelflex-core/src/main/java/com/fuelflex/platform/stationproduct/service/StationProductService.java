package com.fuelflex.platform.stationproduct.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.stationproduct.dto.request.StationProductRequest;
import com.fuelflex.platform.stationproduct.dto.response.StationProductResponse;

public interface StationProductService {
    StationProductResponse create(UUID organizationId, UUID stationId, StationProductRequest request);
    List<StationProductResponse> findAllByStation(UUID organizationId, UUID stationId);
    List<StationProductResponse> findActiveByStation(UUID organizationId, UUID stationId);
    StationProductResponse findById(UUID organizationId, UUID stationId, UUID stationProductId);
    StationProductResponse update(UUID organizationId, UUID stationId, UUID stationProductId, StationProductRequest request);
    void delete(UUID organizationId, UUID stationId, UUID stationProductId);
}
