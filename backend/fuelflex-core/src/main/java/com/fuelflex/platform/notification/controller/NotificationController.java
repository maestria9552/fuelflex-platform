package com.fuelflex.platform.notification.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.notification.dto.response.NotificationPageResponse;
import com.fuelflex.platform.notification.dto.response.NotificationResponse;
import com.fuelflex.platform.notification.dto.response.UnreadNotificationCountResponse;
import com.fuelflex.platform.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public NotificationPageResponse findMine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return notificationService.findMine(page, size);
    }

    @GetMapping("/unread-count")
    public UnreadNotificationCountResponse countMineUnread() {
        return notificationService.countMineUnread();
    }

    @PutMapping("/{notificationId}/read")
    public NotificationResponse markMineAsRead(
            @PathVariable UUID notificationId
    ) {
        return notificationService.markMineAsRead(notificationId);
    }

    @PutMapping("/read-all")
    public UnreadNotificationCountResponse markAllMineAsRead() {
        return notificationService.markAllMineAsRead();
    }
}
