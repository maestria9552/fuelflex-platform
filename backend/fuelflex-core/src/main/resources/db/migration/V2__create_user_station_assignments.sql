ALTER TABLE users
    ADD CONSTRAINT uk_users_id_organization UNIQUE (id, organization_id);

ALTER TABLE stations
    ADD CONSTRAINT uk_stations_id_organization UNIQUE (id, organization_id);

CREATE TABLE user_station_assignments (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    station_id UUID NOT NULL,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    ended_by UUID,
    ended_at TIMESTAMPTZ,
    reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_assignment_id_organization_user UNIQUE (id, organization_id, user_id),
    CONSTRAINT fk_assignment_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_assignment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_assignment_station FOREIGN KEY (station_id) REFERENCES stations(id),
    CONSTRAINT fk_assignment_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_assignment_ended_by FOREIGN KEY (ended_by) REFERENCES users(id),
    CONSTRAINT fk_assignment_user_organization FOREIGN KEY (user_id, organization_id)
        REFERENCES users(id, organization_id),
    CONSTRAINT fk_assignment_station_organization FOREIGN KEY (station_id, organization_id)
        REFERENCES stations(id, organization_id),
    CONSTRAINT ck_assignment_dates CHECK (valid_until IS NULL OR valid_until >= valid_from),
    CONSTRAINT ck_assignment_end_state CHECK (
        (valid_until IS NULL AND ended_at IS NULL AND ended_by IS NULL)
        OR (valid_until IS NOT NULL AND ended_at IS NOT NULL AND ended_by IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uk_assignment_active_user_station
    ON user_station_assignments (organization_id, user_id, station_id)
    WHERE valid_until IS NULL;

CREATE INDEX idx_assignment_org_user_end
    ON user_station_assignments (organization_id, user_id, valid_until);
CREATE INDEX idx_assignment_org_station_end
    ON user_station_assignments (organization_id, station_id, valid_until);
CREATE INDEX idx_assignment_org_valid_from
    ON user_station_assignments (organization_id, valid_from DESC);
CREATE INDEX idx_assignment_created_by ON user_station_assignments (created_by);
CREATE INDEX idx_assignment_ended_by ON user_station_assignments (ended_by);
