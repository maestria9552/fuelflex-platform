package com.fuelflex.platform.stationproduct.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.station.entity.Station;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "station_products",
        uniqueConstraints = @UniqueConstraint(name = "uk_station_product", columnNames = {"station_id", "product_id"}),
        indexes = {
                @Index(name = "idx_station_product_station", columnList = "station_id"),
                @Index(name = "idx_station_product_product", columnList = "product_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationProduct {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_station_product_station"))
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_station_product_product"))
    private Product product;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
    }
}
