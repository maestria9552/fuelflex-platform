package com.fuelflex.platform.fuelmeter.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.common.util.TextNormalizer;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.pump.entity.Pump;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "fuel_meters",
        indexes = {
                @Index(
                        name = "idx_fuel_meter_pump",
                        columnList = "pump_id"
                ),
                @Index(
                        name = "idx_fuel_meter_dispensing_point",
                        columnList = "dispensing_point_id"
                ),
                @Index(
                        name = "idx_fuel_meter_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_fuel_meter_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuelMeter {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "pump_id",
            foreignKey = @ForeignKey(name = "fk_fuel_meter_pump")
    )
    private Pump pump;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dispensing_point_id",
            foreignKey = @ForeignKey(
                    name = "fk_fuel_meter_dispensing_point"
            )
    )
    private DispensingPoint dispensingPoint;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MeterTechnology technology;

    @Column(
            name = "current_index",
            nullable = false,
            precision = 19,
            scale = 3
    )
    private BigDecimal currentIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FuelMeterStatus status;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalizeFields();

        if (currentIndex == null) {
            currentIndex = BigDecimal.ZERO;
        }
        if (status == null) {
            status = FuelMeterStatus.INACTIVE;
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
