package com.fuelflex.platform.purchaseorder.model;

public enum PurchaseOrderStatus {
    DRAFT,
    PENDING_SUPERVISOR_APPROVAL,
    SUPERVISOR_REJECTED,
    PENDING_SUPPLIER_APPROVAL,
    SUPPLIER_REJECTED,
    AWAITING_RECEPTION,
    PARTIALLY_RECEIVED,
    RECEIVED
}
