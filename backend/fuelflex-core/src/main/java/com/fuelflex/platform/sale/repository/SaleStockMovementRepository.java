package com.fuelflex.platform.sale.repository;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fuelflex.platform.sale.entity.SaleStockMovement;

public interface SaleStockMovementRepository extends JpaRepository<SaleStockMovement, UUID> {
    boolean existsBySaleId(UUID saleId);
    long countBySaleId(UUID saleId);
    @Query("select coalesce(sum(movement.quantity), 0) from SaleStockMovement movement where movement.tank.id = :tankId")
    BigDecimal sumOutboundByTankId(@Param("tankId") UUID tankId);
}
