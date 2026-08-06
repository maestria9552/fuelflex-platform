package com.fuelflex.platform.pump.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.common.util.TextNormalizer;
import com.fuelflex.platform.station.entity.Station;

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
        name = "pumps",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pump_station_code",
                        columnNames = {
                                "station_id",
                                "code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_pump_station_number",
                        columnNames = {
                                "station_id",
                                "pump_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_pump_station",
                        columnList = "station_id"
                ),
                @Index(
                        name = "idx_pump_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_pump_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pump {

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
                    name = "fk_pump_station"
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
            name = "pump_number",
            nullable = false
    )
    private Integer pumpNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "metering_level",
            nullable = false,
            length = 30
    )
    private MeteringLevel meteringLevel;

    @Column(
            length = 100
    )
    private String manufacturer;

    @Column(
            length = 100
    )
    private String model;

    @Column(
            name = "serial_number",
            length = 100
    )
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private PumpStatus status;

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

        normalizeFields();

        if (status == null) {
            status = PumpStatus.INACTIVE;
        }

        if (meteringLevel == null) {
            meteringLevel = MeteringLevel.PUMP;
        }

        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }

        /*
         * Le champ active est un boolean primitif.
         * Il vaut false par défaut, sans permettre de distinguer :
         * - une valeur non fournie ;
         * - une désactivation volontaire.
         *
         * Le service définit donc active=true lors de la création
         * lorsque la requête ne fournit aucune valeur.
         */
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();

        normalizeFields();

        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
    }

    private void normalizeFields() {
        code = TextNormalizer.normalizeCode(code);
        name = TextNormalizer.normalizeText(name);

        manufacturer = TextNormalizer.normalizeNullableText(
                manufacturer
        );

        model = TextNormalizer.normalizeNullableText(
                model
        );

        serialNumber = TextNormalizer.normalizeNullableText(
                serialNumber
        );

        location = TextNormalizer.normalizeNullableText(
                location
        );
    }
}
