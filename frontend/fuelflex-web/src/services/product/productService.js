import {
  apiDelete,
  apiGet,
  apiPost,
  apiPut,
} from "../api/apiClient";
import { validateRequiredId } from "../api/validateRequiredId";

function getProductsEndpoint(organizationId) {
  const validOrganizationId = validateRequiredId(
    organizationId,
    "L’identifiant de l’organisation"
  );

  return `/api/v1/organizations/${validOrganizationId}/products`;
}

export function createProduct(
  organizationId,
  payload,
  options = {}
) {
  return apiPost(
    getProductsEndpoint(organizationId),
    payload,
    options
  );
}

export function getProducts(organizationId, options = {}) {
  return apiGet(getProductsEndpoint(organizationId), options);
}

export function getActiveProducts(
  organizationId,
  options = {}
) {
  return apiGet(
    `${getProductsEndpoint(organizationId)}/active`,
    options
  );
}

export async function hasActiveProducts(
  organizationId,
  options = {}
) {
  const products = await getActiveProducts(
    organizationId,
    options
  );

  return Array.isArray(products) && products.length > 0;
}

export function getProductById(
  organizationId,
  productId,
  options = {}
) {
  const validProductId = validateRequiredId(
    productId,
    "L’identifiant du produit"
  );

  return apiGet(
    `${getProductsEndpoint(organizationId)}/${validProductId}`,
    options
  );
}

export function updateProduct(
  organizationId,
  productId,
  payload,
  options = {}
) {
  const validProductId = validateRequiredId(
    productId,
    "L’identifiant du produit"
  );

  return apiPut(
    `${getProductsEndpoint(organizationId)}/${validProductId}`,
    payload,
    options
  );
}

export function deactivateProduct(
  organizationId,
  productId,
  options = {}
) {
  const validProductId = validateRequiredId(
    productId,
    "L’identifiant du produit"
  );

  return apiDelete(
    `${getProductsEndpoint(organizationId)}/${validProductId}`,
    options
  );
}
