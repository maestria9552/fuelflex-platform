import { apiGet, apiPost } from "../api/apiClient";

export function getSupervisorOrders({ page = 0, size = 50, sort = "createdAt,desc" } = {}, options = {}) {
  return apiGet(`/api/v1/supervisor/orders?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`, options);
}

export function getSupervisorOrder(id, options = {}) {
  return apiGet(`/api/v1/supervisor/orders/${id}`, options);
}

export function getSupervisorOrderHistory(id, options = {}) {
  return apiGet(`/api/v1/supervisor/orders/${id}/history`, options);
}

export function approveSupervisorOrder(id, options = {}) {
  return apiPost(`/api/v1/supervisor/orders/${id}/approve`, undefined, options);
}

export function rejectSupervisorOrder(id, comment, options = {}) {
  return apiPost(`/api/v1/supervisor/orders/${id}/reject`, { comment }, options);
}

export function getSupervisorPendingOrderCount(options = {}) { return apiGet("/api/v1/supervisor/orders/pending-count", options); }
export function getSupervisorPendingOrders({ page = 0, size = 20, sort = "submittedAt,desc" } = {}, options = {}) {
  return apiGet(`/api/v1/supervisor/orders/pending?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`, options).then((response) => (Array.isArray(response) ? response : (response?.content ?? [])));
}
