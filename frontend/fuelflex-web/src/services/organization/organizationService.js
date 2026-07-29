import {
  apiGet,
  apiPatch,
  apiPost,
  apiPut,
} from "../api/apiClient";

const ORGANIZATIONS_ENDPOINT =
  "/api/v1/organizations";

function validateOrganizationId(organizationId) {
  if (!organizationId) {
    throw new Error(
      "L’identifiant de l’organisation est obligatoire."
    );
  }

  return organizationId;
}

export function createOrganization(payload, options = {}) {
  return apiPost(
    ORGANIZATIONS_ENDPOINT,
    payload,
    options
  );
}

export function getOrganizations(options = {}) {
  return apiGet(
    ORGANIZATIONS_ENDPOINT,
    options
  );
}

export function getOrganizationById(
  organizationId,
  options = {}
) {
  const validOrganizationId =
    validateOrganizationId(organizationId);

  return apiGet(
    `${ORGANIZATIONS_ENDPOINT}/${validOrganizationId}`,
    options
  );
}

export function updateOrganization(
  organizationId,
  payload,
  options = {}
) {
  const validOrganizationId =
    validateOrganizationId(organizationId);

  return apiPut(
    `${ORGANIZATIONS_ENDPOINT}/${validOrganizationId}`,
    payload,
    options
  );
}

export function uploadOrganizationLogo(
  organizationId,
  file,
  options = {}
) {
  const validOrganizationId =
    validateOrganizationId(organizationId);

  if (!(file instanceof File)) {
    throw new Error(
      "Le fichier du logo est obligatoire."
    );
  }

  const formData = new FormData();

  formData.append("file", file);

  return apiPost(
    `${ORGANIZATIONS_ENDPOINT}/${validOrganizationId}/logo`,
    formData,
    options
  );
}

export function activateOrganization(
  organizationId,
  options = {}
) {
  const validOrganizationId =
    validateOrganizationId(organizationId);

  return apiPatch(
    `${ORGANIZATIONS_ENDPOINT}/${validOrganizationId}/activate`,
    null,
    options
  );
}

export function suspendOrganization(
  organizationId,
  options = {}
) {
  const validOrganizationId =
    validateOrganizationId(organizationId);

  return apiPatch(
    `${ORGANIZATIONS_ENDPOINT}/${validOrganizationId}/suspend`,
    null,
    options
  );
}