package com.fuelflex.platform.tariffcategory.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.tariffcategory.config.DefaultTariffCategories;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.tariffcategory.repository.TariffCategoryRepository;
import com.fuelflex.platform.tariffcategory.service.DefaultTariffCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DefaultTariffCategoryServiceImpl implements DefaultTariffCategoryService {
    private final TariffCategoryRepository repository;

    @Override
    public void ensureDefaults(Organization organization) {
        if (organization == null || organization.getId() == null) {
            throw new IllegalArgumentException("L’organisation persistée est obligatoire.");
        }
        for (var definition : DefaultTariffCategories.ALL) {
            if (repository.existsByOrganizationIdAndCodeIgnoreCase(organization.getId(), definition.code())) {
                continue;
            }
            repository.save(TariffCategory.builder()
                    .organization(organization)
                    .code(definition.code())
                    .name(definition.name())
                    .description(definition.description())
                    .system(true)
                    .displayOrder(definition.displayOrder())
                    .active(true)
                    .build());
        }
    }
}
