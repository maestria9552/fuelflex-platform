package com.fuelflex.platform.dispensingpoint.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.common.util.TextNormalizer;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.tank.entity.Tank;

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
        name = "dispensing_points",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dispensing_point_pump_code",
                        columnNames = {
                                "pump_id",
                                "code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_dispensing_point_pump_nozzle",
                        columnNames = {
                                "pump_id",
                                "nozzle_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_dispensing_point_pump",
                        columnList = "pump_id"
                ),
                @Index(
                        name = "idx_dispensing_point_tank",
                        columnList = "tank_id"
                ),
                @Index(
                        name = "idx_dispensing_point_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_dispensing_point_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispensingPoint {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "pump_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_dispensing_point_pump"
            )
    )
    private Pump pump;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "tank_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_dispensing_point_tank"
            )
    )
    private Tank tank;

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
            name = "nozzle_number"
    )
    private Integer nozzleNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "metering_mode",
            nullable = false,
            length = 30
    )
    private MeteringMode meteringMode;

    @Column(
            name = "current_index",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal currentIndex;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private DispensingPointStatus status;

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

        if (meteringMode == null) {
            meteringMode = MeteringMode.MECHANICAL;
        }

        if (currentIndex == null) {
            currentIndex = BigDecimal.ZERO;
        }

        if (status == null) {
            status = DispensingPointStatus.INACTIVE;
        }

        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();

        normalizeFields();

        if (currentIndex == null) {
            currentIndex = BigDecimal.ZERO;
        }

        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
    }

    private void normalizeFields() {
        code = TextNormalizer.normalizeCode(code);
        name = TextNormalizer.normalizeText(name);
    }
}