import {
  apiDelete,
  apiGet,
  apiPost,
  apiPut,
} from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getPumpsEndpoint(organizationId, stationId) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );

  return `/api/v1/organizations/${validOrganizationId}/stations/${validStationId}/pumps`;
}

export function createPump(
  organizationId,
  stationId,
  payload,
  options = {}
) {
  return apiPost(
    getPumpsEndpoint(organizationId, stationId),
    payload,
    options
  );
}

export function getPumps(
  organizationId,
  stationId,
  options = {}
) {
  return apiGet(
    getPumpsEndpoint(organizationId, stationId),
    options
  );
}

export function getActivePumps(
  organizationId,
  stationId,
  options = {}
) {
  return apiGet(
    `${getPumpsEndpoint(organizationId, stationId)}/active`,
    options
  );
}

export function getPumpById(
  organizationId,
  stationId,
  pumpId,
  options = {}
) {
  const validPumpId = validateRequiredId(
    pumpId,
    "L’identifiant de la pompe"
  );

  return apiGet(
    `${getPumpsEndpoint(organizationId, stationId)}/${validPumpId}`,
    options
  );
}

export function updatePump(
  organizationId,
  stationId,
  pumpId,
  payload,
  options = {}
) {
  const validPumpId = validateRequiredId(
    pumpId,
    "L’identifiant de la pompe"
  );

  return apiPut(
    `${getPumpsEndpoint(organizationId, stationId)}/${validPumpId}`,
    payload,
    options
  );
}

export function deactivatePump(
  organizationId,
  stationId,
  pumpId,
  options = {}
) {
  const validPumpId = validateRequiredId(
    pumpId,
    "L’identifiant de la pompe"
  );

  return apiDelete(
    `${getPumpsEndpoint(organizationId, stationId)}/${validPumpId}`,
    options
  );
}
