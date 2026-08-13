import {
  apiDelete,
  apiGet,
  apiPost,
  apiPut,
} from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getProductCategoriesEndpoint(organizationId) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );

  return `/api/v1/organizations/${validOrganizationId}/product-categories`;
}

export function createProductCategory(
  organizationId,
  payload,
  options = {}
) {
  return apiPost(
    getProductCategoriesEndpoint(organizationId),
    payload,
    options
  );
}

export function getProductCategories(
  organizationId,
  options = {}
) {
  return apiGet(
    getProductCategoriesEndpoint(organizationId),
    options
  );
}

export function getProductCategoryById(
  organizationId,
  categoryId,
  options = {}
) {
  const validCategoryId = validateRequiredId(
    categoryId,
    "L’identifiant de la catégorie"
  );

  return apiGet(
    `${getProductCategoriesEndpoint(organizationId)}/${validCategoryId}`,
    options
  );
}

export function updateProductCategory(
  organizationId,
  categoryId,
  payload,
  options = {}
) {
  const validCategoryId = validateRequiredId(
    categoryId,
    "L’identifiant de la catégorie"
  );

  return apiPut(
    `${getProductCategoriesEndpoint(organizationId)}/${validCategoryId}`,
    payload,
    options
  );
}

export function deactivateProductCategory(
  organizationId,
  categoryId,
  options = {}
) {
  const validCategoryId = validateRequiredId(
    categoryId,
    "L’identifiant de la catégorie"
  );

  return apiDelete(
    `${getProductCategoriesEndpoint(organizationId)}/${validCategoryId}`,
    options
  );
}
