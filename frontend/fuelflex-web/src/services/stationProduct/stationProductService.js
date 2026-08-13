import { apiDelete, apiGet, apiPost, apiPut } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getEndpoint(organizationId, stationId) {
  const validOrganizationId = validateRequiredId(organizationId, "L’identifiant de l’organisation");
  const validStationId = validateRequiredId(stationId, "L’identifiant de la station");
  return `/api/v1/organizations/${validOrganizationId}/stations/${validStationId}/products`;
}

export function createStationProduct(organizationId, stationId, payload, options = {}) {
  return apiPost(getEndpoint(organizationId, stationId), payload, options);
}

export function getStationProducts(organizationId, stationId, options = {}) {
  return apiGet(getEndpoint(organizationId, stationId), options);
}

export function getActiveStationProducts(organizationId, stationId, options = {}) {
  return apiGet(`${getEndpoint(organizationId, stationId)}/active`, options);
}

export function getStationProductById(organizationId, stationId, stationProductId, options = {}) {
  const validId = validateRequiredId(stationProductId, "L’identifiant du produit de station");
  return apiGet(`${getEndpoint(organizationId, stationId)}/${validId}`, options);
}

export function updateStationProduct(
  organizationId,
  stationId,
  stationProductId,
  payload,
  options = {}
) {
  const validId = validateRequiredId(stationProductId, "L’identifiant du produit de station");
  return apiPut(`${getEndpoint(organizationId, stationId)}/${validId}`, payload, options);
}

export function deactivateStationProduct(
  organizationId,
  stationId,
  stationProductId,
  options = {}
) {
  const validId = validateRequiredId(stationProductId, "L’identifiant du produit de station");
  return apiDelete(`${getEndpoint(organizationId, stationId)}/${validId}`, options);
}

export const deleteStationProduct = deactivateStationProduct;
