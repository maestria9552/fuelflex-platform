package com.fuelflex.platform.notification.service;

import java.util.UUID;

import com.fuelflex.platform.notification.dto.request.CreateNotificationCommand;
import com.fuelflex.platform.notification.dto.response.NotificationPageResponse;
import com.fuelflex.platform.notification.dto.response.NotificationResponse;
import com.fuelflex.platform.notification.dto.response.UnreadNotificationCountResponse;

public interface NotificationService {

    NotificationResponse create(CreateNotificationCommand command);

    NotificationPageResponse findMine(int page, int size);

    UnreadNotificationCountResponse countMineUnread();

    NotificationResponse markMineAsRead(UUID notificationId);

    UnreadNotificationCountResponse markAllMineAsRead();
}
