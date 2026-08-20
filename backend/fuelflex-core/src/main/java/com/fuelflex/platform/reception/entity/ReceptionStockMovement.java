package com.fuelflex.platform.reception.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Immutable inbound stock ledger entry produced by a validated reception. */
@Entity
@Table(name = "reception_stock_movements", uniqueConstraints =
        @UniqueConstraint(name = "uk_reception_stock_movement_allocation", columnNames = "allocation_id"))
@Getter @Setter @NoArgsConstructor
public class ReceptionStockMovement {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reception_id", nullable = false) private Reception reception;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "allocation_id", nullable = false) private ReceptionTankAllocation allocation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "station_id", nullable = false) private Station station;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tank_id", nullable = false) private Tank tank;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false) private Product product;
    @Column(nullable = false, precision = 19, scale = 3) private BigDecimal quantity;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private User createdBy;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
}
