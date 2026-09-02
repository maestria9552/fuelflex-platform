import { apiGet } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

export function getManagerDashboard(stationId, options = {}) {
  const id = validateRequiredId(stationId, "L’identifiant de la station");
  return apiGet(`/api/v1/manager/dashboard?stationId=${encodeURIComponent(id)}`, options);
}
