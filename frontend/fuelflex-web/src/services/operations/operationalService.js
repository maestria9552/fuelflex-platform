import { apiGet, apiPost } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

const managerBase = "/api/v1/manager";

function operationalDayId(value) {
  return validateRequiredId(value, "L’identifiant de la journée opérationnelle");
}

function assignmentId(value) {
  return validateRequiredId(value, "L’identifiant de l’affectation");
}

function roleBase(role) {
  if (role !== "manager" && role !== "supervisor") {
    throw new Error("Le rôle opérationnel demandé n’est pas pris en charge.");
  }
  return `/api/v1/${role}`;
}

export function getManagerStations(options = {}) {
  return apiGet(`${managerBase}/stations`, options);
}

export function getOperationalDays(options = {}) {
  return apiGet(`${managerBase}/operational-days`, options);
}

export function getOperationalDay(id, options = {}) {
  return apiGet(`${managerBase}/operational-days/${operationalDayId(id)}`, options);
}

export function openOperationalDay(payload, options = {}) {
  return apiPost(`${managerBase}/operational-days`, payload, options);
}

export function getEligiblePumpAttendants(stationId, options = {}) {
  const validStationId = validateRequiredId(stationId, "L’identifiant de la station");
  return apiGet(`${managerBase}/stations/${validStationId}/eligible-pump-attendants`, options);
}

export function getAvailableFuelMeters(id, options = {}) {
  return apiGet(`${managerBase}/operational-days/${operationalDayId(id)}/available-fuel-meters`, options);
}

export function getShiftAssignments(id, options = {}) {
  return apiGet(`${managerBase}/operational-days/${operationalDayId(id)}/assignments`, options);
}

export function createShiftAssignment(id, payload, options = {}) {
  return apiPost(`${managerBase}/operational-days/${operationalDayId(id)}/assignments`, payload, options);
}

export function closeShiftAssignment(id, payload, options = {}) {
  return apiPost(
    `${managerBase}/shift-assignments/${assignmentId(id)}/close`,
    payload,
    options
  );
}

export function closeOperationalDay(id, payload, options = {}) {
  return apiPost(`${managerBase}/operational-days/${operationalDayId(id)}/close`, payload, options);
}

export function getDailyExpenses(role, id, options = {}) {
  return apiGet(`${roleBase(role)}/operational-days/${operationalDayId(id)}/expenses`, options);
}

export function createDailyExpense(id, payload, options = {}) {
  return apiPost(`${managerBase}/operational-days/${operationalDayId(id)}/expenses`, payload, options);
}

export function getTankGaugeReadings(role, id, options = {}) {
  return apiGet(`${roleBase(role)}/operational-days/${operationalDayId(id)}/gauges`, options);
}

export function createTankGaugeReading(id, payload, options = {}) {
  return apiPost(`${managerBase}/operational-days/${operationalDayId(id)}/gauges`, payload, options);
}

export function getShiftReconciliations(role, id, options = {}) {
  return apiGet(`${roleBase(role)}/operational-days/${operationalDayId(id)}/reconciliations`, options);
}

export function getOperationalDayRjv(role, id, options = {}) {
  return apiGet(`${roleBase(role)}/operational-days/${operationalDayId(id)}/rjv`, options);
}

export function getTankReturns(role, id, options = {}) {
  return apiGet(`${roleBase(role)}/operational-days/${operationalDayId(id)}/tank-returns`, options);
}

export function createTankReturn(id, payload, options = {}) {
  return apiPost(`${managerBase}/shift-assignments/${assignmentId(id)}/tank-returns`, payload, options);
}

export function getInternalConsumptions(role, id, options = {}) {
  return apiGet(`${roleBase(role)}/shift-assignments/${assignmentId(id)}/internal-consumptions`, options);
}

export function createInternalConsumption(id, payload, options = {}) {
  return apiPost(`${managerBase}/shift-assignments/${assignmentId(id)}/internal-consumptions`, payload, options);
}

// Ces read-models communs restent sous /manager côté backend, où MANAGER et
// SUPERVISOR sont explicitement autorisés. Aucun endpoint fictif n'est créé.
export function getOperationalDaysForRole(role, options = {}) {
  roleBase(role);
  return getOperationalDays(options);
}

export function getOperationalDayForRole(role, id, options = {}) {
  roleBase(role);
  return getOperationalDay(id, options);
}

export function getShiftAssignmentsForRole(role, id, options = {}) {
  roleBase(role);
  return getShiftAssignments(id, options);
}
