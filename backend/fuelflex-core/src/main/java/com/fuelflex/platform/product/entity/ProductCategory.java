package com.fuelflex.platform.product.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.organization.entity.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_categories_organization_code",
                        columnNames = {
                                "organization_id",
                                "code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_product_categories_organization_name",
                        columnNames = {
                                "organization_id",
                                "name"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * Organisation propriétaire de la catégorie.
     *
     * Une catégorie ne peut appartenir qu'à une seule organisation.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_product_categories_organization"
            )
    )
    private Organization organization;

    /*
     * Code interne de la catégorie.
     *
     * Exemples :
     * CARBURANT
     * LUBRIFIANT
     * GAZ
     *
     * Le code est unique à l'intérieur d'une organisation.
     */
    @Column(nullable = false, length = 50)
    private String code;

    /*
     * Nom de la catégorie.
     *
     * Exemples :
     * Carburants
     * Lubrifiants
     * Gaz
     */
    @Column(nullable = false, length = 120)
    private String name;

    /*
     * Description facultative de la catégorie.
     */
    @Column(length = 500)
    private String description;

    /*
     * Une catégorie inactive ne pourra plus être utilisée pour créer
     * de nouveaux produits, mais restera conservée dans l'historique.
     */
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
        name = normalizeText(name);
        description = normalizeOptionalText(description);

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        code = normalizeCode(code);
        name = normalizeText(name);
        description = normalizeOptionalText(description);

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

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }
}