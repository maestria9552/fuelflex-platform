CREATE TABLE tank_returns (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
 operational_day_id UUID NOT NULL REFERENCES operational_days(id) ON DELETE RESTRICT,
 shift_assignment_id UUID NOT NULL REFERENCES pump_shift_assignments(id) ON DELETE RESTRICT,
 tank_id UUID NOT NULL REFERENCES tanks(id) ON DELETE RESTRICT, quantity NUMERIC(19,3) NOT NULL,
 reason VARCHAR(1000), occurred_at TIMESTAMPTZ NOT NULL, created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
 created_at TIMESTAMPTZ NOT NULL, CONSTRAINT ck_tank_return_quantity CHECK (quantity > 0)
);
CREATE INDEX idx_tank_return_day ON tank_returns(operational_day_id,occurred_at);
CREATE INDEX idx_tank_return_shift ON tank_returns(shift_assignment_id,occurred_at);
CREATE INDEX idx_tank_return_org ON tank_returns(organization_id);

CREATE TABLE tank_return_stock_movements (
 id UUID PRIMARY KEY, tank_return_id UUID NOT NULL REFERENCES tank_returns(id) ON DELETE RESTRICT,
 station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT, tank_id UUID NOT NULL REFERENCES tanks(id) ON DELETE RESTRICT,
 product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT, quantity NUMERIC(19,3) NOT NULL,
 movement_type VARCHAR(30) NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_tank_return_movement_return UNIQUE(tank_return_id), CONSTRAINT ck_tank_return_movement_quantity CHECK(quantity>0),
 CONSTRAINT ck_tank_return_movement_type CHECK(movement_type='TANK_RETURN')
);
CREATE INDEX idx_tank_return_movement_tank ON tank_return_stock_movements(tank_id,created_at);

ALTER TABLE shift_reconciliations ADD COLUMN tank_return_volume NUMERIC(19,3) NOT NULL DEFAULT 0;
ALTER TABLE shift_reconciliations ADD COLUMN accounted_volume NUMERIC(19,3) NOT NULL DEFAULT 0;
UPDATE shift_reconciliations SET accounted_volume=total_sold_volume;
ALTER TABLE shift_reconciliations DROP CONSTRAINT ck_shift_reconciliation_variance;
ALTER TABLE shift_reconciliations ADD CONSTRAINT ck_shift_reconciliation_accounted CHECK(accounted_volume=ROUND(total_sold_volume+tank_return_volume,3));
ALTER TABLE shift_reconciliations ADD CONSTRAINT ck_shift_reconciliation_variance CHECK(volume_variance=ROUND(metered_volume-accounted_volume,3));

ALTER TABLE operational_day_summaries ADD COLUMN tank_return_volume NUMERIC(19,3) NOT NULL DEFAULT 0;
ALTER TABLE operational_day_summaries ADD COLUMN accounted_volume NUMERIC(19,3) NOT NULL DEFAULT 0;
ALTER TABLE operational_day_summaries ADD COLUMN expected_cash NUMERIC(19,3) NOT NULL DEFAULT 0;
ALTER TABLE operational_day_summaries ADD COLUMN expected_net_cash NUMERIC(19,3) NOT NULL DEFAULT 0;
UPDATE operational_day_summaries SET accounted_volume=sold_volume,expected_cash=cash_amount,expected_net_cash=cash_amount-expense_amount;
ALTER TABLE operational_day_summaries DROP CONSTRAINT ck_day_summary_volume_variance;
ALTER TABLE operational_day_summaries ADD CONSTRAINT ck_day_summary_accounted CHECK(accounted_volume=ROUND(sold_volume+tank_return_volume,3));
ALTER TABLE operational_day_summaries ADD CONSTRAINT ck_day_summary_volume_variance CHECK(volume_variance=ROUND(metered_volume-accounted_volume,3));
ALTER TABLE operational_day_summaries ADD CONSTRAINT ck_day_summary_expected_cash CHECK(expected_cash=cash_amount);
ALTER TABLE operational_day_summaries ADD CONSTRAINT ck_day_summary_expected_net_cash CHECK(expected_net_cash=ROUND(expected_cash-expense_amount,3));

INSERT INTO permissions(id,code,name,description,module,system_permission,active,created_at,updated_at)
SELECT gen_random_uuid(),v.code,v.name,v.name,'OPERATIONS',TRUE,TRUE,now(),now() FROM (VALUES
 ('tank-return:view','Consulter les remises en cuve'),('tank-return:create','Créer une remise en cuve')
) v(code,name) WHERE NOT EXISTS(SELECT 1 FROM permissions p WHERE p.code=v.code);
INSERT INTO role_permissions(role_id,permission_id) SELECT r.id,p.id FROM roles r JOIN permissions p ON
 (r.code='MANAGER' AND p.code IN('tank-return:view','tank-return:create')) OR
 (r.code='SUPERVISOR' AND p.code='tank-return:view') OR r.code='SUPER_ADMIN'
WHERE p.module='OPERATIONS' AND p.code IN('tank-return:view','tank-return:create')
AND NOT EXISTS(SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
