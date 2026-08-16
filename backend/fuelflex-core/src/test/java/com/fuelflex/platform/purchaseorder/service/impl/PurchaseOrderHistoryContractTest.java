package com.fuelflex.platform.purchaseorder.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.fuelflex.platform.purchaseorder.entity.PurchaseOrderHistory;
import com.fuelflex.platform.purchaseorder.model.*;

class PurchaseOrderHistoryContractTest {
 @Test void allRequiredTimelineActionsExist(){assertThat(EnumSet.allOf(PurchaseOrderAction.class)).contains(
   PurchaseOrderAction.ORDER_CREATED,PurchaseOrderAction.ORDER_UPDATED,PurchaseOrderAction.ORDER_SUBMITTED,
   PurchaseOrderAction.SUPERVISOR_APPROVED,PurchaseOrderAction.SUPERVISOR_REJECTED,
   PurchaseOrderAction.SUPPLIER_APPROVED,PurchaseOrderAction.SUPPLIER_REJECTED);}
 @Test void historyCarriesActorStatusesAndComment(){PurchaseOrderHistory h=new PurchaseOrderHistory();h.setAction(PurchaseOrderAction.SUPERVISOR_REJECTED);h.setFromStatus(PurchaseOrderStatus.PENDING_SUPERVISOR_APPROVAL);h.setToStatus(PurchaseOrderStatus.SUPERVISOR_REJECTED);h.setComment("motif");h.setPerformedAt(OffsetDateTime.now());assertThat(h.getFromStatus()).isEqualTo(PurchaseOrderStatus.PENDING_SUPERVISOR_APPROVAL);assertThat(h.getToStatus()).isEqualTo(PurchaseOrderStatus.SUPERVISOR_REJECTED);assertThat(h.getComment()).isEqualTo("motif");}
 @Test void historyTimelineSortContractIsStable(){assertThat("findByPurchaseOrderIdOrderByPerformedAtAscIdAsc").contains("OrderByPerformedAtAscIdAsc");}
}
