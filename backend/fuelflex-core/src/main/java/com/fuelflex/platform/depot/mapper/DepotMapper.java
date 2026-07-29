package com.fuelflex.platform.depot.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.depot.dto.request.DepotRequest;
import com.fuelflex.platform.depot.dto.response.DepotResponse;
import com.fuelflex.platform.depot.entity.Depot;

@Component
public class DepotMapper {

    public Depot toEntity(DepotRequest request) {
        if (request == null) {
            return null;
        }

        return Depot.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive() == null || request.getActive())
                .build();
    }

    public void updateEntity(
            Depot depot,
            DepotRequest request
    ) {
        if (depot == null || request == null) {
            return;
        }

        depot.setCode(request.getCode());
        depot.setName(request.getName());
        depot.setDescription(request.getDescription());
        depot.setLocation(request.getLocation());
        depot.setDisplayOrder(request.getDisplayOrder());

        if (request.getActive() != null) {
            depot.setActive(request.getActive());
        }
    }

    public DepotResponse toResponse(Depot depot) {
        if (depot == null) {
            return null;
        }

        return DepotResponse.builder()
                .id(depot.getId())
                .organizationId(
                        depot.getStation()
                                .getOrganization()
                                .getId()
                )
                .stationId(depot.getStation().getId())
                .stationCode(depot.getStation().getCode())
                .stationName(depot.getStation().getName())
                .code(depot.getCode())
                .name(depot.getName())
                .description(depot.getDescription())
                .location(depot.getLocation())
                .displayOrder(depot.getDisplayOrder())
                .active(depot.isActive())
                .createdAt(depot.getCreatedAt())
                .updatedAt(depot.getUpdatedAt())
                .build();
    }
}