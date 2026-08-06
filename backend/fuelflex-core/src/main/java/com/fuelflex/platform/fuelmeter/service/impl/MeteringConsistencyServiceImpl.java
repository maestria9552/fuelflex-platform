package com.fuelflex.platform.fuelmeter.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.dispensingpoint.repository.DispensingPointRepository;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.repository.FuelMeterRepository;
import com.fuelflex.platform.fuelmeter.service.MeteringConsistencyService;
import com.fuelflex.platform.pump.entity.MeteringLevel;
import com.fuelflex.platform.pump.entity.Pump;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeteringConsistencyServiceImpl
        implements MeteringConsistencyService {

    private final FuelMeterRepository fuelMeterRepository;
    private final DispensingPointRepository dispensingPointRepository;

    @Override
    public void validateBeforeActivatingGlobalMeter(
            Pump pump,
            FuelMeter currentMeter
    ) {
        if (pump.getMeteringLevel() != MeteringLevel.PUMP) {
            throw new BusinessException(
                    "Un compteur global est incompatible avec le niveau de comptage par point de distribution."
            );
        }

        long globalCount = currentMeter == null
                ? fuelMeterRepository.countByPumpAndActiveTrue(pump)
                : fuelMeterRepository.countByPumpAndActiveTrueAndIdNot(
                        pump,
                        currentMeter.getId()
                );
        if (globalCount > 0) {
            throw new BusinessException(
                    "Une pompe en comptage global ne peut posséder qu’un seul compteur actif."
            );
        }

        if (fuelMeterRepository
                .existsByDispensingPointPumpAndActiveTrue(pump)) {
            throw new BusinessException(
                    "Le mode hybride de comptage est interdit."
            );
        }

        validateSameTankForActiveDispensingPoints(pump, null);
    }

    @Override
    public void validateBeforeActivatingDispensingPointMeter(
            Pump pump,
            DispensingPoint dispensingPoint,
            FuelMeter currentMeter
    ) {
        if (pump.getMeteringLevel()
                != MeteringLevel.DISPENSING_POINT) {
            throw new BusinessException(
                    "Un compteur individuel est incompatible avec le niveau de comptage global."
            );
        }

        if (fuelMeterRepository.countByPumpAndActiveTrue(pump) > 0) {
            throw new BusinessException(
                    "Le mode hybride de comptage est interdit."
            );
        }

        long individualCount = currentMeter == null
                ? fuelMeterRepository
                        .countByDispensingPointAndActiveTrue(
                                dispensingPoint
                        )
                : fuelMeterRepository
                        .countByDispensingPointAndActiveTrueAndIdNot(
                                dispensingPoint,
                                currentMeter.getId()
                        );
        if (individualCount > 0) {
            throw new BusinessException(
                    "Chaque point de distribution actif ne peut posséder qu’un seul compteur actif."
            );
        }
    }

    @Override
    public void validateBeforeActivatingDispensingPoint(
            Pump pump,
            DispensingPoint dispensingPoint
    ) {
        if (pump.getMeteringLevel() == MeteringLevel.PUMP) {
            validateSameTankForActiveDispensingPoints(
                    pump,
                    dispensingPoint
            );
            if (fuelMeterRepository
                    .existsByDispensingPointPumpAndActiveTrue(pump)) {
                throw new BusinessException(
                        "Le mode hybride de comptage est interdit."
                );
            }
            return;
        }

        long count = fuelMeterRepository
                .countByDispensingPointAndActiveTrue(
                        dispensingPoint
                );
        if (count != 1) {
            throw new BusinessException(
                    "Chaque point de distribution actif doit posséder exactement un compteur actif."
            );
        }
        if (fuelMeterRepository.countByPumpAndActiveTrue(pump) > 0) {
            throw new BusinessException(
                    "Le mode hybride de comptage est interdit."
            );
        }
    }

    @Override
    public void validateBeforeChangingMeteringLevel(
            Pump pump,
            MeteringLevel newLevel
    ) {
        if (newLevel == pump.getMeteringLevel()) {
            return;
        }

        if (newLevel == MeteringLevel.PUMP) {
            if (fuelMeterRepository
                    .existsByDispensingPointPumpAndActiveTrue(pump)) {
                throw incoherentLevelChange();
            }
            validateSameTankForActiveDispensingPoints(pump, null);
            return;
        }

        if (fuelMeterRepository.countByPumpAndActiveTrue(pump) > 0) {
            throw incoherentLevelChange();
        }

        for (DispensingPoint point : activePoints(pump)) {
            if (fuelMeterRepository
                    .countByDispensingPointAndActiveTrue(point) != 1) {
                throw incoherentLevelChange();
            }
        }
    }

    @Override
    public void validateBeforeActivatingPump(Pump pump) {
        if (pump.getMeteringLevel() == MeteringLevel.PUMP) {
            if (fuelMeterRepository.countByPumpAndActiveTrue(pump) != 1) {
                throw new BusinessException(
                        "Une pompe en comptage global doit posséder exactement un compteur actif."
                );
            }
            if (fuelMeterRepository
                    .existsByDispensingPointPumpAndActiveTrue(pump)) {
                throw new BusinessException(
                        "Le mode hybride de comptage est interdit."
                );
            }
            validateSameTankForActiveDispensingPoints(pump, null);
            return;
        }

        if (fuelMeterRepository.countByPumpAndActiveTrue(pump) > 0) {
            throw new BusinessException(
                    "Un compteur global est incompatible avec le niveau de comptage par point de distribution."
            );
        }
        for (DispensingPoint point : activePoints(pump)) {
            if (fuelMeterRepository
                    .countByDispensingPointAndActiveTrue(point) != 1) {
                throw new BusinessException(
                        "Chaque point de distribution actif doit posséder exactement un compteur actif."
                );
            }
        }
    }

    @Override
    public void validateBeforeDeactivatingMeter(FuelMeter fuelMeter) {
        if (fuelMeter.getDispensingPoint() == null) {
            return;
        }

        DispensingPoint point = fuelMeter.getDispensingPoint();
        if (point.isActive()
                && point.getPump().getMeteringLevel()
                        == MeteringLevel.DISPENSING_POINT
                && fuelMeterRepository
                        .countByDispensingPointAndActiveTrueAndIdNot(
                                point,
                                fuelMeter.getId()
                        ) != 1) {
            throw new BusinessException(
                    "Un point de distribution actif ne peut pas rester sans compteur actif."
            );
        }
    }

    @Override
    public void validateBeforeDeactivatingDispensingPoint(
            DispensingPoint dispensingPoint
    ) {
        if (dispensingPoint.getPump().getMeteringLevel()
                == MeteringLevel.PUMP) {
            return;
        }

        long activeMeters = fuelMeterRepository
                .countByDispensingPointAndActiveTrue(
                        dispensingPoint
                );
        if (activeMeters > 1) {
            throw new BusinessException(
                    "La désactivation laisserait plusieurs compteurs actifs sur ce point de distribution."
            );
        }
    }

    private void validateSameTankForActiveDispensingPoints(
            Pump pump,
            DispensingPoint prospectivePoint
    ) {
        Set<UUID> tankIds = new HashSet<>();
        for (DispensingPoint point : activePoints(pump)) {
            if (prospectivePoint == null
                    || !point.getId().equals(prospectivePoint.getId())) {
                tankIds.add(point.getTank().getId());
            }
        }
        if (prospectivePoint != null) {
            tankIds.add(prospectivePoint.getTank().getId());
        }

        if (tankIds.size() > 1) {
            throw new BusinessException(
                    "Tous les points actifs d’une pompe en comptage global doivent utiliser la même citerne."
            );
        }
    }

    private List<DispensingPoint> activePoints(Pump pump) {
        return dispensingPointRepository
                .findByPumpAndActiveTrueOrderByDisplayOrderAscNameAsc(
                        pump
                );
    }

    private BusinessException incoherentLevelChange() {
        return new BusinessException(
                "Le changement de niveau de comptage rendrait la configuration actuelle incohérente."
        );
    }
}
