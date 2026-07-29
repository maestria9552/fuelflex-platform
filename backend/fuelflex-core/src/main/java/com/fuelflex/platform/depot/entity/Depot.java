package com.fuelflex.platform.depot.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.station.entity.Station;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "depots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_depot_station_code",
                        columnNames = {
                                "station_id",
                                "code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_depot_station_name",
                        columnNames = {
                                "station_id",
                                "name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_depot_station",
                        columnList = "station_id"
                ),
                @Index(
                        name = "idx_depot_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Depot {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "station_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_depot_station"
            )
    )
    private Station station;

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
            length = 255
    )
    private String description;

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
        description = normalizeNullableText(description);
        location = normalizeNullableText(location);

        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }

        if (!active) {
            active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();

        code = normalizeCode(code);
        name = normalizeRequiredText(name);
        description = normalizeNullableText(description);
        location = normalizeNullableText(location);

        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
    }

    private String normalizeCode(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String normalizeRequiredText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim().replaceAll("\\s+", " ");

        return normalized.isBlank()
                ? null
                : normalized;
    }
}