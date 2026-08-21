package com.fuelflex.platform.reception.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fuelflex.platform.reception.entity.ReceptionStockMovement;

/** Read model over the definitive reception stock ledger. */
public interface ReceptionStockBalanceRepository extends JpaRepository<ReceptionStockMovement, UUID> {
    interface BalanceProjection {
        UUID getStationId();
        String getStationName();
        UUID getTankId();
        String getTankName();
        UUID getProductId();
        String getProductName();
        BigDecimal getCapacity();
        BigDecimal getCurrentStock();
    }

    @Query("""
            select tank.depot.station.id as stationId,
                   tank.depot.station.name as stationName,
                   tank.id as tankId,
                   tank.name as tankName,
                   tank.product.id as productId,
                   tank.product.name as productName,
                   tank.maximumLevelLiters as capacity,
                   coalesce(sum(movement.quantity), 0) as currentStock
              from Tank tank
              left join ReceptionStockMovement movement on movement.tank.id = tank.id
             where tank.depot.station.id in :stationIds
               and tank.active = true
             group by tank.depot.station.id, tank.depot.station.name,
                      tank.id, tank.name, tank.product.id, tank.product.name,
                      tank.maximumLevelLiters, tank.displayOrder
             order by tank.depot.station.name, tank.displayOrder, tank.name
            """)
    List<BalanceProjection> findDashboardBalances(@Param("stationIds") Collection<UUID> stationIds);
}
