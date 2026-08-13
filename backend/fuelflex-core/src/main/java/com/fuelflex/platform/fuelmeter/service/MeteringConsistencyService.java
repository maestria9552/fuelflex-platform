package com.fuelflex.platform.fuelmeter.service;

import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.pump.entity.MeteringLevel;
import com.fuelflex.platform.pump.entity.Pump;

public interface MeteringConsistencyService {

    void validateBeforeActivatingGlobalMeter(
            Pump pump,
            FuelMeter currentMeter
    );

    void validateBeforeActivatingDispensingPointMeter(
            Pump pump,
            DispensingPoint dispensingPoint,
            FuelMeter currentMeter
    );

    void validateBeforeActivatingDispensingPoint(
            Pump pump,
            DispensingPoint dispensingPoint
    );

    void validateBeforeChangingMeteringLevel(
            Pump pump,
            MeteringLevel newLevel
    );

    void validateBeforeActivatingPump(Pump pump);

    void validateCompletePumpConfiguration(Pump pump);

    void validateBeforeDeactivatingMeter(FuelMeter fuelMeter);

    void validateBeforeDeactivatingDispensingPoint(
            DispensingPoint dispensingPoint
    );
}
