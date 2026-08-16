package com.fuelflex.platform.purchaseorder.service.impl;
import static org.assertj.core.api.Assertions.assertThat; import java.math.BigDecimal; import java.util.*; import org.junit.jupiter.api.*; import jakarta.validation.*;
import com.fuelflex.platform.purchaseorder.dto.PurchaseOrderDtos.*; import com.fuelflex.platform.purchaseorder.model.PurchaseOrderStatus;
class PurchaseOrderValidationTest {Validator validator; @BeforeEach void setup(){validator=Validation.buildDefaultValidatorFactory().getValidator();}
 @Test void positiveQuantityAccepted(){assertThat(validator.validate(new ItemRequest(UUID.randomUUID(),new BigDecimal("10000.125")))).isEmpty();}
 @Test void zeroQuantityRejected(){assertThat(validator.validate(new ItemRequest(UUID.randomUUID(),BigDecimal.ZERO))).isNotEmpty();}
 @Test void negativeQuantityRejected(){assertThat(validator.validate(new ItemRequest(UUID.randomUUID(),BigDecimal.ONE.negate()))).isNotEmpty();}
 @Test void statusesIncludeReceptionCompletionStates(){assertThat(PurchaseOrderStatus.values()).containsExactly(PurchaseOrderStatus.DRAFT,PurchaseOrderStatus.PENDING_SUPERVISOR_APPROVAL,PurchaseOrderStatus.SUPERVISOR_REJECTED,PurchaseOrderStatus.PENDING_SUPPLIER_APPROVAL,PurchaseOrderStatus.SUPPLIER_REJECTED,PurchaseOrderStatus.AWAITING_RECEPTION,PurchaseOrderStatus.PARTIALLY_RECEIVED,PurchaseOrderStatus.RECEIVED);}
}
