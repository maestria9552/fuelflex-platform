package com.fuelflex.platform.tariffcategory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.tariffcategory.repository.TariffCategoryRepository;

@ExtendWith(MockitoExtension.class)
class DefaultTariffCategoryServiceImplTest {
    @Mock private TariffCategoryRepository repository;

    @Test
    void initializesCashCreditAndInternal() {
        Organization organization = organization();
        new DefaultTariffCategoryServiceImpl(repository).ensureDefaults(organization);

        ArgumentCaptor<TariffCategory> captor = ArgumentCaptor.forClass(TariffCategory.class);
        verify(repository, times(3)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(TariffCategory::getCode)
                .containsExactly("CASH", "CREDIT", "INTERNAL");
        assertThat(captor.getAllValues()).allSatisfy(category -> {
            assertThat(category.isSystem()).isTrue();
            assertThat(category.isActive()).isTrue();
            assertThat(category.getOrganization()).isSameAs(organization);
        });
    }

    @Test
    void secondInitializationCreatesNoDuplicate() {
        Organization organization = organization();
        var service = new DefaultTariffCategoryServiceImpl(repository);
        service.ensureDefaults(organization);
        clearInvocations(repository);
        when(repository.existsByOrganizationIdAndCodeIgnoreCase(any(), any())).thenReturn(true);

        service.ensureDefaults(organization);

        verify(repository, never()).save(any());
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        return organization;
    }
}
