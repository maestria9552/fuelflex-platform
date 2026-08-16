import { apiDelete, apiGet, apiPost, apiPut } from "../api/apiClient";
export function uploadManagerOrderAttachment(id, displayName, file, options = {}) { const body = new FormData(); body.append("displayName", displayName); body.append("file", file); return apiPost(`/api/v1/manager/orders/${id}/attachments`, body, options); }
export function deleteManagerOrderAttachment(id, attachmentId, options = {}) { return apiDelete(`/api/v1/manager/orders/${id}/attachments/${attachmentId}`, options); }
export function getOrderAttachments(id, options = {}) { return apiGet(`/api/v1/orders/${id}/attachments`, options); }
export function getOrderAttachmentDownloadUrl(id, attachmentId) { return `/api/v1/orders/${id}/attachments/${attachmentId}/download`; }

export function getManagerOrders({ page = 0, size = 20, sort = "createdAt,desc" } = {}, options = {}) {
  return apiGet(`/api/v1/manager/orders?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`, options);
}

export function getManagerOrder(id, options = {}) {
  return apiGet(`/api/v1/manager/orders/${id}`, options);
}

export function getManagerOrderHistory(id, options = {}) {
  return apiGet(`/api/v1/manager/orders/${id}/history`, options);
}

export function createManagerOrder(payload, options = {}) {
  return apiPost("/api/v1/manager/orders", payload, options);
}

export function updateManagerOrder(id, payload, options = {}) {
  return apiPut(`/api/v1/manager/orders/${id}`, payload, options);
}

export function submitManagerOrder(id, options = {}) {
  return apiPost(`/api/v1/manager/orders/${id}/submit`, undefined, options);
}
