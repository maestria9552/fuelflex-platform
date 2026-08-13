import {
  apiDelete,
  apiGet,
  apiPost,
  apiPut,
} from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getTanksEndpoint(organizationId, stationId, depotId) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );
  const validDepotId = validateRequiredId(
    depotId,
    "L’identifiant du dépôt"
  );

  return `/api/v1/organizations/${validOrganizationId}/stations/${validStationId}/depots/${validDepotId}/tanks`;
}

export function createTank(
  organizationId,
  stationId,
  depotId,
  payload,
  options = {}
) {
  return apiPost(
    getTanksEndpoint(organizationId, stationId, depotId),
    payload,
    options
  );
}

export function getTanks(
  organizationId,
  stationId,
  depotId,
  options = {}
) {
  return apiGet(
    getTanksEndpoint(organizationId, stationId, depotId),
    options
  );
}

export function getActiveTanks(
  organizationId,
  stationId,
  depotId,
  options = {}
) {
  return apiGet(
    `${getTanksEndpoint(organizationId, stationId, depotId)}/active`,
    options
  );
}

export function getTankById(
  organizationId,
  stationId,
  depotId,
  tankId,
  options = {}
) {
  const validTankId = validateRequiredId(
    tankId,
    "L’identifiant de la citerne"
  );

  return apiGet(
    `${getTanksEndpoint(organizationId, stationId, depotId)}/${validTankId}`,
    options
  );
}

export function updateTank(
  organizationId,
  stationId,
  depotId,
  tankId,
  payload,
  options = {}
) {
  const validTankId = validateRequiredId(
    tankId,
    "L’identifiant de la citerne"
  );

  return apiPut(
    `${getTanksEndpoint(organizationId, stationId, depotId)}/${validTankId}`,
    payload,
    options
  );
}

export function deactivateTank(
  organizationId,
  stationId,
  depotId,
  tankId,
  options = {}
) {
  const validTankId = validateRequiredId(
    tankId,
    "L’identifiant de la citerne"
  );

  return apiDelete(
    `${getTanksEndpoint(organizationId, stationId, depotId)}/${validTankId}`,
    options
  );
}
