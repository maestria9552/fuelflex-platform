package com.fuelflex.platform.purchaseorder.repository;
import java.util.List; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fuelflex.platform.purchaseorder.entity.PurchaseOrderHistory;
public interface PurchaseOrderHistoryRepository extends JpaRepository<PurchaseOrderHistory, UUID> {
    List<PurchaseOrderHistory> findByPurchaseOrderIdOrderByPerformedAtAscIdAsc(UUID purchaseOrderId);
}
