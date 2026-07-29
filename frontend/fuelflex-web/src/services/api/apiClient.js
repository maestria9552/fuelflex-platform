import {
  clearAuthSession,
  getAccessToken,
} from "../auth/authStorage";

class ApiError extends Error {
  constructor(
    message,
    {
      status = 0,
      data = null,
      url = "",
      method = "GET",
    } = {}
  ) {
    super(message);

    this.name = "ApiError";
    this.status = status;
    this.data = data;
    this.url = url;
    this.method = method;
  }
}

async function parseResponse(response) {
  if (response.status === 204) {
    return null;
  }

  const contentType =
    response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    try {
      return await response.json();
    } catch {
      return null;
    }
  }

  const text = await response.text();

  return text || null;
}

function getErrorMessage(data, status) {
  if (typeof data === "string" && data.trim()) {
    return data.trim();
  }

  if (data && typeof data === "object") {
    return (
      data.message ||
      data.error ||
      data.details ||
      `Une erreur HTTP ${status} est survenue.`
    );
  }

  if (status === 401) {
    return "Votre session a expiré. Veuillez vous reconnecter.";
  }

  if (status === 403) {
    return "Vous ne disposez pas des autorisations nécessaires.";
  }

  if (status === 404) {
    return "La ressource demandée est introuvable.";
  }

  if (status >= 500) {
    return "Le serveur rencontre actuellement un problème.";
  }

  return `Une erreur HTTP ${status} est survenue.`;
}

export async function apiRequest(
  endpoint,
  {
    method = "GET",
    body,
    headers = {},
    authenticated = true,
    signal,
  } = {}
) {
  const requestHeaders = new Headers(headers);

  if (
    body !== undefined &&
    body !== null &&
    !(body instanceof FormData) &&
    !requestHeaders.has("Content-Type")
  ) {
    requestHeaders.set(
      "Content-Type",
      "application/json"
    );
  }

  if (authenticated) {
    const accessToken = getAccessToken();

    if (!accessToken) {
      clearAuthSession();

      throw new ApiError(
        "Aucune session authentifiée n’est disponible.",
        {
          status: 401,
          url: endpoint,
          method,
        }
      );
    }

    requestHeaders.set(
      "Authorization",
      `Bearer ${accessToken}`
    );
  }

  let response;

  try {
    response = await fetch(endpoint, {
      method,
      headers: requestHeaders,
      body:
        body === undefined || body === null
          ? undefined
          : body instanceof FormData
            ? body
            : JSON.stringify(body),
      signal,
    });
  } catch (error) {
    if (error?.name === "AbortError") {
      throw error;
    }

    throw new ApiError(
      "Impossible de contacter le serveur.",
      {
        status: 0,
        data: error,
        url: endpoint,
        method,
      }
    );
  }

  const data = await parseResponse(response);

  if (!response.ok) {
    if (response.status === 401) {
      clearAuthSession();
    }

    throw new ApiError(
      getErrorMessage(data, response.status),
      {
        status: response.status,
        data,
        url: endpoint,
        method,
      }
    );
  }

  return data;
}

export function apiGet(
  endpoint,
  options = {}
) {
  return apiRequest(endpoint, {
    ...options,
    method: "GET",
  });
}

export function apiPost(
  endpoint,
  body,
  options = {}
) {
  return apiRequest(endpoint, {
    ...options,
    method: "POST",
    body,
  });
}

export function apiPut(
  endpoint,
  body,
  options = {}
) {
  return apiRequest(endpoint, {
    ...options,
    method: "PUT",
    body,
  });
}

export function apiPatch(
  endpoint,
  body,
  options = {}
) {
  return apiRequest(endpoint, {
    ...options,
    method: "PATCH",
    body,
  });
}

export function apiDelete(
  endpoint,
  options = {}
) {
  return apiRequest(endpoint, {
    ...options,
    method: "DELETE",
  });
}

export { ApiError };