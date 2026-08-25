package com.fuelflex.platform.employeevalidation.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class EmployeeValidationMigrationContractTest {

    @Test
    void v14AddsResolutionAndDocumentWorkflowWithoutChangingAppliedMigrations()
            throws Exception {
        String sql = Files.readString(Path.of(
                "src/main/resources/db/migration/"
                        + "V14__resolve_actions_and_validate_pump_attendants.sql"));

        assertThat(sql).contains(
                "resolved_at TIMESTAMPTZ",
                "resolved_by_id UUID",
                "ck_notification_resolution_state",
                "RECEPTION_OVERAGE_SUBMITTED",
                "RECEPTION_RETURNED_FOR_CORRECTION",
                "ORDER_SUBMITTED",
                "reception.status <> 'PENDING_SUPERVISOR_APPROVAL'",
                "pump_attendant_validation_status",
                "UPDATE users user_account",
                "'VALIDATED'",
                "CREATE SEQUENCE pump_attendant_validation_request_number_seq",
                "CREATE TABLE pump_attendant_validation_requests",
                "CREATE TABLE pump_attendant_validation_items",
                "CREATE TABLE pump_attendant_validation_history",
                "uk_pump_validation_item_request_employee",
                "uk_pump_validation_item_employee",
                "pump-attendant:prepare",
                "pump-attendant-validation:review",
                "WHERE NOT EXISTS"
        ).doesNotContain(
                "DELETE FROM role_permissions",
                "DROP TABLE",
                "DROP COLUMN"
        );
    }

    @Test
    void appliedV10ToV13AreNotRepurposedForEmployeeValidation()
            throws Exception {
        for (int version = 10; version <= 13; version++) {
            String prefix = "V" + version + "__";
            Path migration = Files.list(Path.of(
                            "src/main/resources/db/migration"))
                    .filter(path -> path.getFileName().toString()
                            .startsWith(prefix))
                    .findFirst()
                    .orElseThrow();
            assertThat(Files.readString(migration))
                    .doesNotContain("pump_attendant_validation_requests");
        }
    }
}
