package com.fuelflex.platform.tank.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.fuelflex.platform.tank.entity.Tank;

public interface TankRepository extends JpaRepository<Tank, UUID> {

    List<Tank> findByDepotIdOrderByDisplayOrderAscNameAsc(
            UUID depotId
    );

    List<Tank> findByDepotIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
            UUID depotId
    );

    List<Tank> findByProductIdAndActiveTrueOrderByNameAsc(
            UUID productId
    );

    Optional<Tank> findByIdAndDepotId(
            UUID tankId,
            UUID depotId
    );

    Optional<Tank> findByIdAndDepotStationId(
            UUID tankId,
            UUID stationId
    );

    boolean existsByDepotIdAndCodeIgnoreCase(
            UUID depotId,
            String code
    );

    boolean existsByDepotIdAndNameIgnoreCase(
            UUID depotId,
            String name
    );

    boolean existsByDepotIdAndCodeIgnoreCaseAndIdNot(
            UUID depotId,
            String code,
            UUID tankId
    );

    boolean existsByDepotIdAndNameIgnoreCaseAndIdNot(
            UUID depotId,
            String name,
            UUID tankId
    );

    boolean existsByDepotStationIdAndProductId(
            UUID stationId,
            UUID productId
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select tank from Tank tank where tank.id = :id")
    Optional<Tank> lockById(@Param("id") UUID id);
}
