package com.fuelflex.platform.purchaseorder.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.fuelflex.platform.common.exception.*;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.product.entity.ProductUnit;
import com.fuelflex.platform.product.repository.ProductRepository;
import com.fuelflex.platform.purchaseorder.dto.PurchaseOrderDtos.*;
import com.fuelflex.platform.purchaseorder.entity.*;
import com.fuelflex.platform.purchaseorder.model.*;
import com.fuelflex.platform.purchaseorder.repository.*;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.stationproduct.repository.StationProductRepository;
import com.fuelflex.platform.supplier.entity.*;
import com.fuelflex.platform.supplier.repository.*;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness=Strictness.LENIENT)
class PurchaseOrderManagerMatrixTest {
 @Mock PurchaseOrderRepository orders; @Mock PurchaseOrderHistoryRepository history; @Mock PurchaseOrderNumberRepository numbers; @Mock StationRepository stations; @Mock ProductRepository products; @Mock StationProductRepository stationProducts; @Mock OrganizationSupplierRepository partnerships; @Mock SupplierUserMembershipRepository memberships; @Mock StationAccessService access; @Mock AuthorizationService auth; @Mock EntityManager em; @InjectMocks PurchaseOrderServiceImpl service;
 Organization org; User manager; Station station; Product p1,p2; StationProduct sp1,sp2;
 @BeforeEach void setUp(){org=org();manager=user("MANAGER");station=Station.builder().id(UUID.randomUUID()).organization(org).code("ST-1").name("Gombe").build();p1=product(true);p2=product(true);sp1=stationProduct(p1,true);sp2=stationProduct(p2,true);when(auth.getAuthenticatedUser()).thenReturn(manager);when(stations.findByIdAndOrganizationId(station.getId(),org.getId())).thenReturn(Optional.of(station));when(access.canAccessStation(manager,station.getId())).thenReturn(true);doNothing().when(access).checkStationAccess(manager,station.getId());when(numbers.nextValue()).thenReturn(1L);when(products.findAllById(any())).thenAnswer(i->{Iterable<UUID> ids=i.getArgument(0);List<Product> out=new ArrayList<>();for(UUID id:ids){if(id.equals(p1.getId()))out.add(p1);if(id.equals(p2.getId()))out.add(p2);}return out;});when(stationProducts.findByStationIdAndProductId(station.getId(),p1.getId())).thenReturn(Optional.of(sp1));when(stationProducts.findByStationIdAndProductId(station.getId(),p2.getId())).thenReturn(Optional.of(sp2));}
 @Test void managerCreatesWithoutSupplier(){Response r=service.create(request(null,List.of(item(p1,100))));assertThat(r.supplier()).isNull();assertThat(r.status()).isEqualTo(PurchaseOrderStatus.DRAFT);}
 @Test void managerCreatesWithOrganizationSupplier(){OrganizationSupplier os=partnership(true,true);when(partnerships.findByIdAndOrganizationId(os.getId(),org.getId())).thenReturn(Optional.of(os));assertThat(service.create(request(os.getId(),List.of(item(p1,100)))).supplier().organizationSupplierId()).isEqualTo(os.getId());}
 @Test void managerCreatesMultiProductBasket(){assertThat(service.create(request(null,List.of(item(p1,100),item(p2,200)))).items()).hasSize(2);}
 @Test void emptyBasketIsAllowedInDraft(){assertThat(service.create(request(null,List.of())).items()).isEmpty();}
 @Test void emptyBasketRejectedOnSubmit(){PurchaseOrder po=po(PurchaseOrderStatus.DRAFT);po.getItems().clear();when(orders.lockByIdAndOrganizationId(po.getId(),org.getId())).thenReturn(Optional.of(po));assertThatThrownBy(()->service.submit(po.getId())).isInstanceOf(BusinessException.class);}
 @Test void duplicateProductRejected(){assertThatThrownBy(()->service.create(request(null,List.of(item(p1,1),item(p1,2))))).isInstanceOf(BusinessException.class);}
 @Test void inaccessibleStationRejected(){doThrow(new ForbiddenException("no")).when(access).checkStationAccess(manager,station.getId());assertThatThrownBy(()->service.create(request(null,List.of(item(p1,1))))).isInstanceOf(ForbiddenException.class);}
 @Test void crossTenantStationIsNotFound(){when(stations.findByIdAndOrganizationId(station.getId(),org.getId())).thenReturn(Optional.empty());assertThatThrownBy(()->service.create(request(null,List.of(item(p1,1))))).isInstanceOf(ResourceNotFoundException.class);}
 @Test void inactiveProductRejected(){p1.setActive(false);assertThatThrownBy(()->service.create(request(null,List.of(item(p1,1))))).isInstanceOf(ResourceNotFoundException.class);}
 @Test void unknownProductRejected(){UUID unknown=UUID.randomUUID();assertThatThrownBy(()->service.create(request(null,List.of(new ItemRequest(unknown,BigDecimal.ONE))))).isInstanceOf(ResourceNotFoundException.class);}
 @Test void stationProductMissingRejected(){when(stationProducts.findByStationIdAndProductId(station.getId(),p1.getId())).thenReturn(Optional.empty());assertThatThrownBy(()->service.create(request(null,List.of(item(p1,1))))).isInstanceOf(ResourceNotFoundException.class);}
 @Test void inactiveStationProductRejected(){sp1.setActive(false);assertThatThrownBy(()->service.create(request(null,List.of(item(p1,1))))).isInstanceOf(ResourceNotFoundException.class);}
 @Test void inactivePartnershipRejected(){OrganizationSupplier os=partnership(false,true);when(partnerships.findByIdAndOrganizationId(os.getId(),org.getId())).thenReturn(Optional.of(os));assertThatThrownBy(()->service.create(request(os.getId(),List.of(item(p1,1))))).isInstanceOf(BusinessException.class);}
 @Test void inactiveSupplierRejected(){OrganizationSupplier os=partnership(true,false);when(partnerships.findByIdAndOrganizationId(os.getId(),org.getId())).thenReturn(Optional.of(os));assertThatThrownBy(()->service.create(request(os.getId(),List.of(item(p1,1))))).isInstanceOf(BusinessException.class);}
 @Test void doubleSubmitRejectedByStatus(){PurchaseOrder po=po(PurchaseOrderStatus.PENDING_SUPERVISOR_APPROVAL);when(orders.lockByIdAndOrganizationId(po.getId(),org.getId())).thenReturn(Optional.of(po));assertThatThrownBy(()->service.submit(po.getId())).isInstanceOf(ConflictException.class);}
 @Test void postSubmitUpdateRejectedByStatus(){PurchaseOrder po=po(PurchaseOrderStatus.PENDING_SUPERVISOR_APPROVAL);when(orders.lockByIdAndOrganizationId(po.getId(),org.getId())).thenReturn(Optional.of(po));assertThatThrownBy(()->service.update(po.getId(),new UpdateRequest(station.getId(),null,List.of(item(p1,1))))).isInstanceOf(ConflictException.class);}
 private CreateRequest request(UUID supplier,List<ItemRequest> items){return new CreateRequest(station.getId(),supplier,items);} private ItemRequest item(Product p,int q){return new ItemRequest(p.getId(),BigDecimal.valueOf(q));}
 private PurchaseOrder po(PurchaseOrderStatus s){PurchaseOrder x=new PurchaseOrder();x.setId(UUID.randomUUID());x.setOrganization(org);x.setStation(station);x.setCreatedBy(manager);x.setStatus(s);x.setOrderNumber("FF-PO-2026-000001");return x;}
 private Product product(boolean active){Product p=new Product();p.setId(UUID.randomUUID());p.setOrganization(org);p.setActive(active);p.setCode("P"+UUID.randomUUID().toString().substring(0,4));p.setName("Fuel");p.setUnit(ProductUnit.LITRE);return p;}
 private StationProduct stationProduct(Product p,boolean active){StationProduct sp=new StationProduct();sp.setStation(station);sp.setProduct(p);sp.setActive(active);return sp;}
 private OrganizationSupplier partnership(boolean active,boolean supplierActive){Supplier s=new Supplier();s.setId(UUID.randomUUID());s.setDisplayName("COBIL");s.setActive(supplierActive);OrganizationSupplier os=new OrganizationSupplier();os.setId(UUID.randomUUID());os.setOrganization(org);os.setSupplier(s);os.setActive(active);return os;}
 private Organization org(){Organization o=new Organization();o.setId(UUID.randomUUID());return o;} private User user(String code){Role role=new Role();role.setCode(code);role.setActive(true);User u=new User();u.setId(UUID.randomUUID());u.setOrganization(org);u.setEnabled(true);u.setRoles(Set.of(role));u.setFirstName("Jean");u.setLastName("Manager");return u;}
}
