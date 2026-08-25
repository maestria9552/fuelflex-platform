package com.fuelflex.platform.sale.migration;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PosSaleMigrationContractTest {
    @Test void v11DefinesSalesStockProtectionAndPermissions() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V11__create_pos_fuel_sales.sql"));
        assertThat(sql).contains("CREATE TABLE fuel_sales", "CREATE TABLE sale_stock_movements",
                "uk_sale_stock_movement_sale", "quantity > 0", "fuel_sale_number_seq",
                "pos-sale:view", "pos-sale:create", "PUMP_ATTENDANT");
    }
    @Test void v11IsAdditiveForPermissionsAndKeepsReceptionLedgerUntouched() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V11__create_pos_fuel_sales.sql"));
        assertThat(sql).contains("WHERE NOT EXISTS").doesNotContain("DELETE FROM role_permissions",
                "UPDATE reception_stock_movements", "DELETE FROM reception_stock_movements");
    }
}
