package com.fuelflex.platform.operations.repository;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository; import com.fuelflex.platform.operations.entity.TankGaugeReading;
public interface TankGaugeReadingRepository extends JpaRepository<TankGaugeReading,UUID>{List<TankGaugeReading> findByOperationalDayIdOrderByTankNameAsc(UUID dayId);boolean existsByOperationalDayIdAndTankId(UUID dayId,UUID tankId);long countByOperationalDayId(UUID dayId);}
