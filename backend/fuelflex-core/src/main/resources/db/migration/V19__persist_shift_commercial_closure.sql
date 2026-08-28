ALTER TABLE shift_reconciliations ADD COLUMN source_tank_id UUID REFERENCES tanks(id) ON DELETE RESTRICT;
ALTER TABLE shift_reconciliations ADD COLUMN product_id UUID REFERENCES products(id) ON DELETE RESTRICT;
ALTER TABLE shift_reconciliations ADD COLUMN cash_unit_price NUMERIC(19,3);
ALTER TABLE shift_reconciliations ADD COLUMN credit_unit_price NUMERIC(19,3);
ALTER TABLE shift_reconciliations ADD COLUMN cash_amount NUMERIC(19,3);
ALTER TABLE shift_reconciliations ADD COLUMN credit_amount NUMERIC(19,3);
ALTER TABLE shift_reconciliations ADD COLUMN turnover NUMERIC(19,3);

-- Existing V18 rows predate manager-entered commercial closure. Their POS aggregates are retained as a historical backfill.
UPDATE shift_reconciliations reconciliation SET
 source_tank_id=movement.tank_id,product_id=movement.product_id,
 cash_unit_price=COALESCE((SELECT MAX(sale.unit_price) FROM fuel_sales sale WHERE sale.shift_assignment_id=reconciliation.shift_assignment_id AND sale.sale_type='CASH'),0),
 credit_unit_price=COALESCE((SELECT MAX(sale.unit_price) FROM fuel_sales sale WHERE sale.shift_assignment_id=reconciliation.shift_assignment_id AND sale.sale_type='CREDIT'),0),
 cash_amount=COALESCE((SELECT SUM(sale.total_amount) FROM fuel_sales sale WHERE sale.shift_assignment_id=reconciliation.shift_assignment_id AND sale.sale_type='CASH' AND sale.status='EFFECTIVE'),0),
 credit_amount=COALESCE((SELECT SUM(sale.total_amount) FROM fuel_sales sale WHERE sale.shift_assignment_id=reconciliation.shift_assignment_id AND sale.sale_type='CREDIT' AND sale.status='EFFECTIVE'),0)
FROM metered_stock_movements movement WHERE movement.shift_assignment_id=reconciliation.shift_assignment_id;
UPDATE shift_reconciliations SET turnover=ROUND(cash_amount+credit_amount,3);

ALTER TABLE shift_reconciliations ALTER COLUMN source_tank_id SET NOT NULL;
ALTER TABLE shift_reconciliations ALTER COLUMN product_id SET NOT NULL;
ALTER TABLE shift_reconciliations ALTER COLUMN cash_unit_price SET NOT NULL;
ALTER TABLE shift_reconciliations ALTER COLUMN credit_unit_price SET NOT NULL;
ALTER TABLE shift_reconciliations ALTER COLUMN cash_amount SET NOT NULL;
ALTER TABLE shift_reconciliations ALTER COLUMN credit_amount SET NOT NULL;
ALTER TABLE shift_reconciliations ALTER COLUMN turnover SET NOT NULL;
ALTER TABLE shift_reconciliations ADD CONSTRAINT ck_shift_reconciliation_commercial_values CHECK(cash_unit_price>=0 AND credit_unit_price>=0 AND cash_amount>=0 AND credit_amount>=0 AND turnover>=0);
ALTER TABLE shift_reconciliations ADD CONSTRAINT ck_shift_reconciliation_turnover CHECK(turnover=ROUND(cash_amount+credit_amount,3));
CREATE INDEX idx_shift_reconciliation_source_tank ON shift_reconciliations(source_tank_id);
CREATE INDEX idx_shift_reconciliation_product ON shift_reconciliations(product_id);
