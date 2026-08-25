package com.fuelflex.platform.employeevalidation.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.station.entity.Station;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "pump_attendant_validation_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pump_attendant_validation_request_number",
                columnNames = "request_number"
        ),
        indexes = {
                @Index(
                        name = "idx_pump_attendant_validation_manager",
                        columnList = "organization_id, created_by_id, created_at"
                ),
                @Index(
                        name = "idx_pump_attendant_validation_supervisor",
                        columnList = "organization_id, station_id, status, created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PumpAttendantValidationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "request_number", nullable = false, length = 30)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pump_validation_organization")
    )
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "station_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pump_validation_station")
    )
    private Station station;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PumpAttendantValidationRequestStatus status =
            PumpAttendantValidationRequestStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "created_by_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pump_validation_created_by")
    )
    private User createdBy;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reviewed_by_id",
            foreignKey = @ForeignKey(name = "fk_pump_validation_reviewed_by")
    )
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "review_comment", length = 1000)
    private String reviewComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
