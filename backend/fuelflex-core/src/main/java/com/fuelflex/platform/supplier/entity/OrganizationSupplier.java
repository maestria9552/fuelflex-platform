package com.fuelflex.platform.supplier.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.fuelflex.platform.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organization_suppliers", uniqueConstraints = @UniqueConstraint(name = "uk_organization_suppliers_pair", columnNames = {"organization_id", "supplier_id"}))
@Getter @Setter @NoArgsConstructor
public class OrganizationSupplier {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organization_id", nullable = false) private Organization organization;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "supplier_id", nullable = false) private Supplier supplier;
    @Column(name = "internal_code", length = 100) private String internalCode;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "partnership_started_at", nullable = false) private OffsetDateTime partnershipStartedAt;
    @Column(name = "partnership_ended_at") private OffsetDateTime partnershipEndedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @Version @Column(nullable = false) private long version;
    @PrePersist void create() { var now = OffsetDateTime.now(); if (partnershipStartedAt == null) partnershipStartedAt = now; createdAt = now; updatedAt = now; }
    @PreUpdate void update() { updatedAt = OffsetDateTime.now(); }
}
