import { apiGet, apiPost, apiPut } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

const MANAGER_BASE = "/api/v1/manager";
const SUPERVISOR_BASE = "/api/v1/supervisor";

function queryString(parameters = {}) {
  const query = new URLSearchParams();
  Object.entries(parameters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, String(value));
    }
  });
  const serialized = query.toString();
  return serialized ? `?${serialized}` : "";
}

function pumpAttendantId(value) {
  return validateRequiredId(value, "L’identifiant du pompiste");
}

function validationRequestId(value) {
  return validateRequiredId(value, "L’identifiant de la demande de validation");
}

function requestBase(role) {
  if (role !== "manager" && role !== "supervisor") {
    throw new Error("Le rôle de validation demandé n’est pas pris en charge.");
  }
  const base = role === "manager" ? MANAGER_BASE : SUPERVISOR_BASE;
  return `${base}/pump-attendant-validation-requests`;
}

export function getManagerPumpAttendants(parameters = {}, options = {}) {
  return apiGet(
    `${MANAGER_BASE}/pump-attendants${queryString(parameters)}`,
    options,
  );
}

export function getManagerPumpAttendant(id, options = {}) {
  return apiGet(
    `${MANAGER_BASE}/pump-attendants/${pumpAttendantId(id)}`,
    options,
  );
}

export function createManagerPumpAttendant(payload, options = {}) {
  return apiPost(`${MANAGER_BASE}/pump-attendants`, payload, options);
}

export function updateManagerPumpAttendant(id, payload, options = {}) {
  return apiPut(
    `${MANAGER_BASE}/pump-attendants/${pumpAttendantId(id)}`,
    payload,
    options,
  );
}

export function getPumpAttendantValidationRequests(
  role,
  parameters = {},
  options = {},
) {
  return apiGet(`${requestBase(role)}${queryString(parameters)}`, options);
}

export function getPumpAttendantValidationRequest(role, id, options = {}) {
  return apiGet(`${requestBase(role)}/${validationRequestId(id)}`, options);
}

export function createPumpAttendantValidationRequest(payload, options = {}) {
  return apiPost(
    `${requestBase("manager")}`,
    payload,
    options,
  );
}

export function submitPumpAttendantValidationRequest(id, options = {}) {
  return apiPost(
    `${requestBase("manager")}/${validationRequestId(id)}/submit`,
    undefined,
    options,
  );
}

export function cancelPumpAttendantValidationRequest(
  id,
  comment,
  options = {},
) {
  return apiPost(
    `${requestBase("manager")}/${validationRequestId(id)}/cancel`,
    { comment },
    options,
  );
}

export function approvePumpAttendantValidationRequest(
  id,
  comment = null,
  options = {},
) {
  return apiPost(
    `${requestBase("supervisor")}/${validationRequestId(id)}/approve`,
    comment ? { comment } : undefined,
    options,
  );
}

export function returnPumpAttendantValidationRequest(
  id,
  comment,
  options = {},
) {
  return apiPost(
    `${requestBase("supervisor")}/${validationRequestId(id)}/return`,
    { comment },
    options,
  );
}

export function rejectPumpAttendantValidationRequest(
  id,
  comment,
  options = {},
) {
  return apiPost(
    `${requestBase("supervisor")}/${validationRequestId(id)}/reject`,
    { comment },
    options,
  );
}
