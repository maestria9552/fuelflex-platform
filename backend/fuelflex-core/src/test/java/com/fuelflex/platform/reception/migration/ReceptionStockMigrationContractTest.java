package com.fuelflex.platform.reception.migration;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReceptionStockMigrationContractTest {
    @Test
    void v8CreatesAnIdempotentInboundStockLedger() throws Exception {
        String sql = Files.readString(Path.of(
                "src/main/resources/db/migration/V8__add_reception_stock_movements.sql"));
        assertThat(sql).contains(
                "CREATE TABLE reception_stock_movements",
                "uk_reception_stock_movement_allocation UNIQUE (allocation_id)",
                "CHECK (quantity > 0)",
                "reception_id UUID NOT NULL REFERENCES receptions",
                "tank_id UUID NOT NULL REFERENCES tanks");
    }
}
