package com.fuelflex.platform.notification.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fuelflex.platform.notification.entity.NotificationCategory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID organizationId;
    private UUID stationId;
    private UUID actorId;
    private String eventType;
    private NotificationCategory category;
    private String titleKey;
    private String messageKey;
    private String resourceType;
    private UUID resourceId;
    private boolean requiresAction;
    private boolean read;
    private OffsetDateTime readAt;
    private boolean resolved;
    private OffsetDateTime resolvedAt;
    private UUID resolvedById;
    private OffsetDateTime createdAt;
}
