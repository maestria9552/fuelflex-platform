CREATE SEQUENCE purchase_order_number_seq START WITH 1 INCREMENT BY 1 NO CYCLE;

CREATE TABLE purchase_orders (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
 station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
 organization_supplier_id UUID REFERENCES organization_suppliers(id) ON DELETE RESTRICT,
 order_number VARCHAR(30) NOT NULL UNIQUE, status VARCHAR(50) NOT NULL,
 created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT, created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, submitted_at TIMESTAMPTZ,
 supervisor_reviewed_by UUID REFERENCES users(id) ON DELETE RESTRICT, supervisor_reviewed_at TIMESTAMPTZ,
 supplier_reviewed_by UUID REFERENCES users(id) ON DELETE RESTRICT, supplier_reviewed_at TIMESTAMPTZ,
 version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_purchase_order_status CHECK (status IN ('DRAFT','PENDING_SUPERVISOR_APPROVAL','SUPERVISOR_REJECTED','PENDING_SUPPLIER_APPROVAL','SUPPLIER_REJECTED','AWAITING_RECEPTION'))
);
CREATE INDEX idx_purchase_orders_org_status ON purchase_orders(organization_id,status);
CREATE INDEX idx_purchase_orders_org_station ON purchase_orders(organization_id,station_id);
CREATE INDEX idx_purchase_orders_org_supplier ON purchase_orders(organization_id,organization_supplier_id);
CREATE INDEX idx_purchase_orders_created_by ON purchase_orders(created_by);

CREATE TABLE purchase_order_items (
 id UUID PRIMARY KEY, purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE RESTRICT,
 product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT, quantity NUMERIC(19,3) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT ck_purchase_order_item_quantity CHECK (quantity > 0),
 CONSTRAINT uk_purchase_order_item_product UNIQUE(purchase_order_id,product_id)
);
CREATE INDEX idx_purchase_order_items_order ON purchase_order_items(purchase_order_id);

CREATE TABLE purchase_order_history (
 id UUID PRIMARY KEY, purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE RESTRICT,
 from_status VARCHAR(50), to_status VARCHAR(50) NOT NULL, action VARCHAR(50) NOT NULL,
 performed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT, performed_at TIMESTAMPTZ NOT NULL,
 comment VARCHAR(1000),
 CONSTRAINT ck_purchase_order_history_to_status CHECK (to_status IN ('DRAFT','PENDING_SUPERVISOR_APPROVAL','SUPERVISOR_REJECTED','PENDING_SUPPLIER_APPROVAL','SUPPLIER_REJECTED','AWAITING_RECEPTION')),
 CONSTRAINT ck_purchase_order_history_from_status CHECK (from_status IS NULL OR from_status IN ('DRAFT','PENDING_SUPERVISOR_APPROVAL','SUPERVISOR_REJECTED','PENDING_SUPPLIER_APPROVAL','SUPPLIER_REJECTED','AWAITING_RECEPTION')),
 CONSTRAINT ck_purchase_order_history_action CHECK (action IN ('ORDER_CREATED','ORDER_UPDATED','ORDER_SUBMITTED','SUPERVISOR_APPROVED','SUPERVISOR_REJECTED','SUPPLIER_APPROVED','SUPPLIER_REJECTED'))
);
CREATE INDEX idx_purchase_order_history_timeline ON purchase_order_history(purchase_order_id,performed_at,id);

INSERT INTO permissions(id,code,name,description,module,system_permission,active,created_at,updated_at)
SELECT gen_random_uuid(),v.code,v.name,v.description,'ORDER',TRUE,TRUE,now(),now() FROM (VALUES
 ('order:view','Consulter les commandes','Consulter les commandes autorisées'),('order:create','Créer une commande','Créer un brouillon de commande'),
 ('order:update','Modifier une commande','Modifier un brouillon de commande'),('order:submit','Soumettre une commande','Soumettre une commande'),
 ('order:supervisor_approve','Valider une commande','Valider une commande comme superviseur'),('order:supervisor_reject','Refuser une commande','Refuser une commande comme superviseur'),
 ('order:supplier_approve','Accepter une commande fournisseur','Accepter une commande comme fournisseur'),('order:supplier_reject','Refuser une commande fournisseur','Refuser une commande comme fournisseur')
) AS v(code,name,description) WHERE NOT EXISTS(SELECT 1 FROM permissions p WHERE p.code=v.code);
INSERT INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r JOIN permissions p ON
 (r.code='MANAGER' AND p.code IN ('order:view','order:create','order:update','order:submit')) OR
 (r.code='SUPERVISOR' AND p.code IN ('order:view','order:supervisor_approve','order:supervisor_reject')) OR
 (r.code='SUPPLIER_USER' AND p.code IN ('order:view','order:supplier_approve','order:supplier_reject')) OR r.code='SUPER_ADMIN'
WHERE NOT EXISTS(SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
