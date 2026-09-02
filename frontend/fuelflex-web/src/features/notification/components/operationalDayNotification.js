export const OPERATIONAL_DAY_ACTIVITY_EVENT = "OPERATIONAL_DAY_ACTIVITY";

export function isOperationalDayActivity(notification) {
  return notification?.eventType === OPERATIONAL_DAY_ACTIVITY_EVENT;
}

export function operationalDayActivityTitleKey(notification) {
  return notification?.lastActivityType === "OPERATIONAL_DAY_CLOSED"
    ? "notifications:operationalDayActivity.closedTitle"
    : "notifications:operationalDayActivity.title";
}

export function operationalDayActivityTranslationKey(notification) {
  return `notifications:operationalDayActivity.activities.${notification?.lastActivityType || "UNKNOWN"}`;
}

export function notificationActivityTimestamp(notification) {
  return notification?.updatedAt || notification?.createdAt;
}

export function notificationTarget(notification, isSupervisor) {
  if (!isOperationalDayActivity(notification) || !notification?.resourceId) return null;
  return `${isSupervisor ? "/superviseur" : "/gerant"}/operations/${notification.resourceId}`;
}
