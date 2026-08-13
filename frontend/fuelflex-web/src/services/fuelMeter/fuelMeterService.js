import {
  apiDelete,
  apiGet,
  apiPost,
  apiPut,
} from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getPumpFuelMetersEndpoint(
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

  return `/api/v1/organizations/${validOrganizationId}/stations/${validStationId}/pumps/${validPumpId}/fuel-meters`;
}

function getDispensingPointFuelMetersEndpoint(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId
) {
  const validDispensingPointId = validateRequiredId(
    dispensingPointId,
    "L’identifiant du point de distribution"
  );

  return `${getPumpFuelMetersEndpoint(organizationId, stationId, pumpId).replace(
    "/fuel-meters",
    ""
  )}/dispensing-points/${validDispensingPointId}/fuel-meters`;
}

function getFuelMeterResourceEndpoint(endpoint, fuelMeterId) {
  const validFuelMeterId = validateRequiredId(
    fuelMeterId,
    "L’identifiant du compteur"
  );

  return `${endpoint}/${validFuelMeterId}`;
}

export function createPumpFuelMeter(
  organizationId,
  stationId,
  pumpId,
  payload,
  options = {}
) {
  return apiPost(
    getPumpFuelMetersEndpoint(organizationId, stationId, pumpId),
    payload,
    options
  );
}

export function getPumpFuelMeters(
  organizationId,
  stationId,
  pumpId,
  options = {}
) {
  return apiGet(
    getPumpFuelMetersEndpoint(organizationId, stationId, pumpId),
    options
  );
}

export function getActivePumpFuelMeters(
  organizationId,
  stationId,
  pumpId,
  options = {}
) {
  return apiGet(
    `${getPumpFuelMetersEndpoint(organizationId, stationId, pumpId)}/active`,
    options
  );
}

export function getPumpFuelMeterById(
  organizationId,
  stationId,
  pumpId,
  fuelMeterId,
  options = {}
) {
  return apiGet(
    getFuelMeterResourceEndpoint(
      getPumpFuelMetersEndpoint(organizationId, stationId, pumpId),
      fuelMeterId
    ),
    options
  );
}

export function updatePumpFuelMeter(
  organizationId,
  stationId,
  pumpId,
  fuelMeterId,
  payload,
  options = {}
) {
  return apiPut(
    getFuelMeterResourceEndpoint(
      getPumpFuelMetersEndpoint(organizationId, stationId, pumpId),
      fuelMeterId
    ),
    payload,
    options
  );
}

export function deactivatePumpFuelMeter(
  organizationId,
  stationId,
  pumpId,
  fuelMeterId,
  options = {}
) {
  return apiDelete(
    getFuelMeterResourceEndpoint(
      getPumpFuelMetersEndpoint(organizationId, stationId, pumpId),
      fuelMeterId
    ),
    options
  );
}

export function createDispensingPointFuelMeter(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  payload,
  options = {}
) {
  return apiPost(
    getDispensingPointFuelMetersEndpoint(
      organizationId,
      stationId,
      pumpId,
      dispensingPointId
    ),
    payload,
    options
  );
}

export function getDispensingPointFuelMeters(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  options = {}
) {
  return apiGet(
    getDispensingPointFuelMetersEndpoint(
      organizationId,
      stationId,
      pumpId,
      dispensingPointId
    ),
    options
  );
}

export function getActiveDispensingPointFuelMeters(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  options = {}
) {
  return apiGet(
    `${getDispensingPointFuelMetersEndpoint(
      organizationId,
      stationId,
      pumpId,
      dispensingPointId
    )}/active`,
    options
  );
}

export function getDispensingPointFuelMeterById(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  fuelMeterId,
  options = {}
) {
  return apiGet(
    getFuelMeterResourceEndpoint(
      getDispensingPointFuelMetersEndpoint(
        organizationId,
        stationId,
        pumpId,
        dispensingPointId
      ),
      fuelMeterId
    ),
    options
  );
}

export function updateDispensingPointFuelMeter(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  fuelMeterId,
  payload,
  options = {}
) {
  return apiPut(
    getFuelMeterResourceEndpoint(
      getDispensingPointFuelMetersEndpoint(
        organizationId,
        stationId,
        pumpId,
        dispensingPointId
      ),
      fuelMeterId
    ),
    payload,
    options
  );
}

export function deactivateDispensingPointFuelMeter(
  organizationId,
  stationId,
  pumpId,
  dispensingPointId,
  fuelMeterId,
  options = {}
) {
  return apiDelete(
    getFuelMeterResourceEndpoint(
      getDispensingPointFuelMetersEndpoint(
        organizationId,
        stationId,
        pumpId,
        dispensingPointId
      ),
      fuelMeterId
    ),
    options
  );
}
