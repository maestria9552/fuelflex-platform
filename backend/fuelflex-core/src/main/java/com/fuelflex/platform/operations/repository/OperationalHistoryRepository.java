package com.fuelflex.platform.operations.repository;
import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository; import com.fuelflex.platform.operations.entity.OperationalHistory;
public interface OperationalHistoryRepository extends JpaRepository<OperationalHistory,UUID> {}
