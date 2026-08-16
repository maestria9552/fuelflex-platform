package com.fuelflex.platform.purchaseorder.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.purchaseorder.model.PurchaseOrderStatus;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.supplier.entity.OrganizationSupplier;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_orders")
@Getter @Setter @NoArgsConstructor
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organization_id", nullable = false) private Organization organization;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "station_id", nullable = false) private Station station;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "organization_supplier_id") private OrganizationSupplier organizationSupplier;
    @Column(name = "order_number", nullable = false, unique = true, updatable = false, length = 30) private String orderNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50) private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false, updatable = false) private User createdBy;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @Column(name = "submitted_at") private OffsetDateTime submittedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "supervisor_reviewed_by") private User supervisorReviewedBy;
    @Column(name = "supervisor_reviewed_at") private OffsetDateTime supervisorReviewedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "supplier_reviewed_by") private User supplierReviewedBy;
    @Column(name = "supplier_reviewed_at") private OffsetDateTime supplierReviewedAt;
    @Version @Column(nullable = false) private long version;
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC") private List<PurchaseOrderItem> items = new ArrayList<>();

    @PrePersist void create() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void update() { updatedAt = OffsetDateTime.now(); }
    public void replaceItems(List<PurchaseOrderItem> replacements) {
        items.clear();
        replacements.forEach(item -> { item.setPurchaseOrder(this); items.add(item); });
    }
}
