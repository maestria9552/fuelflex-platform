CREATE TABLE employee_station_transfers (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    source_assignment_id UUID NOT NULL,
    destination_assignment_id UUID NOT NULL,
    transferred_by UUID NOT NULL,
    transferred_at TIMESTAMPTZ NOT NULL,
    effective_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(500),
    CONSTRAINT fk_transfer_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_transfer_employee FOREIGN KEY (employee_id) REFERENCES users(id),
    CONSTRAINT fk_transfer_source FOREIGN KEY (source_assignment_id) REFERENCES user_station_assignments(id),
    CONSTRAINT fk_transfer_destination FOREIGN KEY (destination_assignment_id) REFERENCES user_station_assignments(id),
    CONSTRAINT fk_transfer_actor FOREIGN KEY (transferred_by) REFERENCES users(id),
    CONSTRAINT fk_transfer_employee_organization FOREIGN KEY (employee_id, organization_id)
        REFERENCES users(id, organization_id),
    CONSTRAINT fk_transfer_source_scope
        FOREIGN KEY (source_assignment_id, organization_id, employee_id)
        REFERENCES user_station_assignments(id, organization_id, user_id),
    CONSTRAINT fk_transfer_destination_scope
        FOREIGN KEY (destination_assignment_id, organization_id, employee_id)
        REFERENCES user_station_assignments(id, organization_id, user_id),
    CONSTRAINT ck_transfer_distinct_assignments CHECK (source_assignment_id <> destination_assignment_id),
    CONSTRAINT uk_transfer_source_assignment UNIQUE (source_assignment_id),
    CONSTRAINT uk_transfer_destination_assignment UNIQUE (destination_assignment_id)
);

CREATE INDEX idx_transfer_org_employee_effective
    ON employee_station_transfers (organization_id, employee_id, effective_at DESC);
