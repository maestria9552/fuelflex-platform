package com.fuelflex.platform.operations.repository;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository; import com.fuelflex.platform.operations.entity.ShiftReconciliation;
public interface ShiftReconciliationRepository extends JpaRepository<ShiftReconciliation,UUID>{Optional<ShiftReconciliation> findByShiftAssignmentId(UUID shiftId);List<ShiftReconciliation> findByShiftAssignmentOperationalDayIdOrderByShiftAssignmentOpenedAtAsc(UUID dayId);long countByShiftAssignmentOperationalDayId(UUID dayId);}
