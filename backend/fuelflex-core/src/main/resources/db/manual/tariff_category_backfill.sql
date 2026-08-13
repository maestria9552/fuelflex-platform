-- Manual and idempotent backfill for organizations created before TariffCategory.
-- This file is intentionally not an automatic migration.

WITH defaults(code, name, description, display_order) AS (
    VALUES
        ('CASH', 'Prix officiel / Vente cash',
         'Prix de vente appliqué aux ventes ordinaires au comptant.', 1),
        ('CREDIT', 'Prix vente à crédit / Partenaires',
         'Prix de vente appliqué aux ventes à crédit et aux partenaires.', 2),
        ('INTERNAL', 'Prix interne station',
         'Prix de vente appliqué aux consommations internes de la station.', 3)
)
SELECT o.id AS organization_id, d.code, d.name, d.display_order
FROM organizations o
CROSS JOIN defaults d
LEFT JOIN tariff_categories tc
    ON tc.organization_id = o.id
    AND upper(tc.code) = d.code
WHERE tc.id IS NULL
ORDER BY o.id, d.display_order;

WITH defaults(code, name, description, display_order) AS (
    VALUES
        ('CASH', 'Prix officiel / Vente cash',
         'Prix de vente appliqué aux ventes ordinaires au comptant.', 1),
        ('CREDIT', 'Prix vente à crédit / Partenaires',
         'Prix de vente appliqué aux ventes à crédit et aux partenaires.', 2),
        ('INTERNAL', 'Prix interne station',
         'Prix de vente appliqué aux consommations internes de la station.', 3)
)
INSERT INTO tariff_categories (
    id, organization_id, code, name, description, system_category,
    display_order, active, created_at, updated_at
)
SELECT gen_random_uuid(), o.id, d.code, d.name, d.description, TRUE,
       d.display_order, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organizations o
CROSS JOIN defaults d
ON CONFLICT (organization_id, code) DO NOTHING;
