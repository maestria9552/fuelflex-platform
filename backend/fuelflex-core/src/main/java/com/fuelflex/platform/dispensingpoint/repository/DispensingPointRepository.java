package com.fuelflex.platform.dispensingpoint.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.pump.entity.Pump;

@Repository
public interface DispensingPointRepository
        extends JpaRepository<DispensingPoint, UUID> {

    List<DispensingPoint> findByPumpOrderByDisplayOrderAscNameAsc(
            Pump pump
    );

    List<DispensingPoint>
    findByPumpAndActiveTrueOrderByDisplayOrderAscNameAsc(
            Pump pump
    );

    Optional<DispensingPoint> findByPumpAndId(
            Pump pump,
            UUID dispensingPointId
    );

    boolean existsByPumpAndCode(
            Pump pump,
            String code
    );

    boolean existsByPumpAndCodeAndIdNot(
            Pump pump,
            String code,
            UUID dispensingPointId
    );

    boolean existsByPumpAndNozzleNumber(
            Pump pump,
            Integer nozzleNumber
    );

    boolean existsByPumpAndNozzleNumberAndIdNot(
            Pump pump,
            Integer nozzleNumber,
            UUID dispensingPointId
    );
}