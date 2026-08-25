package com.fuelflex.platform.operations.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class WebOperationalReadContractTest {

    @Test
    void managerOperationalReadEndpointsRequireExistingDedicatedPermissions() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/operations/controller/ManagerOperationalDayController.java"
        ));
        assertThat(source).contains(
                "@GetMapping(\"/stations\")",
                "@GetMapping(\"/stations/{stationId}/eligible-pump-attendants\")",
                "@GetMapping(\"/operational-days/{id}/available-fuel-meters\")",
                "hasAuthority('MANAGER')",
                "hasAuthority('operational-day:view')",
                "hasAuthority('shift-assignment:create')"
        );
    }

    @Test
    void managerAndSupervisorSaleReadsRequireRoleAndViewPermission() throws Exception {
        String manager = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/sale/controller/ManagerSaleController.java"
        ));
        String supervisor = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/sale/controller/SupervisorSaleController.java"
        ));
        assertThat(manager).contains(
                "@GetMapping",
                "@GetMapping(\"/{id}\")",
                "hasAuthority('MANAGER') and hasAuthority('pos-sale:view')"
        );
        assertThat(supervisor).contains(
                "@GetMapping",
                "@GetMapping(\"/{id}\")",
                "hasAuthority('SUPERVISOR') and hasAuthority('pos-sale:view')"
        );
    }

    @Test
    void saleRepositoryEnforcesOrganizationAndAccessibleStationScopes() throws Exception {
        String repository = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/sale/repository/FuelSaleRepository.java"
        ));
        assertThat(repository).contains(
                "s.organization.id=:organizationId",
                "s.station.id in :stationIds",
                "findByIdAndOrganizationIdAndStationIdIn"
        );
    }

    @Test
    void viewPermissionGrantIsAdditiveInInitializerAndMigration() throws Exception {
        String initializer = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/operations/config/OperationsPermissionDataInitializer.java"
        ));
        String migration = Files.readString(Path.of(
                "src/main/resources/db/migration/V13__grant_web_pos_sale_read_permissions.sql"
        ));
        assertThat(initializer).contains(
                "new HashSet<>(role.getPermissions())",
                "assign(\"MANAGER\",\"pos-sale:view\"",
                "assign(\"SUPERVISOR\",\"pos-sale:view\""
        ).doesNotContain("role.setPermissions(Set.of");
        assertThat(migration).contains(
                "permission.code = 'pos-sale:view'",
                "role.code IN ('MANAGER', 'SUPERVISOR')",
                "NOT EXISTS"
        ).doesNotContain("DELETE", "UPDATE role_permissions");
    }
}
