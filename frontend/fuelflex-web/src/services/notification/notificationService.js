import { apiGet, apiPut } from "../api/apiClient";

const NOTIFICATIONS_ENDPOINT = "/api/v1/notifications";

export function getMyNotifications({ page = 0, size = 20, ...options } = {}) {
  const query = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  return apiGet(`${NOTIFICATIONS_ENDPOINT}?${query}`, options);
}

export function getMyUnreadNotificationCount(options = {}) {
  return apiGet(`${NOTIFICATIONS_ENDPOINT}/unread-count`, options);
}

export function markMyNotificationAsRead(notificationId, options = {}) {
  return apiPut(
    `${NOTIFICATIONS_ENDPOINT}/${encodeURIComponent(notificationId)}/read`,
    undefined,
    options,
  );
}

export function markAllMyNotificationsAsRead(options = {}) {
  return apiPut(`${NOTIFICATIONS_ENDPOINT}/read-all`, undefined, options);
}
