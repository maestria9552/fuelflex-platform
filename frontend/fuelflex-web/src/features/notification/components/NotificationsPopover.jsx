import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getStoredUser } from "../../../services/auth/authStorage";
import { getSupervisorPendingOrderCount, getSupervisorPendingOrders } from "../../../services/purchaseOrder/supervisorPurchaseOrderService";
import { useTranslation } from "react-i18next";
import {
  Bell,
  Check,
  CheckCheck,
  CircleAlert,
  Inbox,
  LoaderCircle,
  RefreshCw,
} from "lucide-react";

import { formatDateTime } from "../../../i18n/formatters";
import {
  getMyNotifications,
  getMyUnreadNotificationCount,
  markAllMyNotificationsAsRead,
  markMyNotificationAsRead,
} from "../../../services/notification/notificationService";
import "./NotificationsPopover.css";

const PAGE_SIZE = 20;
const NAVIGABLE_RESOURCE_TYPES = ["PURCHASE_ORDER", "RECEPTION", "OPERATIONAL_DAY", "SHIFT_ASSIGNMENT", "DAILY_EXPENSE", "TANK_GAUGE", "FUEL_SALE", "PUMP_ATTENDANT_VALIDATION_REQUEST"];

function NotificationsPopover({ isOpen, onOpen, onClose }) {
  const { t, i18n } = useTranslation(["notifications", "common"]);
  const navigate = useNavigate();
  const currentUser = getStoredUser() || {};
  const isSupervisor = (currentUser.roles || []).some((role) => String(role?.code || role).toUpperCase() === "SUPERVISOR");
  const containerRef = useRef(null);
  const buttonRef = useRef(null);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [unreadIndependentCount, setUnreadIndependentCount] = useState(0);
  const [managerAttentionCount, setManagerAttentionCount] = useState(0);
  const [pendingOrderCount, setPendingOrderCount] = useState(0);
  const [pendingOrders, setPendingOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isMarkingAll, setIsMarkingAll] = useState(false);
  const [markingId, setMarkingId] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [loadAttempt, setLoadAttempt] = useState(0);

  useEffect(() => {
    if (!isSupervisor) return undefined;
    const refresh = () => Promise.all([getSupervisorPendingOrderCount(), getSupervisorPendingOrders({ page: 0, size: 20 })]).then(([count, page]) => { setPendingOrderCount(Number(count?.count ?? count) || 0); setPendingOrders(Array.isArray(page) ? page : (page?.content ?? [])); }).catch(() => {});
    refresh();
    const timer = window.setInterval(refresh, 45000); window.addEventListener("fuelflex:notifications-refresh", refresh);
    return () => { window.clearInterval(timer); window.removeEventListener("fuelflex:notifications-refresh", refresh); };
  }, [isSupervisor]);

  useEffect(() => {
    const controller = new AbortController();

    getMyUnreadNotificationCount({ signal: controller.signal })
      .then((result) => { setUnreadCount(result?.unreadCount || 0); setUnreadIndependentCount((result?.unreadNonOrderSubmittedCount ?? result?.unreadCount ?? 0)); setManagerAttentionCount(result?.attentionCount ?? result?.unreadCount ?? 0); })
      .catch((error) => {
        if (error?.name !== "AbortError") {
          setErrorMessage(
            error?.message || t("notifications:feedback.countError"),
          );
        }
      });

    const interval = window.setInterval(() => { getMyUnreadNotificationCount().then((result) => { setUnreadCount(result?.unreadCount || 0); setUnreadIndependentCount((result?.unreadNonOrderSubmittedCount ?? result?.unreadCount ?? 0)); setManagerAttentionCount(result?.attentionCount ?? result?.unreadCount ?? 0); }).catch(() => {}); }, 45000);
    return () => { controller.abort(); window.clearInterval(interval); };
  }, [t]);

  useEffect(() => {
    const refreshNotificationState = () => {
      getMyUnreadNotificationCount()
        .then((result) => {
          setUnreadCount(result?.unreadCount || 0);
          setUnreadIndependentCount(result?.unreadNonOrderSubmittedCount ?? result?.unreadCount ?? 0);
          setManagerAttentionCount(result?.attentionCount ?? result?.unreadCount ?? 0);
          if (isOpen) setLoadAttempt((attempt) => attempt + 1);
        })
        .catch(() => {});
    };
    window.addEventListener("fuelflex:notifications-refresh", refreshNotificationState);
    return () => window.removeEventListener("fuelflex:notifications-refresh", refreshNotificationState);
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    const controller = new AbortController();

    async function loadNotifications() {
      setIsLoading(true);
      setErrorMessage("");

      try {
        const result = await getMyNotifications({
          page: 0,
          size: PAGE_SIZE,
          signal: controller.signal,
        });
        setNotifications(Array.isArray(result?.content) ? result.content : []);
      } catch (error) {
        if (error?.name !== "AbortError") {
          setErrorMessage(
            error?.message || t("notifications:feedback.loadError"),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    loadNotifications();
    return () => controller.abort();
  }, [isOpen, loadAttempt, t]);


  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    const handlePointerDown = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        onClose();
      }
    };
    const handleKeyDown = (event) => {
      if (event.key === "Escape") {
        onClose();
        window.requestAnimationFrame(() => buttonRef.current?.focus());
      }
    };

    document.addEventListener("pointerdown", handlePointerDown);
    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("pointerdown", handlePointerDown);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, onClose]);

  const translateNotificationText = (key, fallbackKey) => {
    if (!key) {
      return t(fallbackKey);
    }

    return t(key, {
      ns: "notifications",
      defaultValue: t(fallbackKey),
    });
  };

  const handleNotificationClick = async (notification) => {
    if (!notification.read) await handleMarkAsRead(notification.id);
    const prefix = isSupervisor ? "/superviseur" : "/gerant";
    if (notification.resourceType === "PURCHASE_ORDER" && notification.resourceId) {
      navigate("/superviseur/commandes/" + notification.resourceId);
    } else if (notification.resourceType === "RECEPTION" && notification.resourceId) {
      navigate(prefix + "/receptions/" + notification.resourceId);
    } else if (notification.resourceType === "OPERATIONAL_DAY" && notification.resourceId) {
      navigate(prefix + "/operations/" + notification.resourceId);
    } else if (notification.resourceType === "FUEL_SALE" && notification.resourceId) {
      navigate(prefix + "/ventes/" + notification.resourceId);
    } else if (notification.resourceType === "PUMP_ATTENDANT_VALIDATION_REQUEST" && notification.resourceId) {
      navigate(prefix + "/validations-pompistes/" + notification.resourceId);
    } else if (["SHIFT_ASSIGNMENT", "DAILY_EXPENSE", "TANK_GAUGE"].includes(notification.resourceType)) {
      navigate(prefix + "/operations");
    }
    onClose();
  };


  const handleMarkAsRead = async (notificationId) => {
    setMarkingId(notificationId);
    setErrorMessage("");

    try {
      const updated = await markMyNotificationAsRead(notificationId);
      setNotifications((current) => current.map((notification) => (
        notification.id === notificationId ? updated : notification
      )));
      getMyUnreadNotificationCount().then((result) => { setUnreadCount(result?.unreadCount || 0); setUnreadIndependentCount(result?.unreadNonOrderSubmittedCount ?? result?.unreadCount ?? 0); setManagerAttentionCount(result?.attentionCount ?? result?.unreadCount ?? 0); }).catch(() => {});
    } catch (error) {
      setErrorMessage(
        error?.message || t("notifications:feedback.markReadError"),
      );
    } finally {
      setMarkingId(null);
    }
  };

  const handleMarkAllAsRead = async () => {
    setIsMarkingAll(true);
    setErrorMessage("");

    try {
      const countResult = await markAllMyNotificationsAsRead();
      const readAt = new Date().toISOString();
      setNotifications((current) => current.map((notification) => ({
        ...notification,
        read: true,
        readAt: notification.readAt || readAt,
      })));
      setUnreadCount(0);
      setUnreadIndependentCount(0);
      setManagerAttentionCount(countResult?.attentionCount ?? countResult?.actionRequiredCount ?? 0);
    } catch (error) {
      setErrorMessage(
        error?.message || t("notifications:feedback.markAllReadError"),
      );
    } finally {
      setIsMarkingAll(false);
    }
  };

  const formatCreatedAt = (value) => formatDateTime(value, {
    language: i18n.resolvedLanguage || i18n.language,
  });

  const visibleNotifications = (() => {
    const pendingByResource = new Map(pendingOrders.map((order) => [String(order.id), order]));
    const activeNotifications = notifications.filter((notification) => !notification.read || notification.requiresAction);
    const merged = activeNotifications.map((notification) => {
      if (notification.resourceType !== "PURCHASE_ORDER" || !pendingByResource.has(String(notification.resourceId))) return notification;
      return { ...notification, requiresAction: true, pendingOrder: pendingByResource.get(String(notification.resourceId)) };
    });
    const existing = new Set(merged.filter((notification) => notification.resourceType === "PURCHASE_ORDER").map((notification) => String(notification.resourceId)));
    const synthetic = pendingOrders.filter((order) => !existing.has(String(order.id))).map((order) => ({
      id: `pending-${order.id}`, read: true, requiresAction: true, pendingOrder: order, pendingSynthetic: true,
      resourceType: "PURCHASE_ORDER", resourceId: order.id, titleKey: "notifications:events.orderSubmitted.title",
      messageKey: "notifications:events.orderSubmitted.message", createdAt: order.submittedAt || order.createdAt,
    }));
    return [...synthetic, ...merged];
  })();
  const badgeCount = isSupervisor
    ? Math.max(managerAttentionCount, pendingOrderCount + unreadIndependentCount)
    : managerAttentionCount;

  const badgeLabel = t("notifications:accessibility.unreadCount", {
    count: badgeCount,
  });

  return (
    <div className="notification-center" ref={containerRef}>
      <button
        ref={buttonRef}
        type="button"
        className="supervisor-topbar-action-button supervisor-topbar-notification-button"
        aria-label={t("notifications:accessibility.open")}
        title={t("notifications:accessibility.open")}
        aria-expanded={isOpen}
        aria-haspopup="true"
        aria-controls="supervisor-notifications-popover"
        onClick={isOpen ? onClose : onOpen}
      >
        <Bell size={19} strokeWidth={1.8} aria-hidden="true" />
        {badgeCount > 0 && (
          <span
            className="notification-badge"
            aria-label={badgeLabel}
            title={badgeLabel}
          >
            {badgeCount > 99 ? "99+" : badgeCount}
          </span>
        )}
      </button>

      {isOpen && (
        <section
          id="supervisor-notifications-popover"
          className="notifications-popover"
          aria-label={t("notifications:title")}
        >
          <header className="notifications-popover-header">
            <div>
              <strong>{t("notifications:title")}</strong>
              <span>{t("notifications:description")}</span>
            </div>
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={handleMarkAllAsRead}
                disabled={isMarkingAll}
              >
                <CheckCheck size={16} aria-hidden="true" />
                {t("notifications:actions.markAllRead")}
              </button>
            )}
          </header>

          {errorMessage && (
            <div className="notifications-state error" role="alert">
              <CircleAlert size={19} aria-hidden="true" />
              <span>{errorMessage}</span>
              <button
                type="button"
                onClick={() => setLoadAttempt((attempt) => attempt + 1)}
              >
                <RefreshCw size={15} aria-hidden="true" />
                {t("common:actions.retry")}
              </button>
            </div>
          )}

          {isLoading ? (
            <div className="notifications-state" role="status">
              <LoaderCircle
                size={22}
                className="notifications-spinner"
                aria-hidden="true"
              />
              <span>{t("notifications:feedback.loading")}</span>
            </div>
          ) : !errorMessage && visibleNotifications.length === 0 ? (
            <div className="notifications-state" role="status">
              <Inbox size={25} aria-hidden="true" />
              <strong>{t("notifications:empty.title")}</strong>
              <span>{t("notifications:empty.description")}</span>
            </div>
          ) : !errorMessage ? (
            <ul className="notifications-list">
              {visibleNotifications.map((notification) => (
                <li
                  key={notification.id}
                  onClick={(event) => { if (!event.target.closest("button") && NAVIGABLE_RESOURCE_TYPES.includes(notification.resourceType)) handleNotificationClick(notification); }}
                  className={[
                    "notification-item",
                    notification.read ? "read" : "unread",
                    notification.requiresAction ? "action-required" : "information",
                  ].join(" ")}
                >
                  <div className="notification-item-heading">
                    <span className="notification-category">
                      {t(
                        notification.requiresAction
                          ? "notifications:category.actionRequired"
                          : "notifications:category.information",
                      )}
                    </span>
                    {!notification.read && (
                      <span className="notification-unread-dot">
                        {t("notifications:status.unread")}
                      </span>
                    )}
                  </div>
                  <strong>
                    {translateNotificationText(
                      notification.titleKey,
                      "notifications:fallback.title",
                    )}
                  </strong>
                  <p>
                    {notification.pendingOrder && (
                      <>{notification.pendingOrder.orderNumber} · {notification.pendingOrder.station?.name || notification.pendingOrder.station?.code}</>
                    )}
                    {notification.pendingOrder && <br />}
                    {translateNotificationText(
                      notification.messageKey,
                      "notifications:fallback.message",
                    )}
                  </p>
                  <div className="notification-item-footer">
                    <time dateTime={notification.createdAt}>
                      {formatCreatedAt(notification.createdAt)}
                    </time>
                    {!notification.read && (
                      <button
                        type="button"
                        onClick={() => handleMarkAsRead(notification.id)}
                        disabled={markingId === notification.id}
                      >
                        <Check size={14} aria-hidden="true" />
                        {t("notifications:actions.markRead")}
                      </button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          ) : null}
        </section>
      )}
    </div>
  );
}

export default NotificationsPopover;
