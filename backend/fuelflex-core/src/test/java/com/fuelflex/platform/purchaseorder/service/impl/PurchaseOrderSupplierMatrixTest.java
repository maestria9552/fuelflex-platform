package com.fuelflex.platform.purchaseorder.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageRequest;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.fuelflex.platform.common.exception.*;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.purchaseorder.dto.PurchaseOrderDtos.*;
import com.fuelflex.platform.purchaseorder.entity.*;
import com.fuelflex.platform.purchaseorder.model.*;
import com.fuelflex.platform.purchaseorder.repository.*;
import com.fuelflex.platform.product.repository.ProductRepository;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.stationproduct.repository.StationProductRepository;
import com.fuelflex.platform.supplier.entity.OrganizationSupplier;
import com.fuelflex.platform.supplier.entity.Supplier;
import com.fuelflex.platform.supplier.repository.*;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness=Strictness.LENIENT)
class PurchaseOrderSupplierMatrixTest {
 @Mock PurchaseOrderRepository orders; @Mock PurchaseOrderHistoryRepository history; @Mock PurchaseOrderNumberRepository numbers; @Mock StationRepository stations; @Mock ProductRepository products; @Mock StationProductRepository stationProducts; @Mock OrganizationSupplierRepository partnerships; @Mock SupplierUserMembershipRepository memberships; @Mock StationAccessService access; @Mock AuthorizationService auth; @Mock EntityManager em; @Mock TypedQuery<UUID> supplierQuery; @InjectMocks PurchaseOrderServiceImpl service;
 Organization org; User supplierUser; UUID supplierId; PurchaseOrder po;
 @BeforeEach void setUp(){org=org();supplierId=UUID.randomUUID();supplierUser=user("SUPPLIER_USER");po=new PurchaseOrder();po.setId(UUID.randomUUID());po.setOrganization(org);po.setStation(Station.builder().id(UUID.randomUUID()).code("ST-1").name("Gombe").build());po.setStatus(PurchaseOrderStatus.PENDING_SUPPLIER_APPROVAL);po.setOrganizationSupplier(partnership(supplierId,true));when(auth.getAuthenticatedUser()).thenReturn(supplierUser);when(em.createQuery(anyString(),eq(UUID.class))).thenReturn(supplierQuery);when(supplierQuery.setParameter(eq("uid"),any())).thenReturn(supplierQuery);when(supplierQuery.getResultList()).thenReturn(List.of(supplierId));when(orders.lockByIdForSupplier(po.getId(),supplierId)).thenReturn(Optional.of(po));}
 @Test void correctSupplierApproves(){assertThat(service.supplierApprove(po.getId(),null).status()).isEqualTo(PurchaseOrderStatus.AWAITING_RECEPTION);}
 @Test void correctSupplierRejectsWithReason(){assertThat(service.supplierReject(po.getId(),new DecisionRequest("rupture"))).extracting(Response::status).isEqualTo(PurchaseOrderStatus.SUPPLIER_REJECTED);verify(history).save(argThat(h->"rupture".equals(h.getComment())));}
 @Test void supplierRejectWithoutReasonFails(){assertThatThrownBy(()->service.supplierReject(po.getId(),new DecisionRequest(" "))).isInstanceOf(BusinessException.class);}
 @Test void supplierWrongStatusFails(){po.setStatus(PurchaseOrderStatus.SUPPLIER_REJECTED);assertThatThrownBy(()->service.supplierApprove(po.getId(),null)).isInstanceOf(ConflictException.class);}
 @Test void secondApproveFails(){service.supplierApprove(po.getId(),null);po.setStatus(PurchaseOrderStatus.AWAITING_RECEPTION);assertThatThrownBy(()->service.supplierApprove(po.getId(),null)).isInstanceOf(ConflictException.class);}
 @Test void secondRejectFails(){service.supplierReject(po.getId(),new DecisionRequest("no stock"));po.setStatus(PurchaseOrderStatus.SUPPLIER_REJECTED);assertThatThrownBy(()->service.supplierReject(po.getId(),new DecisionRequest("again"))).isInstanceOf(ConflictException.class);}
 @Test void wrongSupplierCannotSeeOrder(){when(supplierQuery.getResultList()).thenReturn(List.of(UUID.randomUUID()));assertThatThrownBy(()->service.supplierOrder(po.getId())).isInstanceOf(ResourceNotFoundException.class);}
 @Test void inactiveMembershipCannotSeeOrder(){when(supplierQuery.getResultList()).thenReturn(List.of());assertThat(service.supplierOrders(PageRequest.of(0,20))).isEmpty();}
 @Test void disabledUserCannotAct(){supplierUser.setEnabled(false);assertThatThrownBy(()->service.supplierApprove(po.getId(),null)).isInstanceOf(ForbiddenException.class);}
 @Test void missingSupplierRoleCannotAct(){supplierUser.setRoles(Set.of());assertThatThrownBy(()->service.supplierApprove(po.getId(),null)).isInstanceOf(ForbiddenException.class);}
  void inactiveSupplierCannotDecide(){po.getOrganizationSupplier().getSupplier().setActive(false);assertThatThrownBy(()->service.supplierApprove(po.getId(),null)).isInstanceOf(ConflictException.class);}
 private Organization org(){Organization o=new Organization();o.setId(UUID.randomUUID());return o;} private User user(String code){Role r=new Role();r.setCode(code);r.setActive(true);User u=new User();u.setId(UUID.randomUUID());u.setOrganization(org);u.setEnabled(true);u.setRoles(Set.of(r));return u;} private OrganizationSupplier partnership(UUID id,boolean active){Supplier s=new Supplier();s.setId(id);s.setDisplayName("COBIL");s.setActive(active);OrganizationSupplier os=new OrganizationSupplier();os.setId(UUID.randomUUID());os.setOrganization(org);os.setSupplier(s);os.setActive(true);return os;}
}
