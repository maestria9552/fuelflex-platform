package com.fuelflex.platform.operations.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class OperationalDayNotificationConsolidationContractTest {

    @Test
    void migrationKeepsHistoryAndUniquelyScopesEachSupervisorAggregate() throws Exception {
        String migration = Files.readString(Path.of(
                "src/main/resources/db/migration/V25__consolidate_operational_day_notifications.sql"));
        assertThat(migration).contains("ADD COLUMN activity_count", "ADD COLUMN last_activity_type",
                "ADD COLUMN updated_at", "recipient_id, organization_id, station_id, resource_id",
                "event_type = 'OPERATIONAL_DAY_ACTIVITY'", "resource_type = 'OPERATIONAL_DAY'",
                "category = 'INFORMATION'");
        assertThat(migration).doesNotContain("DELETE FROM notifications", "finalized_at");
    }

    @Test
    void repositoryUsesAtomicUpsertAndReactivatesTheSameUnreadNotification() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/operations/repository/OperationalDayActivityNotificationRepository.java"));
        assertThat(source).contains("ON CONFLICT", "activity_count = notifications.activity_count + 1",
                "last_activity_type = EXCLUDED.last_activity_type", "is_read = FALSE", "read_at = NULL",
                "updated_at = CURRENT_TIMESTAMP", "'INFORMATION'", "'OPERATIONAL_DAY'");
        assertThat(source).doesNotContain("ACTION_REQUIRED");
    }

    @Test
    void exactlyTheApprovedActivitiesAreAggregateCandidates() {
        assertThat(OperationalDayActivityType.values()).containsExactly(
                OperationalDayActivityType.OPERATIONAL_DAY_OPENED,
                OperationalDayActivityType.SHIFT_ASSIGNMENT_OPENED,
                OperationalDayActivityType.DAILY_EXPENSE_RECORDED,
                OperationalDayActivityType.TANK_GAUGE_RECORDED,
                OperationalDayActivityType.INTERNAL_CONSUMPTION_RECORDED,
                OperationalDayActivityType.TANK_RETURN_RECORDED,
                OperationalDayActivityType.SHIFT_ASSIGNMENT_CLOSED,
                OperationalDayActivityType.OPERATIONAL_DAY_CLOSED);
    }
}
