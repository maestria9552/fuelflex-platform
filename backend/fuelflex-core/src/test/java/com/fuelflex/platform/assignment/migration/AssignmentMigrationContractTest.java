package com.fuelflex.platform.assignment.migration;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AssignmentMigrationContractTest {
    @Test
    void v2ContainsTenantHistoryAndConcurrencyProtections() throws IOException {
        String sql = resource("db/migration/V2__create_user_station_assignments.sql");
        assertThat(sql).contains(
                "CREATE TABLE user_station_assignments",
                "FOREIGN KEY (user_id, organization_id)",
                "FOREIGN KEY (station_id, organization_id)",
                "WHERE valid_until IS NULL",
                "ck_assignment_end_state",
                "version BIGINT NOT NULL DEFAULT 0");
        assertThat(sql).doesNotContain("pump_id", "nozzle_id", "shift_id");
    }

    @Test
    void v3CorrelatesSourceAndDestinationWithinTenant() throws IOException {
        String sql = resource("db/migration/V3__create_employee_station_transfers.sql");
        assertThat(sql).contains(
                "CREATE TABLE employee_station_transfers",
                "source_assignment_id",
                "destination_assignment_id",
                "fk_transfer_source_scope",
                "fk_transfer_destination_scope");
    }

    private String resource(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(name)) {
            assertThat(stream).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
