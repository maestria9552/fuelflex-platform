ALTER TABLE operational_day_summaries
    ADD COLUMN cash_reconciliation_available BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN operational_day_summaries.cash_reconciliation_available IS
    'True only when the physical cash composition was actually declared through the cash reconciliation workflow.';
