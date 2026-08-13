package com.fuelflex.platform.organization.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.AuthorityUtils;

import com.fuelflex.platform.organization.dto.request.OrganizationRequest;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.organization.mapper.OrganizationMapper;
import com.fuelflex.platform.organization.repository.OrganizationRepository;
import com.fuelflex.platform.storage.service.OrganizationLogoStorageService;
import com.fuelflex.platform.tariffcategory.service.DefaultTariffCategoryService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrganizationTariffCategoryInitializationTest {
    @Mock private OrganizationRepository organizationRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrganizationLogoStorageService logoStorageService;
    @Mock private DefaultTariffCategoryService defaultTariffCategoryService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void newOrganizationReceivesDefaultTariffCategoriesAfterPersistence() {
        String email = "supervisor@fuelflex.test";
        User user = new User();
        user.setEmail(email);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email, "password", AuthorityUtils.NO_AUTHORITIES));
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(organizationRepository.findByCodeIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenAnswer(invocation -> {
            Organization organization = invocation.getArgument(0);
            organization.setId(UUID.randomUUID());
            return organization;
        });

        var service = new OrganizationServiceImpl(organizationRepository, new OrganizationMapper(),
                userRepository, logoStorageService, defaultTariffCategoryService);
        OrganizationRequest request = new OrganizationRequest();
        request.setName("Nouvelle organisation");

        var response = service.create(request);

        assertThat(response.getId()).isNotNull();
        verify(defaultTariffCategoryService).ensureDefaults(argThat(
                organization -> organization.getId() != null));
        verify(userRepository).save(user);
    }
}
