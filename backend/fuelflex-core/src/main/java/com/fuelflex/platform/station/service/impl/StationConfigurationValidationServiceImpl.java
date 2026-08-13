package com.fuelflex.platform.station.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.depot.repository.DepotRepository;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPointStatus;
import com.fuelflex.platform.dispensingpoint.repository.DispensingPointRepository;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.fuelmeter.repository.FuelMeterRepository;
import com.fuelflex.platform.fuelmeter.service.MeteringConsistencyService;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.entity.PumpStatus;
import com.fuelflex.platform.pump.repository.PumpRepository;
import com.fuelflex.platform.station.dto.response.StationConfigurationValidationResponse;
import com.fuelflex.platform.station.dto.response.StationConfigurationValidationResponse.ConfigurationIssue;
import com.fuelflex.platform.station.dto.response.StationConfigurationValidationResponse.ConfigurationSummary;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.entity.StationStatus;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationConfigurationValidationService;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.entity.TankStatus;
import com.fuelflex.platform.tank.repository.TankRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationConfigurationValidationServiceImpl
        implements StationConfigurationValidationService {

    private final AuthorizationService authorizationService;
    private final StationRepository stationRepository;
    private final DepotRepository depotRepository;
    private final TankRepository tankRepository;
    private final PumpRepository pumpRepository;
    private final DispensingPointRepository dispensingPointRepository;
    private final FuelMeterRepository fuelMeterRepository;
    private final MeteringConsistencyService meteringConsistencyService;

    @Override
    public StationConfigurationValidationResponse validate(
            UUID organizationId,
            UUID stationId
    ) {
        authorizationService.checkOrganizationAccess(organizationId);

        Station station = stationRepository
                .findByIdAndOrganizationId(stationId, organizationId)
                .orElseThrow(() -> new BusinessException(
                        "Station introuvable."
                ));

        List<ConfigurationIssue> issues = new ArrayList<>();
        validateStation(station, issues);

        List<Depot> depots = depotRepository
                .findByStationIdOrderByDisplayOrderAscNameAsc(stationId);
        if (depots.isEmpty()) {
            issues.add(issue(
                    "DEPOT_MISSING",
                    "depots",
                    "STATION",
                    station.getId(),
                    station.getName(),
                    "La station doit posséder au moins un dépôt."
            ));
        }

        List<Tank> tanks = new ArrayList<>();
        for (Depot depot : depots) {
            if (!depot.isActive()) {
                issues.add(issue(
                        "DEPOT_INACTIVE",
                        "depots",
                        "DEPOT",
                        depot.getId(),
                        depot.getName(),
                        "Le dépôt doit être actif."
                ));
            }
            List<Tank> depotTanks = tankRepository
                    .findByDepotIdOrderByDisplayOrderAscNameAsc(depot.getId());
            tanks.addAll(depotTanks);
            depotTanks.forEach(tank -> validateTank(tank, depot, issues));
        }
        if (tanks.isEmpty()) {
            issues.add(issue(
                    "TANK_MISSING",
                    "tanks",
                    "STATION",
                    station.getId(),
                    station.getName(),
                    "La station doit posséder au moins une citerne."
            ));
        }

        List<Pump> pumps = pumpRepository
                .findByStationOrderByDisplayOrderAscNameAsc(station);
        if (pumps.isEmpty()) {
            issues.add(issue(
                    "PUMP_MISSING",
                    "pumps",
                    "STATION",
                    station.getId(),
                    station.getName(),
                    "La station doit posséder au moins une pompe."
            ));
        }

        int dispensingPointCount = 0;
        int fuelMeterCount = 0;
        for (Pump pump : pumps) {
            validatePump(pump, issues);
            List<DispensingPoint> points = dispensingPointRepository
                    .findByPumpOrderByDisplayOrderAscNameAsc(pump);
            dispensingPointCount += points.size();
            if (points.isEmpty()) {
                issues.add(issue(
                        "DISPENSING_POINT_MISSING",
                        "dispensing-points",
                        "PUMP",
                        pump.getId(),
                        pump.getName(),
                        "La pompe doit posséder au moins un pistolet."
                ));
            }
            for (DispensingPoint point : points) {
                validateDispensingPoint(point, station, issues);
                List<FuelMeter> pointMeters = fuelMeterRepository
                        .findByDispensingPointOrderByDisplayOrderAscNameAsc(point);
                fuelMeterCount += pointMeters.size();
                validateActiveMeterStatuses(pointMeters, point, issues);
            }

            List<FuelMeter> pumpMeters = fuelMeterRepository
                    .findByPumpOrderByDisplayOrderAscNameAsc(pump);
            fuelMeterCount += pumpMeters.size();
            validateActiveMeterStatuses(pumpMeters, pump, issues);

            try {
                meteringConsistencyService
                        .validateCompletePumpConfiguration(pump);
            } catch (BusinessException exception) {
                issues.add(issue(
                        "PUMP_CONFIGURATION_INVALID",
                        "fuel-meters",
                        "PUMP",
                        pump.getId(),
                        pump.getName(),
                        exception.getMessage()
                ));
            }
        }

        return StationConfigurationValidationResponse.builder()
                .valid(issues.isEmpty())
                .stationId(station.getId())
                .issues(List.copyOf(issues))
                .summary(ConfigurationSummary.builder()
                        .depots(depots.size())
                        .tanks(tanks.size())
                        .pumps(pumps.size())
                        .dispensingPoints(dispensingPointCount)
                        .fuelMeters(fuelMeterCount)
                        .build())
                .build();
    }

    private void validateStation(
            Station station,
            List<ConfigurationIssue> issues
    ) {
        if (!station.isActive() || station.getStatus() != StationStatus.ACTIVE) {
            issues.add(issue(
                    "STATION_NOT_OPERATIONAL",
                    "station",
                    "STATION",
                    station.getId(),
                    station.getName(),
                    "La station doit être active avec le statut ACTIVE."
            ));
        }
    }

    private void validateTank(
            Tank tank,
            Depot depot,
            List<ConfigurationIssue> issues
    ) {
        if (tank.getDepot() == null
                || !depot.getId().equals(tank.getDepot().getId())) {
            issues.add(issue(
                    "TANK_DEPOT_INVALID",
                    "tanks",
                    "TANK",
                    tank.getId(),
                    tank.getName(),
                    "La citerne doit être rattachée à un dépôt valide."
            ));
        }
        if (tank.getProduct() == null || !tank.getProduct().isActive()) {
            issues.add(issue(
                    "TANK_PRODUCT_INVALID",
                    "tanks",
                    "TANK",
                    tank.getId(),
                    tank.getName(),
                    "La citerne doit être rattachée à un produit actif."
            ));
        }
        boolean invalidLevels = tank.getCapacityLiters() == null
                || tank.getMinimumLevelLiters() == null
                || tank.getMaximumLevelLiters() == null
                || tank.getCapacityLiters().signum() <= 0
                || tank.getMinimumLevelLiters().signum() < 0
                || tank.getMaximumLevelLiters().signum() <= 0
                || tank.getMinimumLevelLiters().compareTo(tank.getMaximumLevelLiters()) > 0
                || tank.getMaximumLevelLiters().compareTo(tank.getCapacityLiters()) > 0;
        if (invalidLevels) {
            issues.add(issue(
                    "TANK_LEVELS_INVALID",
                    "tanks",
                    "TANK",
                    tank.getId(),
                    tank.getName(),
                    "La capacité et les niveaux de la citerne sont incohérents."
            ));
        }
                if (!tank.isActive() || tank.getStatus() != TankStatus.ACTIVE) {
            issues.add(issue(
                    "TANK_NOT_OPERATIONAL",
                    "tanks",
                    "TANK",
                    tank.getId(),
                    tank.getName(),
                    "La citerne doit être active avec le statut ACTIVE."
            ));
        }
    }

    private void validatePump(
            Pump pump,
            List<ConfigurationIssue> issues
    ) {
        if (pump.getMeteringLevel() == null) {
            issues.add(issue(
                    "PUMP_METERING_LEVEL_MISSING",
                    "pumps",
                    "PUMP",
                    pump.getId(),
                    pump.getName(),
                    "Le niveau de comptage de la pompe est obligatoire."
            ));
        }
        if (!pump.isActive() || pump.getStatus() != PumpStatus.ACTIVE) {
            issues.add(issue(
                    "PUMP_NOT_OPERATIONAL",
                    "pumps",
                    "PUMP",
                    pump.getId(),
                    pump.getName(),
                    "La pompe doit être active avec le statut ACTIVE."
            ));
        }
    }

    private void validateDispensingPoint(
            DispensingPoint point,
            Station station,
            List<ConfigurationIssue> issues
    ) {
        boolean validTank = point.getTank() != null
                && point.getTank().getDepot() != null
                && point.getTank().getDepot().getStation() != null
                && station.getId().equals(
                        point.getTank().getDepot().getStation().getId()
                );
        if (!validTank) {
            issues.add(issue(
                    "DISPENSING_POINT_TANK_INVALID",
                    "dispensing-points",
                    "DISPENSING_POINT",
                    point.getId(),
                    point.getName(),
                    "Le pistolet doit être rattaché à une citerne de la station."
            ));
        }
        if (!point.isActive()
                || point.getStatus() != DispensingPointStatus.ACTIVE) {
            issues.add(issue(
                    "DISPENSING_POINT_NOT_OPERATIONAL",
                    "dispensing-points",
                    "DISPENSING_POINT",
                    point.getId(),
                    point.getName(),
                    "Le pistolet doit être actif avec le statut ACTIVE."
            ));
        }
    }

    private void validateActiveMeterStatuses(
            List<FuelMeter> meters,
            Object parent,
            List<ConfigurationIssue> issues
    ) {
        for (FuelMeter meter : meters) {
            if (meter.isActive()
                    && meter.getStatus() != FuelMeterStatus.ACTIVE) {
                String parentName = parent instanceof Pump pump
                        ? pump.getName()
                        : ((DispensingPoint) parent).getName();
                issues.add(issue(
                        "FUEL_METER_STATUS_INVALID",
                        "fuel-meters",
                        "FUEL_METER",
                        meter.getId(),
                        meter.getName(),
                        "Le compteur actif de " + parentName
                                + " doit avoir le statut ACTIVE."
                ));
            }
        }
    }

    private ConfigurationIssue issue(
            String code,
            String step,
            String objectType,
            UUID objectId,
            String objectName,
            String message
    ) {
        return ConfigurationIssue.builder()
                .code(code)
                .step(step)
                .objectType(objectType)
                .objectId(objectId)
                .objectName(objectName)
                .message(message)
                .build();
    }
}
