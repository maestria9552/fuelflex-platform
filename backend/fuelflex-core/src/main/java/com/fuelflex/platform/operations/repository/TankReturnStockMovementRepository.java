package com.fuelflex.platform.operations.repository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.fuelflex.platform.operations.entity.TankReturnStockMovement;
public interface TankReturnStockMovementRepository extends JpaRepository<TankReturnStockMovement, UUID> {
    boolean existsByTankReturnId(UUID tankReturnId);
    long countByTankReturnId(UUID tankReturnId);
    @Query("select coalesce(sum(m.quantity),0) from TankReturnStockMovement m where m.tank.id=:tankId")
    BigDecimal sumInboundByTankId(@Param("tankId") UUID tankId);
}
