package com.fuelflex.platform.tank.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

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
}