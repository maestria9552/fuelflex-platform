# FuelFlex database migrations

The existing PostgreSQL `public` schema is registered as Flyway baseline version `1`.
Existing non-empty environments must set `FLYWAY_BASELINE_ON_MIGRATE=true` only for their first controlled startup; it is disabled by default.
Versioned schema changes start at `V2`.

This directory intentionally contains no executable migration yet. Employee-to-station
assignments are not part of this baseline block.

Scripts under `db/manual` remain manually reviewed backfills and must not be replayed
automatically without a dedicated migration review.
