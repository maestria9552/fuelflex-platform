package com.fuelflex.platform.assignment.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.organization.entity.Organization;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_station_transfers")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeStationTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfer_organization"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfer_employee"))
    private User employee;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_assignment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfer_source"))
    private UserStationAssignment sourceAssignment;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_assignment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfer_destination"))
    private UserStationAssignment destinationAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transferred_by", nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfer_actor"))
    private User transferredBy;

    @Column(name = "transferred_at", nullable = false, updatable = false)
    private OffsetDateTime transferredAt;

    @Column(name = "effective_at", nullable = false)
    private OffsetDateTime effectiveAt;

    @Column(length = 500)
    private String reason;
}
