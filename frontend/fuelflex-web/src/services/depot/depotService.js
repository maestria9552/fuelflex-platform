import {
  apiDelete,
  apiGet,
  apiPost,
  apiPut,
} from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getDepotsEndpoint(organizationId, stationId) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );

  return `/api/v1/organizations/${validOrganizationId}/stations/${validStationId}/depots`;
}

export function createDepot(
  organizationId,
  stationId,
  payload,
  options = {}
) {
  return apiPost(
    getDepotsEndpoint(organizationId, stationId),
    payload,
    options
  );
}

export function getDepots(
  organizationId,
  stationId,
  options = {}
) {
  return apiGet(
    getDepotsEndpoint(organizationId, stationId),
    options
  );
}

export function getActiveDepots(
  organizationId,
  stationId,
  options = {}
) {
  return apiGet(
    `${getDepotsEndpoint(organizationId, stationId)}/active`,
    options
  );
}

export function getDepotById(
  organizationId,
  stationId,
  depotId,
  options = {}
) {
  const validDepotId = validateRequiredId(
    depotId,
    "L’identifiant du dépôt"
  );

  return apiGet(
    `${getDepotsEndpoint(organizationId, stationId)}/${validDepotId}`,
    options
  );
}

export function updateDepot(
  organizationId,
  stationId,
  depotId,
  payload,
  options = {}
) {
  const validDepotId = validateRequiredId(
    depotId,
    "L’identifiant du dépôt"
  );

  return apiPut(
    `${getDepotsEndpoint(organizationId, stationId)}/${validDepotId}`,
    payload,
    options
  );
}

export function deactivateDepot(
  organizationId,
  stationId,
  depotId,
  options = {}
) {
  const validDepotId = validateRequiredId(
    depotId,
    "L’identifiant du dépôt"
  );

  return apiDelete(
    `${getDepotsEndpoint(organizationId, stationId)}/${validDepotId}`,
    options
  );
}
