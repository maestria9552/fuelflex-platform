package com.fuelflex.platform.supplier.service.impl;
import static org.assertj.core.api.Assertions.assertThat; import java.util.UUID; import org.junit.jupiter.api.Test; import org.springframework.security.access.prepost.PreAuthorize;
import com.fuelflex.platform.supplier.controller.SupplierController; import com.fuelflex.platform.supplier.dto.SupplierDtos.SupplierRequest;
class SupplierControllerSecurityContractTest {
 @Test void supervisorCannotModifyGlobalSupplier(){PreAuthorize rule=assertThat(SupplierController.class.getDeclaredMethods()).filteredOn(m->m.getName().equals("update")).singleElement().extracting(m->m.getAnnotation(PreAuthorize.class)).isNotNull().actual();assertThat(rule.value()).isEqualTo("hasAuthority('SUPER_ADMIN')");}
 @Test void globalMembershipIsNotSupervisorAdministrable() throws Exception {assertThat(SupplierController.class.getDeclaredMethod("endMembership",UUID.class).getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('SUPER_ADMIN')");}
}
