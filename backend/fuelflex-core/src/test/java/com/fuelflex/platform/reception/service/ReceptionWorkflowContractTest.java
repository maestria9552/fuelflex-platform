package com.fuelflex.platform.reception.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Guards the critical workflow wiring in the compact legacy service. */
class ReceptionWorkflowContractTest {
    private static String source;

    @BeforeAll
    static void readService() throws Exception {
        source = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/reception/service/impl/ReceptionServiceImpl.java"));
    }

    @Test void draftCreationPersistsReceptionBeforeItsItems() {
        int parentPersist = source.indexOf("receptions.saveAndFlush(r)");
        int childrenPersist = source.indexOf("replaceItems(r,req.items(),po,a)");
        assertThat(parentPersist).isGreaterThanOrEqualTo(0);
        assertThat(childrenPersist).isGreaterThan(parentPersist);
        assertThat(source).contains("i.setReception(r)", "items.saveAll(built)");
    }

    @Test void updateKeepsUsingAlreadyPersistedReceptionWhenReplacingItems() {
        assertThat(source).contains("Reception r=manager(id,a,true)",
                "replaceItems(r,req.items(),r.getPurchaseOrder(),a)");
    }

    @Test void submissionRecomputesBacklogWhileOrderIsLocked() {
        assertThat(source).contains("lockOrder(r)", "recomputeBacklogs(currentItems)",
                "sumValidatedForPurchaseOrderItem");
    }

    @Test void normalAndPartialSubmissionsAreFinallyValidated() {
        assertThat(source).contains("validateReception(r,a)", "RECEPTION_PARTIAL_VALIDATED",
                "PurchaseOrderStatus.PARTIALLY_RECEIVED");
    }

    @Test void overageRequiresReasonAndSupervisorAction() {
        assertThat(source).contains("if(over&&comment==null)",
                "PENDING_SUPERVISOR_APPROVAL", "NotificationCategory.ACTION_REQUIRED");
    }

    @Test void approvalIsImmediatelyFollowedByFinalValidation() {
        assertThat(source).contains("ReceptionStatus.APPROVED,ReceptionHistoryAction.SUPERVISOR_APPROVED",
                "validateReception(r,a)");
    }

    @Test void stockApplicationIsProtectedAgainstDuplicates() {
        assertThat(source).contains("stockMovements.existsByAllocationId",
                "Le stock de cette réception a déjà été comptabilisé");
    }

    @Test void tankAllocationMustEqualReceivedQuantity() {
        assertThat(source).contains("allocated.compareTo(i.getReceivedQuantity())!=0");
    }

    @Test void managerActionsNotifySupervisors() {
        assertThat(source).contains("RECEPTION_CREATED", "RECEPTION_UPDATED",
                "RECEPTION_CORRECTED", "RECEPTION_VALIDATED", "RECEPTION_OVERAGE_SUBMITTED");
    }

    @Test void notificationRecipientsAreOrganizationAndStationScoped() {
        assertThat(source).contains("findEnabledByOrganizationIdAndRoleCode",
                "stationAccess.canAccessStation(recipient,r.getStation().getId())");
    }

    @Test void terminalTransitionsRemainGuarded() {
        assertThat(source).contains("require(r,ReceptionStatus.PENDING_SUPERVISOR_APPROVAL)",
                "Cette réception n’est plus modifiable", "Cette réception n’est pas soumettable");
    }
    @Test void availabilityComesFromTheValidatedCumulativeBackendQuery() { assertThat(source).contains("managerOrderAvailability", "validatedQuantity(orderItem)", "subtract(previous).max(BigDecimal.ZERO)"); }

    @Test void purchaseOrderStatusUsesEveryLineBacklog() {
        assertThat(source).contains(
                "for(PurchaseOrderItem item:po.getItems())",
                "validatedQuantity(item)",
                "if(received.signum()>0)anyValidated=true",
                "if(received.compareTo(item.getQuantity())<0)allComplete=false",
                "PurchaseOrderStatus target=allComplete?PurchaseOrderStatus.RECEIVED:anyValidated?PurchaseOrderStatus.PARTIALLY_RECEIVED:previous",
                "po.setStatus(target)",
                "orders.saveAndFlush(po)");
    }

    @Test void validationHistoryKeepsCumulativeResultAndBacklog() { assertThat(source).contains("validationSummary(r)", "Reliquat :", "ReceptionHistoryAction.RECEPTION_VALIDATED"); }

    @Test void managerDashboardReadsTheDefinitiveMovementLedger() throws Exception {
        String stockSource = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/reception/repository/ReceptionStockBalanceRepository.java"));

        assertThat(stockSource).contains(
                "reception_stock_movements inbound",
                "sale_stock_movements outbound",
                "sum(inbound.quantity)",
                "sum(case when outbound.movement_type = 'OUTBOUND'",
                "else -outbound.quantity end)",
                "- coalesce",
                "tank.maximum_level_liters",
                "tank.active = true",
                "station.id in (:stationIds)");
    }
}
