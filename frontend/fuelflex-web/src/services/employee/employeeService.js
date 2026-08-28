import { apiGet, apiPost, apiPut } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

const EMPLOYEES_ENDPOINT = "/api/v1/employees";

function employeeEndpoint(employeeId) {
  return `${EMPLOYEES_ENDPOINT}/${validateRequiredId(employeeId, "L’identifiant de l’employé")}`;
}

function queryString(parameters) {
  const query = new URLSearchParams();
  Object.entries(parameters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") query.set(key, value);
  });
  const serialized = query.toString();
  return serialized ? `?${serialized}` : "";
}

export function getEmployees(parameters = {}, options = {}) {
  return apiGet(`${EMPLOYEES_ENDPOINT}${queryString(parameters)}`, options);
}

export function getEmployee(employeeId, options = {}) {
  return apiGet(employeeEndpoint(employeeId), options);
}

export function getAssignableEmployeeRoles(options = {}) {
  return apiGet(`${EMPLOYEES_ENDPOINT}/assignable-roles`, options);
}

export async function createEmployee(payload, options = {}) {
  if (payload?.roleCode === "PUMP_ATTENDANT") {
    const issued = await apiPost(`${EMPLOYEES_ENDPOINT}/pump-attendants`, payload, options);
    return { ...issued.employee, posCredential: issued.posCredential };
  }
  return apiPost(EMPLOYEES_ENDPOINT, payload, options);
}

export function updateEmployee(employeeId, payload, options = {}) {
  return apiPut(employeeEndpoint(employeeId), payload, options);
}

export function updateEmployeeStatus(employeeId, enabled, options = {}) {
  return apiPut(`${employeeEndpoint(employeeId)}/status`, { enabled }, options);
}

export function resendEmployeeInvitation(employeeId, options = {}) {
  return apiPost(`${employeeEndpoint(employeeId)}/resend-invitation`, undefined, options);
}
