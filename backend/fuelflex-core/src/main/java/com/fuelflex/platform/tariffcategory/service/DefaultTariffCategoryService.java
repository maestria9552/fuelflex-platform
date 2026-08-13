package com.fuelflex.platform.tariffcategory.service;

import com.fuelflex.platform.organization.entity.Organization;

public interface DefaultTariffCategoryService {
    void ensureDefaults(Organization organization);
}
