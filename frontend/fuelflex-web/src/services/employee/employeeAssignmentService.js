import { apiGet, apiPost, apiPut } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function employeeEndpoint(employeeId) {
  return `/api/v1/employees/${validateRequiredId(employeeId, "L’identifiant de l’employé")}`;
}

export function getEmployeeAssignments(employeeId, parameters = {}, options = {}) {
  const query = new URLSearchParams({
    status: parameters.status || "ALL",
    page: String(parameters.page ?? 0),
    size: String(parameters.size ?? 20),
  });
  return apiGet(`${employeeEndpoint(employeeId)}/assignments?${query}`, options);
}

export function createEmployeeAssignment(employeeId, payload, options = {}) {
  return apiPost(`${employeeEndpoint(employeeId)}/assignments`, payload, options);
}

export function endEmployeeAssignment(employeeId, assignmentId, payload, options = {}) {
  const validAssignmentId = validateRequiredId(assignmentId, "L’identifiant de l’affectation");
  return apiPut(`${employeeEndpoint(employeeId)}/assignments/${validAssignmentId}/end`, payload, options);
}

export function transferEmployee(employeeId, payload, options = {}) {
  return apiPost(`${employeeEndpoint(employeeId)}/transfers`, payload, options);
}
