package com.fuelflex.platform.role.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

import com.fuelflex.platform.permission.entity.Permission;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * Le code est l’identifiant technique du rôle.
     * Exemples : SUPER_ADMIN, SUPERVISOR, CHEF_DEPOT.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType type;

    @Column(name = "system_role", nullable = false)
    private boolean systemRole = false;

    @Column(nullable = false)
    private boolean customizable = true;

    @Column(nullable = false)
    private boolean active = true;

    /*
     * Null pour les rôles globaux FuelFlex.
     * Renseigné plus tard pour les rôles propres à une organisation.
     */
    @Column(name = "organization_id")
    private UUID organizationId;
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(
                name = "role_id",
                foreignKey = @jakarta.persistence.ForeignKey(
                        name = "fk_role_permissions_role"
                )
        ),
        inverseJoinColumns = @JoinColumn(
                name = "permission_id",
                foreignKey = @jakarta.persistence.ForeignKey(
                        name = "fk_role_permissions_permission"
                )
        ),
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_permissions",
                        columnNames = {"role_id", "permission_id"}
                )
        }
)
private Set<Permission> permissions = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Role(
            String code,
            String name,
            String description,
            RoleType type,
            boolean systemRole,
            boolean customizable
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.type = type;
        this.systemRole = systemRole;
        this.customizable = customizable;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
