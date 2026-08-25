package com.fuelflex.platform.operations.repository;
import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType;
import com.fuelflex.platform.operations.entity.*;
public interface PumpShiftAssignmentRepository extends JpaRepository<PumpShiftAssignment,UUID> {
 List<PumpShiftAssignment> findByOperationalDayIdOrderByOpenedAtAsc(UUID dayId);
 Optional<PumpShiftAssignment> findByIdAndOperationalDayOrganizationId(UUID id,UUID organizationId);
 boolean existsByFuelMeterIdAndStatus(UUID meterId,OperationalStatus status);
 boolean existsByPumpAttendantIdAndStatus(UUID attendantId,OperationalStatus status);
 long countByOperationalDayIdAndStatus(UUID dayId,OperationalStatus status);
 long countByOperationalDayId(UUID dayId);
 Optional<PumpShiftAssignment> findFirstByPumpAttendantIdAndStatusOrderByOpenedAtDesc(UUID attendantId,OperationalStatus status);
 @Query("select distinct a.fuelMeter.id from PumpShiftAssignment a where a.status=:status and a.fuelMeter.id in :meterIds")
 Set<UUID> findFuelMeterIdsByStatusAndFuelMeterIdIn(@Param("status") OperationalStatus status,@Param("meterIds") Collection<UUID> meterIds);
 @Query("select distinct a.pumpAttendant.id from PumpShiftAssignment a where a.status=:status and a.pumpAttendant.id in :attendantIds")
 Set<UUID> findPumpAttendantIdsByStatusAndPumpAttendantIdIn(@Param("status") OperationalStatus status,@Param("attendantIds") Collection<UUID> attendantIds);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from PumpShiftAssignment a join fetch a.operationalDay d where a.pumpAttendant.id=:attendantId and a.status=:status")
 Optional<PumpShiftAssignment> lockOpenByPumpAttendantId(@Param("attendantId") UUID attendantId, @Param("status") OperationalStatus status);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from PumpShiftAssignment a join fetch a.operationalDay d where a.id=:id and d.organization.id=:organizationId")
 Optional<PumpShiftAssignment> lockByIdAndOrganizationId(@Param("id") UUID id,@Param("organizationId") UUID organizationId);
}
