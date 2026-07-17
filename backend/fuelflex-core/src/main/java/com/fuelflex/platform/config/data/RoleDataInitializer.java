package com.fuelflex.platform.config.data;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.entity.RoleType;
import com.fuelflex.platform.role.repository.RoleRepository;

@Component
@Order(1)
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        List<Role> systemRoles = List.of(
                new Role(
                        "SUPER_ADMIN",
                        "Super administrateur",
                        "Administration globale de la plateforme FuelFlex",
                        RoleType.INTERNAL,
                        true,
                        false
                ),
                new Role(
                        "SUPERVISOR",
                        "Superviseur",
                        "Administration d’une organisation et de ses stations",
                        RoleType.INTERNAL,
                        true,
                        true
                ),
                new Role(
                        "MANAGER",
                        "Gestionnaire",
                        "Gestion opérationnelle d’une ou plusieurs stations",
                        RoleType.INTERNAL,
                        true,
                        true
                ),
                new Role(
                        "PUMP_ATTENDANT",
                        "Pompiste",
                        "Enregistrement des ventes depuis une pompe",
                        RoleType.INTERNAL,
                        true,
                        true
                ),
                new Role(
                        "ACCOUNTANT",
                        "Comptable",
                        "Gestion et consultation des opérations comptables",
                        RoleType.INTERNAL,
                        true,
                        true
                ),
                new Role(
                        "AUDITOR",
                        "Auditeur",
                        "Consultation et contrôle des opérations",
                        RoleType.INTERNAL,
                        true,
                        true
                ),
                new Role(
                        "SUPPLIER_USER",
                        "Utilisateur fournisseur",
                        "Accès externe réservé aux fournisseurs",
                        RoleType.EXTERNAL,
                        true,
                        true
                ),
                new Role(
                        "CREDIT_CUSTOMER_USER",
                        "Client partenaire",
                        "Accès externe réservé aux clients à crédit",
                        RoleType.EXTERNAL,
                        true,
                        true
                )
        );

        systemRoles.stream()
                .filter(role -> !roleRepository.existsByCodeIgnoreCase(role.getCode()))
                .forEach(roleRepository::save);
    }
}
