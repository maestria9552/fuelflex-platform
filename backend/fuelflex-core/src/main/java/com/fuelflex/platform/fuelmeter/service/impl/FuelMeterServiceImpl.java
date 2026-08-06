package com.fuelflex.platform.fuelmeter.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.common.util.TextNormalizer;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.fuelmeter.dto.request.FuelMeterRequest;
import com.fuelflex.platform.fuelmeter.dto.response.FuelMeterResponse;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.fuelmeter.mapper.FuelMeterMapper;
import com.fuelflex.platform.fuelmeter.repository.FuelMeterRepository;
import com.fuelflex.platform.fuelmeter.service.FuelMeterService;
import com.fuelflex.platform.fuelmeter.service.MeteringConsistencyService;
import com.fuelflex.platform.pump.entity.Pump;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FuelMeterServiceImpl implements FuelMeterService {

    private final FuelMeterRepository fuelMeterRepository;
    private final FuelMeterMapper fuelMeterMapper;
    private final EntityLookupService entityLookupService;
    private final AuthorizationService authorizationService;
    private final MeteringConsistencyService meteringConsistencyService;

    @Override
    public FuelMeterResponse create(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            FuelMeterRequest request
    ) {
        ParentContext context = resolveContext(
                organizationId,
                stationId,
                pumpId,
                dispensingPointId
        );
        validateRequestParent(request, context);
        validateCurrentIndex(request.getCurrentIndex());

        String normalizedCode = TextNormalizer.normalizeCode(
                request.getCode()
        );
        String normalizedName = TextNormalizer.normalizeText(
                request.getName()
        );
        validateDuplicateCode(context, normalizedCode, null);

        FuelMeter fuelMeter = fuelMeterMapper.toEntity(request);
        assignParent(fuelMeter, context);
        fuelMeter.setCode(normalizedCode);
        fuelMeter.setName(normalizedName);

        if (request.getActive() == null) {
            fuelMeter.setActive(false);
        }
        if (fuelMeter.isActive()) {
            validateBeforeActivatingMeter(context, fuelMeter, null);
        }

        if (fuelMeter.getStatus() == null) {
            fuelMeter.setStatus(FuelMeterStatus.INACTIVE);
        }
        if (fuelMeter.getDisplayOrder() == null
                || fuelMeter.getDisplayOrder() < 1) {
            fuelMeter.setDisplayOrder(1);
        }

        return fuelMeterMapper.toResponse(
                fuelMeterRepository.save(fuelMeter)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelMeterResponse> findAll(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    ) {
        ParentContext context = resolveContext(
                organizationId,
                stationId,
                pumpId,
                dispensingPointId
        );

        return findAllByContext(context, false)
                .stream()
                .map(fuelMeterMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelMeterResponse> findActive(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    ) {
        ParentContext context = resolveContext(
                organizationId,
                stationId,
                pumpId,
                dispensingPointId
        );

        return findAllByContext(context, true)
                .stream()
                .map(fuelMeterMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FuelMeterResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            UUID fuelMeterId
    ) {
        ParentContext context = resolveContext(
                organizationId,
                stationId,
                pumpId,
                dispensingPointId
        );

        return fuelMeterMapper.toResponse(
                findFuelMeter(context, fuelMeterId)
        );
    }

    @Override
    public FuelMeterResponse update(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            UUID fuelMeterId,
            FuelMeterRequest request
    ) {
        ParentContext context = resolveContext(
                organizationId,
                stationId,
                pumpId,
                dispensingPointId
        );
        validateRequestParent(request, context);
        validateCurrentIndex(request.getCurrentIndex());

        FuelMeter fuelMeter = findFuelMeter(context, fuelMeterId);
        if (request.getCurrentIndex().compareTo(
                fuelMeter.getCurrentIndex()
        ) < 0) {
            throw new BusinessException(
                    "Le nouvel index ne peut pas être inférieur à l’index actuel."
            );
        }

        String normalizedCode = TextNormalizer.normalizeCode(
                request.getCode()
        );
        String normalizedName = TextNormalizer.normalizeText(
                request.getName()
        );
        validateDuplicateCode(
                context,
                normalizedCode,
                fuelMeterId
        );

        boolean targetActive = request.getActive() == null
                ? fuelMeter.isActive()
                : request.getActive();
        if (targetActive) {
            validateBeforeActivatingMeter(
                    context,
                    fuelMeter,
                    fuelMeter
            );
        }

        fuelMeterMapper.updateEntity(fuelMeter, request);
        assignParent(fuelMeter, context);
        fuelMeter.setCode(normalizedCode);
        fuelMeter.setName(normalizedName);

        if (fuelMeter.getDisplayOrder() == null
                || fuelMeter.getDisplayOrder() < 1) {
            fuelMeter.setDisplayOrder(1);
        }

        return fuelMeterMapper.toResponse(
                fuelMeterRepository.save(fuelMeter)
        );
    }

    @Override
    public void delete(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId,
            UUID fuelMeterId
    ) {
        ParentContext context = resolveContext(
                organizationId,
                stationId,
                pumpId,
                dispensingPointId
        );
        FuelMeter fuelMeter = findFuelMeter(context, fuelMeterId);
        meteringConsistencyService
                .validateBeforeDeactivatingMeter(fuelMeter);

        if (!fuelMeter.isActive()) {
            throw new BusinessException(
                    "Ce compteur est déjà désactivé."
            );
        }

        fuelMeter.setActive(false);
        fuelMeter.setStatus(FuelMeterStatus.INACTIVE);
        fuelMeterRepository.save(fuelMeter);
    }

    private ParentContext resolveContext(
            UUID organizationId,
            UUID stationId,
            UUID pumpId,
            UUID dispensingPointId
    ) {
        authorizationService.checkOrganizationAccess(organizationId);
        entityLookupService.findStation(organizationId, stationId);
        Pump pump = entityLookupService.findPump(stationId, pumpId);
        DispensingPoint dispensingPoint = null;

        if (dispensingPointId != null) {
            dispensingPoint = entityLookupService.findDispensingPoint(
                    pumpId,
                    dispensingPointId
            );
        }

        return new ParentContext(pump, dispensingPoint);
    }

    private void validateRequestParent(
            FuelMeterRequest request,
            ParentContext context
    ) {
        boolean hasPump = request.getPumpId() != null;
        boolean hasDispensingPoint =
                request.getDispensingPointId() != null;

        if (hasPump == hasDispensingPoint) {
            throw new BusinessException(
                    "Exactement un parent doit être renseigné : pompe ou point de distribution."
            );
        }

        if (context.dispensingPoint() == null) {
            if (!hasPump
                    || !context.pump().getId().equals(request.getPumpId())) {
                throw new BusinessException(
                        "Le compteur global doit être rattaché à la pompe de l’URL."
                );
            }
            return;
        }

        if (!hasDispensingPoint
                || !context.dispensingPoint().getId().equals(
                        request.getDispensingPointId()
                )) {
            throw new BusinessException(
                    "Le compteur individuel doit être rattaché au point de distribution de l’URL."
            );
        }
    }

    private void validateBeforeActivatingMeter(
            ParentContext context,
            FuelMeter fuelMeter,
            FuelMeter currentMeter
    ) {
        if (context.dispensingPoint() == null) {
            meteringConsistencyService
                    .validateBeforeActivatingGlobalMeter(
                            context.pump(),
                            currentMeter
                    );
        } else {
            meteringConsistencyService
                    .validateBeforeActivatingDispensingPointMeter(
                            context.pump(),
                            context.dispensingPoint(),
                            currentMeter
                    );
        }
    }

    private void validateCurrentIndex(BigDecimal currentIndex) {
        if (currentIndex == null) {
            throw new BusinessException(
                    "L’index actuel est obligatoire."
            );
        }
        if (currentIndex.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    "L’index actuel ne peut pas être négatif."
            );
        }
    }

    private void validateDuplicateCode(
            ParentContext context,
            String code,
            UUID fuelMeterId
    ) {
        boolean exists;

        if (context.dispensingPoint() == null) {
            exists = fuelMeterId == null
                    ? fuelMeterRepository.existsByPumpAndCode(
                            context.pump(),
                            code
                    )
                    : fuelMeterRepository.existsByPumpAndCodeAndIdNot(
                            context.pump(),
                            code,
                            fuelMeterId
                    );
        } else {
            exists = fuelMeterId == null
                    ? fuelMeterRepository.existsByDispensingPointAndCode(
                            context.dispensingPoint(),
                            code
                    )
                    : fuelMeterRepository
                            .existsByDispensingPointAndCodeAndIdNot(
                                    context.dispensingPoint(),
                                    code,
                                    fuelMeterId
                            );
        }

        if (exists) {
            throw new BusinessException(
                    "Un compteur utilisant ce code existe déjà dans ce contexte."
            );
        }
    }

    private void assignParent(
            FuelMeter fuelMeter,
            ParentContext context
    ) {
        if (context.dispensingPoint() == null) {
            fuelMeter.setPump(context.pump());
            fuelMeter.setDispensingPoint(null);
        } else {
            fuelMeter.setPump(null);
            fuelMeter.setDispensingPoint(context.dispensingPoint());
        }
    }

    private List<FuelMeter> findAllByContext(
            ParentContext context,
            boolean activeOnly
    ) {
        if (context.dispensingPoint() == null) {
            return activeOnly
                    ? fuelMeterRepository
                            .findByPumpAndActiveTrueOrderByDisplayOrderAscNameAsc(
                                    context.pump()
                            )
                    : fuelMeterRepository
                            .findByPumpOrderByDisplayOrderAscNameAsc(
                                    context.pump()
                            );
        }

        return activeOnly
                ? fuelMeterRepository
                        .findByDispensingPointAndActiveTrueOrderByDisplayOrderAscNameAsc(
                                context.dispensingPoint()
                        )
                : fuelMeterRepository
                        .findByDispensingPointOrderByDisplayOrderAscNameAsc(
                                context.dispensingPoint()
                        );
    }

    private FuelMeter findFuelMeter(
            ParentContext context,
            UUID fuelMeterId
    ) {
        FuelMeter fuelMeter = entityLookupService.findFuelMeter(
                fuelMeterId
        );

        boolean belongsToContext = context.dispensingPoint() == null
                ? fuelMeter.getPump() != null
                        && fuelMeter.getPump().getId().equals(
                                context.pump().getId()
                        )
                : fuelMeter.getDispensingPoint() != null
                        && fuelMeter.getDispensingPoint().getId().equals(
                                context.dispensingPoint().getId()
                        );

        if (!belongsToContext) {
            throw new BusinessException(
                    "Compteur introuvable dans ce contexte."
            );
        }

        return fuelMeter;
    }

    private record ParentContext(
            Pump pump,
            DispensingPoint dispensingPoint
    ) {
    }
}
