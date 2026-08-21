import { getStoredUser } from "./authStorage";

function normalizePermission(permission) {
  if (typeof permission === "string") return permission.toLowerCase();
  return String(permission?.code || permission?.authority || "").toLowerCase();
}

export function hasPermission(code) {
  const permissions = getStoredUser()?.permissions;
  if (!Array.isArray(permissions)) return false;
  return permissions.map(normalizePermission).includes(String(code).toLowerCase());
}

export function getReceptionPermissions() {
  return {
    canView: hasPermission("reception:view"),
    canCreate: hasPermission("reception:create"),
    canUpdate: hasPermission("reception:update"),
    canSubmit: hasPermission("reception:submit"),
    canApprove: hasPermission("reception:approve"),
    canReturn: hasPermission("reception:return"),
    canCancel: hasPermission("reception:cancel"),
  };
}
