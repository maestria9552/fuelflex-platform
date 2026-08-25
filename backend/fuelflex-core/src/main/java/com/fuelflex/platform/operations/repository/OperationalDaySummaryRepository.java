package com.fuelflex.platform.operations.repository;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository; import com.fuelflex.platform.operations.entity.OperationalDaySummary;
public interface OperationalDaySummaryRepository extends JpaRepository<OperationalDaySummary,UUID>{Optional<OperationalDaySummary> findByOperationalDayId(UUID dayId);}
