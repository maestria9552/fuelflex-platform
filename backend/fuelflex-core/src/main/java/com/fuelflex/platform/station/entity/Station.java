package com.fuelflex.platform.station.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.organization.entity.Organization;

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
        name = "stations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_station_organization_code",
                        columnNames = {
                                "organization_id",
                                "code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_station_organization_name",
                        columnNames = {
                                "organization_id",
                                "name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_station_organization",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_station_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_station_type",
                        columnList = "type"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Station {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "organization_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_station_organization"
            )
    )
    private Organization organization;

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
            name = "short_name",
            length = 100
    )
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private StationType type;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private StationStatus status;

    @Column(
            length = 255
    )
    private String address;

    @Column(
            length = 100
    )
    private String city;

    @Column(
            length = 100
    )
    private String province;

    @Column(
            length = 100
    )
    private String country;

    @Column(
            name = "phone_number",
            length = 30
    )
    private String phoneNumber;

    @Column(
            length = 150
    )
    private String email;

    @Column(
            name = "latitude",
            length = 30
    )
    private String latitude;

    @Column(
            name = "longitude",
            length = 30
    )
    private String longitude;

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
        shortName = normalizeNullableText(shortName);
        address = normalizeNullableText(address);
        city = normalizeNullableText(city);
        province = normalizeNullableText(province);
        country = normalizeNullableText(country);
        phoneNumber = normalizeNullableText(phoneNumber);
        email = normalizeEmail(email);
        latitude = normalizeNullableText(latitude);
        longitude = normalizeNullableText(longitude);

        if (type == null) {
            type = StationType.SERVICE_STATION;
        }

        if (status == null) {
            status = StationStatus.INACTIVE;
        }

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
        shortName = normalizeNullableText(shortName);
        address = normalizeNullableText(address);
        city = normalizeNullableText(city);
        province = normalizeNullableText(province);
        country = normalizeNullableText(country);
        phoneNumber = normalizeNullableText(phoneNumber);
        email = normalizeEmail(email);
        latitude = normalizeNullableText(latitude);
        longitude = normalizeNullableText(longitude);

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

    private String normalizeEmail(
            String value
    ) {
        String normalized =
                normalizeNullableText(value);

        return normalized == null
                ? null
                : normalized.toLowerCase();
    }
}