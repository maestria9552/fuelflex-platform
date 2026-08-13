package com.fuelflex.platform.stationproductprice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "station_product_prices",
        uniqueConstraints = @UniqueConstraint(name = "uk_station_product_price_category",
                columnNames = {"station_product_id", "tariff_category_id"}),
        indexes = @Index(name = "idx_station_product_price_tariff_category", columnList = "tariff_category_id"),
        check = @CheckConstraint(name = "ck_station_product_price_positive", constraint = "price > 0"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationProductPrice {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_station_product_price_station_product"))
    private StationProduct stationProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_station_product_price_tariff_category"))
    private TariffCategory tariffCategory;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal price;

    @Column(nullable = false)
    // Disponible si ce prix, son StationProduct et sa TariffCategory sont actifs.
    // Un changement de montant ne modifie jamais cette disponibilité.
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
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
