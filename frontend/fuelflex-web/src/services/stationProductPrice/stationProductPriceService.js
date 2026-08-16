import { apiDelete, apiGet, apiPost, apiPut } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getEndpoint(organizationId, stationId, stationProductId) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );
  const validStationProductId = validateRequiredId(
    stationProductId,
    "L’identifiant du produit de station"
  );
  return `/api/v1/organizations/${validOrganizationId}/stations/${validStationId}/station-products/${validStationProductId}/prices`;
}

export function createStationProductPrice(
  organizationId,
  stationId,
  stationProductId,
  payload,
  options = {}
) {
  return apiPost(
    getEndpoint(organizationId, stationId, stationProductId),
    payload,
    options
  );
}

export function getStationProductPrices(
  organizationId,
  stationId,
  stationProductId,
  options = {}
) {
  return apiGet(
    getEndpoint(organizationId, stationId, stationProductId),
    options
  );
}

export function updateStationProductPrice(
  organizationId,
  stationId,
  stationProductId,
  stationProductPriceId,
  payload,
  options = {}
) {
  const validPriceId = validateRequiredId(
    stationProductPriceId,
    "L’identifiant du prix"
  );
  return apiPut(
    `${getEndpoint(organizationId, stationId, stationProductId)}/${validPriceId}`,
    payload,
    options
  );
}

export function deactivateStationProductPrice(
  organizationId,
  stationId,
  stationProductId,
  stationProductPriceId,
  options = {}
) {
  const validPriceId = validateRequiredId(
    stationProductPriceId,
    "L’identifiant du prix"
  );
  return apiDelete(
    `${getEndpoint(organizationId, stationId, stationProductId)}/${validPriceId}`,
    options
  );
}
