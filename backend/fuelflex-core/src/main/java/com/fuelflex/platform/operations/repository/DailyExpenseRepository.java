package com.fuelflex.platform.operations.repository;
import java.math.BigDecimal; import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import com.fuelflex.platform.operations.entity.DailyExpense;
public interface DailyExpenseRepository extends JpaRepository<DailyExpense,UUID>{List<DailyExpense> findByOperationalDayIdOrderByCreatedAtAsc(UUID dayId);@Query("select coalesce(sum(e.amount),0) from DailyExpense e where e.operationalDay.id=:dayId")BigDecimal sumByDayId(@Param("dayId")UUID dayId);}
