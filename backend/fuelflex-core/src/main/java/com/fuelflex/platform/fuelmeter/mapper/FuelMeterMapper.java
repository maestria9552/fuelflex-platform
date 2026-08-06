package com.fuelflex.platform.fuelmeter.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.fuelmeter.dto.request.FuelMeterRequest;
import com.fuelflex.platform.fuelmeter.dto.response.FuelMeterResponse;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;

@Component
public class FuelMeterMapper {

    public FuelMeter toEntity(FuelMeterRequest request) {
        if (request == null) {
            return null;
        }

        return FuelMeter.builder()
                .code(request.getCode())
                .name(request.getName())
                .technology(request.getTechnology())
                .currentIndex(request.getCurrentIndex())
                .status(request.getStatus())
                .displayOrder(request.getDisplayOrder())
                .active(
                        request.getActive() == null
                                || request.getActive()
                )
                .build();
    }

    public void updateEntity(
            FuelMeter fuelMeter,
            FuelMeterRequest request
    ) {
        if (fuelMeter == null || request == null) {
            return;
        }

        fuelMeter.setCode(request.getCode());
        fuelMeter.setName(request.getName());
        fuelMeter.setTechnology(request.getTechnology());
        fuelMeter.setCurrentIndex(request.getCurrentIndex());
        fuelMeter.setDisplayOrder(request.getDisplayOrder());

        if (request.getStatus() != null) {
            fuelMeter.setStatus(request.getStatus());
        }

        if (request.getActive() != null) {
            fuelMeter.setActive(request.getActive());
        }
    }

    public FuelMeterResponse toResponse(FuelMeter fuelMeter) {
        if (fuelMeter == null) {
            return null;
        }

        return FuelMeterResponse.builder()
                .id(fuelMeter.getId())
                .pumpId(
                        fuelMeter.getPump() != null
                                ? fuelMeter.getPump().getId()
                                : null
                )
                .dispensingPointId(
                        fuelMeter.getDispensingPoint() != null
                                ? fuelMeter.getDispensingPoint().getId()
                                : null
                )
                .code(fuelMeter.getCode())
                .name(fuelMeter.getName())
                .technology(fuelMeter.getTechnology())
                .currentIndex(fuelMeter.getCurrentIndex())
                .status(fuelMeter.getStatus())
                .displayOrder(fuelMeter.getDisplayOrder())
                .active(fuelMeter.isActive())
                .createdAt(fuelMeter.getCreatedAt())
                .updatedAt(fuelMeter.getUpdatedAt())
                .build();
    }
}
