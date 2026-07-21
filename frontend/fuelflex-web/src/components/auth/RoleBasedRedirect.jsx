import { Navigate } from "react-router-dom";

function getStoredUser() {
  const storedUser =
    localStorage.getItem("fuelflex_user") ||
    sessionStorage.getItem("fuelflex_user");

  if (!storedUser) {
    return null;
  }

  try {
    return JSON.parse(storedUser);
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
        role.code ||
          role.name ||
          role.authority ||
          role.role ||
          ""
      ).toUpperCase();
    }

    return "";
  });
}

function RoleBasedRedirect() {
  const user = getStoredUser();

  if (!user) {
    return <Navigate to="/connexion" replace />;
  }

  const roles = normalizeRoles(user.roles);

  if (roles.includes("SUPER_ADMIN")) {
    return (
      <Navigate
        to="/super-admin/dashboard"
        replace
      />
    );
  }

  if (roles.includes("SUPERVISOR")) {
    return (
      <Navigate
        to="/superviseur/dashboard"
        replace
      />
    );
  }

  if (roles.includes("MANAGER")) {
    return (
      <Navigate
        to="/gerant/dashboard"
        replace
      />
    );
  }

  if (roles.includes("PUMP_ATTENDANT")) {
    return (
      <Navigate
        to="/pompiste/dashboard"
        replace
      />
    );
  }

  if (roles.includes("ACCOUNTANT")) {
    return (
      <Navigate
        to="/comptable/dashboard"
        replace
      />
    );
  }

  if (roles.includes("AUDITOR")) {
    return (
      <Navigate
        to="/auditeur/dashboard"
        replace
      />
    );
  }

  if (roles.includes("SUPPLIER_USER")) {
    return (
      <Navigate
        to="/fournisseur/dashboard"
        replace
      />
    );
  }

  if (roles.includes("CREDIT_CUSTOMER_USER")) {
    return (
      <Navigate
        to="/client-partenaire/dashboard"
        replace
      />
    );
  }

  return <Navigate to="/acces-refuse" replace />;
}

export default RoleBasedRedirect;