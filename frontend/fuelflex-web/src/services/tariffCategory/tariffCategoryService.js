import { apiGet } from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getEndpoint(organizationId) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );
  return `/api/v1/organizations/${validOrganizationId}/tariff-categories`;
}

export function getTariffCategories(organizationId, options = {}) {
  return apiGet(getEndpoint(organizationId), options);
}

export function getActiveTariffCategories(
  organizationId,
  options = {}
) {
  return apiGet(`${getEndpoint(organizationId)}/active`, options);
}

export function getTariffCategoryById(
  organizationId,
  tariffCategoryId,
  options = {}
) {
  const validId = validateRequiredId(
    tariffCategoryId,
    "L’identifiant de la catégorie tarifaire"
  );
  return apiGet(`${getEndpoint(organizationId)}/${validId}`, options);
}
