package com.fuelflex.platform.reception.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fuelflex.platform.reception.entity.ReceptionStockMovement;

/** Read model over immutable reception entries minus immutable POS sale exits. */
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

    @Query(value = """
            select station.id as "stationId",
                   station.name as "stationName",
                   tank.id as "tankId",
                   tank.name as "tankName",
                   product.id as "productId",
                   product.name as "productName",
                   tank.maximum_level_liters as "capacity",
                   coalesce((select sum(inbound.quantity)
                               from reception_stock_movements inbound
                              where inbound.tank_id = tank.id), 0)
                   - coalesce((select sum(case when outbound.movement_type = 'OUTBOUND' then outbound.quantity else -outbound.quantity end)
                                from sale_stock_movements outbound
                                join fuel_sales sale on sale.id=outbound.sale_id
                                join pump_shift_assignments shift on shift.id=sale.shift_assignment_id
                               where outbound.tank_id = tank.id and shift.status='OPEN'), 0)
                   - coalesce((select sum(metered.quantity)
                                from metered_stock_movements metered
                               where metered.tank_id = tank.id), 0)
                   - coalesce((select sum(return_source.quantity)
                                from tank_return_source_movements return_source
                                join tank_returns returned on returned.id=return_source.tank_return_id
                                join pump_shift_assignments return_shift on return_shift.id=returned.shift_assignment_id
                               where return_source.tank_id=tank.id and return_shift.status='OPEN'),0)
                   + coalesce((select sum(returned.quantity)
                                from tank_return_stock_movements returned
                               where returned.tank_id = tank.id), 0) as "currentStock"
              from tanks tank
              join depots depot on depot.id = tank.depot_id
              join stations station on station.id = depot.station_id
              join products product on product.id = tank.product_id
             where station.id in (:stationIds)
               and tank.active = true
             order by station.name, tank.display_order, tank.name
            """, nativeQuery = true)
    List<BalanceProjection> findDashboardBalances(@Param("stationIds") Collection<UUID> stationIds);

    @Query("select coalesce(sum(movement.quantity), 0) from ReceptionStockMovement movement where movement.tank.id = :tankId")
    BigDecimal sumInboundByTankId(@Param("tankId") UUID tankId);
}
