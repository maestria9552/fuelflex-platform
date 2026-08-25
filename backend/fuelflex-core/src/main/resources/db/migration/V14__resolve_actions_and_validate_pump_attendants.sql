ALTER TABLE notifications
    ADD COLUMN resolved_at TIMESTAMPTZ,
    ADD COLUMN resolved_by_id UUID;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notification_resolved_by
        FOREIGN KEY (resolved_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    ADD CONSTRAINT ck_notification_resolution_state
        CHECK (
            (resolved_at IS NULL AND resolved_by_id IS NULL)
            OR (resolved_at IS NOT NULL AND resolved_by_id IS NOT NULL
                AND requires_action = FALSE)
        );

CREATE INDEX idx_notification_pending_action
    ON notifications(recipient_id, organization_id, requires_action, resolved_at);

CREATE INDEX idx_notification_resource_pending_action
    ON notifications(organization_id, resource_type, resource_id)
    WHERE requires_action = TRUE AND resolved_at IS NULL;

-- Resolve only historical decisions that are already terminal in their source
-- workflow. Reading a notification remains independent from this business state.
UPDATE notifications notification
   SET requires_action = FALSE,
       resolved_at = COALESCE(reception.supervisor_reviewed_at,
                              reception.updated_at),
       resolved_by_id = COALESCE(reception.supervisor_reviewed_by,
                                 reception.created_by)
  FROM receptions reception
 WHERE notification.organization_id = reception.organization_id
   AND notification.resource_type = 'RECEPTION'
   AND notification.resource_id = reception.id
   AND notification.event_type = 'RECEPTION_OVERAGE_SUBMITTED'
   AND notification.requires_action = TRUE
   AND notification.resolved_at IS NULL
   AND reception.status <> 'PENDING_SUPERVISOR_APPROVAL';

UPDATE notifications notification
   SET requires_action = FALSE,
       resolved_at = COALESCE(reception.supervisor_reviewed_at,
                              reception.updated_at),
       resolved_by_id = COALESCE(reception.supervisor_reviewed_by,
                                 reception.created_by)
  FROM receptions reception
 WHERE notification.organization_id = reception.organization_id
   AND notification.resource_type = 'RECEPTION'
   AND notification.resource_id = reception.id
   AND notification.event_type = 'RECEPTION_RETURNED_FOR_CORRECTION'
   AND notification.requires_action = TRUE
   AND notification.resolved_at IS NULL
   AND reception.status <> 'RETURNED_FOR_CORRECTION';

UPDATE notifications notification
   SET requires_action = FALSE,
       resolved_at = COALESCE(purchase_order.supervisor_reviewed_at,
                              purchase_order.updated_at),
       resolved_by_id = COALESCE(purchase_order.supervisor_reviewed_by,
                                 purchase_order.created_by)
  FROM purchase_orders purchase_order
 WHERE notification.organization_id = purchase_order.organization_id
   AND notification.resource_type = 'PURCHASE_ORDER'
   AND notification.resource_id = purchase_order.id
   AND notification.event_type = 'ORDER_SUBMITTED'
   AND notification.requires_action = TRUE
   AND notification.resolved_at IS NULL
   AND purchase_order.status <> 'PENDING_SUPERVISOR_APPROVAL';

ALTER TABLE users
    ADD COLUMN pump_attendant_validation_status VARCHAR(40),
    ADD COLUMN prepared_by_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_prepared_by
        FOREIGN KEY (prepared_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    ADD CONSTRAINT ck_users_pump_attendant_validation_status
        CHECK (pump_attendant_validation_status IS NULL OR
               pump_attendant_validation_status IN (
                   'PREPARATION',
                   'PENDING_SUPERVISOR_APPROVAL',
                   'RETURNED_FOR_CORRECTION',
                   'VALIDATED',
                   'REJECTED',
                   'CANCELLED'
               ));

UPDATE users user_account
   SET pump_attendant_validation_status = 'VALIDATED'
 WHERE pump_attendant_validation_status IS NULL
   AND EXISTS (
       SELECT 1
         FROM user_roles user_role
         JOIN roles role ON role.id = user_role.role_id
        WHERE user_role.user_id = user_account.id
          AND role.code = 'PUMP_ATTENDANT'
   );

CREATE INDEX idx_users_prepared_pump_attendant
    ON users(organization_id, prepared_by_id, pump_attendant_validation_status)
    WHERE prepared_by_id IS NOT NULL;

CREATE SEQUENCE pump_attendant_validation_request_number_seq
    START WITH 1 INCREMENT BY 1 NO CYCLE;

CREATE TABLE pump_attendant_validation_requests (
    id UUID PRIMARY KEY,
    request_number VARCHAR(30) NOT NULL,
    organization_id UUID NOT NULL,
    station_id UUID NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_by_id UUID NOT NULL,
    submitted_at TIMESTAMPTZ,
    reviewed_by_id UUID,
    reviewed_at TIMESTAMPTZ,
    review_comment VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_pump_attendant_validation_request_number
        UNIQUE (request_number),
    CONSTRAINT fk_pump_validation_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pump_validation_station
        FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pump_validation_station_org
        FOREIGN KEY (station_id, organization_id)
        REFERENCES stations(id, organization_id),
    CONSTRAINT fk_pump_validation_created_by
        FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pump_validation_reviewed_by
        FOREIGN KEY (reviewed_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT ck_pump_validation_request_status CHECK (status IN (
        'DRAFT',
        'PENDING_SUPERVISOR_APPROVAL',
        'RETURNED_FOR_CORRECTION',
        'VALIDATED',
        'REJECTED',
        'CANCELLED'
    )),
    CONSTRAINT ck_pump_validation_review_state CHECK (
        (reviewed_at IS NULL AND reviewed_by_id IS NULL)
        OR (reviewed_at IS NOT NULL AND reviewed_by_id IS NOT NULL)
    )
);

CREATE INDEX idx_pump_attendant_validation_manager
    ON pump_attendant_validation_requests(
        organization_id, created_by_id, created_at DESC
    );

CREATE INDEX idx_pump_attendant_validation_supervisor
    ON pump_attendant_validation_requests(
        organization_id, station_id, status, created_at DESC
    );

CREATE TABLE pump_attendant_validation_items (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    pump_attendant_id UUID NOT NULL,
    first_name_snapshot VARCHAR(100) NOT NULL,
    last_name_snapshot VARCHAR(100) NOT NULL,
    email_snapshot VARCHAR(180) NOT NULL,
    phone_number_snapshot VARCHAR(30) NOT NULL,
    operational_code_snapshot VARCHAR(20) NOT NULL,
    CONSTRAINT fk_pump_validation_item_request
        FOREIGN KEY (request_id)
        REFERENCES pump_attendant_validation_requests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pump_validation_item_employee
        FOREIGN KEY (pump_attendant_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uk_pump_validation_item_request_employee
        UNIQUE (request_id, pump_attendant_id),
    CONSTRAINT uk_pump_validation_item_employee
        UNIQUE (pump_attendant_id)
);

CREATE INDEX idx_pump_validation_item_request
    ON pump_attendant_validation_items(request_id);

CREATE TABLE pump_attendant_validation_history (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    action VARCHAR(40) NOT NULL,
    old_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    performed_by_id UUID NOT NULL,
    performed_at TIMESTAMPTZ NOT NULL,
    comment VARCHAR(1000),
    CONSTRAINT fk_pump_validation_history_request
        FOREIGN KEY (request_id)
        REFERENCES pump_attendant_validation_requests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pump_validation_history_actor
        FOREIGN KEY (performed_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT ck_pump_validation_history_action CHECK (action IN (
        'CREATED',
        'SUBMITTED',
        'RESUBMITTED',
        'RETURNED_FOR_CORRECTION',
        'VALIDATED',
        'REJECTED',
        'CANCELLED'
    )),
    CONSTRAINT ck_pump_validation_history_status CHECK (
        old_status IS NULL OR old_status IN (
            'DRAFT',
            'PENDING_SUPERVISOR_APPROVAL',
            'RETURNED_FOR_CORRECTION',
            'VALIDATED',
            'REJECTED',
            'CANCELLED'
        )
    ),
    CONSTRAINT ck_pump_validation_history_new_status CHECK (new_status IN (
        'DRAFT',
        'PENDING_SUPERVISOR_APPROVAL',
        'RETURNED_FOR_CORRECTION',
        'VALIDATED',
        'REJECTED',
        'CANCELLED'
    ))
);

CREATE INDEX idx_pump_validation_history_timeline
    ON pump_attendant_validation_history(request_id, performed_at, id);

INSERT INTO permissions(
    id, code, name, description, module,
    system_permission, active, created_at, updated_at
)
SELECT gen_random_uuid(), value.code, value.name, value.description,
       'EMPLOYEE_VALIDATION', TRUE, TRUE, NOW(), NOW()
FROM (VALUES
    ('pump-attendant:prepare',
     'Préparer les pompistes',
     'Créer et corriger les pompistes avant validation'),
    ('pump-attendant-validation:view',
     'Consulter les validations pompistes',
     'Consulter les documents de validation des pompistes'),
    ('pump-attendant-validation:create',
     'Créer une validation pompistes',
     'Regrouper les pompistes dans un document de validation'),
    ('pump-attendant-validation:submit',
     'Soumettre une validation pompistes',
     'Soumettre et resoumettre un document de validation'),
    ('pump-attendant-validation:review',
     'Décider une validation pompistes',
     'Valider, retourner ou rejeter un document de validation')
) AS value(code, name, description)
WHERE NOT EXISTS (
    SELECT 1 FROM permissions permission WHERE permission.code = value.code
);

INSERT INTO role_permissions(role_id, permission_id)
SELECT role.id, permission.id
FROM roles role
JOIN permissions permission ON (
    role.code = 'MANAGER'
    AND permission.code IN (
        'pump-attendant:prepare',
        'pump-attendant-validation:view',
        'pump-attendant-validation:create',
        'pump-attendant-validation:submit'
    )
) OR (
    role.code = 'SUPERVISOR'
    AND permission.code IN (
        'pump-attendant-validation:view',
        'pump-attendant-validation:review'
    )
) OR (
    role.code = 'SUPER_ADMIN'
    AND permission.module = 'EMPLOYEE_VALIDATION'
)
WHERE NOT EXISTS (
    SELECT 1
      FROM role_permissions existing
     WHERE existing.role_id = role.id
       AND existing.permission_id = permission.id
);
