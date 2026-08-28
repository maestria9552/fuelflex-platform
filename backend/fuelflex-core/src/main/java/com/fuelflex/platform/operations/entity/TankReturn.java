package com.fuelflex.platform.operations.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tank_returns", indexes = {
        @Index(name = "idx_tank_return_day", columnList = "operational_day_id"),
        @Index(name = "idx_tank_return_shift", columnList = "shift_assignment_id")
})
@Getter @Setter @NoArgsConstructor
public class TankReturn {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organization_id", nullable = false) private Organization organization;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "operational_day_id", nullable = false) private OperationalDay operationalDay;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "shift_assignment_id", nullable = false) private PumpShiftAssignment shiftAssignment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tank_id", nullable = false) private Tank tank;
    @Column(nullable = false, precision = 19, scale = 3) private BigDecimal quantity;
    @Column(length = 1000) private String reason;
    @Column(name = "occurred_at", nullable = false) private OffsetDateTime occurredAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private User createdBy;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @PrePersist void create() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
}
