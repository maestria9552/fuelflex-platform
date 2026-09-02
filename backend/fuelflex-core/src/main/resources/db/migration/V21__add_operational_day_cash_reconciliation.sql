ALTER TABLE shift_reconciliations
    ADD COLUMN internal_unit_price NUMERIC(19,3);

UPDATE shift_reconciliations
SET internal_unit_price = CASE
    WHEN internal_consumption_volume > 0
        THEN ROUND(internal_consumption_amount / internal_consumption_volume, 3)
    ELSE 0
END;

ALTER TABLE shift_reconciliations
    ALTER COLUMN internal_unit_price SET NOT NULL,
    ADD CONSTRAINT ck_shift_reconciliation_internal_price CHECK (internal_unit_price >= 0);

ALTER TABLE operational_day_summaries
    ADD COLUMN reference_currency VARCHAR(10),
    ADD COLUMN cash_gross_expected NUMERIC(19,3),
    ADD COLUMN disbursed_expense_amount NUMERIC(19,3),
    ADD COLUMN cash_net_expected NUMERIC(19,3),
    ADD COLUMN physical_reference_amount NUMERIC(19,3),
    ADD COLUMN physical_usd_amount NUMERIC(19,3),
    ADD COLUMN usd_exchange_rate NUMERIC(19,6),
    ADD COLUMN converted_usd_amount NUMERIC(19,3),
    ADD COLUMN observed_cash_amount NUMERIC(19,3),
    ADD COLUMN cash_variance NUMERIC(19,3);

UPDATE operational_day_summaries summary
SET reference_currency = UPPER(organization.default_currency),
    cash_gross_expected = summary.expected_cash,
    disbursed_expense_amount = summary.expense_amount,
    cash_net_expected = summary.expected_net_cash,
    physical_reference_amount = GREATEST(summary.expected_net_cash, 0),
    physical_usd_amount = 0,
    usd_exchange_rate = 1,
    converted_usd_amount = 0,
    observed_cash_amount = GREATEST(summary.expected_net_cash, 0),
    cash_variance = GREATEST(summary.expected_net_cash, 0) - summary.expected_net_cash
FROM operational_days day
JOIN organizations organization ON organization.id = day.organization_id
WHERE summary.operational_day_id = day.id;

ALTER TABLE operational_day_summaries
    ALTER COLUMN reference_currency SET NOT NULL,
    ALTER COLUMN cash_gross_expected SET NOT NULL,
    ALTER COLUMN disbursed_expense_amount SET NOT NULL,
    ALTER COLUMN cash_net_expected SET NOT NULL,
    ALTER COLUMN physical_reference_amount SET NOT NULL,
    ALTER COLUMN physical_usd_amount SET NOT NULL,
    ALTER COLUMN usd_exchange_rate SET NOT NULL,
    ALTER COLUMN converted_usd_amount SET NOT NULL,
    ALTER COLUMN observed_cash_amount SET NOT NULL,
    ALTER COLUMN cash_variance SET NOT NULL,
    ADD CONSTRAINT ck_day_summary_reference_currency CHECK (reference_currency = UPPER(reference_currency) AND LENGTH(TRIM(reference_currency)) > 0),
    ADD CONSTRAINT ck_day_summary_cash_inputs CHECK (cash_gross_expected >= 0 AND disbursed_expense_amount >= 0 AND physical_reference_amount >= 0 AND physical_usd_amount >= 0 AND usd_exchange_rate > 0 AND converted_usd_amount >= 0 AND observed_cash_amount >= 0),
    ADD CONSTRAINT ck_day_summary_cash_net CHECK (cash_net_expected = ROUND(cash_gross_expected - disbursed_expense_amount, 3)),
    ADD CONSTRAINT ck_day_summary_usd_conversion CHECK (converted_usd_amount = ROUND(physical_usd_amount * usd_exchange_rate, 3)),
    ADD CONSTRAINT ck_day_summary_observed_cash CHECK (observed_cash_amount = ROUND(physical_reference_amount + converted_usd_amount, 3)),
    ADD CONSTRAINT ck_day_summary_cash_variance CHECK (cash_variance = ROUND(observed_cash_amount - cash_net_expected, 3));
