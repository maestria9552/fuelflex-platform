package com.fuelflex.platform.assignment.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_station_assignments")
@Getter
@Setter
@NoArgsConstructor
public class UserStationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_assignment_organization"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_assignment_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_assignment_station"))
    private Station station;

    @Column(name = "valid_from", nullable = false)
    private OffsetDateTime validFrom;

    @Column(name = "valid_until")
    private OffsetDateTime validUntil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false,
            foreignKey = @ForeignKey(name = "fk_assignment_created_by"))
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ended_by", foreignKey = @ForeignKey(name = "fk_assignment_ended_by"))
    private User endedBy;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @Column(length = 500)
    private String reason;

    @Version
    @Column(nullable = false)
    private long version;

    public boolean isActive() {
        return validUntil == null;
    }
}
