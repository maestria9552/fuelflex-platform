package com.fuelflex.platform.common.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.depot.repository.DepotRepository;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.dispensingpoint.repository.DispensingPointRepository;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.repository.FuelMeterRepository;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.product.repository.ProductRepository;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.repository.PumpRepository;
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
    private final PumpRepository pumpRepository;
    private final DispensingPointRepository dispensingPointRepository;
    private final FuelMeterRepository fuelMeterRepository;

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
    public Tank findTankByStation(
            UUID stationId,
            UUID tankId
    ) {
        return tankRepository
                .findByIdAndDepotStationId(
                        tankId,
                        stationId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Citerne introuvable dans cette station."
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

    @Override
    public Pump findPump(
            UUID stationId,
            UUID pumpId
    ) {
        Pump pump = pumpRepository.findById(pumpId)
                .orElseThrow(
                        () -> new BusinessException(
                                "Pompe introuvable."
                        )
                );

        if (!pump.getStation().getId().equals(stationId)) {
            throw new BusinessException(
                    "Pompe introuvable dans cette station."
            );
        }

        return pump;
    }

    @Override
    public DispensingPoint findDispensingPoint(
            UUID pumpId,
            UUID dispensingPointId
    ) {
        DispensingPoint dispensingPoint = dispensingPointRepository
                .findById(dispensingPointId)
                .orElseThrow(
                        () -> new BusinessException(
                                "Point de distribution introuvable."
                        )
                );

        if (!dispensingPoint.getPump().getId().equals(pumpId)) {
            throw new BusinessException(
                    "Point de distribution introuvable sur cette pompe."
            );
        }

        return dispensingPoint;
    }

    @Override
    public FuelMeter findFuelMeter(UUID fuelMeterId) {
        return fuelMeterRepository.findById(fuelMeterId)
                .orElseThrow(
                        () -> new BusinessException(
                                "Compteur introuvable."
                        )
                );
    }
}
