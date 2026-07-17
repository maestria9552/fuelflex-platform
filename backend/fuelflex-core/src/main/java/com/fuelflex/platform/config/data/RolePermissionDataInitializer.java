package com.fuelflex.platform.config.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.permission.entity.Permission;
import com.fuelflex.platform.permission.repository.PermissionRepository;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;

@Component
@Order(3)
public class RolePermissionDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionDataInitializer(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        assignAllPermissionsToSuperAdmin();

        assignPermissions(
                "SUPERVISOR",
                "organization:view",
                "organization:update",

                "user:create",
                "user:view",
                "user:update",
                "user:disable",

                "role:create",
                "role:view",
                "role:update",

                "station:create",
                "station:view",
                "station:update",

                "supplier:create",
                "supplier:view",
                "supplier:update",

                "sale:view",
                "report:view"
        );

        assignPermissions(
                "MANAGER",
                "organization:view",

                "user:view",

                "role:view",

                "station:view",
                "station:update",

                "supplier:create",
                "supplier:view",
                "supplier:update",

                "sale:create",
                "sale:view",

                "report:view"
        );

        assignPermissions(
                "PUMP_ATTENDANT",
                "station:view",
                "sale:create",
                "sale:view"
        );

        assignPermissions(
                "ACCOUNTANT",
                "organization:view",
                "station:view",
                "supplier:view",
                "sale:view",
                "report:view"
        );

        assignPermissions(
                "AUDITOR",
                "organization:view",
                "user:view",
                "role:view",
                "station:view",
                "supplier:view",
                "sale:view",
                "report:view"
        );

        assignPermissions(
                "SUPPLIER_USER",
                "supplier:view"
        );

        assignPermissions(
                "CREDIT_CUSTOMER_USER",
                "station:view",
                "sale:view"
        );
    }

    private void assignAllPermissionsToSuperAdmin() {

        Role superAdmin = roleRepository
                .findByCodeIgnoreCase("SUPER_ADMIN")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Le rôle SUPER_ADMIN est introuvable"
                        )
                );

        List<Permission> allPermissions =
                permissionRepository.findByActiveTrue();

        superAdmin.setPermissions(new HashSet<>(allPermissions));

        roleRepository.save(superAdmin);
    }

    private void assignPermissions(
            String roleCode,
            String... permissionCodes
    ) {

        Role role = roleRepository
                .findByCodeIgnoreCase(roleCode)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Le rôle " + roleCode + " est introuvable"
                        )
                );

        Set<Permission> permissions = new HashSet<>();

        Arrays.stream(permissionCodes)
                .forEach(permissionCode -> {

                    Permission permission = permissionRepository
                            .findByCodeIgnoreCase(permissionCode)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "La permission "
                                                    + permissionCode
                                                    + " est introuvable"
                                    )
                            );

                    permissions.add(permission);
                });

        role.setPermissions(permissions);

        roleRepository.save(role);
    }
}
