package com.fuelflex.platform.notification.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.notification.dto.response.NotificationResponse;
import com.fuelflex.platform.notification.entity.Notification;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .organizationId(notification.getOrganization().getId())
                .stationId(notification.getStation() == null
                        ? null : notification.getStation().getId())
                .actorId(notification.getActor() == null
                        ? null : notification.getActor().getId())
                .eventType(notification.getEventType())
                .category(notification.getCategory())
                .titleKey(notification.getTitleKey())
                .messageKey(notification.getMessageKey())
                .resourceType(notification.getResourceType())
                .resourceId(notification.getResourceId())
                .requiresAction(notification.isRequiresAction())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
