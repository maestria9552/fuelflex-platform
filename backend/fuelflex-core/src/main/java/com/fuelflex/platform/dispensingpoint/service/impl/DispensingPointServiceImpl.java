package com.fuelflex.platform.dispensingpoint.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.common.util.TextNormalizer;
import com.fuelflex.platform.dispensingpoint.dto.request.DispensingPointRequest;
import com.fuelflex.platform.dispensingpoint.dto.response.DispensingPointResponse;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPointStatus;
import com.fuelflex.platform.dispensingpoint.mapper.DispensingPointMapper;
import com.fuelflex.platform.dispensingpoint.repository.DispensingPointRepository;
import com.fuelflex.platform.dispensingpoint.service.DispensingPointService;
import com.fuelflex.platform.fuelmeter.service.MeteringConsistencyService;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.repository.PumpRepository;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.tank.entity.Tank;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DispensingPointServiceImpl
        implements DispensingPointService {

    private final DispensingPointRepository dispensingPointRepository;
    private final PumpRepository pumpRepository;
    private final DispensingPointMapper dispensingPointMapper;
    private final EntityLookupService entityLookupService;
    private final AuthorizationService authorizationService;
    private final MeteringConsistencyService meteringConsistencyService;

    @Override
    public DispensingPointResponse create(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            DispensingPointRequest request
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        validateStationActive(station);

        Pump pump = findPump(
                station,
                pumpId
        );

        validatePumpActive(pump);

        Tank tank = entityLookupService.findTankByStation(
                stationId,
                request.getTankId()
        );

        validateTankActive(tank);

        String normalizedCode = TextNormalizer.normalizeCode(
                request.getCode()
        );

        String normalizedName = TextNormalizer.normalizeText(
                request.getName()
        );

        validateDuplicateCode(
                pump,
                normalizedCode,
                null
        );

        validateDuplicateNozzleNumber(
                pump,
                request.getNozzleNumber(),
                null
        );

        DispensingPoint dispensingPoint =
                dispensingPointMapper.toEntity(
                        request
                );

        dispensingPoint.setPump(pump);
        dispensingPoint.setTank(tank);
        dispensingPoint.setCode(normalizedCode);
        dispensingPoint.setName(normalizedName);

        if (
                dispensingPoint.getDisplayOrder() == null
                        || dispensingPoint.getDisplayOrder() < 1
        ) {
            dispensingPoint.setDisplayOrder(1);
        }

        if (request.getActive() == null) {
            dispensingPoint.setActive(false);
        }

        if (dispensingPoint.getStatus() == null) {
            dispensingPoint.setStatus(
                    DispensingPointStatus.INACTIVE
            );
        }

        if (dispensingPoint.isActive()) {
            meteringConsistencyService
                    .validateBeforeActivatingDispensingPoint(
                            pump,
                            dispensingPoint
                    );
        }

        DispensingPoint savedDispensingPoint =
                dispensingPointRepository.save(
                        dispensingPoint
                );

        return dispensingPointMapper.toResponse(
                savedDispensingPoint
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispensingPointResponse> findAllByPump(
            UUID organizationId,
            UUID stationId,
            UUID pumpId
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        Pump pump = findPump(
                station,
                pumpId
        );

        return dispensingPointRepository
                .findByPumpOrderByDisplayOrderAscNameAsc(
                        pump
                )
                .stream()
                .map(dispensingPointMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispensingPointResponse> findActiveByPump(
            UUID organizationId,
            UUID stationId,
            UUID pumpId
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        Pump pump = findPump(
                station,
                pumpId
        );

        return dispensingPointRepository
                .findByPumpAndActiveTrueOrderByDisplayOrderAscNameAsc(
                        pump
                )
                .stream()
                .map(dispensingPointMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DispensingPointResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        Pump pump = findPump(
                station,
                pumpId
        );

        DispensingPoint dispensingPoint =
                findDispensingPoint(
                        pump,
                        dispensingPointId
                );

        return dispensingPointMapper.toResponse(
                dispensingPoint
        );
    }

    @Override
    public DispensingPointResponse update(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            DispensingPointRequest request
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        validateStationActive(station);

        Pump pump = findPump(
                station,
                pumpId
        );

        validatePumpActive(pump);

        DispensingPoint dispensingPoint =
                findDispensingPoint(
                        pump,
                        dispensingPointId
                );

        Tank tank = entityLookupService.findTankByStation(
                stationId,
                request.getTankId()
        );

        validateTankActive(tank);

        String normalizedCode = TextNormalizer.normalizeCode(
                request.getCode()
        );

        String normalizedName = TextNormalizer.normalizeText(
                request.getName()
        );

        validateDuplicateCode(
                pump,
                normalizedCode,
                dispensingPointId
        );

        validateDuplicateNozzleNumber(
                pump,
                request.getNozzleNumber(),
                dispensingPointId
        );

        dispensingPointMapper.updateEntity(
                dispensingPoint,
                request
        );

        dispensingPoint.setTank(tank);
        dispensingPoint.setCode(normalizedCode);
        dispensingPoint.setName(normalizedName);

        if (
                dispensingPoint.getDisplayOrder() == null
                        || dispensingPoint.getDisplayOrder() < 1
        ) {
            dispensingPoint.setDisplayOrder(1);
        }

        if (dispensingPoint.isActive()) {
            meteringConsistencyService
                    .validateBeforeActivatingDispensingPoint(
                            pump,
                            dispensingPoint
                    );
        }

        DispensingPoint updatedDispensingPoint =
                dispensingPointRepository.save(
                        dispensingPoint
                );

        return dispensingPointMapper.toResponse(
                updatedDispensingPoint
        );
    }

    @Override
    public void delete(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        Pump pump = findPump(
                station,
                pumpId
        );

        DispensingPoint dispensingPoint =
                findDispensingPoint(
                        pump,
                        dispensingPointId
                );

        if (!dispensingPoint.isActive()) {
            throw new BusinessException(
                    "Ce point de distribution est déjà désactivé."
            );
        }

        meteringConsistencyService
                .validateBeforeDeactivatingDispensingPoint(
                        dispensingPoint
                );

        dispensingPoint.setActive(false);
        dispensingPoint.setStatus(
                DispensingPointStatus.INACTIVE
        );

        dispensingPointRepository.save(
                dispensingPoint
        );
    }

    private Pump findPump(
            Station station,
            UUID pumpId
    ) {
        if (pumpId == null) {
            throw new BusinessException(
                    "L’identifiant de la pompe est obligatoire."
            );
        }

        return pumpRepository
                .findByStationAndId(
                        station,
                        pumpId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Pompe introuvable dans cette station."
                        )
                );
    }

    private DispensingPoint findDispensingPoint(
            Pump pump,
            UUID dispensingPointId
    ) {
        if (dispensingPointId == null) {
            throw new BusinessException(
                    "L’identifiant du point de distribution est obligatoire."
            );
        }

        return dispensingPointRepository
                .findByPumpAndId(
                        pump,
                        dispensingPointId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Point de distribution introuvable sur cette pompe."
                        )
                );
    }

    private void validateStationActive(
            Station station
    ) {
        if (!station.isActive()) {
            throw new BusinessException(
                    "La station sélectionnée est inactive."
            );
        }
    }

    private void validatePumpActive(
            Pump pump
    ) {
        if (!pump.isActive()) {
            throw new BusinessException(
                    "La pompe sélectionnée est inactive."
            );
        }
    }

    private void validateTankActive(
            Tank tank
    ) {
        if (!tank.isActive()) {
            throw new BusinessException(
                    "La citerne sélectionnée est inactive."
            );
        }
    }

    private void validateDuplicateCode(
            Pump pump,
            String code,
            UUID dispensingPointId
    ) {
        boolean exists;

        if (dispensingPointId == null) {
            exists = dispensingPointRepository
                    .existsByPumpAndCode(
                            pump,
                            code
                    );
        } else {
            exists = dispensingPointRepository
                    .existsByPumpAndCodeAndIdNot(
                            pump,
                            code,
                            dispensingPointId
                    );
        }

        if (exists) {
            throw new BusinessException(
                    "Un point de distribution utilisant ce code existe déjà sur cette pompe."
            );
        }
    }

    private void validateDuplicateNozzleNumber(
            Pump pump,
            Integer nozzleNumber,
            UUID dispensingPointId
    ) {
        if (nozzleNumber == null) {
            return;
        }

        boolean exists;

        if (dispensingPointId == null) {
            exists = dispensingPointRepository
                    .existsByPumpAndNozzleNumber(
                            pump,
                            nozzleNumber
                    );
        } else {
            exists = dispensingPointRepository
                    .existsByPumpAndNozzleNumberAndIdNot(
                            pump,
                            nozzleNumber,
                            dispensingPointId
                    );
        }

        if (exists) {
            throw new BusinessException(
                    "Un point de distribution utilisant ce numéro de pistolet existe déjà sur cette pompe."
            );
        }
    }

}
