package com.fuelflex.platform.tariffcategory.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.common.util.TextNormalizer;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tariff_categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tariff_category_organization_code",
                        columnNames = {"organization_id", "code"}),
                @UniqueConstraint(name = "uk_tariff_category_organization_name",
                        columnNames = {"organization_id", "name"})
        },
        indexes = @Index(name = "idx_tariff_category_organization", columnList = "organization_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffCategory {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tariff_category_organization"))
    private Organization organization;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "system_category", nullable = false)
    private boolean system;

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
        normalize();
        if (displayOrder == null || displayOrder < 1) displayOrder = 1;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        normalize();
        if (displayOrder == null || displayOrder < 1) displayOrder = 1;
        updatedAt = LocalDateTime.now();
    }

    private void normalize() {
        code = TextNormalizer.normalizeCode(code);
        name = TextNormalizer.normalizeText(name);
        description = TextNormalizer.normalizeNullableText(description);
    }
}
