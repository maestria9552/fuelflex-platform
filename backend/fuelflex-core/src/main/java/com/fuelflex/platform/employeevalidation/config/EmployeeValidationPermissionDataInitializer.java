package com.fuelflex.platform.employeevalidation.config;

import java.util.HashSet;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.permission.entity.Permission;
import com.fuelflex.platform.permission.repository.PermissionRepository;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(7)
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "fuelflex.data-initialization.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class EmployeeValidationPermissionDataInitializer
        implements CommandLineRunner {

    private final PermissionRepository permissions;
    private final RoleRepository roles;

    @Override
    @Transactional
    public void run(String... args) {
        ensure("pump-attendant:prepare",
                "Préparer les pompistes",
                "Créer et corriger les pompistes avant validation");
        ensure("pump-attendant-validation:view",
                "Consulter les validations pompistes",
                "Consulter les documents de validation des pompistes");
        ensure("pump-attendant-validation:create",
                "Créer une validation pompistes",
                "Regrouper les pompistes dans un document de validation");
        ensure("pump-attendant-validation:submit",
                "Soumettre une validation pompistes",
                "Soumettre et resoumettre un document de validation");
        ensure("pump-attendant-validation:review",
                "Décider une validation pompistes",
                "Valider, retourner ou rejeter un document de validation");

        assign("MANAGER",
                "pump-attendant:prepare",
                "pump-attendant-validation:view",
                "pump-attendant-validation:create",
                "pump-attendant-validation:submit");
        assign("SUPERVISOR",
                "pump-attendant-validation:view",
                "pump-attendant-validation:review");
        assign("SUPER_ADMIN",
                "pump-attendant:prepare",
                "pump-attendant-validation:view",
                "pump-attendant-validation:create",
                "pump-attendant-validation:submit",
                "pump-attendant-validation:review");
    }

    private void ensure(String code, String name, String description) {
        if (!permissions.existsByCodeIgnoreCase(code)) {
            permissions.save(new Permission(
                    code, name, description, "EMPLOYEE_VALIDATION"));
        }
    }

    private void assign(String roleCode, String... permissionCodes) {
        Role role = roles.findByCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new IllegalStateException(
                        "Role not found: " + roleCode));
        var existing = new HashSet<>(role.getPermissions());
        List.of(permissionCodes).stream()
                .map(code -> permissions.findByCodeIgnoreCase(code)
                        .orElseThrow(() -> new IllegalStateException(
                                "Permission not found: " + code)))
                .forEach(existing::add);
        role.setPermissions(existing);
        roles.save(role);
    }
}
