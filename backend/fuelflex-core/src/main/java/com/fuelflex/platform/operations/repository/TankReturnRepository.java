package com.fuelflex.platform.operations.repository;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.fuelflex.platform.operations.entity.TankReturn;
public interface TankReturnRepository extends JpaRepository<TankReturn, UUID> {
    List<TankReturn> findByShiftAssignmentIdOrderByOccurredAtAsc(UUID shiftId);
    List<TankReturn> findByOperationalDayIdOrderByOccurredAtAsc(UUID dayId);
    @Query("select coalesce(sum(r.quantity),0) from TankReturn r where r.shiftAssignment.id=:shiftId")
    BigDecimal sumQuantityByShiftId(@Param("shiftId") UUID shiftId);
    @Query("select coalesce(sum(r.quantity),0) from TankReturn r where r.operationalDay.id=:dayId")
    BigDecimal sumQuantityByDayId(@Param("dayId") UUID dayId);
    @Query("select coalesce(sum(r.quantity),0) from TankReturn r where r.operationalDay.id=:dayId and r.shiftAssignment.status='OPEN'")
    BigDecimal sumOpenQuantityByDayId(@Param("dayId") UUID dayId);
}
