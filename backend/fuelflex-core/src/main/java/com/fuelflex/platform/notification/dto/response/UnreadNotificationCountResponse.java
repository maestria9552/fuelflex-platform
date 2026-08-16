package com.fuelflex.platform.notification.dto.response;

import lombok.Getter;

@Getter
public class UnreadNotificationCountResponse {
    private final long unreadCount;
    private final long unreadNonOrderSubmittedCount;

    public UnreadNotificationCountResponse(long unreadCount) {
        this(unreadCount, unreadCount);
    }

    public UnreadNotificationCountResponse(long unreadCount, long unreadNonOrderSubmittedCount) {
        this.unreadCount = unreadCount;
        this.unreadNonOrderSubmittedCount = unreadNonOrderSubmittedCount;
    }
}
