package com.fuelflex.platform.depot.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.depot.entity.Depot;

public interface DepotRepository extends JpaRepository<Depot, UUID> {

    List<Depot> findByStationIdOrderByDisplayOrderAscNameAsc(
            UUID stationId
    );

    List<Depot> findByStationIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
            UUID stationId
    );

    Optional<Depot> findByIdAndStationId(
            UUID depotId,
            UUID stationId
    );

    boolean existsByStationIdAndCodeIgnoreCase(
            UUID stationId,
            String code
    );

    boolean existsByStationIdAndNameIgnoreCase(
            UUID stationId,
            String name
    );

    boolean existsByStationIdAndCodeIgnoreCaseAndIdNot(
            UUID stationId,
            String code,
            UUID depotId
    );

    boolean existsByStationIdAndNameIgnoreCaseAndIdNot(
            UUID stationId,
            String name,
            UUID depotId
    );
}