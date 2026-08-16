package com.fuelflex.platform.supplier.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "suppliers", indexes = @Index(name = "idx_suppliers_active", columnList = "active"))
@Getter @Setter @NoArgsConstructor
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "legal_name", nullable = false, length = 180) private String legalName;
    @Column(name = "display_name", nullable = false, length = 180) private String displayName;
    @Column(length = 180) private String email;
    @Column(length = 30) private String phone;
    @Column(length = 500) private String address;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @Version @Column(nullable = false) private long version;
    @PrePersist void create() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; normalize(); }
    @PreUpdate void update() { updatedAt = OffsetDateTime.now(); normalize(); }
    private void normalize() { if (legalName != null) legalName = legalName.trim(); if (displayName != null) displayName = displayName.trim(); }
}
