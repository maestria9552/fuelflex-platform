package com.fuelflex.platform.notification.dto.response;

import lombok.Getter;

@Getter
public class UnreadNotificationCountResponse {
    private final long unreadCount;
    private final long unreadNonOrderSubmittedCount;
    private final long actionRequiredCount;
    private final long attentionCount;

    public UnreadNotificationCountResponse(long unreadCount) {
        this(unreadCount, unreadCount, 0, unreadCount);
    }

    public UnreadNotificationCountResponse(long unreadCount, long unreadNonOrderSubmittedCount) {
        this(unreadCount, unreadNonOrderSubmittedCount, 0, unreadCount);
    }

    public UnreadNotificationCountResponse(long unreadCount, long unreadNonOrderSubmittedCount, long actionRequiredCount, long attentionCount) {
        this.unreadCount = unreadCount;
        this.unreadNonOrderSubmittedCount = unreadNonOrderSubmittedCount;
        this.actionRequiredCount = actionRequiredCount;
        this.attentionCount = attentionCount;
    }
}
