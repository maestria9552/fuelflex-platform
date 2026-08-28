ALTER TABLE users
    ADD COLUMN pos_credential_hash VARCHAR(100);

ALTER TABLE users
    ADD CONSTRAINT uk_users_pos_credential_hash UNIQUE (pos_credential_hash);
