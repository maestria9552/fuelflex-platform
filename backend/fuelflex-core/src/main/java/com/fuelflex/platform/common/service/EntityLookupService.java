package com.fuelflex.platform.common.service;

import java.util.UUID;

import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.product.entity.Product;
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

    Product findProduct(
            UUID organizationId,
            UUID productId
    );
}