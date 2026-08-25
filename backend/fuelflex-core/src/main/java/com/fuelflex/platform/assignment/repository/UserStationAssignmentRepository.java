package com.fuelflex.platform.assignment.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fuelflex.platform.assignment.entity.UserStationAssignment;

import jakarta.persistence.LockModeType;

public interface UserStationAssignmentRepository
        extends JpaRepository<UserStationAssignment, UUID> {

    Optional<UserStationAssignment> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Page<UserStationAssignment> findByUserIdAndOrganizationId(
            UUID userId, UUID organizationId, Pageable pageable);

    Page<UserStationAssignment> findByUserIdAndOrganizationIdAndValidUntilIsNull(
            UUID userId, UUID organizationId, Pageable pageable);

    Page<UserStationAssignment> findByUserIdAndOrganizationIdAndValidUntilIsNotNull(
            UUID userId, UUID organizationId, Pageable pageable);

    List<UserStationAssignment> findAllByUserIdAndOrganizationIdAndValidUntilIsNull(
            UUID userId, UUID organizationId);

    List<UserStationAssignment> findAllByUserIdInAndOrganizationIdAndValidUntilIsNull(
            List<UUID> userIds, UUID organizationId);

    List<UserStationAssignment> findAllByStationIdAndOrganizationIdAndValidUntilIsNull(
            UUID stationId, UUID organizationId);

    long countByUserIdAndOrganizationIdAndValidUntilIsNull(UUID userId, UUID organizationId);

    boolean existsByUserIdAndStationIdAndOrganizationIdAndValidUntilIsNull(
            UUID userId, UUID stationId, UUID organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select assignment from UserStationAssignment assignment
            where assignment.id = :assignmentId
              and assignment.user.id = :userId
              and assignment.organization.id = :organizationId
              and assignment.validUntil is null
            """)
    Optional<UserStationAssignment> lockActive(
            @Param("assignmentId") UUID assignmentId,
            @Param("userId") UUID userId,
            @Param("organizationId") UUID organizationId);

    @Query("""
            select assignment.station.id from UserStationAssignment assignment
            where assignment.user.id = :userId
              and assignment.organization.id = :organizationId
              and assignment.validUntil is null
            """)
    Set<UUID> findActiveStationIds(
            @Param("userId") UUID userId,
            @Param("organizationId") UUID organizationId);
}
