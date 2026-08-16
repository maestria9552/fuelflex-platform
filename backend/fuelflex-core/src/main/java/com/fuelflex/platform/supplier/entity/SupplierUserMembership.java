package com.fuelflex.platform.supplier.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "supplier_user_memberships")
@Getter @Setter @NoArgsConstructor
public class SupplierUserMembership {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "supplier_id", nullable = false) private Supplier supplier;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "ended_at") private OffsetDateTime endedAt;
    @Version @Column(nullable = false) private long version;
    @PrePersist void create() { createdAt = OffsetDateTime.now(); }
}
