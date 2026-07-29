package com.fuelflex.platform.product.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.organization.entity.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_org_code",
                        columnNames = {
                                "organization_id",
                                "code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_product_org_name",
                        columnNames = {
                                "organization_id",
                                "name"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false
    )
    private ProductCategory category;

    @Column(
            nullable = false,
            length = 30
    )
    private String code;

    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    @Column(length = 80)
    private String shortName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private ProductUnit unit;

    @Column(length = 20)
    private String barcode;

    @Column(length = 20)
    private String color;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    private void prePersist() {

        if (id == null) {
            id = UUID.randomUUID();
        }

        code = normalize(code);
        name = normalize(name);
        shortName = normalizeNullable(shortName);
        description = normalizeNullable(description);
        barcode = normalizeNullable(barcode);
        color = normalizeNullable(color);

        if (displayOrder == null) {
            displayOrder = 1;
        }

        active = true;

        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void preUpdate() {

        code = normalize(code);
        name = normalize(name);
        shortName = normalizeNullable(shortName);
        description = normalizeNullable(description);
        barcode = normalizeNullable(barcode);
        color = normalizeNullable(color);

        updatedAt = OffsetDateTime.now();
    }

    private String normalize(String value) {
        return value == null
                ? null
                : value.trim();
    }

    private String normalizeNullable(String value) {

        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}