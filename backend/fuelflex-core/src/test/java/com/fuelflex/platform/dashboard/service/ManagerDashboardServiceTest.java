package com.fuelflex.platform.dashboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.dashboard.dto.ManagerDashboardDtos.Response;
import com.fuelflex.platform.dashboard.repository.ManagerDashboardRepository;
import com.fuelflex.platform.dashboard.repository.ManagerDashboardRepository.DailyProjection;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.reception.repository.ReceptionStockBalanceRepository;
import com.fuelflex.platform.reception.repository.ReceptionStockBalanceRepository.BalanceProjection;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;

@ExtendWith(MockitoExtension.class)
class ManagerDashboardServiceTest {
    @Mock AuthorizationService authorization;
    @Mock StationAccessService stationAccess;
    @Mock StationRepository stations;
    @Mock ManagerDashboardRepository repository;
    @Mock ReceptionStockBalanceRepository stockBalances;
    private ManagerDashboardService service;
    private User manager;
    private Station station;

    @BeforeEach
    void setUp() {
        service = new ManagerDashboardService(authorization, stationAccess, stations, repository, stockBalances);
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setDefaultCurrency("CDF");
        Role role = new Role(); role.setCode("MANAGER"); role.setActive(true);
        manager = new User(); manager.setOrganization(organization); manager.setRoles(Set.of(role));
        station = new Station(); station.setId(UUID.randomUUID()); station.setName("Kinshasa Centre");
        when(authorization.getAuthenticatedUser()).thenReturn(manager);
    }

    @Test
    void consolidatesPeriodsAndStructuresAuthoritativeTankStocksByProduct() {
        when(stationAccess.getAccessibleStationIds(manager)).thenReturn(Set.of(station.getId()));
        when(stations.findByIdAndOrganizationId(station.getId(), manager.getOrganization().getId()))
                .thenReturn(Optional.of(station));
        LocalDate today = LocalDate.now();
        DailyProjection previous = row(today.minusDays(35), "50", "20", "5", "1", "30");
        DailyProjection currentOne = row(today.minusDays(2), "100", "40", "10", "2", "60");
        DailyProjection currentTwo = row(today, "200", "60", "20", "3", "90");
        when(repository.findDailySnapshots(eq(manager.getOrganization().getId()), eq(station.getId()),
                eq(today.minusDays(59)), eq(today))).thenReturn(List.of(previous, currentOne, currentTwo));
        BalanceProjection tankOne = stock("Essence", "E-01", "7250.500", "10000");
        BalanceProjection tankTwo = stock("Essence", "E-02", "5200", "10000");
        when(stockBalances.findDashboardBalances(Set.of(station.getId())))
                .thenReturn(List.of(tankOne, tankTwo));

        Response response = service.get(station.getId());

        assertThat(response.currency()).isEqualTo("CDF");
        assertThat(response.current().cash()).isEqualByComparingTo("300.000");
        assertThat(response.current().credit()).isEqualByComparingTo("100.000");
        assertThat(response.current().revenue()).isEqualByComparingTo("400.000");
        assertThat(response.current().disbursedExpenses()).isEqualByComparingTo("30.000");
        assertThat(response.current().cashAfterExpenses()).isEqualByComparingTo("270.000");
        assertThat(response.current().internalVolume()).isEqualByComparingTo("5.000");
        assertThat(response.previous().revenue()).isEqualByComparingTo("70.000");
        assertThat(response.dailySales()).hasSize(30);
        assertThat(response.dailySales().getFirst().date()).isEqualTo(today.minusDays(29));
        assertThat(response.dailySales().getLast().cash()).isEqualByComparingTo("200.000");
        assertThat(response.products()).singleElement().satisfies(product -> {
            assertThat(product.productName()).isEqualTo("Essence");
            assertThat(product.stockQuantity()).isEqualByComparingTo("12450.500");
            assertThat(product.totalCapacity()).isEqualByComparingTo("20000.000");
            assertThat(product.fillPercentage()).isEqualByComparingTo("62.3");
            assertThat(product.tanks()).hasSize(2);
            assertThat(product.tanks().getFirst().fillPercentage()).isEqualByComparingTo("72.5");
        });
    }

    @Test
    void refusesAStationOutsideManagerScope() {
        when(stationAccess.getAccessibleStationIds(manager)).thenReturn(Set.of());
        assertThatThrownBy(() -> service.get(station.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(repository, stations, stockBalances);
    }

    private DailyProjection row(LocalDate date, String cash, String credit, String expenses,
                                String internalVolume, String internalAmount) {
        DailyProjection row = mock(DailyProjection.class);
        when(row.getBusinessDate()).thenReturn(date);
        when(row.getCash()).thenReturn(new BigDecimal(cash));
        when(row.getCredit()).thenReturn(new BigDecimal(credit));
        when(row.getExpenses()).thenReturn(new BigDecimal(expenses));
        when(row.getInternalVolume()).thenReturn(new BigDecimal(internalVolume));
        when(row.getInternalAmount()).thenReturn(new BigDecimal(internalAmount));
        return row;
    }

    private BalanceProjection stock(String productName, String tankName, String stock, String capacity) {
        BalanceProjection row = mock(BalanceProjection.class);
        when(row.getProductId()).thenReturn(UUID.nameUUIDFromBytes(productName.getBytes()));
        lenient().when(row.getProductName()).thenReturn(productName);
        when(row.getTankId()).thenReturn(UUID.nameUUIDFromBytes(tankName.getBytes()));
        when(row.getTankName()).thenReturn(tankName);
        when(row.getCurrentStock()).thenReturn(new BigDecimal(stock));
        when(row.getCapacity()).thenReturn(new BigDecimal(capacity));
        return row;
    }
}
