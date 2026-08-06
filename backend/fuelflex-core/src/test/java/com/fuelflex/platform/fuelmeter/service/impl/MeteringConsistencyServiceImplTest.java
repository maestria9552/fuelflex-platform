package com.fuelflex.platform.fuelmeter.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.dispensingpoint.repository.DispensingPointRepository;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.repository.FuelMeterRepository;
import com.fuelflex.platform.pump.entity.MeteringLevel;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.tank.entity.Tank;

class MeteringConsistencyServiceImplTest {

    private FuelMeterRepository fuelMeterRepository;
    private DispensingPointRepository dispensingPointRepository;
    private MeteringConsistencyServiceImpl service;

    @BeforeEach
    void setUp() {
        fuelMeterRepository = mock(FuelMeterRepository.class);
        dispensingPointRepository = mock(
                DispensingPointRepository.class
        );
        service = new MeteringConsistencyServiceImpl(
                fuelMeterRepository,
                dispensingPointRepository
        );
    }

    @Test
    void acceptsFirstGlobalMeterForPumpLevel() {
        Pump pump = pump(MeteringLevel.PUMP);
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(0L);
        when(fuelMeterRepository
                .existsByDispensingPointPumpAndActiveTrue(pump))
                .thenReturn(false);
        when(activePoints(pump)).thenReturn(List.of());

        assertDoesNotThrow(
                () -> service.validateBeforeActivatingGlobalMeter(
                        pump,
                        null
                )
        );
    }

    @Test
    void refusesSecondGlobalMeter() {
        Pump pump = pump(MeteringLevel.PUMP);
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(1L);

        assertThrows(
                BusinessException.class,
                () -> service.validateBeforeActivatingGlobalMeter(
                        pump,
                        null
                )
        );
    }

    @Test
    void refusesIndividualMeterForPumpLevel() {
        Pump pump = pump(MeteringLevel.PUMP);
        DispensingPoint point = point(pump, tank());

        assertThrows(
                BusinessException.class,
                () -> service
                        .validateBeforeActivatingDispensingPointMeter(
                                pump,
                                point,
                                null
                        )
        );
    }

    @Test
    void acceptsSameTankForPumpLevel() {
        Pump pump = pump(MeteringLevel.PUMP);
        Tank tank = tank();
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(0L);
        when(fuelMeterRepository
                .existsByDispensingPointPumpAndActiveTrue(pump))
                .thenReturn(false);
        when(activePoints(pump)).thenReturn(
                List.of(point(pump, tank), point(pump, tank))
        );

        assertDoesNotThrow(
                () -> service.validateBeforeActivatingGlobalMeter(
                        pump,
                        null
                )
        );
    }

    @Test
    void refusesDifferentTanksForPumpLevel() {
        Pump pump = pump(MeteringLevel.PUMP);
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(0L);
        when(fuelMeterRepository
                .existsByDispensingPointPumpAndActiveTrue(pump))
                .thenReturn(false);
        when(activePoints(pump)).thenReturn(
                List.of(point(pump, tank()), point(pump, tank()))
        );

        assertThrows(
                BusinessException.class,
                () -> service.validateBeforeActivatingGlobalMeter(
                        pump,
                        null
                )
        );
    }

    @Test
    void refusesGlobalMeterForDispensingPointLevel() {
        Pump pump = pump(MeteringLevel.DISPENSING_POINT);

        assertThrows(
                BusinessException.class,
                () -> service.validateBeforeActivatingGlobalMeter(
                        pump,
                        null
                )
        );
    }

    @Test
    void acceptsFirstIndividualMeter() {
        Pump pump = pump(MeteringLevel.DISPENSING_POINT);
        DispensingPoint point = point(pump, tank());
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(0L);
        when(fuelMeterRepository
                .countByDispensingPointAndActiveTrue(point))
                .thenReturn(0L);

        assertDoesNotThrow(
                () -> service
                        .validateBeforeActivatingDispensingPointMeter(
                                pump,
                                point,
                                null
                        )
        );
    }

    @Test
    void refusesSecondIndividualMeterOnSamePoint() {
        Pump pump = pump(MeteringLevel.DISPENSING_POINT);
        DispensingPoint point = point(pump, tank());
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(0L);
        when(fuelMeterRepository
                .countByDispensingPointAndActiveTrue(point))
                .thenReturn(1L);

        assertThrows(
                BusinessException.class,
                () -> service
                        .validateBeforeActivatingDispensingPointMeter(
                                pump,
                                point,
                                null
                        )
        );
    }

    @Test
    void acceptsDifferentTanksForDispensingPointLevel() {
        Pump pump = pump(MeteringLevel.DISPENSING_POINT);
        DispensingPoint first = point(pump, tank());
        DispensingPoint second = point(pump, tank());
        when(fuelMeterRepository
                .countByDispensingPointAndActiveTrue(first))
                .thenReturn(1L);
        when(fuelMeterRepository
                .countByDispensingPointAndActiveTrue(second))
                .thenReturn(1L);
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(0L);

        assertDoesNotThrow(
                () -> service.validateBeforeActivatingDispensingPoint(
                        pump,
                        first
                )
        );
        assertDoesNotThrow(
                () -> service.validateBeforeActivatingDispensingPoint(
                        pump,
                        second
                )
        );
    }

    @Test
    void refusesActivePointWithoutMeter() {
        Pump pump = pump(MeteringLevel.DISPENSING_POINT);
        DispensingPoint point = point(pump, tank());
        when(fuelMeterRepository
                .countByDispensingPointAndActiveTrue(point))
                .thenReturn(0L);

        assertThrows(
                BusinessException.class,
                () -> service.validateBeforeActivatingDispensingPoint(
                        pump,
                        point
                )
        );
    }

    @Test
    void refusesPumpToDispensingPointWithGlobalMeter() {
        Pump pump = pump(MeteringLevel.PUMP);
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(1L);

        assertThrows(
                BusinessException.class,
                () -> service.validateBeforeChangingMeteringLevel(
                        pump,
                        MeteringLevel.DISPENSING_POINT
                )
        );
    }

    @Test
    void refusesDispensingPointToPumpWithIndividualMeter() {
        Pump pump = pump(MeteringLevel.DISPENSING_POINT);
        when(fuelMeterRepository
                .existsByDispensingPointPumpAndActiveTrue(pump))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.validateBeforeChangingMeteringLevel(
                        pump,
                        MeteringLevel.PUMP
                )
        );
    }

    @Test
    void alwaysRefusesHybridMode() {
        Pump pump = pump(MeteringLevel.PUMP);
        when(fuelMeterRepository.countByPumpAndActiveTrue(pump))
                .thenReturn(0L);
        when(fuelMeterRepository
                .existsByDispensingPointPumpAndActiveTrue(pump))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.validateBeforeActivatingGlobalMeter(
                        pump,
                        null
                )
        );
    }

    private List<DispensingPoint> activePoints(Pump pump) {
        return dispensingPointRepository
                .findByPumpAndActiveTrueOrderByDisplayOrderAscNameAsc(
                        pump
                );
    }

    private Pump pump(MeteringLevel level) {
        return Pump.builder()
                .id(UUID.randomUUID())
                .meteringLevel(level)
                .build();
    }

    private Tank tank() {
        return Tank.builder()
                .id(UUID.randomUUID())
                .build();
    }

    private DispensingPoint point(Pump pump, Tank tank) {
        return DispensingPoint.builder()
                .id(UUID.randomUUID())
                .pump(pump)
                .tank(tank)
                .active(true)
                .build();
    }
}
