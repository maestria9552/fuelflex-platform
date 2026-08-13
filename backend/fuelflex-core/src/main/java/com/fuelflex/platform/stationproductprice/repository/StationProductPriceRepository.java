package com.fuelflex.platform.stationproductprice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.stationproductprice.entity.StationProductPrice;

public interface StationProductPriceRepository extends JpaRepository<StationProductPrice, UUID> {
    List<StationProductPrice> findByStationProductIdOrderByTariffCategoryDisplayOrderAsc(UUID stationProductId);
    List<StationProductPrice> findByStationProductIdAndActiveTrueOrderByTariffCategoryDisplayOrderAsc(UUID stationProductId);
    Optional<StationProductPrice> findByIdAndStationProductId(UUID id, UUID stationProductId);
    Optional<StationProductPrice> findByStationProductIdAndTariffCategoryId(UUID stationProductId, UUID tariffCategoryId);
}
