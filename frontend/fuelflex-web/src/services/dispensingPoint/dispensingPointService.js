import {
  apiDelete,
  apiGet,
  apiPost,
  apiPut,
} from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getDispensingPointsEndpoint(
  organizationId,
  stationId,
  pumpId
) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );
  const validStationId = validateRequiredId(
    stationId,
    "L’identifiant de la station"
  );
  const validPumpId = validateRequiredId(
    pumpId,
    "L’identifiant de la pompe"
  );

  return `/api/v1/organizations/${validOrganizationId}/stations/${validStationId}/pumps/${validPumpId}/dispensing-points`;
}

function sanitizeDispensingPointPayload(payload) {
  const sanitizedPayload = { ...payload };

  delete sanitizedPayload.currentIndex;
  delete sanitizedPayload.meteringMode;
  delete sanitizedPayload.MeteringMode;

  return sanitizedPayload;
}

export function createDispensingPoint(
  organizationId,
  stationId,
  pumpId,
  payload,
  options = {}
) {
  return apiPost(
    getDispensingPointsEndpoint(
      organizationId,
      stationId,
      pumpId
    ),
    sanitizeDispensingPointPayload(payload),
    options
  );
}

export function getDispensingPoints(
  organizationId,
  stationId,
  pumpId,
  options = {}
) {
  return apiGet(
    getDispensingPointsEndpoint(
      organizationId,
      stationId,
      pumpId
    ),
    options
  );
}

export function getActiveDispensingPoints(
  organizationId,
  stationId,
  pumpId,
  options = {}
) {
  return apiGet(
    `${getDispensingPointsEndpoint(organizationId, stationId, pumpId)}/active`,
    options
  );
}

export function getDispensingPointById(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  options = {}
) {
  const validDispensingPointId = validateRequiredId(
    dispensingPointId,
    "L’identifiant du point de distribution"
  );

  return apiGet(
    `${getDispensingPointsEndpoint(organizationId, stationId, pumpId)}/${validDispensingPointId}`,
    options
  );
}

export function updateDispensingPoint(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  payload,
  options = {}
) {
  const validDispensingPointId = validateRequiredId(
    dispensingPointId,
    "L’identifiant du point de distribution"
  );

  return apiPut(
    `${getDispensingPointsEndpoint(organizationId, stationId, pumpId)}/${validDispensingPointId}`,
    sanitizeDispensingPointPayload(payload),
    options
  );
}

export function deactivateDispensingPoint(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  options = {}
) {
  const validDispensingPointId = validateRequiredId(
    dispensingPointId,
    "L’identifiant du point de distribution"
  );

  return apiDelete(
    `${getDispensingPointsEndpoint(organizationId, stationId, pumpId)}/${validDispensingPointId}`,
    options
  );
}
