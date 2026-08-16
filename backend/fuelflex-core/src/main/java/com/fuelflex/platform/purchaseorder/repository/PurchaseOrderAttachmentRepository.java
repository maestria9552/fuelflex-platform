package com.fuelflex.platform.purchaseorder.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fuelflex.platform.purchaseorder.entity.PurchaseOrderAttachment;

public interface PurchaseOrderAttachmentRepository extends JpaRepository<PurchaseOrderAttachment, UUID> {
    List<PurchaseOrderAttachment> findByPurchaseOrderIdOrderByUploadedAtAsc(UUID purchaseOrderId);
    Optional<PurchaseOrderAttachment> findByIdAndPurchaseOrderOrganizationId(UUID id, UUID organizationId);
    long countByPurchaseOrderId(UUID purchaseOrderId);
}
