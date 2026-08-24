package com.fuelflex.platform.operations.migration;
import static org.assertj.core.api.Assertions.assertThat; import java.nio.file.*; import org.junit.jupiter.api.Test;
class OperationsMigrationContractTest {
 @Test void v10ProtectsCodesDaysAssignmentsAndIndexes() throws Exception {String sql=Files.readString(Path.of("src/main/resources/db/migration/V10__create_operational_days_and_shift_assignments.sql")); assertThat(sql).contains("pump_attendant_operational_code_seq","uk_users_operational_code","uk_operational_day_station_date","uk_operational_day_open_station","uk_shift_open_meter","uk_shift_open_attendant","closing_index>=opening_index","operational_history");}
 @Test void permissionsAreGrantedAdditively() throws Exception {String sql=Files.readString(Path.of("src/main/resources/db/migration/V10__create_operational_days_and_shift_assignments.sql")); assertThat(sql)
        .contains("NOT EXISTS(SELECT 1 FROM role_permissions")
        .doesNotContain("DELETE FROM role_permissions");}
}
