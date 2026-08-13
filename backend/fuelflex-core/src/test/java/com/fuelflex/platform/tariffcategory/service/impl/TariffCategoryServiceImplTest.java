package com.fuelflex.platform.tariffcategory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.tariffcategory.dto.request.TariffCategoryRequest;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.tariffcategory.mapper.TariffCategoryMapper;
import com.fuelflex.platform.tariffcategory.repository.TariffCategoryRepository;
import com.fuelflex.platform.user.entity.User;

@ExtendWith(MockitoExtension.class)
class TariffCategoryServiceImplTest {
    @Mock private TariffCategoryRepository repository;
    @Mock private AuthorizationService authorizationService;

    private TariffCategoryServiceImpl service;
    private UUID organizationId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        service = new TariffCategoryServiceImpl(repository, new TariffCategoryMapper(), authorizationService);
        organizationId = UUID.randomUUID();
        organization = new Organization();
        organization.setId(organizationId);
        User user = new User();
        user.setOrganization(organization);
        lenient().when(authorizationService.getAuthenticatedUser()).thenReturn(user);
    }

    @Test
    void createsCustomCategoryWithDefaultsAndNormalizedCode() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(organizationId, request("vip partner", "  VIP   Partenaires ", null));

        assertThat(response.getCode()).isEqualTo("VIP_PARTNER");
        assertThat(response.isSystem()).isFalse();
        assertThat(response.isActive()).isTrue();
        assertThat(response.getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateCodeAndName() {
        when(repository.existsByOrganizationIdAndCodeIgnoreCase(organizationId, "VIP")).thenReturn(true);
        assertThatThrownBy(() -> service.create(organizationId, request("vip", "VIP", true)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("code");

        reset(repository);
        when(repository.existsByOrganizationIdAndNameIgnoreCase(organizationId, "VIP")).thenReturn(true);
        assertThatThrownBy(() -> service.create(organizationId, request("VIP2", "VIP", true)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("nom");
    }

    @Test
    void sameCodeIsScopedToOrganization() {
        UUID otherOrganizationId = UUID.randomUUID();
        Organization otherOrganization = new Organization();
        otherOrganization.setId(otherOrganizationId);
        User otherUser = new User();
        otherUser.setOrganization(otherOrganization);
        User firstUser = new User();
        firstUser.setOrganization(organization);
        when(authorizationService.getAuthenticatedUser()).thenReturn(firstUser, otherUser);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var first = service.create(organizationId, request("VIP", "VIP organisation A", true));
        var second = service.create(otherOrganizationId, request("VIP", "VIP organisation B", true));

        assertThat(first.getCode()).isEqualTo("VIP");
        assertThat(second.getCode()).isEqualTo("VIP");
        assertThat(first.getOrganizationId()).isEqualTo(organizationId);
        assertThat(second.getOrganizationId()).isEqualTo(otherOrganizationId);
        verify(repository).existsByOrganizationIdAndCodeIgnoreCase(organizationId, "VIP");
        verify(repository).existsByOrganizationIdAndCodeIgnoreCase(otherOrganizationId, "VIP");
    }

    @Test
    void findAllAndFindActiveAreOrganizationScoped() {
        TariffCategory active = entity(false, true);
        when(repository.findByOrganizationIdOrderByDisplayOrderAsc(organizationId)).thenReturn(List.of(active));
        when(repository.findByOrganizationIdAndActiveTrueOrderByDisplayOrderAsc(organizationId)).thenReturn(List.of(active));
        assertThat(service.findAllByOrganization(organizationId)).hasSize(1);
        assertThat(service.findActiveByOrganization(organizationId)).hasSize(1);
        verify(authorizationService, times(2)).checkOrganizationAccess(organizationId);
    }

    @Test
    void updatesCustomCategory() {
        TariffCategory entity = entity(false, true);
        when(repository.findByIdAndOrganizationId(entity.getId(), organizationId)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        var response = service.update(organizationId, entity.getId(), request("wholesale", "Grossiste", true));
        assertThat(response.getCode()).isEqualTo("WHOLESALE");
        assertThat(response.getName()).isEqualTo("Grossiste");
    }

    @Test
    void protectsSystemCodeAndActivation() {
        TariffCategory entity = entity(true, true);
        entity.setCode("CASH");
        when(repository.findByIdAndOrganizationId(entity.getId(), organizationId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(organizationId, entity.getId(), request("VIP", "Cash", true)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("code");
        assertThatThrownBy(() -> service.update(organizationId, entity.getId(), request("CASH", "Cash", false)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("désactivée");
        assertThatThrownBy(() -> service.delete(organizationId, entity.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("système");
    }

    @Test
    void deleteIsLogicalForCustomCategory() {
        TariffCategory entity = entity(false, true);
        when(repository.findByIdAndOrganizationId(entity.getId(), organizationId)).thenReturn(Optional.of(entity));
        service.delete(organizationId, entity.getId());
        assertThat(entity.isActive()).isFalse();
        verify(repository).save(entity);
        verify(repository, never()).delete(any());
    }

    @Test
    void rejectsOtherOrganizationScope() {
        doThrow(new BusinessException("Accès refusé")).when(authorizationService)
                .checkOrganizationAccess(organizationId);
        assertThatThrownBy(() -> service.findAllByOrganization(organizationId))
                .isInstanceOf(BusinessException.class).hasMessage("Accès refusé");
        verifyNoInteractions(repository);
    }

    private TariffCategoryRequest request(String code, String name, Boolean active) {
        TariffCategoryRequest request = new TariffCategoryRequest();
        request.setCode(code);
        request.setName(name);
        request.setActive(active);
        return request;
    }

    private TariffCategory entity(boolean system, boolean active) {
        return TariffCategory.builder().id(UUID.randomUUID()).organization(organization)
                .code("VIP").name("VIP").displayOrder(4).system(system).active(active).build();
    }
}
