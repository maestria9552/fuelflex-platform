import { apiGet, apiPut } from "../api/apiClient";

const MY_ACCOUNT_ENDPOINT = "/api/v1/users/me/account";
const MY_PASSWORD_ENDPOINT = "/api/v1/users/me/password";

export function getMyAccount(options = {}) {
  return apiGet(MY_ACCOUNT_ENDPOINT, options);
}

export function changeMyPassword(payload, options = {}) {
  return apiPut(MY_PASSWORD_ENDPOINT, payload, options);
}
