package com.fuelflex.platform.tank.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.tank.dto.request.TankRequest;
import com.fuelflex.platform.tank.dto.response.TankResponse;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.entity.TankStatus;

@Component
public class TankMapper {

    public Tank toEntity(TankRequest request) {
        if (request == null) {
            return null;
        }

        return Tank.builder()
                .code(request.getCode())
                .name(request.getName())
                .capacityLiters(request.getCapacityLiters())
                .minimumLevelLiters(request.getMinimumLevelLiters())
                .maximumLevelLiters(request.getMaximumLevelLiters())
                .status(
                        request.getStatus() == null
                                ? TankStatus.INACTIVE
                                : request.getStatus()
                )
                .location(request.getLocation())
                .displayOrder(request.getDisplayOrder())
                .active(
                        request.getActive() == null
                                || request.getActive()
                )
                .build();
    }

    public void updateEntity(
            Tank tank,
            TankRequest request
    ) {
        if (tank == null || request == null) {
            return;
        }

        tank.setCode(request.getCode());
        tank.setName(request.getName());
        tank.setCapacityLiters(request.getCapacityLiters());
        tank.setMinimumLevelLiters(request.getMinimumLevelLiters());
        tank.setMaximumLevelLiters(request.getMaximumLevelLiters());
        tank.setLocation(request.getLocation());
        tank.setDisplayOrder(request.getDisplayOrder());

        if (request.getStatus() != null) {
            tank.setStatus(request.getStatus());
        }

        if (request.getActive() != null) {
            tank.setActive(request.getActive());
        }
    }

    public TankResponse toResponse(Tank tank) {
        if (tank == null) {
            return null;
        }

        return TankResponse.builder()
                .id(tank.getId())
                .organizationId(
                        tank.getDepot()
                                .getStation()
                                .getOrganization()
                                .getId()
                )
                .stationId(
                        tank.getDepot()
                                .getStation()
                                .getId()
                )
                .stationCode(
                        tank.getDepot()
                                .getStation()
                                .getCode()
                )
                .stationName(
                        tank.getDepot()
                                .getStation()
                                .getName()
                )
                .depotId(tank.getDepot().getId())
                .depotCode(tank.getDepot().getCode())
                .depotName(tank.getDepot().getName())
                .productId(tank.getProduct().getId())
                .productCode(tank.getProduct().getCode())
                .productName(tank.getProduct().getName())
                .code(tank.getCode())
                .name(tank.getName())
                .capacityLiters(tank.getCapacityLiters())
                .minimumLevelLiters(tank.getMinimumLevelLiters())
                .maximumLevelLiters(tank.getMaximumLevelLiters())
                .status(tank.getStatus())
                .location(tank.getLocation())
                .displayOrder(tank.getDisplayOrder())
                .active(tank.isActive())
                .createdAt(tank.getCreatedAt())
                .updatedAt(tank.getUpdatedAt())
                .build();
    }
}