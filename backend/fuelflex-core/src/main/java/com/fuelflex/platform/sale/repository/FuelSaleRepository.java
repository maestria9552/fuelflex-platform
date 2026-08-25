package com.fuelflex.platform.sale.repository;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.*; import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import com.fuelflex.platform.sale.entity.*;
public interface FuelSaleRepository extends JpaRepository<FuelSale,UUID>{
 List<FuelSale> findByPumpAttendantIdAndOperationalDayIdOrderBySoldAtDesc(UUID attendantId,UUID dayId);
 Optional<FuelSale> findByIdAndPumpAttendantIdAndOperationalDayIdAndOrganizationIdAndStationId(UUID id,UUID attendantId,UUID dayId,UUID organizationId,UUID stationId);
 List<FuelSale> findByOperationalDayIdOrderBySoldAtAsc(UUID dayId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from FuelSale s where s.id=:id and s.organization.id=:org") Optional<FuelSale> lockByIdAndOrganizationId(@Param("id")UUID id,@Param("org")UUID org);
 @Query("select coalesce(sum(s.quantity),0) from FuelSale s where s.shiftAssignment.id=:shiftId and s.saleType=:type and s.status='EFFECTIVE'") BigDecimal sumEffectiveQuantityByShiftAndType(@Param("shiftId")UUID shiftId,@Param("type")SaleType type);
 @Query("select coalesce(sum(s.quantity),0) from FuelSale s where s.operationalDay.id=:dayId and s.saleType=:type and s.status='EFFECTIVE'") BigDecimal sumEffectiveQuantityByDayAndType(@Param("dayId")UUID dayId,@Param("type")SaleType type);
 @Query("select coalesce(sum(s.totalAmount),0) from FuelSale s where s.operationalDay.id=:dayId and s.saleType=:type and s.status='EFFECTIVE'") BigDecimal sumEffectiveAmountByDayAndType(@Param("dayId")UUID dayId,@Param("type")SaleType type);
 @EntityGraph(attributePaths={"station","operationalDay","shiftAssignment","pumpAttendant","fuelMeter","fuelMeter.pump","dispensingPoint","dispensingPoint.pump","tank","product","tariffCategory","creditCustomer"})
 @Query("""
   select s from FuelSale s
   where s.organization.id=:organizationId
     and s.station.id in :stationIds
     and (:stationId is null or s.station.id=:stationId)
     and (:operationalDayId is null or s.operationalDay.id=:operationalDayId)
     and (:pumpAttendantId is null or s.pumpAttendant.id=:pumpAttendantId)
     and (:saleType is null or s.saleType=:saleType)
     and (:status is null or s.status=:status)
     and (:soldFrom is null or s.soldAt>=:soldFrom)
     and (:soldTo is null or s.soldAt<=:soldTo)
   """)
 Page<FuelSale> findForWeb(
   @Param("organizationId")UUID organizationId,
   @Param("stationIds")Collection<UUID> stationIds,
   @Param("stationId")UUID stationId,
   @Param("operationalDayId")UUID operationalDayId,
   @Param("pumpAttendantId")UUID pumpAttendantId,
   @Param("saleType")SaleType saleType,
   @Param("status")SaleStatus status,
   @Param("soldFrom")OffsetDateTime soldFrom,
   @Param("soldTo")OffsetDateTime soldTo,
   Pageable pageable
 );
 @EntityGraph(attributePaths={"station","operationalDay","shiftAssignment","pumpAttendant","fuelMeter","fuelMeter.pump","dispensingPoint","dispensingPoint.pump","tank","product","tariffCategory","creditCustomer"})
 Optional<FuelSale> findByIdAndOrganizationIdAndStationIdIn(UUID id,UUID organizationId,Collection<UUID> stationIds);
}
