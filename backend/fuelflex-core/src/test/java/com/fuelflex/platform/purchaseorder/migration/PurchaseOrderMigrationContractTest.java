package com.fuelflex.platform.purchaseorder.migration;
import static org.assertj.core.api.Assertions.assertThat; import java.nio.file.*; import org.junit.jupiter.api.Test;
class PurchaseOrderMigrationContractTest {
 @Test void v5ContainsCoreDatabaseProtections() throws Exception {String sql=Files.readString(Path.of("src/main/resources/db/migration/V5__create_purchase_orders.sql"));assertThat(sql).contains("CREATE SEQUENCE purchase_order_number_seq","quantity > 0","UNIQUE(purchase_order_id,product_id)","organization_supplier_id UUID REFERENCES organization_suppliers","ON DELETE RESTRICT");}
 @Test void v4WasNotExtendedWithOrders() throws Exception {String sql=Files.readString(Path.of("src/main/resources/db/migration/V4__create_suppliers_and_memberships.sql"));assertThat(sql).doesNotContain("purchase_orders");}
}
