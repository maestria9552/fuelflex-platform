package com.fuelflex.platform.common.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.depot.repository.DepotRepository;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.product.repository.ProductRepository;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.repository.TankRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntityLookupServiceImpl implements EntityLookupService {

    private final StationRepository stationRepository;
    private final DepotRepository depotRepository;
    private final TankRepository tankRepository;
    private final ProductRepository productRepository;

    @Override
    public Station findStation(
            UUID organizationId,
            UUID stationId
    ) {
        return stationRepository
                .findByIdAndOrganizationId(
                        stationId,
                        organizationId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Station introuvable dans cette organisation."
                        )
                );
    }

    @Override
    public Depot findDepot(
            UUID stationId,
            UUID depotId
    ) {
        return depotRepository
                .findByIdAndStationId(
                        depotId,
                        stationId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Dépôt introuvable dans cette station."
                        )
                );
    }

    @Override
    public Tank findTank(
            UUID depotId,
            UUID tankId
    ) {
        return tankRepository
                .findByIdAndDepotId(
                        tankId,
                        depotId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Citerne introuvable dans ce dépôt."
                        )
                );
    }

    @Override
    public Product findProduct(
            UUID organizationId,
            UUID productId
    ) {
        return productRepository
                .findByIdAndOrganizationId(
                        productId,
                        organizationId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Produit introuvable dans cette organisation."
                        )
                );
    }
}