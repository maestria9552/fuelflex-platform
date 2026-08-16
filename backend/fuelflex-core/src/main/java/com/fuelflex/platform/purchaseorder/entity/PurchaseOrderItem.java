package com.fuelflex.platform.purchaseorder.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fuelflex.platform.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_order_items", uniqueConstraints = @UniqueConstraint(name = "uk_purchase_order_item_product", columnNames = {"purchase_order_id", "product_id"}))
@Getter @Setter @NoArgsConstructor
public class PurchaseOrderItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "purchase_order_id", nullable = false) private PurchaseOrder purchaseOrder;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false) private Product product;
    @Column(nullable = false, precision = 19, scale = 3) private BigDecimal quantity;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void create() { var now=OffsetDateTime.now(); createdAt=now; updatedAt=now; }
    @PreUpdate void update() { updatedAt=OffsetDateTime.now(); }
}
