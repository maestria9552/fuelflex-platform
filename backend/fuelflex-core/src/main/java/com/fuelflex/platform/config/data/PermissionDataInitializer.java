package com.fuelflex.platform.config.data;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;
import com.fuelflex.platform.permission.entity.Permission;
import com.fuelflex.platform.permission.repository.PermissionRepository;

@Component
@Order(2)
public class PermissionDataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    public PermissionDataInitializer(
            PermissionRepository permissionRepository
    ) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        List<Permission> permissions = List.of(

                new Permission(
                        "user:create",
                        "Créer un utilisateur",
                        "Permet de créer un utilisateur",
                        "USER"
                ),
                new Permission(
                        "user:view",
                        "Consulter les utilisateurs",
                        "Permet de consulter les utilisateurs",
                        "USER"
                ),
                new Permission(
                        "user:update",
                        "Modifier un utilisateur",
                        "Permet de modifier un utilisateur",
                        "USER"
                ),
                new Permission(
                        "user:disable",
                        "Désactiver un utilisateur",
                        "Permet de désactiver un utilisateur",
                        "USER"
                ),

                new Permission(
                        "role:create",
                        "Créer un rôle",
                        "Permet de créer un rôle personnalisé",
                        "ROLE"
                ),
                new Permission(
                        "role:view",
                        "Consulter les rôles",
                        "Permet de consulter les rôles",
                        "ROLE"
                ),
                new Permission(
                        "role:update",
                        "Modifier un rôle",
                        "Permet de modifier un rôle personnalisable",
                        "ROLE"
                ),

                new Permission(
                        "station:create",
                        "Créer une station",
                        "Permet de créer une station-service",
                        "STATION"
                ),
                new Permission(
                        "station:view",
                        "Consulter les stations",
                        "Permet de consulter les stations-service",
                        "STATION"
                ),
                new Permission(
                        "station:update",
                        "Modifier une station",
                        "Permet de modifier une station-service",
                        "STATION"
                ),

                new Permission(
                        "supplier:create",
                        "Créer un fournisseur",
                        "Permet d’enregistrer un fournisseur",
                        "SUPPLIER"
                ),
                new Permission(
                        "supplier:view",
                        "Consulter les fournisseurs",
                        "Permet de consulter les fournisseurs",
                        "SUPPLIER"
                ),
                new Permission(
                        "supplier:update",
                        "Modifier un fournisseur",
                        "Permet de modifier un fournisseur",
                        "SUPPLIER"
                ),

                new Permission(
                        "sale:create",
                        "Enregistrer une vente",
                        "Permet d’enregistrer une vente",
                        "SALE"
                ),
                new Permission(
                        "sale:view",
                        "Consulter les ventes",
                        "Permet de consulter les ventes",
                        "SALE"
                ),

                new Permission(
                        "report:view",
                        "Consulter les rapports",
                        "Permet de consulter les rapports opérationnels",
                        "REPORT"
                )
        );

        permissions.stream()
                .filter(permission ->
                        !permissionRepository.existsByCodeIgnoreCase(
                                permission.getCode()
                        )
                )
                .forEach(permissionRepository::save);
    }
}
