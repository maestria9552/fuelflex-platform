CREATE TABLE internal_consumptions (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
 station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT, operational_day_id UUID NOT NULL REFERENCES operational_days(id) ON DELETE RESTRICT,
 shift_assignment_id UUID NOT NULL REFERENCES pump_shift_assignments(id) ON DELETE RESTRICT, pump_attendant_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
 fuel_meter_id UUID NOT NULL REFERENCES fuel_meters(id) ON DELETE RESTRICT, tank_id UUID NOT NULL REFERENCES tanks(id) ON DELETE RESTRICT,
 product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT, tariff_category_id UUID NOT NULL REFERENCES tariff_categories(id) ON DELETE RESTRICT,
 quantity NUMERIC(19,3) NOT NULL, unit_price NUMERIC(19,3) NOT NULL, total_amount NUMERIC(19,3) NOT NULL,
 usage_beneficiary VARCHAR(180) NOT NULL, observation VARCHAR(1000), recorded_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
 recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT ck_internal_consumption_quantity CHECK(quantity>0), CONSTRAINT ck_internal_consumption_price CHECK(unit_price>0),
 CONSTRAINT ck_internal_consumption_amount CHECK(total_amount=ROUND(quantity*unit_price,3))
);
CREATE INDEX idx_internal_consumption_shift ON internal_consumptions(shift_assignment_id,recorded_at);
CREATE INDEX idx_internal_consumption_day ON internal_consumptions(operational_day_id,recorded_at);

CREATE TABLE internal_consumption_stock_movements (
 id UUID PRIMARY KEY, internal_consumption_id UUID NOT NULL REFERENCES internal_consumptions(id) ON DELETE RESTRICT,
 tank_id UUID NOT NULL REFERENCES tanks(id) ON DELETE RESTRICT, product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
 quantity NUMERIC(19,3) NOT NULL, movement_type VARCHAR(40) NOT NULL DEFAULT 'INTERNAL_CONSUMPTION', created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT uk_internal_consumption_stock UNIQUE(internal_consumption_id), CONSTRAINT ck_internal_consumption_stock_quantity CHECK(quantity>0),
 CONSTRAINT ck_internal_consumption_stock_type CHECK(movement_type='INTERNAL_CONSUMPTION')
);
CREATE INDEX idx_internal_consumption_stock_tank ON internal_consumption_stock_movements(tank_id);

ALTER TABLE shift_reconciliations ADD COLUMN internal_consumption_volume NUMERIC(19,3) NOT NULL DEFAULT 0;
ALTER TABLE shift_reconciliations ADD COLUMN internal_consumption_amount NUMERIC(19,3) NOT NULL DEFAULT 0;
ALTER TABLE operational_day_summaries ADD COLUMN internal_consumption_volume NUMERIC(19,3) NOT NULL DEFAULT 0;
ALTER TABLE operational_day_summaries ADD COLUMN internal_consumption_amount NUMERIC(19,3) NOT NULL DEFAULT 0;

INSERT INTO permissions(id,code,name,description,module,active,system_permission,created_at,updated_at)
SELECT gen_random_uuid(),v.code,v.name,v.name,'OPERATIONS',TRUE,TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP FROM (VALUES
 ('internal-consumption:view','Consulter les consommations internes'),('internal-consumption:create','Créer une consommation interne')) v(code,name)
WHERE NOT EXISTS(SELECT 1 FROM permissions p WHERE lower(p.code)=lower(v.code));
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r JOIN permissions p ON p.code='internal-consumption:view' WHERE r.code IN('MANAGER','SUPERVISOR','SUPER_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r JOIN permissions p ON p.code='internal-consumption:create' WHERE r.code IN('MANAGER','SUPER_ADMIN') ON CONFLICT DO NOTHING;
