CREATE SEQUENCE fuel_sale_number_seq START WITH 1 INCREMENT BY 1 NO CYCLE;

ALTER TABLE operational_days
    ADD CONSTRAINT uk_operational_day_identity_scope UNIQUE (id, organization_id, station_id);
ALTER TABLE pump_shift_assignments
    ADD CONSTRAINT uk_shift_identity_scope UNIQUE (id, operational_day_id, pump_attendant_id, fuel_meter_id);

CREATE TABLE fuel_sales (
    id UUID PRIMARY KEY,
    sale_number VARCHAR(30) NOT NULL,
    organization_id UUID NOT NULL,
    station_id UUID NOT NULL,
    operational_day_id UUID NOT NULL,
    shift_assignment_id UUID NOT NULL,
    pump_attendant_id UUID NOT NULL,
    fuel_meter_id UUID NOT NULL,
    dispensing_point_id UUID,
    tank_id UUID NOT NULL,
    product_id UUID NOT NULL,
    tariff_category_id UUID NOT NULL,
    quantity NUMERIC(19,3) NOT NULL,
    unit_price NUMERIC(19,3) NOT NULL,
    total_amount NUMERIC(19,3) NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    license_plate VARCHAR(40),
    sold_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_fuel_sale_number UNIQUE (sale_number),
    CONSTRAINT fk_fuel_sale_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_day FOREIGN KEY (operational_day_id) REFERENCES operational_days(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_shift FOREIGN KEY (shift_assignment_id) REFERENCES pump_shift_assignments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_attendant FOREIGN KEY (pump_attendant_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_meter FOREIGN KEY (fuel_meter_id) REFERENCES fuel_meters(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_point FOREIGN KEY (dispensing_point_id) REFERENCES dispensing_points(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_tank FOREIGN KEY (tank_id) REFERENCES tanks(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_tariff FOREIGN KEY (tariff_category_id) REFERENCES tariff_categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_day_scope FOREIGN KEY (operational_day_id, organization_id, station_id)
        REFERENCES operational_days(id, organization_id, station_id) ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_sale_shift_scope FOREIGN KEY (shift_assignment_id, operational_day_id, pump_attendant_id, fuel_meter_id)
        REFERENCES pump_shift_assignments(id, operational_day_id, pump_attendant_id, fuel_meter_id) ON DELETE RESTRICT,
    CONSTRAINT ck_fuel_sale_quantity CHECK (quantity > 0),
    CONSTRAINT ck_fuel_sale_unit_price CHECK (unit_price > 0),
    CONSTRAINT ck_fuel_sale_total CHECK (total_amount >= 0),
    CONSTRAINT ck_fuel_sale_vehicle_type CHECK (vehicle_type IN ('MOTORCYCLE','CAR','JEEP','MINIBUS','TRUCK')),
    CONSTRAINT ck_fuel_sale_license_plate CHECK (vehicle_type = 'MOTORCYCLE' OR license_plate IS NOT NULL)
);
CREATE INDEX idx_fuel_sale_attendant_day ON fuel_sales(pump_attendant_id, operational_day_id, sold_at DESC);
CREATE INDEX idx_fuel_sale_station_day ON fuel_sales(station_id, operational_day_id, sold_at DESC);
CREATE INDEX idx_fuel_sale_shift ON fuel_sales(shift_assignment_id);
CREATE INDEX idx_fuel_sale_tank_product ON fuel_sales(tank_id, product_id);

CREATE TABLE sale_stock_movements (
    id UUID PRIMARY KEY,
    sale_id UUID NOT NULL,
    station_id UUID NOT NULL,
    tank_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(19,3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_sale_stock_movement_sale UNIQUE (sale_id),
    CONSTRAINT fk_sale_stock_movement_sale FOREIGN KEY (sale_id) REFERENCES fuel_sales(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sale_stock_movement_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sale_stock_movement_tank FOREIGN KEY (tank_id) REFERENCES tanks(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sale_stock_movement_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT ck_sale_stock_movement_quantity CHECK (quantity > 0)
);
CREATE INDEX idx_sale_stock_movement_tank ON sale_stock_movements(tank_id);
CREATE INDEX idx_sale_stock_movement_station_product ON sale_stock_movements(station_id, product_id);

INSERT INTO permissions(id, code, name, description, module, system_permission, active, created_at, updated_at)
SELECT gen_random_uuid(), value.code, value.name, value.name, 'POS_SALE', TRUE, TRUE, now(), now()
FROM (VALUES
    ('pos-sale:view', 'Consulter les ventes POS'),
    ('pos-sale:create', 'Créer une vente POS')
) AS value(code, name)
WHERE NOT EXISTS (SELECT 1 FROM permissions permission WHERE permission.code = value.code);

INSERT INTO role_permissions(role_id, permission_id)
SELECT role.id, permission.id
FROM roles role
JOIN permissions permission ON
    (role.code = 'PUMP_ATTENDANT' AND permission.code IN ('pos-sale:view','pos-sale:create'))
    OR (role.code = 'SUPER_ADMIN' AND permission.code IN ('pos-sale:view','pos-sale:create'))
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions existing
    WHERE existing.role_id = role.id AND existing.permission_id = permission.id
);
