import { apiGet, apiPost, apiPut } from "../api/apiClient";
const page = (value) => Array.isArray(value) ? { content: value, totalPages: 1, number: 0 } : { content: value?.content || [], totalPages: value?.totalPages || 0, number: value?.number || 0, totalElements: value?.totalElements || 0 };
export async function getManagerReceptions(params = {}, options = {}) { const { page: number = 0, size = 20, sort = "createdAt,desc" } = params; return page(await apiGet("/api/v1/manager/receptions?page=" + number + "&size=" + size + "&sort=" + encodeURIComponent(sort), options)); }
export function getManagerReception(id, options = {}) { return apiGet("/api/v1/manager/receptions/" + id, options); }
export function createManagerReception(payload, options = {}) { return apiPost("/api/v1/manager/receptions", payload, options); }
export function updateManagerReception(id, payload, options = {}) { return apiPut("/api/v1/manager/receptions/" + id, payload, options); }
export function submitManagerReception(id, payload = {}, options = {}) { return apiPost("/api/v1/manager/receptions/" + id + "/submit", payload, options); }
export function getManagerReceptionHistory(id, options = {}) { return apiGet("/api/v1/manager/receptions/" + id + "/history", options); }
