package com.fuelflex.platform.purchaseorder.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.fuelflex.platform.purchaseorder.entity.PurchaseOrder;
import jakarta.persistence.LockModeType;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    @EntityGraph(attributePaths={"station","organizationSupplier","organizationSupplier.supplier","createdBy","items","items.product"})
    Optional<PurchaseOrder> findByIdAndOrganizationId(UUID id, UUID organizationId);
    @EntityGraph(attributePaths={"station","organizationSupplier","organizationSupplier.supplier","createdBy"})
    Page<PurchaseOrder> findByOrganizationIdAndStationIdIn(UUID organizationId, Iterable<UUID> stationIds, Pageable pageable);
    @EntityGraph(attributePaths={"station","organizationSupplier","organizationSupplier.supplier","createdBy"})
    Page<PurchaseOrder> findByOrganizationId(UUID organizationId, Pageable pageable);
    long countByOrganizationIdAndStatus(UUID organizationId, com.fuelflex.platform.purchaseorder.model.PurchaseOrderStatus status);
    @EntityGraph(attributePaths={"station","organizationSupplier","organizationSupplier.supplier","createdBy"})
    Page<PurchaseOrder> findByOrganizationIdAndStatus(UUID organizationId, com.fuelflex.platform.purchaseorder.model.PurchaseOrderStatus status, Pageable pageable);
    @EntityGraph(attributePaths={"station","organizationSupplier","organizationSupplier.supplier","createdBy"})
    @Query("select po from PurchaseOrder po join po.organizationSupplier os where os.supplier.id=:supplierId")
    Page<PurchaseOrder> findForSupplier(@Param("supplierId") UUID supplierId, Pageable pageable);
    @EntityGraph(attributePaths={"station","organizationSupplier","organizationSupplier.supplier","createdBy","items","items.product"})
    @Query("select po from PurchaseOrder po join po.organizationSupplier os where po.id=:id and os.supplier.id=:supplierId")
    Optional<PurchaseOrder> findByIdForSupplier(@Param("id") UUID id, @Param("supplierId") UUID supplierId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select po from PurchaseOrder po where po.id=:id and po.organization.id=:organizationId")
    Optional<PurchaseOrder> lockByIdAndOrganizationId(@Param("id") UUID id, @Param("organizationId") UUID organizationId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select po from PurchaseOrder po join po.organizationSupplier os where po.id=:id and os.supplier.id=:supplierId")
    Optional<PurchaseOrder> lockByIdForSupplier(@Param("id") UUID id, @Param("supplierId") UUID supplierId);
}
