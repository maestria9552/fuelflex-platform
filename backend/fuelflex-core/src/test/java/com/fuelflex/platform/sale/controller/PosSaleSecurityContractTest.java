package com.fuelflex.platform.sale.controller;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PosSaleSecurityContractTest {
    @Test void endpointsRequirePumpAttendantAndDedicatedPermissions() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/fuelflex/platform/sale/controller/PumpAttendantPosController.java"));
        assertThat(source).contains("hasAuthority('PUMP_ATTENDANT')", "hasAuthority('pos-sale:view')", "hasAuthority('pos-sale:create')")
                .doesNotContain("@PutMapping", "@PatchMapping", "@DeleteMapping");
    }
    @Test void initializerPreservesExistingRolePermissions() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/fuelflex/platform/operations/config/OperationsPermissionDataInitializer.java"));
        assertThat(source).contains("new HashSet<>(role.getPermissions())", "PUMP_ATTENDANT", "pos-sale:view", "pos-sale:create")
                .doesNotContain("role.setPermissions(Set.of");
    }
    @Test void dashboardSubtractsImmutableSaleMovements() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/fuelflex/platform/reception/repository/ReceptionStockBalanceRepository.java"));
        assertThat(source).contains("reception_stock_movements", "sale_stock_movements", "- coalesce");
    }
}
