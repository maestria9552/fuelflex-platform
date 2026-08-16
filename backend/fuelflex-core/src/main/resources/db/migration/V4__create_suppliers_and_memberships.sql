CREATE TABLE suppliers (
    id UUID PRIMARY KEY,
    legal_name VARCHAR(180) NOT NULL,
    display_name VARCHAR(180) NOT NULL,
    email VARCHAR(180),
    phone VARCHAR(30),
    address VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_suppliers_active ON suppliers(active);

CREATE TABLE organization_suppliers (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    internal_code VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    partnership_started_at TIMESTAMPTZ NOT NULL,
    partnership_ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_organization_suppliers_pair UNIQUE (organization_id, supplier_id),
    CONSTRAINT ck_organization_supplier_dates CHECK (partnership_ended_at IS NULL OR partnership_ended_at >= partnership_started_at)
);

CREATE UNIQUE INDEX uk_organization_suppliers_code
    ON organization_suppliers(organization_id, internal_code)
    WHERE internal_code IS NOT NULL;
CREATE INDEX idx_organization_suppliers_org_active ON organization_suppliers(organization_id, active);

CREATE TABLE supplier_user_memberships (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    user_id UUID NOT NULL REFERENCES users(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_supplier_membership_end CHECK ((active AND ended_at IS NULL) OR (NOT active AND ended_at IS NOT NULL))
);

CREATE UNIQUE INDEX uk_supplier_membership_active
    ON supplier_user_memberships(supplier_id, user_id)
    WHERE active = TRUE;
CREATE INDEX idx_supplier_membership_supplier_active ON supplier_user_memberships(supplier_id, active);
CREATE INDEX idx_supplier_membership_user_active ON supplier_user_memberships(user_id, active);

INSERT INTO permissions (id, code, name, description, module, system_permission, active, created_at, updated_at)
SELECT gen_random_uuid(), 'supplier:manage_users', 'Gérer les utilisateurs fournisseur', 'Gérer les rattachements de comptes fournisseur', 'SUPPLIER', TRUE, TRUE, now(), now() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'supplier:manage_users');
INSERT INTO role_permissions (role_id, permission_id) SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'SUPERVISOR' AND p.code = 'supplier:manage_users' AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
