package com.fuelflex.platform.supplier.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.supplier.entity.*;
import com.fuelflex.platform.supplier.repository.*;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.role.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {
    @Mock SupplierRepository suppliers; @Mock OrganizationSupplierRepository partnerships;
    @Mock SupplierUserMembershipRepository memberships; @Mock UserRepository users;
    @Mock RoleRepository roles; @Mock AuthorizationService authorization;
    @InjectMocks SupplierServiceImpl service;

    @Test void integratedSupplierRequiresActivePortalMembership() {
        OrganizationSupplier p = new OrganizationSupplier();
        Supplier s = new Supplier(); s.setDisplayName("COBIL"); s.setActive(true); p.setSupplier(s); p.setActive(true);
        var org = new com.fuelflex.platform.organization.entity.Organization();
        var id = UUID.randomUUID(); org.setId(id); p.setOrganization(org);
        var user = new com.fuelflex.platform.user.entity.User(); user.setOrganization(org);
        when(authorization.getAuthenticatedUser()).thenReturn(user);
        when(partnerships.findByOrganizationIdAndActiveTrueAndSupplierActiveTrueOrderBySupplierDisplayNameAsc(id)).thenReturn(List.of(p));
        when(memberships.hasActivePortalUser(any())).thenReturn(true);
        var result = service.findSelectable();
        assertThat(result).hasSize(1); assertThat(result.getFirst().integrated()).isTrue();
    }
}
