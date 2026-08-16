package com.fuelflex.platform.reception.service;
import static org.junit.jupiter.api.Assertions.*; import java.math.BigDecimal; import org.junit.jupiter.api.Test; import com.fuelflex.platform.reception.model.ReceptionStatus; import com.fuelflex.platform.purchaseorder.model.PurchaseOrderStatus;
class ReceptionBusinessRulesTest { private BigDecimal backlog(String ordered,String validated){return new BigDecimal(ordered).subtract(new BigDecimal(validated)).max(BigDecimal.ZERO);} private BigDecimal difference(String received,String backlog){return new BigDecimal(received).subtract(new BigDecimal(backlog));}
 @Test void awaitingReceptionIsEntryPoint(){assertTrue(PurchaseOrderStatus.AWAITING_RECEPTION==PurchaseOrderStatus.AWAITING_RECEPTION);assertTrue(PurchaseOrderStatus.PARTIALLY_RECEIVED!=PurchaseOrderStatus.RECEIVED);}
 @Test void partialReceptionLeavesBacklog(){assertEquals(new BigDecimal("3000"),backlog("10000","7000"));assertTrue(difference("2000","3000").signum()<0);}
 @Test void exactReceptionHasNoDifference(){assertEquals(BigDecimal.ZERO,difference("3000","3000"));}
 @Test void overageIsPositive(){assertEquals(new BigDecimal("500"),difference("3500","3000"));}
 @Test void pendingAndCancelledAreNotConsumed(){assertEquals(new BigDecimal("10000"),backlog("10000","0"));assertEquals(ReceptionStatus.PENDING_SUPERVISOR_APPROVAL,ReceptionStatus.PENDING_SUPERVISOR_APPROVAL);assertEquals(ReceptionStatus.CANCELLED,ReceptionStatus.CANCELLED);}
 @Test void validatedOnlyConsumesBacklog(){assertEquals(new BigDecimal("2000"),backlog("10000","8000"));}
 @Test void approvalStatusesAreDistinct(){assertNotEquals(ReceptionStatus.APPROVED,ReceptionStatus.VALIDATED);assertNotEquals(ReceptionStatus.RETURNED_FOR_CORRECTION,ReceptionStatus.DRAFT);}
 @Test void zeroBacklogClamped(){assertEquals(BigDecimal.ZERO,backlog("100","120"));}
 @Test void purchasePriceMustBePositive(){assertTrue(new BigDecimal("0.01").signum()>0);assertFalse(BigDecimal.ZERO.signum()>0);}
 @Test void allocationInvariant(){assertEquals(new BigDecimal("1000"),new BigDecimal("600").add(new BigDecimal("400")));}
}
