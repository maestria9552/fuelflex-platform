package com.fuelflex.platform.tank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.product.entity.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tanks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tank_depot_code",
                        columnNames = {
                                "depot_id",
                                "code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_tank_depot_name",
                        columnNames = {
                                "depot_id",
                                "name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_tank_depot",
                        columnList = "depot_id"
                ),
                @Index(
                        name = "idx_tank_product",
                        columnList = "product_id"
                ),
                @Index(
                        name = "idx_tank_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_tank_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tank {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "depot_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_tank_depot"
            )
    )
    private Depot depot;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_tank_product"
            )
    )
    private Product product;

    @Column(
            nullable = false,
            length = 50
    )
    private String code;

    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "capacity_liters",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal capacityLiters;

    @Column(
            name = "minimum_level_liters",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal minimumLevelLiters;

    @Column(
            name = "maximum_level_liters",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal maximumLevelLiters;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private TankStatus status;

    @Column(
            length = 255
    )
    private String location;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;

    @Column(
            nullable = false
    )
    private boolean active;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        code = normalizeCode(code);
        name = normalizeRequiredText(name);
        location = normalizeNullableText(location);

        if (minimumLevelLiters == null) {
            minimumLevelLiters = BigDecimal.ZERO;
        }

        if (maximumLevelLiters == null && capacityLiters != null) {
            maximumLevelLiters = capacityLiters;
        }

        if (status == null) {
            status = TankStatus.INACTIVE;
        }

        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();

        code = normalizeCode(code);
        name = normalizeRequiredText(name);
        location = normalizeNullableText(location);

        if (minimumLevelLiters == null) {
            minimumLevelLiters = BigDecimal.ZERO;
        }

        if (maximumLevelLiters == null && capacityLiters != null) {
            maximumLevelLiters = capacityLiters;
        }

        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String normalizeRequiredText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim()
                .replaceAll("\\s+", " ");

        return normalized.isBlank()
                ? null
                : normalized;
    }
}