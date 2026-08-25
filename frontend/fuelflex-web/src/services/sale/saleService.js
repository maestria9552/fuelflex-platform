import { apiGet, apiPost } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function base(role) {
  if (role !== "manager" && role !== "supervisor") {
    throw new Error("Le rôle de consultation des ventes n’est pas pris en charge.");
  }
  return `/api/v1/${role}/pos-sales`;
}

function buildQuery(filters = {}) {
  const query = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, String(value));
    }
  });
  const encoded = query.toString();
  return encoded ? `?${encoded}` : "";
}

export function getPosSales(role, filters = {}, options = {}) {
  return apiGet(`${base(role)}${buildQuery(filters)}`, options);
}

export function getPosSale(role, saleId, options = {}) {
  const validSaleId = validateRequiredId(saleId, "L’identifiant de la vente");
  return apiGet(`${base(role)}/${validSaleId}`, options);
}

export function reversePosSale(saleId, reason, options = {}) {
  const validSaleId = validateRequiredId(saleId, "L’identifiant de la vente");
  return apiPost(`/api/v1/manager/pos-sales/${validSaleId}/reverse`, { reason }, options);
}
