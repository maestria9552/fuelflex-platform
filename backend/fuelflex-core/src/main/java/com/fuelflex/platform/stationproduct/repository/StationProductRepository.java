package com.fuelflex.platform.stationproduct.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.stationproduct.entity.StationProduct;

public interface StationProductRepository extends JpaRepository<StationProduct, UUID> {
    List<StationProduct> findByStationIdOrderByDisplayOrderAsc(UUID stationId);
    List<StationProduct> findByStationIdAndActiveTrueOrderByDisplayOrderAsc(UUID stationId);
    Optional<StationProduct> findByIdAndStationId(UUID id, UUID stationId);
    Optional<StationProduct> findByStationIdAndProductId(UUID stationId, UUID productId);
    boolean existsByStationIdAndProductId(UUID stationId, UUID productId);
}
