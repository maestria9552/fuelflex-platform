package com.fuelflex.platform.operations.repository;
import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType;
import com.fuelflex.platform.operations.entity.*;
public interface PumpShiftAssignmentRepository extends JpaRepository<PumpShiftAssignment,UUID> {
 List<PumpShiftAssignment> findByOperationalDayIdOrderByOpenedAtAsc(UUID dayId);
 Optional<PumpShiftAssignment> findByIdAndOperationalDayOrganizationId(UUID id,UUID organizationId);
 boolean existsByFuelMeterIdAndStatus(UUID meterId,OperationalStatus status);
 boolean existsByPumpAttendantIdAndStatus(UUID attendantId,OperationalStatus status);
 long countByOperationalDayIdAndStatus(UUID dayId,OperationalStatus status);
 Optional<PumpShiftAssignment> findFirstByPumpAttendantIdAndStatusOrderByOpenedAtDesc(UUID attendantId,OperationalStatus status);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from PumpShiftAssignment a join fetch a.operationalDay d where a.pumpAttendant.id=:attendantId and a.status=:status")
 Optional<PumpShiftAssignment> lockOpenByPumpAttendantId(@Param("attendantId") UUID attendantId, @Param("status") OperationalStatus status);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from PumpShiftAssignment a join fetch a.operationalDay d where a.id=:id and d.organization.id=:organizationId")
 Optional<PumpShiftAssignment> lockByIdAndOrganizationId(@Param("id") UUID id,@Param("organizationId") UUID organizationId);
}
