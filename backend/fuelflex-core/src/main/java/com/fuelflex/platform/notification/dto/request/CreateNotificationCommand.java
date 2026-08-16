package com.fuelflex.platform.notification.dto.request;

import java.util.UUID;

import com.fuelflex.platform.notification.entity.NotificationCategory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateNotificationCommand {

    private UUID recipientId;
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
}
