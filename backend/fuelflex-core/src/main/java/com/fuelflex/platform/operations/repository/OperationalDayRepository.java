package com.fuelflex.platform.operations.repository;
import java.time.LocalDate; import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType;
import com.fuelflex.platform.operations.entity.*;
public interface OperationalDayRepository extends JpaRepository<OperationalDay,UUID> {
 boolean existsByStationIdAndBusinessDate(UUID stationId, LocalDate businessDate);
 boolean existsByStationIdAndStatus(UUID stationId, OperationalStatus status);
 Optional<OperationalDay> findByIdAndOrganizationId(UUID id,UUID organizationId);
 List<OperationalDay> findByOrganizationIdAndStationIdInOrderByBusinessDateDescCreatedAtDesc(UUID organizationId,Collection<UUID> stationIds);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select d from OperationalDay d where d.id=:id and d.organization.id=:organizationId")
 Optional<OperationalDay> lockByIdAndOrganizationId(@Param("id") UUID id,@Param("organizationId") UUID organizationId);
}
