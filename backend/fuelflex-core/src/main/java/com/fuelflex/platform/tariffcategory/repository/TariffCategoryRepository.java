package com.fuelflex.platform.tariffcategory.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.tariffcategory.entity.TariffCategory;

public interface TariffCategoryRepository extends JpaRepository<TariffCategory, UUID> {
    List<TariffCategory> findByOrganizationIdOrderByDisplayOrderAsc(UUID organizationId);
    List<TariffCategory> findByOrganizationIdAndActiveTrueOrderByDisplayOrderAsc(UUID organizationId);
    Optional<TariffCategory> findByIdAndOrganizationId(UUID id, UUID organizationId);
    boolean existsByOrganizationIdAndCodeIgnoreCase(UUID organizationId, String code);
    boolean existsByOrganizationIdAndNameIgnoreCase(UUID organizationId, String name);
    boolean existsByOrganizationIdAndCodeIgnoreCaseAndIdNot(UUID organizationId, String code, UUID id);
    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(UUID organizationId, String name, UUID id);
}
