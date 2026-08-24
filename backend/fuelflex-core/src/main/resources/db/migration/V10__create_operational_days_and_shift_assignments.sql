CREATE SEQUENCE pump_attendant_operational_code_seq START WITH 1 INCREMENT BY 1 NO CYCLE;
ALTER TABLE users ADD COLUMN operational_code VARCHAR(20);
UPDATE users u SET operational_code = 'PMP-' || LPAD(nextval('pump_attendant_operational_code_seq')::text, 6, '0')
WHERE operational_code IS NULL AND EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.code='PUMP_ATTENDANT');
ALTER TABLE users ADD CONSTRAINT uk_users_operational_code UNIQUE (operational_code);
ALTER TABLE users ADD CONSTRAINT ck_users_operational_code_format CHECK (operational_code IS NULL OR operational_code ~ '^PMP-[0-9]{6,}$');

CREATE TABLE operational_days (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT, station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
 business_date DATE NOT NULL, status VARCHAR(20) NOT NULL, opened_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT, opened_at TIMESTAMPTZ NOT NULL,
 created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_operational_day_status CHECK(status IN ('OPEN','CLOSED')), CONSTRAINT uk_operational_day_station_date UNIQUE(station_id,business_date),
 CONSTRAINT fk_operational_day_station_org FOREIGN KEY(station_id,organization_id) REFERENCES stations(id,organization_id)
);
CREATE UNIQUE INDEX uk_operational_day_open_station ON operational_days(station_id) WHERE status='OPEN';
CREATE INDEX idx_operational_day_org_date ON operational_days(organization_id,business_date DESC); CREATE INDEX idx_operational_day_station_status ON operational_days(station_id,status);

CREATE TABLE pump_shift_assignments (
 id UUID PRIMARY KEY, operational_day_id UUID NOT NULL REFERENCES operational_days(id) ON DELETE RESTRICT, pump_attendant_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
 fuel_meter_id UUID NOT NULL REFERENCES fuel_meters(id) ON DELETE RESTRICT, opening_index NUMERIC(19,3) NOT NULL, closing_index NUMERIC(19,3), status VARCHAR(20) NOT NULL,
 opened_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT, opened_at TIMESTAMPTZ NOT NULL, closed_by UUID REFERENCES users(id) ON DELETE RESTRICT, closed_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_shift_status CHECK(status IN ('OPEN','CLOSED')), CONSTRAINT ck_shift_indexes CHECK(opening_index>=0 AND (closing_index IS NULL OR closing_index>=opening_index)),
 CONSTRAINT ck_shift_close_state CHECK((status='OPEN' AND closing_index IS NULL AND closed_by IS NULL AND closed_at IS NULL) OR (status='CLOSED' AND closing_index IS NOT NULL AND closed_by IS NOT NULL AND closed_at IS NOT NULL))
);
CREATE UNIQUE INDEX uk_shift_open_meter ON pump_shift_assignments(fuel_meter_id) WHERE status='OPEN';
CREATE UNIQUE INDEX uk_shift_open_attendant ON pump_shift_assignments(pump_attendant_id) WHERE status='OPEN';
CREATE INDEX idx_shift_day_status ON pump_shift_assignments(operational_day_id,status); CREATE INDEX idx_shift_attendant_status ON pump_shift_assignments(pump_attendant_id,status);

CREATE TABLE operational_history (
 id UUID PRIMARY KEY, resource_type VARCHAR(40) NOT NULL, resource_id UUID NOT NULL, action VARCHAR(50) NOT NULL, old_status VARCHAR(20), new_status VARCHAR(20) NOT NULL,
 performed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT, performed_at TIMESTAMPTZ NOT NULL, details VARCHAR(1000),
 CONSTRAINT ck_operational_history_action CHECK(action IN ('OPERATIONAL_DAY_OPENED','SHIFT_ASSIGNMENT_OPENED','SHIFT_ASSIGNMENT_CLOSED','OPERATIONAL_DAY_CLOSED'))
);
CREATE INDEX idx_operational_history_resource ON operational_history(resource_type,resource_id,performed_at,id);

INSERT INTO permissions(id,code,name,description,module,system_permission,active,created_at,updated_at) SELECT gen_random_uuid(),v.code,v.name,v.name,'OPERATIONS',TRUE,TRUE,now(),now() FROM (VALUES
 ('operational-day:view','Consulter les journées opérationnelles'),('operational-day:open','Ouvrir une journée opérationnelle'),('operational-day:close','Fermer une journée opérationnelle'),
 ('shift-assignment:view','Consulter les affectations pompistes'),('shift-assignment:create','Créer une affectation pompiste'),('shift-assignment:close','Fermer une affectation pompiste')
) v(code,name) WHERE NOT EXISTS(SELECT 1 FROM permissions p WHERE p.code=v.code);
INSERT INTO role_permissions(role_id,permission_id) SELECT r.id,p.id FROM roles r JOIN permissions p ON
 (r.code='MANAGER' AND p.code IN ('operational-day:view','operational-day:open','operational-day:close','shift-assignment:view','shift-assignment:create','shift-assignment:close')) OR
 (r.code='SUPERVISOR' AND p.code IN ('operational-day:view','shift-assignment:view')) OR r.code='SUPER_ADMIN'
WHERE p.module='OPERATIONS' AND NOT EXISTS(SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
