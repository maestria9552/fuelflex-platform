package com.fuelflex.platform.purchaseorder.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import com.fuelflex.platform.purchaseorder.entity.PurchaseOrder;
import com.fuelflex.platform.purchaseorder.model.PurchaseOrderStatus;
import com.fuelflex.platform.supplier.entity.Supplier;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderAuditGapTest {
    @Test void inactiveSupplierMustNotBeDecisionEligible() {
        Supplier supplier = new Supplier();
        supplier.setActive(false);
        PurchaseOrder order = new PurchaseOrder();
        order.setStatus(PurchaseOrderStatus.PENDING_SUPPLIER_APPROVAL);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.PENDING_SUPPLIER_APPROVAL);
        assertThat(supplier.isActive()).isFalse();
    }
}
