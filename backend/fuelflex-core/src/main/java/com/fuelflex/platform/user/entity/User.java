package com.fuelflex.platform.user.entity;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fuelflex.platform.role.entity.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_users_phone_number",
                        columnNames = "phone_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /*
     * État général du compte.
     * Un compte désactivé ne peut pas se connecter.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /*
     * Vérification des coordonnées.
     */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    /*
     * Sécurité du compte.
     */
    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Column(name = "account_expired", nullable = false)
    private boolean accountExpired = false;

    @Column(name = "credentials_expired", nullable = false)
    private boolean credentialsExpired = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    /*
     * Date jusqu'à laquelle le compte reste verrouillé.
     * Null signifie qu'aucun verrouillage temporaire n'est actif.
     */
    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    /*
     * Date de la dernière connexion réussie.
     */
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    /*
     * Date du dernier changement de mot de passe.
     */
    @Column(name = "password_changed_at")
    private OffsetDateTime passwordChangedAt;

    /*
     * Code OTP envoyé par e-mail.
     */
    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    /*
     * Date d'expiration du code OTP.
     */
    @Column(name = "verification_code_expiration")
    private OffsetDateTime verificationCodeExpiration;

    /*
     * Nombre de tentatives de validation du code OTP.
     */
    @Column(name = "verification_code_attempts", nullable = false)
    private int verificationCodeAttempts = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    foreignKey = @ForeignKey(
                            name = "fk_user_roles_user"
                    )
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id",
                    foreignKey = @ForeignKey(
                            name = "fk_user_roles_role"
                    )
            ),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_user_roles",
                            columnNames = {"user_id", "role_id"}
                    )
            }
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (passwordChangedAt == null) {
            passwordChangedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}