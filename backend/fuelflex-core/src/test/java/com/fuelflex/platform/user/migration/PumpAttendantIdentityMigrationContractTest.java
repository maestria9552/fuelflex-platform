package com.fuelflex.platform.user.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PumpAttendantIdentityMigrationContractTest {

    @Test
    void v15AddsIdentityToUsersAndValidationSnapshotsAdditively()
            throws Exception {
        String sql = Files.readString(Path.of(
                "src/main/resources/db/migration/"
                        + "V15__add_pump_attendant_identity_profile.sql"));

        assertThat(sql).contains(
                "ALTER TABLE users",
                "ADD COLUMN post_name",
                "ADD COLUMN gender",
                "ADD COLUMN birth_place",
                "ADD COLUMN birth_date",
                "ADD COLUMN address",
                "ck_users_gender",
                "ALTER TABLE pump_attendant_validation_items",
                "post_name_snapshot",
                "gender_snapshot",
                "birth_place_snapshot",
                "birth_date_snapshot",
                "address_snapshot",
                "UPDATE pump_attendant_validation_items"
        ).doesNotContain(
                "DROP TABLE",
                "DROP COLUMN",
                "DELETE FROM role_permissions"
        );
    }
}
