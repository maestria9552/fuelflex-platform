package com.fuelflex.platform.sale.repository;
import java.math.BigDecimal; import java.util.UUID; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import com.fuelflex.platform.sale.entity.*;
public interface SaleStockMovementRepository extends JpaRepository<SaleStockMovement,UUID>{
 boolean existsBySaleIdAndMovementType(UUID saleId,StockMovementType type); long countBySaleId(UUID saleId);
 @Query("select coalesce(sum(case when m.movementType='OUTBOUND' then m.quantity else -m.quantity end),0) from SaleStockMovement m where m.tank.id=:tankId") BigDecimal sumOutboundByTankId(@Param("tankId")UUID tankId);
}
