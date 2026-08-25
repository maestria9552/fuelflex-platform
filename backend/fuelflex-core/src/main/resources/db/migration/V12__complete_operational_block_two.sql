ALTER TABLE fuel_sales DROP CONSTRAINT ck_fuel_sale_total;
ALTER TABLE fuel_sales ADD CONSTRAINT ck_fuel_sale_total_positive CHECK (total_amount > 0);
ALTER TABLE fuel_sales ADD CONSTRAINT ck_fuel_sale_total_calculation CHECK (total_amount = ROUND(quantity * unit_price, 3));

CREATE TABLE credit_customers (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
 code VARCHAR(50) NOT NULL, name VARCHAR(180) NOT NULL, phone VARCHAR(30), email VARCHAR(180), active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT uk_credit_customer_org_code UNIQUE(organization_id,code), CONSTRAINT uk_credit_customer_org_name UNIQUE(organization_id,name),
 CONSTRAINT uk_credit_customer_id_org UNIQUE(id,organization_id)
);
CREATE INDEX idx_credit_customer_org_active ON credit_customers(organization_id,active);

ALTER TABLE fuel_sales ADD COLUMN sale_type VARCHAR(20) NOT NULL DEFAULT 'CASH';
ALTER TABLE fuel_sales ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'EFFECTIVE';
ALTER TABLE fuel_sales ADD COLUMN credit_customer_id UUID;
ALTER TABLE fuel_sales ADD COLUMN reversed_by UUID REFERENCES users(id) ON DELETE RESTRICT;
ALTER TABLE fuel_sales ADD COLUMN reversed_at TIMESTAMPTZ;
ALTER TABLE fuel_sales ADD COLUMN reversal_reason VARCHAR(1000);
ALTER TABLE fuel_sales ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE fuel_sales ADD CONSTRAINT fk_fuel_sale_credit_customer FOREIGN KEY(credit_customer_id) REFERENCES credit_customers(id) ON DELETE RESTRICT;
ALTER TABLE fuel_sales ADD CONSTRAINT ck_fuel_sale_type CHECK(sale_type IN ('CASH','CREDIT'));
ALTER TABLE fuel_sales ADD CONSTRAINT ck_fuel_sale_status CHECK(status IN ('EFFECTIVE','REVERSED'));
ALTER TABLE fuel_sales ADD CONSTRAINT ck_fuel_sale_credit_customer CHECK((sale_type='CASH' AND credit_customer_id IS NULL) OR (sale_type='CREDIT' AND credit_customer_id IS NOT NULL));
ALTER TABLE fuel_sales ADD CONSTRAINT ck_fuel_sale_reversal_state CHECK((status='EFFECTIVE' AND reversed_by IS NULL AND reversed_at IS NULL AND reversal_reason IS NULL) OR (status='REVERSED' AND reversed_by IS NOT NULL AND reversed_at IS NOT NULL AND reversal_reason IS NOT NULL));
CREATE INDEX idx_fuel_sale_type_status ON fuel_sales(operational_day_id,sale_type,status);
CREATE INDEX idx_fuel_sale_credit_customer ON fuel_sales(credit_customer_id) WHERE credit_customer_id IS NOT NULL;

ALTER TABLE sale_stock_movements ADD COLUMN movement_type VARCHAR(20) NOT NULL DEFAULT 'OUTBOUND';
ALTER TABLE sale_stock_movements DROP CONSTRAINT uk_sale_stock_movement_sale;
ALTER TABLE sale_stock_movements ADD CONSTRAINT ck_sale_stock_movement_type CHECK(movement_type IN ('OUTBOUND','REVERSAL'));
ALTER TABLE sale_stock_movements ADD CONSTRAINT uk_sale_stock_movement_sale_type UNIQUE(sale_id,movement_type);

CREATE TABLE daily_expenses (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT, station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
 operational_day_id UUID NOT NULL REFERENCES operational_days(id) ON DELETE RESTRICT, label VARCHAR(180) NOT NULL, amount NUMERIC(19,3) NOT NULL,
 reference VARCHAR(100), comment VARCHAR(1000), created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT, created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT fk_daily_expense_day_scope FOREIGN KEY(operational_day_id,organization_id,station_id) REFERENCES operational_days(id,organization_id,station_id), CONSTRAINT ck_daily_expense_amount CHECK(amount>0)
);
CREATE INDEX idx_daily_expense_day ON daily_expenses(operational_day_id,created_at);

CREATE TABLE tank_gauge_readings (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT, station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
 operational_day_id UUID NOT NULL REFERENCES operational_days(id) ON DELETE RESTRICT, tank_id UUID NOT NULL REFERENCES tanks(id) ON DELETE RESTRICT,
 theoretical_stock NUMERIC(19,3) NOT NULL, physical_stock NUMERIC(19,3) NOT NULL, stock_variance NUMERIC(19,3) NOT NULL,
 comment VARCHAR(1000), recorded_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT, recorded_at TIMESTAMPTZ NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_tank_gauge_day_tank UNIQUE(operational_day_id,tank_id), CONSTRAINT fk_tank_gauge_day_scope FOREIGN KEY(operational_day_id,organization_id,station_id) REFERENCES operational_days(id,organization_id,station_id),
 CONSTRAINT ck_tank_gauge_values CHECK(theoretical_stock>=0 AND physical_stock>=0), CONSTRAINT ck_tank_gauge_variance CHECK(stock_variance=ROUND(physical_stock-theoretical_stock,3))
);
CREATE INDEX idx_tank_gauge_day ON tank_gauge_readings(operational_day_id,tank_id);

CREATE TABLE shift_reconciliations (
 id UUID PRIMARY KEY, shift_assignment_id UUID NOT NULL REFERENCES pump_shift_assignments(id) ON DELETE RESTRICT,
 opening_index NUMERIC(19,3) NOT NULL, closing_index NUMERIC(19,3) NOT NULL, metered_volume NUMERIC(19,3) NOT NULL,
 cash_volume NUMERIC(19,3) NOT NULL, credit_volume NUMERIC(19,3) NOT NULL, total_sold_volume NUMERIC(19,3) NOT NULL, volume_variance NUMERIC(19,3) NOT NULL, calculated_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_shift_reconciliation_assignment UNIQUE(shift_assignment_id), CONSTRAINT ck_shift_reconciliation_indexes CHECK(closing_index>=opening_index),
 CONSTRAINT ck_shift_reconciliation_metered CHECK(metered_volume=ROUND(closing_index-opening_index,3)), CONSTRAINT ck_shift_reconciliation_sold CHECK(total_sold_volume=ROUND(cash_volume+credit_volume,3)),
 CONSTRAINT ck_shift_reconciliation_variance CHECK(volume_variance=ROUND(metered_volume-total_sold_volume,3))
);

CREATE TABLE operational_day_summaries (
 id UUID PRIMARY KEY, operational_day_id UUID NOT NULL REFERENCES operational_days(id) ON DELETE RESTRICT,
 cash_volume NUMERIC(19,3) NOT NULL, cash_amount NUMERIC(19,3) NOT NULL, credit_volume NUMERIC(19,3) NOT NULL, credit_amount NUMERIC(19,3) NOT NULL,
 metered_volume NUMERIC(19,3) NOT NULL, sold_volume NUMERIC(19,3) NOT NULL, volume_variance NUMERIC(19,3) NOT NULL, expense_amount NUMERIC(19,3) NOT NULL,
 theoretical_stock NUMERIC(19,3) NOT NULL, physical_stock NUMERIC(19,3) NOT NULL, stock_variance NUMERIC(19,3) NOT NULL,
 closed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT, closed_at TIMESTAMPTZ NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_operational_day_summary_day UNIQUE(operational_day_id), CONSTRAINT ck_day_summary_sales CHECK(sold_volume=ROUND(cash_volume+credit_volume,3)),
 CONSTRAINT ck_day_summary_volume_variance CHECK(volume_variance=ROUND(metered_volume-sold_volume,3)), CONSTRAINT ck_day_summary_stock_variance CHECK(stock_variance=ROUND(physical_stock-theoretical_stock,3))
);

INSERT INTO permissions(id,code,name,description,module,system_permission,active,created_at,updated_at)
SELECT gen_random_uuid(),v.code,v.name,v.name,'OPERATIONS',TRUE,TRUE,now(),now() FROM (VALUES
 ('pos-credit-sale:create','Créer une vente POS à crédit'),('pos-sale:reverse','Annuler une vente POS'),('credit-customer:view','Consulter les clients crédit'),('credit-customer:manage','Gérer les clients crédit'),
 ('daily-expense:view','Consulter les dépenses'),('daily-expense:create','Créer une dépense'),('tank-gauge:view','Consulter les jauges'),('tank-gauge:create','Créer une jauge'),('reconciliation:view','Consulter les rapprochements'),('rjv:view','Consulter le RJV')
) v(code,name) WHERE NOT EXISTS(SELECT 1 FROM permissions p WHERE p.code=v.code);
INSERT INTO role_permissions(role_id,permission_id) SELECT r.id,p.id FROM roles r JOIN permissions p ON
 (r.code='PUMP_ATTENDANT' AND p.code='pos-credit-sale:create') OR
 (r.code='MANAGER' AND p.code IN('pos-sale:reverse','credit-customer:view','credit-customer:manage','daily-expense:view','daily-expense:create','tank-gauge:view','tank-gauge:create','reconciliation:view','rjv:view')) OR
 (r.code='SUPERVISOR' AND p.code IN('daily-expense:view','tank-gauge:view','reconciliation:view','rjv:view')) OR r.code='SUPER_ADMIN'
WHERE p.module='OPERATIONS' AND NOT EXISTS(SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
