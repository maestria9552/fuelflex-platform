ALTER TABLE users
    ADD COLUMN post_name VARCHAR(100),
    ADD COLUMN gender VARCHAR(20),
    ADD COLUMN birth_place VARCHAR(150),
    ADD COLUMN birth_date DATE,
    ADD COLUMN address VARCHAR(500);

ALTER TABLE users
    ADD CONSTRAINT ck_users_gender
        CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE'));

ALTER TABLE pump_attendant_validation_items
    ADD COLUMN post_name_snapshot VARCHAR(100),
    ADD COLUMN gender_snapshot VARCHAR(20),
    ADD COLUMN birth_place_snapshot VARCHAR(150),
    ADD COLUMN birth_date_snapshot DATE,
    ADD COLUMN address_snapshot VARCHAR(500);

ALTER TABLE pump_attendant_validation_items
    ADD CONSTRAINT ck_pump_validation_item_gender
        CHECK (gender_snapshot IS NULL OR gender_snapshot IN ('MALE', 'FEMALE'));

UPDATE pump_attendant_validation_items validation_item
   SET post_name_snapshot = pump_attendant.post_name,
       gender_snapshot = pump_attendant.gender,
       birth_place_snapshot = pump_attendant.birth_place,
       birth_date_snapshot = pump_attendant.birth_date,
       address_snapshot = pump_attendant.address
  FROM users pump_attendant
 WHERE pump_attendant.id = validation_item.pump_attendant_id;
