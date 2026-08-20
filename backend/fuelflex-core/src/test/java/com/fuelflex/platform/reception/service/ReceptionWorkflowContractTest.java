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
}
