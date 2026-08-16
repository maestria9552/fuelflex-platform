package com.fuelflex.platform.purchaseorder.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.fuelflex.platform.purchaseorder.model.PurchaseOrderAction;
import com.fuelflex.platform.purchaseorder.model.PurchaseOrderStatus;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_order_history")
@Getter @Setter @NoArgsConstructor
public class PurchaseOrderHistory {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "purchase_order_id", nullable = false, updatable = false) private PurchaseOrder purchaseOrder;
    @Enumerated(EnumType.STRING) @Column(name = "from_status", length = 50, updatable = false) private PurchaseOrderStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(name = "to_status", nullable = false, length = 50, updatable = false) private PurchaseOrderStatus toStatus;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50, updatable = false) private PurchaseOrderAction action;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "performed_by", nullable = false, updatable = false) private User performedBy;
    @Column(name = "performed_at", nullable = false, updatable = false) private OffsetDateTime performedAt;
    @Column(length = 1000, updatable = false) private String comment;
    @PrePersist void create() { if (performedAt == null) performedAt = OffsetDateTime.now(); }
}
