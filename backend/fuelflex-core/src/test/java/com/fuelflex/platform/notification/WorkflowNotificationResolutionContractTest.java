package com.fuelflex.platform.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class WorkflowNotificationResolutionContractTest {

    @Test
    void receptionResolvesSupervisorDecisionAndManagerCorrectionActions()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/reception/service/impl/"
                        + "ReceptionServiceImpl.java"));

        assertThat(source).contains(
                "if(resubmission)resolveRequiredNotifications(r,a)",
                "validateReception(r,a);resolveRequiredNotifications(r,a)",
                "RETURNED_FOR_CORRECTION,a,c);resolveRequiredNotifications(r,a)",
                "RECEPTION_CANCELLED,a,c);resolveRequiredNotifications(r,a)",
                "notifications.resolveRequiredActions",
                "\"RECEPTION\""
        ).doesNotContain(
                "notification.setRead(true)",
                "notification.setCategory(NotificationCategory.INFORMATION)"
        );
    }

    @Test
    void purchaseOrderApprovalAndRejectionResolveSubmittedAction()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/fuelflex/platform/purchaseorder/service/impl/"
                        + "PurchaseOrderServiceImpl.java"));

        assertThat(source).contains(
                "supervisorApprove",
                "supervisorReject",
                "notifications.resolveRequiredActions",
                "\"PURCHASE_ORDER\""
        );
        assertThat(count(source, "notifications.resolveRequiredActions"))
                .isGreaterThanOrEqualTo(2);
    }

    private int count(String value, String token) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
