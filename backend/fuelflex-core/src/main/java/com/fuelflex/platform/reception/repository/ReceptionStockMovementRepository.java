package com.fuelflex.platform.reception.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fuelflex.platform.reception.entity.ReceptionStockMovement;

public interface ReceptionStockMovementRepository extends JpaRepository<ReceptionStockMovement, UUID> {
    boolean existsByAllocationId(UUID allocationId);
    long countByReceptionId(UUID receptionId);
}
