package com.fuelflex.platform.station.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.station.entity.Station;

public interface StationRepository extends JpaRepository<Station, UUID> {

    List<Station> findByOrganizationIdOrderByDisplayOrderAscNameAsc(
            UUID organizationId
    );

    List<Station> findByOrganizationIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
            UUID organizationId
    );

    Optional<Station> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    boolean existsByOrganizationIdAndCodeIgnoreCase(
            UUID organizationId,
            String code
    );

    boolean existsByOrganizationIdAndNameIgnoreCase(
            UUID organizationId,
            String name
    );

    boolean existsByOrganizationIdAndCodeIgnoreCaseAndIdNot(
            UUID organizationId,
            String code,
            UUID id
    );

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(
            UUID organizationId,
            String name,
            UUID id
    );
}