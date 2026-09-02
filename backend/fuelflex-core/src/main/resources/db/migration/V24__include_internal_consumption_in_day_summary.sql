ALTER TABLE operational_day_summaries
    DROP CONSTRAINT ck_day_summary_accounted,
    ADD CONSTRAINT ck_day_summary_accounted
        CHECK (accounted_volume = ROUND(sold_volume + tank_return_volume + internal_consumption_volume, 3));
