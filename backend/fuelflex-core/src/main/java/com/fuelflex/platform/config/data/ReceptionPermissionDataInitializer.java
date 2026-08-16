package com.fuelflex.platform.config.data;

import java.util.HashSet;
import java.util.Set;

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
@Order(5)
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "fuelflex.data-initialization.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ReceptionPermissionDataInitializer implements CommandLineRunner {

    private final RoleRepository roles;
    private final PermissionRepository permissions;

    @Override
    @Transactional
    public void run(String... args) {
        assign(
                "MANAGER",
                "reception:view",
                "reception:create",
                "reception:update",
                "reception:submit"
        );

        assign(
                "SUPERVISOR",
                "reception:view",
                "reception:approve",
                "reception:return",
                "reception:cancel"
        );
    }

    private void assign(String roleCode, String... codes) {
        Role role = roles.findByCodeIgnoreCase(roleCode)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Rôle absent: " + roleCode
                        )
                );

        Set<Permission> all = new HashSet<>(role.getPermissions());

        for (String code : codes) {
            Permission permission = permissions.findByCodeIgnoreCase(code)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Permission absente: " + code
                            )
                    );

            all.add(permission);
        }

        role.setPermissions(all);
        roles.save(role);
    }
}
