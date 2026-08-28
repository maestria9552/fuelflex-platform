package com.fuelflex.platform.operations.repository;
import java.math.BigDecimal;import java.util.UUID;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;import com.fuelflex.platform.operations.entity.MeteredStockMovement;
public interface MeteredStockMovementRepository extends JpaRepository<MeteredStockMovement,UUID>{
 boolean existsByShiftAssignmentId(UUID shiftId);long countByShiftAssignmentId(UUID shiftId);
 @Query("select coalesce(sum(m.quantity),0) from MeteredStockMovement m where m.tank.id=:tankId")BigDecimal sumOutboundByTankId(@Param("tankId")UUID tankId);
}
