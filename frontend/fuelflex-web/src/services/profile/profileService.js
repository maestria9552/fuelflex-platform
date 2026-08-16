import { apiGet, apiPut } from "../api/apiClient";

const MY_PROFILE_ENDPOINT = "/api/v1/users/me";

export function getMyProfile(options = {}) {
  return apiGet(MY_PROFILE_ENDPOINT, options);
}

export function updateMyProfile(payload, options = {}) {
  return apiPut(MY_PROFILE_ENDPOINT, payload, options);
}
