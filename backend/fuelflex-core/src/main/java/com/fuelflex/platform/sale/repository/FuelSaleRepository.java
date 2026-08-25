package com.fuelflex.platform.sale.repository;
import java.math.BigDecimal; import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import com.fuelflex.platform.sale.entity.*;
public interface FuelSaleRepository extends JpaRepository<FuelSale,UUID>{
 List<FuelSale> findByPumpAttendantIdAndOperationalDayIdOrderBySoldAtDesc(UUID attendantId,UUID dayId);
 Optional<FuelSale> findByIdAndPumpAttendantIdAndOperationalDayIdAndOrganizationIdAndStationId(UUID id,UUID attendantId,UUID dayId,UUID organizationId,UUID stationId);
 List<FuelSale> findByOperationalDayIdOrderBySoldAtAsc(UUID dayId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from FuelSale s where s.id=:id and s.organization.id=:org") Optional<FuelSale> lockByIdAndOrganizationId(@Param("id")UUID id,@Param("org")UUID org);
 @Query("select coalesce(sum(s.quantity),0) from FuelSale s where s.shiftAssignment.id=:shiftId and s.saleType=:type and s.status='EFFECTIVE'") BigDecimal sumEffectiveQuantityByShiftAndType(@Param("shiftId")UUID shiftId,@Param("type")SaleType type);
 @Query("select coalesce(sum(s.quantity),0) from FuelSale s where s.operationalDay.id=:dayId and s.saleType=:type and s.status='EFFECTIVE'") BigDecimal sumEffectiveQuantityByDayAndType(@Param("dayId")UUID dayId,@Param("type")SaleType type);
 @Query("select coalesce(sum(s.totalAmount),0) from FuelSale s where s.operationalDay.id=:dayId and s.saleType=:type and s.status='EFFECTIVE'") BigDecimal sumEffectiveAmountByDayAndType(@Param("dayId")UUID dayId,@Param("type")SaleType type);
}
