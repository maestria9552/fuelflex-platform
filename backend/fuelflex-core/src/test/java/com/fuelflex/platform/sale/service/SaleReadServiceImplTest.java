package com.fuelflex.platform.sale.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleReadFilter;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleResponse;
import com.fuelflex.platform.sale.entity.FuelSale;
import com.fuelflex.platform.sale.entity.SaleStatus;
import com.fuelflex.platform.sale.entity.SaleType;
import com.fuelflex.platform.sale.entity.VehicleType;
import com.fuelflex.platform.sale.mapper.FuelSaleResponseMapper;
import com.fuelflex.platform.sale.repository.FuelSaleRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;

@ExtendWith(MockitoExtension.class)
class SaleReadServiceImplTest {

    @Mock AuthorizationService authorizationService;
    @Mock StationAccessService stationAccessService;
    @Mock FuelSaleRepository fuelSaleRepository;
    @Mock FuelSaleResponseMapper responseMapper;

    SaleReadServiceImpl service;
    Organization organization;
    User manager;

    @BeforeEach
    void setUp() {
        service = new SaleReadServiceImpl(
                authorizationService,
                stationAccessService,
                fuelSaleRepository,
                responseMapper
        );
        organization = organization();
        manager = user(organization, "MANAGER");
        when(authorizationService.getAuthenticatedUser()).thenReturn(manager);
    }

    @Test
    void managerListIsAlwaysScopedToOrganizationAndAccessibleStations() {
        UUID accessibleStation = UUID.randomUUID();
        UUID saleId = UUID.randomUUID();
        FuelSale sale = new FuelSale();
        sale.setId(saleId);
        SaleResponse response = response(saleId, organization.getId());
        when(stationAccessService.getAccessibleStationIds(manager))
                .thenReturn(Set.of(accessibleStation));
        when(fuelSaleRepository.findForWeb(
                eq(organization.getId()),
                eq(Set.of(accessibleStation)),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(java.util.List.of(sale)));
        when(responseMapper.toResponse(sale)).thenReturn(response);

        assertThat(service.findForManager(emptyFilter(), PageRequest.of(0, 20)).getContent())
                .containsExactly(response);
    }

    @Test
    void allSupportedFiltersArePassedWithoutChangingTheSecurityScope() {
        UUID accessibleStation = UUID.randomUUID();
        UUID dayId = UUID.randomUUID();
        UUID attendantId = UUID.randomUUID();
        OffsetDateTime from = OffsetDateTime.parse("2026-08-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-08-31T23:59:59Z");
        SaleReadFilter filter = new SaleReadFilter(
                accessibleStation,
                dayId,
                attendantId,
                SaleType.CREDIT,
                SaleStatus.EFFECTIVE,
                from,
                to
        );
        when(stationAccessService.getAccessibleStationIds(manager))
                .thenReturn(Set.of(accessibleStation));
        when(fuelSaleRepository.findForWeb(
                eq(organization.getId()),
                eq(Set.of(accessibleStation)),
                eq(accessibleStation),
                eq(dayId),
                eq(attendantId),
                eq(SaleType.CREDIT),
                eq(SaleStatus.EFFECTIVE),
                eq(from),
                eq(to),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        service.findForManager(filter, PageRequest.of(0, 20));

        verify(stationAccessService).checkStationAccess(manager, accessibleStation);
    }

    @Test
    void unauthorizedStationFilterIsRejectedBeforeQueryingSales() {
        UUID allowedStation = UUID.randomUUID();
        UUID forbiddenStation = UUID.randomUUID();
        when(stationAccessService.getAccessibleStationIds(manager))
                .thenReturn(Set.of(allowedStation));
        doThrow(new ForbiddenException("denied"))
                .when(stationAccessService).checkStationAccess(manager, forbiddenStation);

        assertThatThrownBy(() -> service.findForManager(
                new SaleReadFilter(
                        forbiddenStation, null, null, null, null, null, null
                ),
                PageRequest.of(0, 20)
        )).isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(fuelSaleRepository);
    }

    @Test
    void detailOutsideManagerScopeIsNotExposed() {
        UUID stationId = UUID.randomUUID();
        UUID saleId = UUID.randomUUID();
        when(stationAccessService.getAccessibleStationIds(manager))
                .thenReturn(Set.of(stationId));
        when(fuelSaleRepository.findByIdAndOrganizationIdAndStationIdIn(
                saleId,
                organization.getId(),
                Set.of(stationId)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findForManager(saleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void supervisorListUsesOnlyItsOrganizationAndStationScope() {
        User supervisor = user(organization, "SUPERVISOR");
        UUID stationId = UUID.randomUUID();
        when(authorizationService.getAuthenticatedUser()).thenReturn(supervisor);
        when(stationAccessService.getAccessibleStationIds(supervisor))
                .thenReturn(Set.of(stationId));
        when(fuelSaleRepository.findForWeb(
                eq(organization.getId()),
                eq(Set.of(stationId)),
                eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        service.findForSupervisor(emptyFilter(), PageRequest.of(0, 20));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<UUID>> stations = ArgumentCaptor.forClass(Collection.class);
        verify(fuelSaleRepository).findForWeb(
                eq(organization.getId()),
                stations.capture(),
                eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
                any(Pageable.class)
        );
        assertThat(stations.getValue()).containsExactly(stationId);
    }

    @Test
    void crossOrganizationSaleCannotBeResolvedByDetailEndpoint() {
        UUID saleId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        when(stationAccessService.getAccessibleStationIds(manager))
                .thenReturn(Set.of(stationId));
        when(fuelSaleRepository.findByIdAndOrganizationIdAndStationIdIn(
                saleId,
                organization.getId(),
                Set.of(stationId)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findForManager(saleId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(fuelSaleRepository).findByIdAndOrganizationIdAndStationIdIn(
                saleId,
                organization.getId(),
                Set.of(stationId)
        );
    }

    private SaleReadFilter emptyFilter() {
        return new SaleReadFilter(null, null, null, null, null, null, null);
    }

    private Organization organization() {
        Organization value = new Organization();
        value.setId(UUID.randomUUID());
        return value;
    }

    private User user(Organization owner, String roleCode) {
        Role role = new Role();
        role.setCode(roleCode);
        role.setActive(true);
        User value = new User();
        value.setId(UUID.randomUUID());
        value.setOrganization(owner);
        value.setEnabled(true);
        value.setRoles(Set.of(role));
        return value;
    }

    private SaleResponse response(UUID id, UUID organizationId) {
        return new SaleResponse(
                id,
                "SALE-2026-000001",
                organizationId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                SaleType.CASH,
                SaleStatus.EFFECTIVE,
                null,
                new BigDecimal("1.000"),
                new BigDecimal("2.000"),
                new BigDecimal("2.000"),
                VehicleType.CAR,
                "ABC 123",
                OffsetDateTime.now(),
                null,
                null
        );
    }
}
