package com.fuelflex.platform.pump.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.station.entity.Station;

public interface PumpRepository extends JpaRepository<Pump, UUID> {

    List<Pump> findByStationOrderByDisplayOrderAscNameAsc(
            Station station
    );

    List<Pump> findByStationAndActiveTrueOrderByDisplayOrderAscNameAsc(
            Station station
    );

    Optional<Pump> findByStationAndId(
            Station station,
            UUID id
    );

    Optional<Pump> findByStationAndCode(
            Station station,
            String code
    );

    Optional<Pump> findByStationAndPumpNumber(
            Station station,
            Integer pumpNumber
    );

    boolean existsByStationAndCode(
            Station station,
            String code
    );

    boolean existsByStationAndCodeAndIdNot(
            Station station,
            String code,
            UUID id
    );

    boolean existsByStationAndPumpNumber(
            Station station,
            Integer pumpNumber
    );

    boolean existsByStationAndPumpNumberAndIdNot(
            Station station,
            Integer pumpNumber,
            UUID id
    );
}