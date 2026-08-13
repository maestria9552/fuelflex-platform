package com.fuelflex.platform.station.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.pump.entity.MeteringLevel;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.entity.PumpStatus;
import com.fuelflex.platform.pump.repository.PumpRepository;
import com.fuelflex.platform.station.dto.response.StationConfigurationValidationResponse;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.entity.StationStatus;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.entity.TankStatus;
import com.fuelflex.platform.tank.repository.TankRepository;

@ExtendWith(MockitoExtension.class)
class StationConfigurationValidationServiceImplTest {

    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private StationRepository stationRepository;
    @Mock
    private DepotRepository depotRepository;
    @Mock
    private TankRepository tankRepository;
    @Mock
    private PumpRepository pumpRepository;
    @Mock
    private DispensingPointRepository dispensingPointRepository;
    @Mock
    private FuelMeterRepository fuelMeterRepository;
    @Mock
    private MeteringConsistencyService meteringConsistencyService;

    @InjectMocks
    private StationConfigurationValidationServiceImpl service;

    private UUID organizationId;
    private Station station;
    private Depot depot;
    private Tank tank;
    private Pump pump;
    private DispensingPoint point;
    private FuelMeter meter;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        station = Station.builder()
                .id(UUID.randomUUID())
                .name("Station Centre")
                .status(StationStatus.ACTIVE)
                .active(true)
                .build();
        depot = Depot.builder()
                .id(UUID.randomUUID())
                .station(station)
                .name("Dépôt principal")
                .active(true)
                .build();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Gasoil");
        product.setActive(true);
        tank = Tank.builder()
                .id(UUID.randomUUID())
                .depot(depot)
                .product(product)

                .capacityLiters(new BigDecimal("20000.000"))
                .minimumLevelLiters(BigDecimal.ZERO)
                .maximumLevelLiters(new BigDecimal("20000.000"))                .name("Citerne Gasoil")
                .status(TankStatus.ACTIVE)
                .active(true)
                .build();
        pump = Pump.builder()
                .id(UUID.randomUUID())
                .station(station)
                .name("Pompe 1")
                .meteringLevel(MeteringLevel.PUMP)
                .status(PumpStatus.ACTIVE)
                .active(true)
                .build();
        point = DispensingPoint.builder()
                .id(UUID.randomUUID())
                .pump(pump)
                .tank(tank)
                .name("Pistolet 1")
                .status(DispensingPointStatus.ACTIVE)
                .active(true)
                .build();
        meter = FuelMeter.builder()
                .id(UUID.randomUUID())
                .pump(pump)
                .name("Compteur global")
                .status(FuelMeterStatus.ACTIVE)
                .active(true)
                .build();

        when(stationRepository.findByIdAndOrganizationId(
                station.getId(), organizationId
        )).thenReturn(Optional.of(station));
        when(depotRepository
                .findByStationIdOrderByDisplayOrderAscNameAsc(station.getId()))
                .thenReturn(List.of(depot));
        when(tankRepository
                .findByDepotIdOrderByDisplayOrderAscNameAsc(depot.getId()))
                .thenReturn(List.of(tank));
        when(pumpRepository
                .findByStationOrderByDisplayOrderAscNameAsc(station))
                .thenReturn(List.of(pump));
        when(dispensingPointRepository
                .findByPumpOrderByDisplayOrderAscNameAsc(pump))
                .thenReturn(List.of(point));
        when(fuelMeterRepository
                .findByPumpOrderByDisplayOrderAscNameAsc(pump))
                .thenReturn(List.of(meter));
        when(fuelMeterRepository
                .findByDispensingPointOrderByDisplayOrderAscNameAsc(point))
                .thenReturn(List.of());
    }

    @Test
    void returnsValidForCompleteStation() {
        StationConfigurationValidationResponse response = service.validate(
                organizationId,
                station.getId()
        );

        assertTrue(response.isValid());
        assertTrue(response.getIssues().isEmpty());
    }

    @Test
    void reportsPumpLevelWithoutGlobalMeter() {
        doThrow(new BusinessException(
                "Une pompe en comptage global doit posséder exactement un compteur actif."
        )).when(meteringConsistencyService)
                .validateCompletePumpConfiguration(pump);

        StationConfigurationValidationResponse response = service.validate(
                organizationId,
                station.getId()
        );

        assertFalse(response.isValid());
        assertTrue(response.getIssues().stream().anyMatch(issue ->
                "fuel-meters".equals(issue.getStep())
        ));
    }

    @Test
    void reportsDispensingPointWithoutMeter() {
        pump.setMeteringLevel(MeteringLevel.DISPENSING_POINT);
        meter.setPump(null);
        meter.setDispensingPoint(point);
        when(fuelMeterRepository
                .findByPumpOrderByDisplayOrderAscNameAsc(pump))
                .thenReturn(List.of());
        when(fuelMeterRepository
                .findByDispensingPointOrderByDisplayOrderAscNameAsc(point))
                .thenReturn(List.of());
        doThrow(new BusinessException(
                "Chaque point de distribution actif doit posséder exactement un compteur actif."
        )).when(meteringConsistencyService)
                .validateCompletePumpConfiguration(pump);

        StationConfigurationValidationResponse response = service.validate(
                organizationId,
                station.getId()
        );

        assertFalse(response.isValid());
        assertTrue(response.getIssues().stream().anyMatch(issue ->
                issue.getMessage().contains("chaque point")
                        || issue.getMessage().contains("Chaque point")
        ));
    }

    @Test
    void acceptsValidPersistedHierarchy() {
        StationConfigurationValidationResponse response = service.validate(
                organizationId,
                station.getId()
        );

        assertTrue(response.isValid());
        assertTrue(response.getSummary().getDepots() == 1);
        assertTrue(response.getSummary().getTanks() == 1);
        assertTrue(response.getSummary().getPumps() == 1);
        assertTrue(response.getSummary().getDispensingPoints() == 1);
    }

    @Test
    void validationDoesNotPersistAnything() {
        service.validate(organizationId, station.getId());

        verify(stationRepository, never()).save(any());
        verify(depotRepository, never()).save(any());
        verify(tankRepository, never()).save(any());
        verify(pumpRepository, never()).save(any());
        verify(dispensingPointRepository, never()).save(any());
        verify(fuelMeterRepository, never()).save(any());
    }
}
