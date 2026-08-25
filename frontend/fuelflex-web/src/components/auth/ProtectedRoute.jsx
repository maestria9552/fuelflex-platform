import { useTranslation } from "react-i18next";
import { Navigate, useLocation } from "react-router-dom";

function getStoredSession() {
  const accessToken =
    localStorage.getItem("fuelflex_access_token") ||
    sessionStorage.getItem("fuelflex_access_token");

  const storedUser =
    localStorage.getItem("fuelflex_user") ||
    sessionStorage.getItem("fuelflex_user");

  if (!accessToken || !storedUser) {
    return null;
  }

  try {
    return {
      accessToken,
      user: JSON.parse(storedUser),
    };
  } catch {
    localStorage.removeItem("fuelflex_access_token");
    localStorage.removeItem("fuelflex_user");
    sessionStorage.removeItem("fuelflex_access_token");
    sessionStorage.removeItem("fuelflex_user");

    return null;
  }
}

function normalizeRoles(roles) {
  if (!Array.isArray(roles)) {
    return [];
  }

  return roles.map((role) => {
    if (typeof role === "string") {
      return role.toUpperCase();
    }

    if (role && typeof role === "object") {
      return String(
        role.name ||
        role.authority ||
        role.role ||
        ""
      ).toUpperCase();
    }

    return "";
  });
}

function normalizePermissions(permissions) {
  if (!Array.isArray(permissions)) return [];

  return permissions.map((permission) => {
    if (typeof permission === "string") return permission.toLowerCase();
    return String(permission?.code || permission?.authority || "").toLowerCase();
  });
}

function ProtectedRoute({
  children,
  allowedRoles = [],
  requiredPermissions = [],
}) {
  const { t } = useTranslation("auth");
  const location = useLocation();
  const session = getStoredSession();

  if (!session) {
    return (
      <Navigate
        to="/connexion"
        replace
        state={{
          from: location.pathname,
          message: t("protectedRoute.loginRequired"),
        }}
      />
    );
  }

  const userRoles = normalizeRoles(session.user.roles);
  const normalizedAllowedRoles = allowedRoles.map((role) =>
    role.toUpperCase()
  );

  const hasRequiredRole =
    normalizedAllowedRoles.length === 0 ||
    normalizedAllowedRoles.some((role) =>
      userRoles.includes(role)
    );

  if (!hasRequiredRole) {
    return (
      <Navigate
        to="/acces-refuse"
        replace
      />
    );
  }

  const userPermissions = normalizePermissions(session.user.permissions);
  const hasRequiredPermissions = requiredPermissions.every((permission) =>
    userPermissions.includes(String(permission).toLowerCase())
  );

  if (!hasRequiredPermissions) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

export default ProtectedRoute;
