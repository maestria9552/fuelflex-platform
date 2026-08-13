package com.fuelflex.platform.stationproductprice.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceRequest;
import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceUpdateRequest;
import com.fuelflex.platform.stationproductprice.dto.response.StationProductPriceResponse;

public interface StationProductPriceService {
    StationProductPriceResponse create(UUID organizationId, UUID stationId, UUID stationProductId,
            StationProductPriceRequest request);
    List<StationProductPriceResponse> findAllByStationProduct(UUID organizationId, UUID stationId,
            UUID stationProductId);
    List<StationProductPriceResponse> findActiveByStationProduct(UUID organizationId, UUID stationId,
            UUID stationProductId);
    StationProductPriceResponse findById(UUID organizationId, UUID stationId, UUID stationProductId,
            UUID stationProductPriceId);
    StationProductPriceResponse update(UUID organizationId, UUID stationId, UUID stationProductId,
            UUID stationProductPriceId, StationProductPriceUpdateRequest request);
    void delete(UUID organizationId, UUID stationId, UUID stationProductId, UUID stationProductPriceId);
}
