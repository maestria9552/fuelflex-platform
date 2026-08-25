package com.fuelflex.platform.sale.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleReadFilter;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleResponse;
import com.fuelflex.platform.sale.mapper.FuelSaleResponseMapper;
import com.fuelflex.platform.sale.repository.FuelSaleRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleReadServiceImpl implements SaleReadService {

    private final AuthorizationService authorizationService;
    private final StationAccessService stationAccessService;
    private final FuelSaleRepository fuelSaleRepository;
    private final FuelSaleResponseMapper responseMapper;

    @Override
    public Page<SaleResponse> findForManager(SaleReadFilter filter, Pageable pageable) {
        return findAll(actor("MANAGER"), filter, pageable);
    }

    @Override
    public SaleResponse findForManager(UUID saleId) {
        return findById(actor("MANAGER"), saleId);
    }

    @Override
    public Page<SaleResponse> findForSupervisor(SaleReadFilter filter, Pageable pageable) {
        return findAll(actor("SUPERVISOR"), filter, pageable);
    }

    @Override
    public SaleResponse findForSupervisor(UUID saleId) {
        return findById(actor("SUPERVISOR"), saleId);
    }

    private Page<SaleResponse> findAll(
            User actor,
            SaleReadFilter requestedFilter,
            Pageable requestedPageable
    ) {
        SaleReadFilter filter = requestedFilter == null
                ? new SaleReadFilter(null, null, null, null, null, null, null)
                : requestedFilter;
        validatePeriod(filter);
        Set<UUID> stationIds = stationAccessService.getAccessibleStationIds(actor);
        if (filter.stationId() != null) {
            stationAccessService.checkStationAccess(actor, filter.stationId());
        }
        Pageable pageable = pageable(requestedPageable);
        if (stationIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return fuelSaleRepository.findForWeb(
                actor.getOrganization().getId(),
                stationIds,
                filter.stationId(),
                filter.operationalDayId(),
                filter.pumpAttendantId(),
                filter.saleType(),
                filter.status(),
                filter.soldFrom(),
                filter.soldTo(),
                pageable
        ).map(responseMapper::toResponse);
    }

    private SaleResponse findById(User actor, UUID saleId) {
        Set<UUID> stationIds = stationAccessService.getAccessibleStationIds(actor);
        if (stationIds.isEmpty()) {
            throw saleNotFound();
        }
        return fuelSaleRepository
                .findByIdAndOrganizationIdAndStationIdIn(
                        saleId,
                        actor.getOrganization().getId(),
                        stationIds
                )
                .map(responseMapper::toResponse)
                .orElseThrow(this::saleNotFound);
    }

    private void validatePeriod(SaleReadFilter filter) {
        if (filter.soldFrom() != null
                && filter.soldTo() != null
                && filter.soldFrom().isAfter(filter.soldTo())) {
            throw new BusinessException(
                    "The sale period start must be before or equal to its end."
            );
        }
    }

    private Pageable pageable(Pageable requested) {
        if (requested == null || requested.isUnpaged()) {
            return PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "soldAt"));
        }
        if (requested.getSort().isSorted()) {
            return requested;
        }
        return PageRequest.of(
                requested.getPageNumber(),
                requested.getPageSize(),
                Sort.by(Sort.Direction.DESC, "soldAt")
        );
    }

    private User actor(String requiredRole) {
        User actor = authorizationService.getAuthenticatedUser();
        if (actor == null
                || !actor.isEnabled()
                || actor.getOrganization() == null
                || actor.getRoles().stream()
                        .filter(Role::isActive)
                        .noneMatch(role -> requiredRole.equalsIgnoreCase(role.getCode()))) {
            throw new ForbiddenException("Required role: " + requiredRole);
        }
        return actor;
    }

    private ResourceNotFoundException saleNotFound() {
        return new ResourceNotFoundException("POS sale was not found.");
    }
}
