ALTER TABLE shift_reconciliations
    DROP CONSTRAINT ck_shift_reconciliation_accounted,
    ADD CONSTRAINT ck_shift_reconciliation_accounted
        CHECK (accounted_volume = ROUND(total_sold_volume + tank_return_volume + internal_consumption_volume, 3));
