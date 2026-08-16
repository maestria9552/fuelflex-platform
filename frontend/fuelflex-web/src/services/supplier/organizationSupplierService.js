import { apiGet } from "../api/apiClient";

export function getManagerSuppliers(options = {}) {
  return apiGet("/api/v1/manager/suppliers", options);
}
