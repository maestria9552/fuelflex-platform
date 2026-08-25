package com.fuelflex.platform.fuelmeter.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.pump.entity.Pump;

@Repository
public interface FuelMeterRepository
        extends JpaRepository<FuelMeter, UUID> {

    List<FuelMeter> findByPumpOrderByDisplayOrderAscNameAsc(
            Pump pump
    );

    List<FuelMeter> findByPumpAndActiveTrueOrderByDisplayOrderAscNameAsc(
            Pump pump
    );

    List<FuelMeter> findByDispensingPointOrderByDisplayOrderAscNameAsc(
            DispensingPoint dispensingPoint
    );

    List<FuelMeter>
    findByDispensingPointAndActiveTrueOrderByDisplayOrderAscNameAsc(
            DispensingPoint dispensingPoint
    );

    Optional<FuelMeter> findByPumpAndId(
            Pump pump,
            UUID fuelMeterId
    );

    Optional<FuelMeter> findByDispensingPointAndId(
            DispensingPoint dispensingPoint,
            UUID fuelMeterId
    );

    long countByPumpAndActiveTrue(Pump pump);

    long countByPumpAndActiveTrueAndIdNot(
            Pump pump,
            UUID fuelMeterId
    );

    long countByDispensingPointAndActiveTrue(
            DispensingPoint dispensingPoint
    );

    long countByDispensingPointAndActiveTrueAndIdNot(
            DispensingPoint dispensingPoint,
            UUID fuelMeterId
    );

    long countByDispensingPointPumpAndActiveTrue(Pump pump);

    boolean existsByDispensingPointPumpAndActiveTrue(Pump pump);

    boolean existsByPumpAndCode(
            Pump pump,
            String code
    );

    boolean existsByPumpAndCodeAndIdNot(
            Pump pump,
            String code,
            UUID fuelMeterId
    );

    boolean existsByDispensingPointAndCode(
            DispensingPoint dispensingPoint,
            String code
    );

    boolean existsByDispensingPointAndCodeAndIdNot(
            DispensingPoint dispensingPoint,
            String code,
            UUID fuelMeterId
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select meter from FuelMeter meter left join fetch meter.pump left join fetch meter.dispensingPoint point left join fetch point.pump where meter.id = :id")
    Optional<FuelMeter> lockById(@Param("id") UUID id);

    @Query("""
            select distinct meter
              from FuelMeter meter
              left join fetch meter.pump directPump
              left join fetch meter.dispensingPoint point
              left join fetch point.pump pointPump
             where meter.active = true
               and meter.status = :status
               and (
                    directPump.station.id = :stationId
                    or pointPump.station.id = :stationId
               )
            """)
    List<FuelMeter> findActiveByStationId(
            @Param("stationId") UUID stationId,
            @Param("status") FuelMeterStatus status
    );
}
