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

export function getOperationalPermissions() {
  return {
    canViewDays: hasPermission("operational-day:view"),
    canOpenDay: hasPermission("operational-day:open"),
    canCloseDay: hasPermission("operational-day:close"),
    canViewAssignments: hasPermission("shift-assignment:view"),
    canCreateAssignment: hasPermission("shift-assignment:create"),
    canCloseAssignment: hasPermission("shift-assignment:close"),
    canViewExpenses: hasPermission("daily-expense:view"),
    canCreateExpense: hasPermission("daily-expense:create"),
    canViewGauges: hasPermission("tank-gauge:view"),
    canCreateGauge: hasPermission("tank-gauge:create"),
    canViewReconciliations: hasPermission("reconciliation:view"),
    canViewRjv: hasPermission("rjv:view"),
  };
}

export function getPumpAttendantValidationPermissions() {
  return {
    canPrepare: hasPermission("pump-attendant:prepare"),
    canView: hasPermission("pump-attendant-validation:view"),
    canCreateRequest: hasPermission("pump-attendant-validation:create"),
    canSubmit: hasPermission("pump-attendant-validation:submit"),
    canReview: hasPermission("pump-attendant-validation:review"),
  };
}

export function getSalePermissions() {
  return {
    canView: hasPermission("pos-sale:view"),
    canReverse: hasPermission("pos-sale:reverse"),
  };
}
