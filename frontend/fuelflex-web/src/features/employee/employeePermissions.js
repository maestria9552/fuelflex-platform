import { hasPermission } from "../../services/auth/permissionService";

export function getEmployeePermissions() {
  return {
    canView: hasPermission("user:view"),
    canCreate: hasPermission("user:create"),
    canUpdate: hasPermission("user:update"),
    canDisable: hasPermission("user:disable"),
    canViewAssignments: hasPermission("assignment:view"),
    canCreateAssignment: hasPermission("assignment:create"),
    canEndAssignment: hasPermission("assignment:end"),
    canTransfer: hasPermission("assignment:transfer"),
  };
}
