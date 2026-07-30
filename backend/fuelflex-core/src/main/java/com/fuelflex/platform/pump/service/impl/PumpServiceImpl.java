package com.fuelflex.platform.pump.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.common.util.TextNormalizer;
import com.fuelflex.platform.pump.dto.request.PumpRequest;
import com.fuelflex.platform.pump.dto.response.PumpResponse;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.entity.PumpStatus;
import com.fuelflex.platform.pump.mapper.PumpMapper;
import com.fuelflex.platform.pump.repository.PumpRepository;
import com.fuelflex.platform.pump.service.PumpService;
import com.fuelflex.platform.station.entity.Station;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PumpServiceImpl implements PumpService {

    private final PumpRepository pumpRepository;
    private final PumpMapper pumpMapper;
    private final EntityLookupService entityLookupService;
    private final AuthorizationService authorizationService;

    @Override
    public PumpResponse create(
            UUID organizationId,
            UUID stationId,
            PumpRequest request
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        validateStationActive(station);

        String normalizedCode = TextNormalizer.normalizeCode(
                request.getCode()
        );

        String normalizedName = TextNormalizer.normalizeText(
                request.getName()
        );

        validateDuplicateCode(
                station,
                normalizedCode,
                null
        );

        validateDuplicatePumpNumber(
                station,
                request.getPumpNumber(),
                null
        );

        Pump pump = pumpMapper.toEntity(request);

        pump.setStation(station);
        pump.setCode(normalizedCode);
        pump.setName(normalizedName);

        pump.setManufacturer(
                TextNormalizer.normalizeNullableText(
                        request.getManufacturer()
                )
        );

        pump.setModel(
                TextNormalizer.normalizeNullableText(
                        request.getModel()
                )
        );

        pump.setSerialNumber(
                TextNormalizer.normalizeNullableText(
                        request.getSerialNumber()
                )
        );

        pump.setLocation(
                TextNormalizer.normalizeNullableText(
                        request.getLocation()
                )
        );

        if (
                pump.getDisplayOrder() == null
                        || pump.getDisplayOrder() < 1
        ) {
            pump.setDisplayOrder(1);
        }

        if (request.getActive() == null) {
            pump.setActive(true);
        }

        if (pump.getStatus() == null) {
            pump.setStatus(PumpStatus.INACTIVE);
        }

        Pump savedPump = pumpRepository.save(pump);

        return pumpMapper.toResponse(savedPump);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PumpResponse> findAllByStation(
            UUID organizationId,
            UUID stationId
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        return pumpRepository
                .findByStationOrderByDisplayOrderAscNameAsc(
                        station
                )
                .stream()
                .map(pumpMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PumpResponse> findActiveByStation(
            UUID organizationId,
            UUID stationId
    ) {
        authorizationService.checkOrganizationAccess(
                organizationId
        );

        Station station = entityLookupService.findStation(
                organizationId,
                stationId
        );

        return pumpRepository
                .findByStationAndActiveTrueOrderByDisplayOrderAscNameAsc(
                        station
                )
                .stream()
                .map(pumpMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PumpResponse findById(
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

        return pumpMapper.toResponse(pump);
    }

    @Override
    public PumpResponse update(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            PumpRequest request
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

        String normalizedCode = TextNormalizer.normalizeCode(
                request.getCode()
        );

        String normalizedName = TextNormalizer.normalizeText(
                request.getName()
        );

        validateDuplicateCode(
                station,
                normalizedCode,
                pumpId
        );

        validateDuplicatePumpNumber(
                station,
                request.getPumpNumber(),
                pumpId
        );

        pumpMapper.updateEntity(
                pump,
                request
        );

        pump.setCode(normalizedCode);
        pump.setName(normalizedName);

        pump.setManufacturer(
                TextNormalizer.normalizeNullableText(
                        request.getManufacturer()
                )
        );

        pump.setModel(
                TextNormalizer.normalizeNullableText(
                        request.getModel()
                )
        );

        pump.setSerialNumber(
                TextNormalizer.normalizeNullableText(
                        request.getSerialNumber()
                )
        );

        pump.setLocation(
                TextNormalizer.normalizeNullableText(
                        request.getLocation()
                )
        );

        if (
                pump.getDisplayOrder() == null
                        || pump.getDisplayOrder() < 1
        ) {
            pump.setDisplayOrder(1);
        }

        Pump updatedPump = pumpRepository.save(pump);

        return pumpMapper.toResponse(updatedPump);
    }

    @Override
    public void delete(
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

        if (!pump.isActive()) {
            throw new BusinessException(
                    "Cette pompe est déjà désactivée."
            );
        }

        pump.setActive(false);
        pump.setStatus(PumpStatus.INACTIVE);

        pumpRepository.save(pump);
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

    private void validateStationActive(
            Station station
    ) {
        if (!station.isActive()) {
            throw new BusinessException(
                    "La station sélectionnée est inactive."
            );
        }
    }

    private void validateDuplicateCode(
            Station station,
            String code,
            UUID pumpId
    ) {
        boolean exists;

        if (pumpId == null) {
            exists = pumpRepository.existsByStationAndCode(
                    station,
                    code
            );
        } else {
            exists = pumpRepository.existsByStationAndCodeAndIdNot(
                    station,
                    code,
                    pumpId
            );
        }

        if (exists) {
            throw new BusinessException(
                    "Une pompe utilisant ce code existe déjà dans cette station."
            );
        }
    }

    private void validateDuplicatePumpNumber(
            Station station,
            Integer pumpNumber,
            UUID pumpId
    ) {
        boolean exists;

        if (pumpId == null) {
            exists = pumpRepository.existsByStationAndPumpNumber(
                    station,
                    pumpNumber
            );
        } else {
            exists = pumpRepository
                    .existsByStationAndPumpNumberAndIdNot(
                            station,
                            pumpNumber,
                            pumpId
                    );
        }

        if (exists) {
            throw new BusinessException(
                    "Une pompe utilisant ce numéro existe déjà dans cette station."
            );
        }
    }
}