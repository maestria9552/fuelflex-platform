import { apiGet, apiPost, apiPut } from "../api/apiClient";
const page = (value) => Array.isArray(value) ? { content: value, totalPages: 1, number: 0 } : { content: value?.content || [], totalPages: value?.totalPages || 0, number: value?.number || 0, totalElements: value?.totalElements || 0 };
export async function getManagerReceptions(params = {}, options = {}) { const { page: number = 0, size = 20, sort = "createdAt,desc" } = params; return page(await apiGet("/api/v1/manager/receptions?page=" + number + "&size=" + size + "&sort=" + encodeURIComponent(sort), options)); }
export function getManagerReception(id, options = {}) { return apiGet("/api/v1/manager/receptions/" + id, options); }
export function getManagerReceptionAvailability(purchaseOrderId, options = {}) { return apiGet("/api/v1/manager/receptions/purchase-orders/" + purchaseOrderId + "/availability", options); }
export function getManagerStockBalances(options = {}) { return apiGet("/api/v1/manager/receptions/stock-balances", options); }
export function createManagerReception(payload, options = {}) { return apiPost("/api/v1/manager/receptions", payload, options); }
export function updateManagerReception(id, payload, options = {}) { return apiPut("/api/v1/manager/receptions/" + id, payload, options); }
export function submitManagerReception(id, payload = {}, options = {}) { return apiPost("/api/v1/manager/receptions/" + id + "/submit", payload, options); }
export function getManagerReceptionHistory(id, options = {}) { return apiGet("/api/v1/manager/receptions/" + id + "/history", options); }
export async function getSupervisorReceptions(params = {}, options = {}) { const { page: number = 0, size = 20, sort = "createdAt,desc" } = params; return page(await apiGet("/api/v1/supervisor/receptions?page=" + number + "&size=" + size + "&sort=" + encodeURIComponent(sort), options)); }
export function getSupervisorReception(id, options = {}) { return apiGet("/api/v1/supervisor/receptions/" + id, options); }
export function approveSupervisorReception(id, payload = {}, options = {}) { return apiPost("/api/v1/supervisor/receptions/" + id + "/approve", payload, options); }
export function returnSupervisorReception(id, payload, options = {}) { return apiPost("/api/v1/supervisor/receptions/" + id + "/return", payload, options); }
export function cancelSupervisorReception(id, payload, options = {}) { return apiPost("/api/v1/supervisor/receptions/" + id + "/cancel", payload, options); }
export function getSupervisorReceptionHistory(id, options = {}) { return apiGet("/api/v1/supervisor/receptions/" + id + "/history", options); }
