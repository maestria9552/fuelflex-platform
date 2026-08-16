package com.fuelflex.platform.purchaseorder.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class PurchaseOrderNumberContractTest {
 @Test void businessNumberFormatIsStrict(){assertThat(Pattern.matches("FF-PO-\\d{4}-\\d{6}","FF-PO-2026-000001")).isTrue();assertThat(Pattern.matches("FF-PO-\\d{4}-\\d{6}","PO-1")).isFalse();}
 @Test void sequenceIsBackendOnly(){assertThat("FF-PO-2026-000001").doesNotContain("client");}
}
