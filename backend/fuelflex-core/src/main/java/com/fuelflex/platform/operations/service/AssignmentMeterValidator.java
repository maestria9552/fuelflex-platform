package com.fuelflex.platform.operations.service;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPointStatus;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.fuelmeter.service.MeteringConsistencyService;
import com.fuelflex.platform.pump.entity.MeteringLevel;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.entity.PumpStatus;
import com.fuelflex.platform.station.entity.Station;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssignmentMeterValidator {

    private final MeteringConsistencyService meteringConsistencyService;

    public Pump validate(FuelMeter meter, Station station) {
        if (!meter.isActive() || meter.getStatus() != FuelMeterStatus.ACTIVE) {
            throw new BusinessException("The fuel meter is inactive.");
        }

        DispensingPoint point = meter.getDispensingPoint();
        Pump pump = point == null ? meter.getPump() : point.getPump();
        if (pump == null || !pump.getStation().getId().equals(station.getId())) {
            throw new ForbiddenException("The fuel meter does not belong to this station.");
        }
        if (!pump.isActive() || pump.getStatus() != PumpStatus.ACTIVE) {
            throw new BusinessException("The fuel meter pump is inactive.");
        }
        if (pump.getMeteringLevel() == MeteringLevel.PUMP
                && (meter.getPump() == null || point != null)) {
            throw new BusinessException(
                    "Fuel meter configuration is inconsistent with PUMP metering level."
            );
        }
        if (pump.getMeteringLevel() == MeteringLevel.DISPENSING_POINT
                && (point == null
                        || meter.getPump() != null
                        || !point.isActive()
                        || point.getStatus() != DispensingPointStatus.ACTIVE)) {
            throw new BusinessException(
                    "Fuel meter configuration is inconsistent with DISPENSING_POINT metering level."
            );
        }

        meteringConsistencyService.validateCompletePumpConfiguration(pump);
        return pump;
    }
}
