package com.fuelflex.platform.stationproduct.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.stationproduct.dto.request.StationProductRequest;
import com.fuelflex.platform.stationproduct.dto.response.StationProductResponse;
import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.stationproduct.mapper.StationProductMapper;
import com.fuelflex.platform.stationproduct.repository.StationProductRepository;
import com.fuelflex.platform.stationproduct.service.StationProductService;
import com.fuelflex.platform.tank.repository.TankRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StationProductServiceImpl implements StationProductService {
    private final StationProductRepository stationProductRepository;
    private final StationProductMapper stationProductMapper;
    private final EntityLookupService entityLookupService;
    private final AuthorizationService authorizationService;
    private final TankRepository tankRepository;

    @Override
    public StationProductResponse create(UUID organizationId, UUID stationId, StationProductRequest request) {
        authorizationService.checkOrganizationAccess(organizationId);
        Station station = entityLookupService.findStation(organizationId, stationId);
        Product product = entityLookupService.findProduct(organizationId, request.getProductId());
        validateSameOrganization(organizationId, station, product);
        if (stationProductRepository.existsByStationIdAndProductId(stationId, product.getId())) {
            throw new BusinessException("Ce produit est déjà associé à cette station.");
        }
        StationProduct entity = stationProductMapper.toEntity(request);
        entity.setStation(station);
        entity.setProduct(product);
        if (entity.getDisplayOrder() == null) {
            entity.setDisplayOrder(1);
        }
        return stationProductMapper.toResponse(stationProductRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationProductResponse> findAllByStation(UUID organizationId, UUID stationId) {
        validateScope(organizationId, stationId);
        return stationProductRepository.findByStationIdOrderByDisplayOrderAsc(stationId)
                .stream().map(stationProductMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationProductResponse> findActiveByStation(UUID organizationId, UUID stationId) {
        validateScope(organizationId, stationId);
        return stationProductRepository.findByStationIdAndActiveTrueOrderByDisplayOrderAsc(stationId)
                .stream().map(stationProductMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StationProductResponse findById(UUID organizationId, UUID stationId, UUID stationProductId) {
        validateScope(organizationId, stationId);
        return stationProductMapper.toResponse(findScoped(stationId, stationProductId));
    }

    @Override
    public StationProductResponse update(UUID organizationId, UUID stationId, UUID stationProductId,
            StationProductRequest request) {
        validateScope(organizationId, stationId);
        StationProduct entity = findScoped(stationId, stationProductId);
        if (!entity.getProduct().getId().equals(request.getProductId())) {
            throw new BusinessException(
                    "Le produit d’une association StationProduct ne peut pas être modifié. Désactivez-la puis créez une autre association.");
        }
        if (Boolean.FALSE.equals(request.getActive()) && entity.isActive()) {
            validateNotUsedByTank(stationId, entity.getProduct().getId());
        }
        stationProductMapper.updateEntity(entity, request);
        return stationProductMapper.toResponse(stationProductRepository.save(entity));
    }

    @Override
    public void delete(UUID organizationId, UUID stationId, UUID stationProductId) {
        validateScope(organizationId, stationId);
        StationProduct entity = findScoped(stationId, stationProductId);
        if (!entity.isActive()) {
            throw new BusinessException("Ce produit est déjà désactivé pour cette station.");
        }
        validateNotUsedByTank(stationId, entity.getProduct().getId());
        entity.setActive(false);
        stationProductRepository.save(entity);
    }

    private void validateScope(UUID organizationId, UUID stationId) {
        authorizationService.checkOrganizationAccess(organizationId);
        entityLookupService.findStation(organizationId, stationId);
    }

    private StationProduct findScoped(UUID stationId, UUID stationProductId) {
        return stationProductRepository.findByIdAndStationId(stationProductId, stationId)
                .orElseThrow(() -> new BusinessException("Produit de station introuvable dans cette station."));
    }

    private void validateSameOrganization(UUID organizationId, Station station, Product product) {
        if (!organizationId.equals(station.getOrganization().getId())
                || !organizationId.equals(product.getOrganization().getId())) {
            throw new BusinessException("La station et le produit doivent appartenir à la même organisation.");
        }
    }

    private void validateNotUsedByTank(UUID stationId, UUID productId) {
        if (tankRepository.existsByDepotStationIdAndProductId(stationId, productId)) {
            throw new BusinessException(
                    "Ce produit ne peut pas être désactivé car il est utilisé par une citerne de la station.");
        }
    }
}
