package com.fuelflex.platform.station.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.station.dto.request.StationRequest;
import com.fuelflex.platform.station.dto.response.StationResponse;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.entity.StationStatus;
import com.fuelflex.platform.station.mapper.StationMapper;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;

    private final StationMapper stationMapper;

    private final UserRepository userRepository;

    @Override
    public StationResponse create(
            UUID organizationId,
            StationRequest request
    ) {
        validateRequest(request);

        Organization organization =
                getAuthorizedOrganization(organizationId);

        String normalizedCode =
                normalizeCode(request.getCode());

        String normalizedName =
                normalizeRequiredText(request.getName());

        validateUniqueFields(
                organization.getId(),
                normalizedCode,
                normalizedName,
                null
        );

        Station station =
                stationMapper.toEntity(request);

        if (request.getActive() == null) {
            station.setActive(true);
        }

        station.setOrganization(organization);
        station.setCode(normalizedCode);
        station.setName(normalizedName);
        station.setShortName(
                normalizeNullableText(request.getShortName())
        );
        station.setAddress(
                normalizeNullableText(request.getAddress())
        );
        station.setCity(
                normalizeNullableText(request.getCity())
        );
        station.setProvince(
                normalizeNullableText(request.getProvince())
        );
        station.setCountry(
                normalizeNullableText(request.getCountry())
        );
        station.setPhoneNumber(
                normalizeNullableText(request.getPhoneNumber())
        );
        station.setEmail(
                normalizeEmail(request.getEmail())
        );
        station.setLatitude(
                normalizeNullableText(request.getLatitude())
        );
        station.setLongitude(
                normalizeNullableText(request.getLongitude())
        );

        if (request.getStatus() == null) {
            station.setStatus(StationStatus.INACTIVE);
        }

        if (request.getDisplayOrder() == null) {
            station.setDisplayOrder(1);
        }

        Station savedStation =
                stationRepository.save(station);

        return stationMapper.toResponse(savedStation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationResponse> findAllByOrganization(
            UUID organizationId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        return stationRepository
                .findByOrganizationIdOrderByDisplayOrderAscNameAsc(
                        organization.getId()
                )
                .stream()
                .map(stationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationResponse> findActiveByOrganization(
            UUID organizationId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        return stationRepository
                .findByOrganizationIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
                        organization.getId()
                )
                .stream()
                .map(stationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StationResponse findById(
            UUID organizationId,
            UUID stationId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        Station station =
                getStation(
                        organization.getId(),
                        stationId
                );

        return stationMapper.toResponse(station);
    }

    @Override
    public StationResponse update(
            UUID organizationId,
            UUID stationId,
            StationRequest request
    ) {
        validateRequest(request);

        Organization organization =
                getAuthorizedOrganization(organizationId);

        Station station =
                getStation(
                        organization.getId(),
                        stationId
                );

        String normalizedCode =
                normalizeCode(request.getCode());

        String normalizedName =
                normalizeRequiredText(request.getName());

        validateUniqueFields(
                organization.getId(),
                normalizedCode,
                normalizedName,
                station.getId()
        );

        StationStatus currentStatus =
                station.getStatus();

        stationMapper.updateEntity(
                station,
                request
        );

        station.setCode(normalizedCode);
        station.setName(normalizedName);
        station.setShortName(
                normalizeNullableText(request.getShortName())
        );
        station.setAddress(
                normalizeNullableText(request.getAddress())
        );
        station.setCity(
                normalizeNullableText(request.getCity())
        );
        station.setProvince(
                normalizeNullableText(request.getProvince())
        );
        station.setCountry(
                normalizeNullableText(request.getCountry())
        );
        station.setPhoneNumber(
                normalizeNullableText(request.getPhoneNumber())
        );
        station.setEmail(
                normalizeEmail(request.getEmail())
        );
        station.setLatitude(
                normalizeNullableText(request.getLatitude())
        );
        station.setLongitude(
                normalizeNullableText(request.getLongitude())
        );

        /*
         * Le statut actuel est conservé lorsque le frontend
         * ne transmet aucun nouveau statut.
         */
        if (request.getStatus() == null) {
            station.setStatus(currentStatus);
        }

        if (request.getDisplayOrder() == null) {
            station.setDisplayOrder(1);
        }

        Station updatedStation =
                stationRepository.save(station);

        return stationMapper.toResponse(updatedStation);
    }

    @Override
    public void delete(
            UUID organizationId,
            UUID stationId
    ) {
        Organization organization =
                getAuthorizedOrganization(organizationId);

        Station station =
                getStation(
                        organization.getId(),
                        stationId
                );

        /*
         * Suppression logique : la station reste conservée
         * pour préserver son historique métier.
         */
        station.setActive(false);
        station.setStatus(StationStatus.CLOSED);

        stationRepository.save(station);
    }

    private Organization getAuthorizedOrganization(
            UUID organizationId
    ) {
        if (organizationId == null) {
            throw new BusinessException(
                    "L’identifiant de l’organisation est obligatoire."
            );
        }

        User currentUser =
                getCurrentUser();

        Organization organization =
                currentUser.getOrganization();

        if (organization == null) {
            throw new BusinessException(
                    "Aucune organisation n’est associée à votre compte."
            );
        }

        if (!organization.getId().equals(organizationId)) {
            throw new BusinessException(
                    "Vous n’êtes pas autorisé à accéder aux stations de cette organisation."
            );
        }

        return organization;
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new BusinessException(
                    "Utilisateur non authentifié."
            );
        }

        return userRepository
                .findByEmailIgnoreCase(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Utilisateur authentifié introuvable."
                        )
                );
    }

    private Station getStation(
            UUID organizationId,
            UUID stationId
    ) {
        if (stationId == null) {
            throw new BusinessException(
                    "L’identifiant de la station est obligatoire."
            );
        }

        return stationRepository
                .findByIdAndOrganizationId(
                        stationId,
                        organizationId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Station introuvable."
                        )
                );
    }

    private void validateRequest(
            StationRequest request
    ) {
        if (request == null) {
            throw new BusinessException(
                    "Les informations de la station sont obligatoires."
            );
        }

        if (request.getCode() == null
                || request.getCode().isBlank()) {

            throw new BusinessException(
                    "Le code de la station est obligatoire."
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new BusinessException(
                    "Le nom de la station est obligatoire."
            );
        }

        if (request.getType() == null) {
            throw new BusinessException(
                    "Le type de station est obligatoire."
            );
        }

        if (request.getDisplayOrder() != null
                && request.getDisplayOrder() < 1) {

            throw new BusinessException(
                    "L’ordre d’affichage doit être supérieur ou égal à 1."
            );
        }
    }

    private void validateUniqueFields(
            UUID organizationId,
            String code,
            String name,
            UUID currentStationId
    ) {
        boolean codeExists;
        boolean nameExists;

        if (currentStationId == null) {
            codeExists =
                    stationRepository
                            .existsByOrganizationIdAndCodeIgnoreCase(
                                    organizationId,
                                    code
                            );

            nameExists =
                    stationRepository
                            .existsByOrganizationIdAndNameIgnoreCase(
                                    organizationId,
                                    name
                            );
        } else {
            codeExists =
                    stationRepository
                            .existsByOrganizationIdAndCodeIgnoreCaseAndIdNot(
                                    organizationId,
                                    code,
                                    currentStationId
                            );

            nameExists =
                    stationRepository
                            .existsByOrganizationIdAndNameIgnoreCaseAndIdNot(
                                    organizationId,
                                    name,
                                    currentStationId
                            );
        }

        if (codeExists) {
            throw new BusinessException(
                    "Une station utilise déjà ce code dans votre organisation."
            );
        }

        if (nameExists) {
            throw new BusinessException(
                    "Une station portant ce nom existe déjà dans votre organisation."
            );
        }
    }

    private String normalizeCode(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String normalizeRequiredText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim().replaceAll("\\s+", " ");

        return normalized.isBlank()
                ? null
                : normalized;
    }

    private String normalizeEmail(
            String value
    ) {
        String normalized =
                normalizeNullableText(value);

        return normalized == null
                ? null
                : normalized.toLowerCase();
    }
}