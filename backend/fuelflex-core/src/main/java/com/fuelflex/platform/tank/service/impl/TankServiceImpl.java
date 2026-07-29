package com.fuelflex.platform.tank.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.depot.repository.DepotRepository;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.product.repository.ProductRepository;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.tank.dto.request.TankRequest;
import com.fuelflex.platform.tank.dto.response.TankResponse;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.entity.TankStatus;
import com.fuelflex.platform.tank.mapper.TankMapper;
import com.fuelflex.platform.tank.repository.TankRepository;
import com.fuelflex.platform.tank.service.TankService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TankServiceImpl implements TankService {

    private final TankRepository tankRepository;
    private final DepotRepository depotRepository;
    private final StationRepository stationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TankMapper tankMapper;

    @Override
    public TankResponse create(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            TankRequest request
    ) {
        validateAuthenticatedOrganization(organizationId);

        Station station = findStation(
                organizationId,
                stationId
        );

        Depot depot = findDepot(
                stationId,
                depotId
        );

        Product product = findProduct(
                organizationId,
                request.getProductId()
        );

        validateOperationalHierarchy(
                station,
                depot,
                product
        );

        String normalizedCode = normalizeCode(
                request.getCode()
        );

        String normalizedName = normalizeText(
                request.getName()
        );

        validateDuplicateCode(
                depotId,
                normalizedCode,
                null
        );

        validateDuplicateName(
                depotId,
                normalizedName,
                null
        );

        validateTankLevels(request);

        Tank tank = tankMapper.toEntity(request);

        tank.setDepot(depot);
        tank.setProduct(product);
        tank.setCode(normalizedCode);
        tank.setName(normalizedName);

        applyDefaultLevels(tank);

        Tank savedTank = tankRepository.save(tank);

        return tankMapper.toResponse(savedTank);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TankResponse> findAllByDepot(
            UUID organizationId,
            UUID stationId,
            UUID depotId
    ) {
        validateAuthenticatedOrganization(organizationId);

        findStation(
                organizationId,
                stationId
        );

        findDepot(
                stationId,
                depotId
        );

        return tankRepository
                .findByDepotIdOrderByDisplayOrderAscNameAsc(depotId)
                .stream()
                .map(tankMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TankResponse> findActiveByDepot(
            UUID organizationId,
            UUID stationId,
            UUID depotId
    ) {
        validateAuthenticatedOrganization(organizationId);

        findStation(
                organizationId,
                stationId
        );

        findDepot(
                stationId,
                depotId
        );

        return tankRepository
                .findByDepotIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
                        depotId
                )
                .stream()
                .map(tankMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TankResponse findById(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            UUID tankId
    ) {
        validateAuthenticatedOrganization(organizationId);

        findStation(
                organizationId,
                stationId
        );

        findDepot(
                stationId,
                depotId
        );

        Tank tank = findTank(
                depotId,
                tankId
        );

        return tankMapper.toResponse(tank);
    }

    @Override
    public TankResponse update(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            UUID tankId,
            TankRequest request
    ) {
        validateAuthenticatedOrganization(organizationId);

        Station station = findStation(
                organizationId,
                stationId
        );

        Depot depot = findDepot(
                stationId,
                depotId
        );

        Tank tank = findTank(
                depotId,
                tankId
        );

        Product product = findProduct(
                organizationId,
                request.getProductId()
        );

        validateOperationalHierarchy(
                station,
                depot,
                product
        );

        String normalizedCode = normalizeCode(
                request.getCode()
        );

        String normalizedName = normalizeText(
                request.getName()
        );

        validateDuplicateCode(
                depotId,
                normalizedCode,
                tankId
        );

        validateDuplicateName(
                depotId,
                normalizedName,
                tankId
        );

        validateTankLevels(request);

        tankMapper.updateEntity(
                tank,
                request
        );

        tank.setProduct(product);
        tank.setCode(normalizedCode);
        tank.setName(normalizedName);

        applyDefaultLevels(tank);

        Tank updatedTank = tankRepository.save(tank);

        return tankMapper.toResponse(updatedTank);
    }

    @Override
    public void delete(
            UUID organizationId,
            UUID stationId,
            UUID depotId,
            UUID tankId
    ) {
        validateAuthenticatedOrganization(organizationId);

        findStation(
                organizationId,
                stationId
        );

        findDepot(
                stationId,
                depotId
        );

        Tank tank = findTank(
                depotId,
                tankId
        );

        if (!tank.isActive()) {
            throw new BusinessException(
                    "Cette citerne est déjà désactivée."
            );
        }

        tank.setActive(false);
        tank.setStatus(TankStatus.OUT_OF_SERVICE);

        tankRepository.save(tank);
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

    private Tank findTank(
            UUID depotId,
            UUID tankId
    ) {
        return tankRepository
                .findByIdAndDepotId(
                        tankId,
                        depotId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Citerne introuvable dans ce dépôt."
                        )
                );
    }

    private Product findProduct(
            UUID organizationId,
            UUID productId
    ) {
        return productRepository
                .findByIdAndOrganizationId(
                        productId,
                        organizationId
                )
                .orElseThrow(
                        () -> new BusinessException(
                                "Produit introuvable dans cette organisation."
                        )
                );
    }

    private void validateOperationalHierarchy(
            Station station,
            Depot depot,
            Product product
    ) {
        if (!station.isActive()) {
            throw new BusinessException(
                    "La station sélectionnée est inactive."
            );
        }

        if (!depot.isActive()) {
            throw new BusinessException(
                    "Le dépôt sélectionné est inactif."
            );
        }

        if (!product.isActive()) {
            throw new BusinessException(
                    "Le produit sélectionné est inactif."
            );
        }
    }

    private void validateTankLevels(
            TankRequest request
    ) {
        BigDecimal capacity = request.getCapacityLiters();

        if (capacity == null) {
            throw new BusinessException(
                    "La capacité de la citerne est obligatoire."
            );
        }

        BigDecimal minimum =
                request.getMinimumLevelLiters() == null
                        ? BigDecimal.ZERO
                        : request.getMinimumLevelLiters();

        BigDecimal maximum =
                request.getMaximumLevelLiters() == null
                        ? capacity
                        : request.getMaximumLevelLiters();

        if (capacity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La capacité de la citerne doit être supérieure à zéro."
            );
        }

        if (minimum.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    "Le niveau minimal de la citerne ne peut pas être négatif."
            );
        }

        if (maximum.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Le niveau maximal de la citerne doit être supérieur à zéro."
            );
        }

        if (minimum.compareTo(maximum) > 0) {
            throw new BusinessException(
                    "Le niveau minimal ne peut pas dépasser le niveau maximal."
            );
        }

        if (maximum.compareTo(capacity) > 0) {
            throw new BusinessException(
                    "Le niveau maximal ne peut pas dépasser la capacité de la citerne."
            );
        }
    }

    private void applyDefaultLevels(Tank tank) {
        if (tank.getMinimumLevelLiters() == null) {
            tank.setMinimumLevelLiters(BigDecimal.ZERO);
        }

        if (tank.getMaximumLevelLiters() == null) {
            tank.setMaximumLevelLiters(
                    tank.getCapacityLiters()
            );
        }
    }

    private void validateDuplicateCode(
            UUID depotId,
            String code,
            UUID tankId
    ) {
        boolean exists;

        if (tankId == null) {
            exists = tankRepository
                    .existsByDepotIdAndCodeIgnoreCase(
                            depotId,
                            code
                    );
        } else {
            exists = tankRepository
                    .existsByDepotIdAndCodeIgnoreCaseAndIdNot(
                            depotId,
                            code,
                            tankId
                    );
        }

        if (exists) {
            throw new BusinessException(
                    "Une citerne utilisant ce code existe déjà dans ce dépôt."
            );
        }
    }

    private void validateDuplicateName(
            UUID depotId,
            String name,
            UUID tankId
    ) {
        boolean exists;

        if (tankId == null) {
            exists = tankRepository
                    .existsByDepotIdAndNameIgnoreCase(
                            depotId,
                            name
                    );
        } else {
            exists = tankRepository
                    .existsByDepotIdAndNameIgnoreCaseAndIdNot(
                            depotId,
                            name,
                            tankId
                    );
        }

        if (exists) {
            throw new BusinessException(
                    "Une citerne utilisant ce nom existe déjà dans ce dépôt."
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
                .findByEmailIgnoreCase(
                        authentication.getName()
                )
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