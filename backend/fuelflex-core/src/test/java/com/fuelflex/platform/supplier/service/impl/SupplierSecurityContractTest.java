package com.fuelflex.platform.supplier.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fuelflex.platform.common.exception.*;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.supplier.dto.SupplierDtos.*;
import com.fuelflex.platform.supplier.entity.*;
import com.fuelflex.platform.supplier.repository.*;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SupplierSecurityContractTest {
 @Mock SupplierRepository suppliers; @Mock OrganizationSupplierRepository partnerships; @Mock SupplierUserMembershipRepository memberships;
 @Mock UserRepository users; @Mock RoleRepository roles; @Mock AuthorizationService authorization; @InjectMocks SupplierServiceImpl service;
 private Organization orgA; private Organization orgB; private User supervisor;
 @BeforeEach void setUp(){orgA=org();orgB=org();supervisor=user(orgA,"SUPERVISOR");when(authorization.getAuthenticatedUser()).thenReturn(supervisor);}
 @Test void supervisorCannotAdministerGlobalMembership(){assertThatThrownBy(()->service.endMembership(UUID.randomUUID())).isInstanceOf(ForbiddenException.class);verifyNoInteractions(memberships);}
 @Test void crossTenantPartnershipIsNotRevealed(){UUID id=UUID.randomUUID();when(partnerships.findByIdAndOrganizationId(id,orgA.getId())).thenReturn(Optional.empty());assertThatThrownBy(()->service.updatePartnership(id,new OrganizationSupplierRequest(null,"X",false))).isInstanceOf(ResourceNotFoundException.class);}
 @Test void disablingPartnershipDoesNotDisableGlobalSupplier(){Supplier s=new Supplier();s.setId(UUID.randomUUID());s.setActive(true);OrganizationSupplier p=new OrganizationSupplier();p.setId(UUID.randomUUID());p.setOrganization(orgA);p.setSupplier(s);p.setActive(true);when(partnerships.findByIdAndOrganizationId(p.getId(),orgA.getId())).thenReturn(Optional.of(p));when(partnerships.save(p)).thenReturn(p);service.updatePartnership(p.getId(),new OrganizationSupplierRequest(null,null,false));assertThat(s.isActive()).isTrue();verifyNoInteractions(suppliers);}
 @Test void managerOnlySeesActivePartnershipsWithActiveSupplier(){User manager=user(orgA,"MANAGER");when(authorization.getAuthenticatedUser()).thenReturn(manager);OrganizationSupplier p=new OrganizationSupplier();p.setOrganization(orgA);Supplier s=new Supplier();s.setId(UUID.randomUUID());s.setDisplayName("COBIL");s.setActive(true);p.setSupplier(s);p.setActive(true);when(partnerships.findByOrganizationIdAndActiveTrueAndSupplierActiveTrueOrderBySupplierDisplayNameAsc(orgA.getId())).thenReturn(List.of(p));assertThat(service.findSelectable()).hasSize(1);verify(partnerships).findByOrganizationIdAndActiveTrueAndSupplierActiveTrueOrderBySupplierDisplayNameAsc(orgA.getId());}
 @Test void crossTenantLookupIsScopedByAuthenticatedOrganization(){UUID id=UUID.randomUUID();when(partnerships.findByIdAndOrganizationId(id,orgA.getId())).thenReturn(Optional.empty());assertThatThrownBy(()->service.updatePartnership(id,new OrganizationSupplierRequest(null,null,true))).isInstanceOf(ResourceNotFoundException.class);verify(partnerships,never()).findByIdAndOrganizationId(id,orgB.getId());}
 private Organization org(){Organization o=new Organization();o.setId(UUID.randomUUID());return o;}
 private User user(Organization o,String code){Role r=new Role();r.setCode(code);r.setActive(true);User u=new User();u.setId(UUID.randomUUID());u.setOrganization(o);u.setEnabled(true);u.setRoles(Set.of(r));return u;}
}
