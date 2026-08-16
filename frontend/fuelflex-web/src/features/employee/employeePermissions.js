import { getStoredUser } from "../../services/auth/authStorage";

export function getEmployeePermissions() {
  const permissions = new Set(getStoredUser()?.permissions || []);
  return {
    canView: permissions.has("user:view"),
    canCreate: permissions.has("user:create"),
    canUpdate: permissions.has("user:update"),
    canDisable: permissions.has("user:disable"),
    canViewAssignments: permissions.has("assignment:view"),
    canCreateAssignment: permissions.has("assignment:create"),
    canEndAssignment: permissions.has("assignment:end"),
    canTransfer: permissions.has("assignment:transfer"),
  };
}
