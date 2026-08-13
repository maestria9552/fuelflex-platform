-- Manual, idempotent backfill. Review the preview before running the INSERT.
-- This file is intentionally not an automatic migration.

SELECT
    d.station_id,
    t.product_id,
    COUNT(*) AS tank_count
FROM tanks t
JOIN depots d ON d.id = t.depot_id
GROUP BY d.station_id, t.product_id
ORDER BY d.station_id, t.product_id;

WITH distinct_station_products AS (
    SELECT DISTINCT d.station_id, t.product_id
    FROM tanks t
    JOIN depots d ON d.id = t.depot_id
),
ordered_station_products AS (
    SELECT
        station_id,
        product_id,
        ROW_NUMBER() OVER (
            PARTITION BY station_id
            ORDER BY product_id
        ) AS display_order
    FROM distinct_station_products
)
INSERT INTO station_products (
    id,
    station_id,
    product_id,
    display_order,
    active,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    station_id,
    product_id,
    display_order,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM ordered_station_products
ON CONFLICT (station_id, product_id) DO NOTHING;
