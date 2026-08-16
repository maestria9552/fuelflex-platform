package com.fuelflex.platform.purchaseorder.controller;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class PurchaseOrderControllerPermissionTest {
    private Map<String,String> rules() {
        return java.util.Arrays.stream(PurchaseOrderController.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(PreAuthorize.class))
                .collect(Collectors.toMap(Method::getName, m -> m.getAnnotation(PreAuthorize.class).value(), (a,b)->a));
    }
    @Test void managerPermissionsAreSeparated() {
        Map<String,String> r=rules();
        assertThat(r.get("create")).contains("order:create");
        assertThat(r.get("update")).contains("order:update");
        assertThat(r.get("submit")).contains("order:submit");
        assertThat(r.get("managerOrder")).contains("order:view");
        assertThat(r.get("supervisorApprove")).doesNotContain("order:create");
        assertThat(r.get("supplierApprove")).doesNotContain("order:create");
    }
    @Test void supervisorAndSupplierPermissionsAreSeparated() {
        Map<String,String> r=rules();
        assertThat(r.get("supervisorApprove")).contains("order:supervisor_approve");
        assertThat(r.get("supervisorReject")).contains("order:supervisor_reject");
        assertThat(r.get("supplierApprove")).contains("order:supplier_approve");
        assertThat(r.get("supplierReject")).contains("order:supplier_reject");
        assertThat(r.get("supervisorApprove")).doesNotContain("supplier_approve");
        assertThat(r.get("supplierApprove")).doesNotContain("supervisor_approve");
    }
    @Test void allHistoryRoutesRequireView() {
        Map<String,String> r=rules();
        assertThat(r.get("managerHistory")).contains("order:view");
        assertThat(r.get("supervisorHistory")).contains("order:view");
        assertThat(r.get("supplierHistory")).contains("order:view");
    }
}
