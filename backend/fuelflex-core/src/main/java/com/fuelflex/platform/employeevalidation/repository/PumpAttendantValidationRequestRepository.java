package com.fuelflex.platform.employeevalidation.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationRequest;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;

import jakarta.persistence.LockModeType;

public interface PumpAttendantValidationRequestRepository
        extends JpaRepository<PumpAttendantValidationRequest, UUID> {

    @Query("""
            select request from PumpAttendantValidationRequest request
             where request.organization.id = :organizationId
               and request.createdBy.id = :createdById
               and (:status is null or request.status = :status)
            """)
    Page<PumpAttendantValidationRequest> findManagerRequests(
            @Param("organizationId") UUID organizationId,
            @Param("createdById") UUID createdById,
            @Param("status") PumpAttendantValidationRequestStatus status,
            Pageable pageable
    );

    @Query("""
            select request from PumpAttendantValidationRequest request
             where request.organization.id = :organizationId
               and request.station.id in :stationIds
               and (:status is null or request.status = :status)
            """)
    Page<PumpAttendantValidationRequest> findSupervisorRequests(
            @Param("organizationId") UUID organizationId,
            @Param("stationIds") Collection<UUID> stationIds,
            @Param("status") PumpAttendantValidationRequestStatus status,
            Pageable pageable
    );

    Optional<PumpAttendantValidationRequest> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select request from PumpAttendantValidationRequest request
             where request.id = :id
               and request.organization.id = :organizationId
            """)
    Optional<PumpAttendantValidationRequest> lockByIdAndOrganizationId(
            @Param("id") UUID id,
            @Param("organizationId") UUID organizationId
    );
}
