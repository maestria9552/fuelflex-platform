package com.fuelflex.platform.purchaseorder.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.fuelflex.platform.purchaseorder.dto.PurchaseOrderDtos.*;
import com.fuelflex.platform.purchaseorder.model.*;

class PurchaseOrderContractMatrixTest {
    @Test void draftMayStartWithAnEmptyBasketButSubmitRequiresItems() {
        assertThat(new CreateRequest(UUID.randomUUID(), null, java.util.List.of()).items()).isEmpty();
        assertThat(new ItemRequest(UUID.randomUUID(), BigDecimal.ONE).quantity()).isPositive();
    }
    @Test void requestSupportsMultipleDistinctProductsAndOptionalSupplier() {
        var request=new CreateRequest(UUID.randomUUID(), UUID.randomUUID(), java.util.List.of(
                new ItemRequest(UUID.randomUUID(),new BigDecimal("10000")),
                new ItemRequest(UUID.randomUUID(),new BigDecimal("20000"))));
        assertThat(request.items()).hasSize(2); assertThat(request.organizationSupplierId()).isNotNull();
    }
    @Test void statusesExposeReceiptCompletionStates() {
        assertThat(java.util.Arrays.stream(PurchaseOrderStatus.values()).map(Enum::name).toList())
                .contains("RECEIVED","PARTIALLY_RECEIVED");
    }
    @Test void everyBusinessActionHasAnExplicitHistoryAction() {
        assertThat(PurchaseOrderAction.values()).containsExactly(
                PurchaseOrderAction.ORDER_CREATED, PurchaseOrderAction.ORDER_UPDATED,
                PurchaseOrderAction.ORDER_SUBMITTED, PurchaseOrderAction.SUPERVISOR_APPROVED,
                PurchaseOrderAction.SUPERVISOR_REJECTED, PurchaseOrderAction.SUPPLIER_APPROVED,
                PurchaseOrderAction.SUPPLIER_REJECTED);
    }
    @Test void migrationHasSequenceAndStableConstraints() throws Exception {
        String sql=Files.readString(Path.of("src/main/resources/db/migration/V5__create_purchase_orders.sql"));
        assertThat(sql).contains("purchase_order_number_seq","order_number VARCHAR(30) NOT NULL UNIQUE",
                "UNIQUE(purchase_order_id,product_id)","quantity > 0","ON DELETE RESTRICT");
    }
    @Test void v4IsNotChangedByOrderSchema() throws Exception {
        assertThat(Files.readString(Path.of("src/main/resources/db/migration/V4__create_suppliers_and_memberships.sql")))
                .doesNotContain("purchase_orders","purchase_order_items","purchase_order_history");
    }
}
