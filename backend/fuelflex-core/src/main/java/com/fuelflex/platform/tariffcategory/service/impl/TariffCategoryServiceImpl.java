package com.fuelflex.platform.tariffcategory.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.common.util.TextNormalizer;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.tariffcategory.dto.request.TariffCategoryRequest;
import com.fuelflex.platform.tariffcategory.dto.response.TariffCategoryResponse;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.tariffcategory.mapper.TariffCategoryMapper;
import com.fuelflex.platform.tariffcategory.repository.TariffCategoryRepository;
import com.fuelflex.platform.tariffcategory.service.TariffCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TariffCategoryServiceImpl implements TariffCategoryService {
    private final TariffCategoryRepository repository;
    private final TariffCategoryMapper mapper;
    private final AuthorizationService authorizationService;

    @Override
    public TariffCategoryResponse create(UUID organizationId, TariffCategoryRequest request) {
        Organization organization = authorizedOrganization(organizationId);
        String code = TextNormalizer.normalizeCode(request.getCode());
        String name = TextNormalizer.normalizeText(request.getName());
        validateUnique(organizationId, code, name, null);
        TariffCategory entity = mapper.toEntity(request);
        entity.setOrganization(organization);
        entity.setCode(code);
        entity.setName(name);
        entity.setSystem(false);
        if (entity.getDisplayOrder() == null) entity.setDisplayOrder(1);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffCategoryResponse> findAllByOrganization(UUID organizationId) {
        authorizedOrganization(organizationId);
        return repository.findByOrganizationIdOrderByDisplayOrderAsc(organizationId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffCategoryResponse> findActiveByOrganization(UUID organizationId) {
        authorizedOrganization(organizationId);
        return repository.findByOrganizationIdAndActiveTrueOrderByDisplayOrderAsc(organizationId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TariffCategoryResponse findById(UUID organizationId, UUID tariffCategoryId) {
        authorizedOrganization(organizationId);
        return mapper.toResponse(findScoped(organizationId, tariffCategoryId));
    }

    @Override
    public TariffCategoryResponse update(UUID organizationId, UUID tariffCategoryId, TariffCategoryRequest request) {
        authorizedOrganization(organizationId);
        TariffCategory entity = findScoped(organizationId, tariffCategoryId);
        String requestedCode = TextNormalizer.normalizeCode(request.getCode());
        String name = TextNormalizer.normalizeText(request.getName());
        if (entity.isSystem() && !entity.getCode().equals(requestedCode)) {
            throw new BusinessException("Le code d’une catégorie tarifaire système ne peut pas être modifié.");
        }
        if (entity.isSystem() && Boolean.FALSE.equals(request.getActive())) {
            throw new BusinessException("Une catégorie tarifaire système ne peut pas être désactivée.");
        }
        validateUnique(organizationId, entity.isSystem() ? entity.getCode() : requestedCode,
                name, tariffCategoryId);
        mapper.updateEntity(entity, request);
        entity.setName(name);
        if (!entity.isSystem()) entity.setCode(requestedCode);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(UUID organizationId, UUID tariffCategoryId) {
        authorizedOrganization(organizationId);
        TariffCategory entity = findScoped(organizationId, tariffCategoryId);
        if (entity.isSystem()) {
            throw new BusinessException("Une catégorie tarifaire système ne peut pas être désactivée.");
        }
        if (!entity.isActive()) {
            throw new BusinessException("Cette catégorie tarifaire est déjà désactivée.");
        }
        entity.setActive(false);
        repository.save(entity);
    }

    private Organization authorizedOrganization(UUID organizationId) {
        authorizationService.checkOrganizationAccess(organizationId);
        return authorizationService.getAuthenticatedUser().getOrganization();
    }

    private TariffCategory findScoped(UUID organizationId, UUID id) {
        return repository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new BusinessException("Catégorie tarifaire introuvable dans cette organisation."));
    }

    private void validateUnique(UUID organizationId, String code, String name, UUID currentId) {
        boolean duplicateCode = currentId == null
                ? repository.existsByOrganizationIdAndCodeIgnoreCase(organizationId, code)
                : repository.existsByOrganizationIdAndCodeIgnoreCaseAndIdNot(organizationId, code, currentId);
        boolean duplicateName = currentId == null
                ? repository.existsByOrganizationIdAndNameIgnoreCase(organizationId, name)
                : repository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(organizationId, name, currentId);
        if (duplicateCode) throw new BusinessException("Une catégorie tarifaire utilise déjà ce code dans cette organisation.");
        if (duplicateName) throw new BusinessException("Une catégorie tarifaire portant ce nom existe déjà dans cette organisation.");
    }
}
