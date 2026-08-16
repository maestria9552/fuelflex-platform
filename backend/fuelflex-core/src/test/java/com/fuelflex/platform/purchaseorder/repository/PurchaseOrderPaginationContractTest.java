package com.fuelflex.platform.purchaseorder.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class PurchaseOrderPaginationContractTest {
 @Test void listQueriesAcceptPageable(){for(Method m:PurchaseOrderRepository.class.getDeclaredMethods()){if(m.getName().startsWith("findByOrganization")||m.getName().startsWith("findForSupplier"))assertThat(java.util.Arrays.stream(m.getParameterTypes()).anyMatch(Pageable.class::equals)).isTrue();}}
 @Test void pageRequestSupportsPageSizeAndSort(){Pageable p=org.springframework.data.domain.PageRequest.of(2,25,org.springframework.data.domain.Sort.by("createdAt").descending());assertThat(p.getPageNumber()).isEqualTo(2);assertThat(p.getPageSize()).isEqualTo(25);assertThat(p.getSort().getOrderFor("createdAt")).isNotNull();}
}
