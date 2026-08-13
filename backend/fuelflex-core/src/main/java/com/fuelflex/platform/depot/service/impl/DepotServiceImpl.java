package com.fuelflex.platform.depot.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.depot.dto.request.DepotRequest;
import com.fuelflex.platform.depot.dto.response.DepotResponse;
import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.depot.mapper.DepotMapper;
import com.fuelflex.platform.depot.repository.DepotRepository;
import com.fuelflex.platform.depot.service.DepotService;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DepotServiceImpl implements DepotService {

    private final DepotRepository depotRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final DepotMapper depotMapper;

    @Override
    public DepotResponse create(
            UUID organizationId,
            UUID stationId,
            DepotRequest request
    ) {
        validateAuthenticatedOrganization(organizationId);

        Station station = findStation(
                organizationId,
                stationId
        );

        String normalizedCode = normalizeCode(request.getCode());
        String normalizedName = normalizeText(request.getName());

        validateDuplicateCode(
                stationId,
                normalizedCode,
                null
        );

        validateDuplicateName(
                stationId,
                normalizedName,
                null
        );

        Depot depot = depotMapper.toEntity(request);

        if (request.getActive() == null) {
            depot.setActive(true);
        }

        depot.setStation(station);
        depot.setCode(normalizedCode);
        depot.setName(normalizedName);

        Depot savedDepot = depotRepository.save(depot);

        return depotMapper.toResponse(savedDepot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepotResponse> findAllByStation(
            UUID organizationId,
            UUID stationId
    ) {
        validateAuthenticatedOrganization(organizationId);
        findStation(organizationId, stationId);

        return depotRepository
                .findByStationIdOrderByDisplayOrderAscNameAsc(stationId)
                .stream()
                .map(depotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepotResponse> findActiveByStation(
            UUID organizationId,
            UUID stationId
    ) {
        validateAuthenticatedOrganization(organizationId);
        findStation(organizationId, stationId);

        return depotRepository
                .findByStationIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
                        stationId
                )
                .stream()
                .map(depotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepotResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID depotId
    ) {
        validateAuthenticatedOrganization(organizationId);
        findStation(organizationId, stationId);

        Depot depot = findDepot(
                stationId,
                depotId
        );

        return depotMapper.toResponse(depot);
    }

    @Override
    public DepotResponse update(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            DepotRequest request
    ) {
        validateAuthenticatedOrganization(organizationId);
        findStation(organizationId, stationId);

        Depot depot = findDepot(
                stationId,
                depotId
        );

        String normalizedCode = normalizeCode(request.getCode());
        String normalizedName = normalizeText(request.getName());

        validateDuplicateCode(
                stationId,
                normalizedCode,
                depotId
        );

        validateDuplicateName(
                stationId,
                normalizedName,
                depotId
        );

        depotMapper.updateEntity(
                depot,
                request
        );

        depot.setCode(normalizedCode);
        depot.setName(normalizedName);

        Depot updatedDepot = depotRepository.save(depot);

        return depotMapper.toResponse(updatedDepot);
    }

    @Override
    public void delete(
            UUID organizationId,
            UUID stationId,
            UUID depotId
    ) {
        validateAuthenticatedOrganization(organizationId);
        findStation(organizationId, stationId);

        Depot depot = findDepot(
                stationId,
                depotId
        );

        if (!depot.isActive()) {
            throw new BusinessException(
                    "Ce dépôt est déjà désactivé."
            );
        }

        depot.setActive(false);

        depotRepository.save(depot);
    }

    private Station findStation(
            UUID organizationId,
            UUID stationId
    ) {
        return stationRepository
                .findByIdAndOrganizationId(
                        stationId,
                        organizationId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Station introuvable dans cette organisation."
                        )
                );
    }

    private Depot findDepot(
            UUID stationId,
            UUID depotId
    ) {
        return depotRepository
                .findByIdAndStationId(
                        depotId,
                        stationId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Dépôt introuvable dans cette station."
                        )
                );
    }

    private void validateDuplicateCode(
            UUID stationId,
            String code,
            UUID depotId
    ) {
        boolean exists;

        if (depotId == null) {
            exists = depotRepository
                    .existsByStationIdAndCodeIgnoreCase(
                            stationId,
                            code
                    );
        } else {
            exists = depotRepository
                    .existsByStationIdAndCodeIgnoreCaseAndIdNot(
                            stationId,
                            code,
                            depotId
                    );
        }

        if (exists) {
            throw new BusinessException(
                    "Un dépôt utilisant ce code existe déjà dans cette station."
            );
        }
    }

    private void validateDuplicateName(
            UUID stationId,
            String name,
            UUID depotId
    ) {
        boolean exists;

        if (depotId == null) {
            exists = depotRepository
                    .existsByStationIdAndNameIgnoreCase(
                            stationId,
                            name
                    );
        } else {
            exists = depotRepository
                    .existsByStationIdAndNameIgnoreCaseAndIdNot(
                            stationId,
                            name,
                            depotId
                    );
        }

        if (exists) {
            throw new BusinessException(
                    "Un dépôt utilisant ce nom existe déjà dans cette station."
            );
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || authentication.getName() == null
                        || authentication.getName().isBlank()
        ) {
            throw new BusinessException(
                    "Utilisateur non authentifié."
            );
        }

        return userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(
                        () -> new BusinessException(
                                "Utilisateur authentifié introuvable."
                        )
                );
    }

    private void validateAuthenticatedOrganization(
            UUID organizationId
    ) {
        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser.getOrganization() == null) {
            throw new BusinessException(
                    "L’utilisateur authentifié n’est rattaché à aucune organisation."
            );
        }

        if (
                !authenticatedUser
                        .getOrganization()
                        .getId()
                        .equals(organizationId)
        ) {
            throw new BusinessException(
                    "Vous n’êtes pas autorisé à accéder à cette organisation."
            );
        }
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }
}