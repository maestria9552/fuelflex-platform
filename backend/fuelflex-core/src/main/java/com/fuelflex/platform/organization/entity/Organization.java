package com.fuelflex.platform.organization.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_organizations_code",
                        columnNames = "code"
                ),
                @UniqueConstraint(
                        name = "uk_organizations_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_organizations_registration_number",
                        columnNames = "registration_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * Code technique unique de l’organisation.
     * Exemples : GANADOR, TOTAL-RDC, PETRO-KIN.
     */
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(name = "legal_name", length = 200)
    private String legalName;

    @Column(name = "trade_name", length = 180)
    private String tradeName;

    /*
     * RCCM.
     */
    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    /*
     * ID NAT.
     */
    @Column(name = "national_id", length = 100)
    private String nationalId;

    /*
     * NIF.
     */
    @Column(name = "tax_number", length = 100)
    private String taxNumber;

    @Column(length = 180)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String website;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String city;

    @Column(length = 500)
    private String address;

    /*
     * Code ISO de la devise.
     * Exemples : USD, CDF, EUR.
     */
    @Column(name = "default_currency", nullable = false, length = 10)
    private String defaultCurrency = "USD";

    @Column(nullable = false, length = 100)
    private String timezone = "Africa/Kinshasa";

    @Column(name = "default_language", nullable = false, length = 10)
    private String defaultLanguage = "fr";

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "secondary_color", length = 20)
    private String secondaryColor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganizationStatus status = OrganizationStatus.PENDING;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        code = normalizeCode(code);
        email = normalizeEmail(email);

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        code = normalizeCode(code);
        email = normalizeEmail(email);

        updatedAt = OffsetDateTime.now();
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }
}
