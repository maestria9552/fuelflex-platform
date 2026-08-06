package com.fuelflex.platform.pump.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.pump.dto.request.PumpRequest;
import com.fuelflex.platform.pump.dto.response.PumpResponse;
import com.fuelflex.platform.pump.entity.Pump;

@Component
public class PumpMapper {

    public Pump toEntity(PumpRequest request) {

        if (request == null) {
            return null;
        }

        return Pump.builder()
                .code(request.getCode())
                .name(request.getName())
                .pumpNumber(request.getPumpNumber())
                .meteringLevel(request.getMeteringLevel())
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .location(request.getLocation())
                .displayOrder(request.getDisplayOrder())
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();
    }

    public void updateEntity(
            Pump pump,
            PumpRequest request
    ) {

        pump.setCode(request.getCode());
        pump.setName(request.getName());
        pump.setPumpNumber(request.getPumpNumber());
        pump.setMeteringLevel(request.getMeteringLevel());
        pump.setManufacturer(request.getManufacturer());
        pump.setModel(request.getModel());
        pump.setSerialNumber(request.getSerialNumber());
        pump.setLocation(request.getLocation());
        pump.setDisplayOrder(request.getDisplayOrder());

        if (request.getActive() != null) {
            pump.setActive(request.getActive());
        }
    }

    public PumpResponse toResponse(Pump pump) {

        if (pump == null) {
            return null;
        }

        return PumpResponse.builder()
                .id(pump.getId())
                .stationId(pump.getStation().getId())
                .stationCode(pump.getStation().getCode())
                .stationName(pump.getStation().getName())
                .code(pump.getCode())
                .name(pump.getName())
                .pumpNumber(pump.getPumpNumber())
                .meteringLevel(pump.getMeteringLevel())
                .manufacturer(pump.getManufacturer())
                .model(pump.getModel())
                .serialNumber(pump.getSerialNumber())
                .status(pump.getStatus())
                .location(pump.getLocation())
                .displayOrder(pump.getDisplayOrder())
                .active(pump.isActive())
                .createdAt(pump.getCreatedAt())
                .updatedAt(pump.getUpdatedAt())
                .build();
    }
}
