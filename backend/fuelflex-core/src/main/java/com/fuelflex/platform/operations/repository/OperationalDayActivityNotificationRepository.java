package com.fuelflex.platform.operations.repository;

import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fuelflex.platform.operations.service.OperationalDayActivityType;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OperationalDayActivityNotificationRepository {

    private static final String EVENT_TYPE = "OPERATIONAL_DAY_ACTIVITY";
    private static final String RESOURCE_TYPE = "OPERATIONAL_DAY";
    private static final String CATEGORY = "INFORMATION";

    private final NamedParameterJdbcTemplate jdbc;

    public void upsert(
            UUID recipientId,
            UUID organizationId,
            UUID stationId,
            UUID actorId,
            UUID operationalDayId,
            OperationalDayActivityType activityType
    ) {
        jdbc.update("""
                INSERT INTO notifications (
                    id, recipient_id, organization_id, station_id, actor_id,
                    event_type, category, title_key, message_key,
                    resource_type, resource_id, requires_action, is_read,
                    read_at, resolved_at, resolved_by_id, activity_count,
                    last_activity_type, created_at, updated_at
                ) VALUES (
                    gen_random_uuid(), :recipientId, :organizationId, :stationId, :actorId,
                    'OPERATIONAL_DAY_ACTIVITY', 'INFORMATION', :titleKey, :messageKey,
                    'OPERATIONAL_DAY', :operationalDayId, FALSE, FALSE,
                    NULL, NULL, NULL, 1, :activityType, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                ON CONFLICT (recipient_id, organization_id, station_id, resource_id)
                    WHERE event_type = 'OPERATIONAL_DAY_ACTIVITY'
                      AND resource_type = 'OPERATIONAL_DAY'
                      AND category = 'INFORMATION'
                DO UPDATE SET
                    actor_id = EXCLUDED.actor_id,
                    title_key = EXCLUDED.title_key,
                    message_key = EXCLUDED.message_key,
                    activity_count = notifications.activity_count + 1,
                    last_activity_type = EXCLUDED.last_activity_type,
                    updated_at = CURRENT_TIMESTAMP,
                    is_read = FALSE,
                    read_at = NULL
                """, Map.of(
                "recipientId", recipientId,
                "organizationId", organizationId,
                "stationId", stationId,
                "actorId", actorId,
                "operationalDayId", operationalDayId,
                "activityType", activityType.name(),
                "titleKey", titleKey(activityType),
                "messageKey", "notifications:operationalDayActivity.message"
        ));
    }

    private String titleKey(OperationalDayActivityType activityType) {
        return activityType == OperationalDayActivityType.OPERATIONAL_DAY_CLOSED
                ? "notifications:operationalDayActivity.closedTitle"
                : "notifications:operationalDayActivity.title";
    }
}
