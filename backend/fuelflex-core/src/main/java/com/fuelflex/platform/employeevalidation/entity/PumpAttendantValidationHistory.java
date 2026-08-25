package com.fuelflex.platform.employeevalidation.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationAction;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "pump_attendant_validation_history",
        indexes = @Index(
                name = "idx_pump_validation_history_timeline",
                columnList = "request_id, performed_at, id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class PumpAttendantValidationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "request_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pump_validation_history_request")
    )
    private PumpAttendantValidationRequest request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PumpAttendantValidationAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 40)
    private PumpAttendantValidationRequestStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 40)
    private PumpAttendantValidationRequestStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "performed_by_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pump_validation_history_actor")
    )
    private User performedBy;

    @Column(name = "performed_at", nullable = false)
    private OffsetDateTime performedAt;

    @Column(length = 1000)
    private String comment;

    @PrePersist
    void onCreate() {
        if (performedAt == null) {
            performedAt = OffsetDateTime.now();
        }
    }
}
