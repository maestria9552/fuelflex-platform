ALTER TABLE notifications
    ADD COLUMN activity_count INTEGER,
    ADD COLUMN last_activity_type VARCHAR(100),
    ADD COLUMN updated_at TIMESTAMPTZ;

UPDATE notifications
   SET updated_at = created_at
 WHERE updated_at IS NULL;

ALTER TABLE notifications
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE notifications
    ADD CONSTRAINT ck_notification_activity_count
    CHECK (activity_count IS NULL OR activity_count > 0);

CREATE UNIQUE INDEX uq_notification_operational_day_activity
    ON notifications(recipient_id, organization_id, station_id, resource_id)
    WHERE event_type = 'OPERATIONAL_DAY_ACTIVITY'
      AND resource_type = 'OPERATIONAL_DAY'
      AND category = 'INFORMATION';
