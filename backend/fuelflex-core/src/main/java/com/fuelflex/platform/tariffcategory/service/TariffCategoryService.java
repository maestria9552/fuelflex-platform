package com.fuelflex.platform.tariffcategory.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.tariffcategory.dto.request.TariffCategoryRequest;
import com.fuelflex.platform.tariffcategory.dto.response.TariffCategoryResponse;

public interface TariffCategoryService {
    TariffCategoryResponse create(UUID organizationId, TariffCategoryRequest request);
    List<TariffCategoryResponse> findAllByOrganization(UUID organizationId);
    List<TariffCategoryResponse> findActiveByOrganization(UUID organizationId);
    TariffCategoryResponse findById(UUID organizationId, UUID tariffCategoryId);
    TariffCategoryResponse update(UUID organizationId, UUID tariffCategoryId, TariffCategoryRequest request);
    void delete(UUID organizationId, UUID tariffCategoryId);
}
