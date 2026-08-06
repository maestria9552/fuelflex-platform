package com.fuelflex.platform.common.service;

import java.util.UUID;

import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.tank.entity.Tank;

public interface EntityLookupService {

    Station findStation(
            UUID organizationId,
            UUID stationId
    );

    Depot findDepot(
            UUID stationId,
            UUID depotId
    );

    Tank findTank(
            UUID depotId,
            UUID tankId
    );

    Tank findTankByStation(
            UUID stationId,
            UUID tankId
    );

    Product findProduct(
            UUID organizationId,
            UUID productId
    );

    Pump findPump(
            UUID stationId,
            UUID pumpId
    );

    DispensingPoint findDispensingPoint(
            UUID pumpId,
            UUID dispensingPointId
    );

    FuelMeter findFuelMeter(UUID fuelMeterId);
}
