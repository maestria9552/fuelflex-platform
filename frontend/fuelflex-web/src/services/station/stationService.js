import {
  apiDelete,
  apiGet,
  apiPost,
  apiPut,
} from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getStationsEndpoint(organizationId) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );

  return `/api/v1/organizations/${validOrganizationId}/stations`;
}

export function createStation(
  organizationId,
  payload,
  options = {}
) {
  return apiPost(
    getStationsEndpoint(organizationId),
    payload,
    options
  );
}

export function getStations(organizationId, options = {}) {
  return apiGet(getStationsEndpoint(organizationId), options);
}

export function getActiveStations(
  organizationId,
  options = {}
) {
  return apiGet(
    `${getStationsEndpoint(organizationId)}/active`,
    options
  );
}

export function getStationById(
  organizationId,
  stationId,
  options = {}
) {
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );

  return apiGet(
    `${getStationsEndpoint(organizationId)}/${validStationId}`,
    options
  );
}

export function validateStationConfiguration(
  organizationId,
  stationId,
  options = {}
) {
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );

  return apiGet(
    getStationsEndpoint(organizationId) + "/" + validStationId + "/configuration-validation",
    options
  );
}

export function updateStation(
  organizationId,
  stationId,
  payload,
  options = {}
) {
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );

  return apiPut(
    `${getStationsEndpoint(organizationId)}/${validStationId}`,
    payload,
    options
  );
}

export function deactivateStation(
  organizationId,
  stationId,
  options = {}
) {
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );

  return apiDelete(
    `${getStationsEndpoint(organizationId)}/${validStationId}`,
    options
  );
}
