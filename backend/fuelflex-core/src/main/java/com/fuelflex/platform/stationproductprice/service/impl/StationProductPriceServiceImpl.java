package com.fuelflex.platform.stationproductprice.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.stationproduct.repository.StationProductRepository;
import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceRequest;
import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceUpdateRequest;
import com.fuelflex.platform.stationproductprice.dto.response.StationProductPriceResponse;
import com.fuelflex.platform.stationproductprice.entity.StationProductPrice;
import com.fuelflex.platform.stationproductprice.mapper.StationProductPriceMapper;
import com.fuelflex.platform.stationproductprice.repository.StationProductPriceRepository;
import com.fuelflex.platform.stationproductprice.service.StationProductPriceService;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.tariffcategory.repository.TariffCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StationProductPriceServiceImpl implements StationProductPriceService {
    private final StationProductPriceRepository repository;
    private final StationProductRepository stationProductRepository;
    private final TariffCategoryRepository tariffCategoryRepository;
    private final StationProductPriceMapper mapper;
    private final EntityLookupService entityLookupService;
    private final AuthorizationService authorizationService;

    @Override
    public StationProductPriceResponse create(UUID organizationId, UUID stationId, UUID stationProductId,
            StationProductPriceRequest request) {
        StationProduct stationProduct = validateScope(organizationId, stationId, stationProductId);
        if (!stationProduct.isActive()) {
            throw new BusinessException("Un prix ne peut pas être créé pour un produit inactif dans cette station.");
        }
        TariffCategory category = tariffCategoryRepository
                .findByIdAndOrganizationId(request.getTariffCategoryId(), organizationId)
                .orElseThrow(() -> new BusinessException("Catégorie tarifaire introuvable dans cette organisation."));
        if (!category.isActive()) {
            throw new BusinessException("Un prix ne peut pas être créé pour une catégorie tarifaire inactive.");
        }
        validateSameOrganization(organizationId, stationProduct, category);
        validatePositivePrice(request.getPrice());

        var existing = repository.findByStationProductIdAndTariffCategoryId(stationProductId, category.getId());
        if (existing.isPresent()) {
            StationProductPrice price = existing.get();
            if (price.isActive()) {
                throw new BusinessException("Un prix existe déjà pour ce produit de station et cette catégorie tarifaire.");
            }
            price.setPrice(request.getPrice());
            price.setActive(true);
            return mapper.toResponse(repository.save(price));
        }

        StationProductPrice entity = mapper.toEntity(request);
        entity.setStationProduct(stationProduct);
        entity.setTariffCategory(category);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationProductPriceResponse> findAllByStationProduct(UUID organizationId, UUID stationId,
            UUID stationProductId) {
        validateScope(organizationId, stationId, stationProductId);
        return repository.findByStationProductIdOrderByTariffCategoryDisplayOrderAsc(stationProductId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationProductPriceResponse> findActiveByStationProduct(UUID organizationId, UUID stationId,
            UUID stationProductId) {
        validateScope(organizationId, stationId, stationProductId);
        return repository.findByStationProductIdAndActiveTrueOrderByTariffCategoryDisplayOrderAsc(stationProductId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StationProductPriceResponse findById(UUID organizationId, UUID stationId, UUID stationProductId,
            UUID stationProductPriceId) {
        validateScope(organizationId, stationId, stationProductId);
        return mapper.toResponse(findScoped(stationProductId, stationProductPriceId));
    }

    @Override
    public StationProductPriceResponse update(UUID organizationId, UUID stationId, UUID stationProductId,
            UUID stationProductPriceId, StationProductPriceUpdateRequest request) {
        validateScope(organizationId, stationId, stationProductId);
        validatePositivePrice(request.getPrice());
        StationProductPrice entity = findScoped(stationProductId, stationProductPriceId);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(UUID organizationId, UUID stationId, UUID stationProductId, UUID stationProductPriceId) {
        validateScope(organizationId, stationId, stationProductId);
        StationProductPrice entity = findScoped(stationProductId, stationProductPriceId);
        if (!entity.isActive()) throw new BusinessException("Ce prix est déjà désactivé.");
        entity.setActive(false);
        repository.save(entity);
    }

    private StationProduct validateScope(UUID organizationId, UUID stationId, UUID stationProductId) {
        authorizationService.checkOrganizationAccess(organizationId);
        entityLookupService.findStation(organizationId, stationId);
        return stationProductRepository.findByIdAndStationId(stationProductId, stationId)
                .orElseThrow(() -> new BusinessException("Produit de station introuvable dans cette station."));
    }

    private StationProductPrice findScoped(UUID stationProductId, UUID priceId) {
        return repository.findByIdAndStationProductId(priceId, stationProductId)
                .orElseThrow(() -> new BusinessException("Prix introuvable pour ce produit de station."));
    }

    private void validateSameOrganization(UUID organizationId, StationProduct stationProduct,
            TariffCategory category) {
        if (!organizationId.equals(stationProduct.getStation().getOrganization().getId())
                || !organizationId.equals(category.getOrganization().getId())) {
            throw new BusinessException("Le produit de station et la catégorie tarifaire doivent appartenir à la même organisation.");
        }
    }

    private void validatePositivePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new BusinessException("Le prix doit être strictement supérieur à zéro.");
        }
    }
}
