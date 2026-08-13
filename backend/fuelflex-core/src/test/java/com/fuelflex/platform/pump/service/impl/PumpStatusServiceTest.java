package com.fuelflex.platform.pump.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.fuelmeter.service.MeteringConsistencyService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.pump.dto.request.PumpRequest;
import com.fuelflex.platform.pump.entity.MeteringLevel;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.entity.PumpStatus;
import com.fuelflex.platform.pump.mapper.PumpMapper;
import com.fuelflex.platform.pump.repository.PumpRepository;
import com.fuelflex.platform.station.entity.Station;

class PumpStatusServiceTest {

    private UUID organizationId;
    private UUID stationId;
    private PumpRepository pumpRepository;
    private MeteringConsistencyService meteringConsistencyService;
    private Station station;
    private PumpServiceImpl pumpService;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        stationId = UUID.randomUUID();

        Organization organization = new Organization();
        organization.setId(organizationId);

        station = Station.builder()
                .id(stationId)
                .organization(organization)
                .code("STATION_01")
                .name("Station principale")
                .active(true)
                .build();

        pumpRepository = mock(PumpRepository.class);
        meteringConsistencyService = mock(MeteringConsistencyService.class);
        EntityLookupService entityLookupService = mock(EntityLookupService.class);

        when(entityLookupService.findStation(organizationId, stationId))
                .thenReturn(station);
        when(pumpRepository.save(any(Pump.class)))
                .thenAnswer(invocation -> {
                    Pump pump = invocation.getArgument(0);
                    if (pump.getCreatedAt() == null) {
                        pump.prePersist();
                    } else {
                        pump.preUpdate();
                    }
                    return pump;
                });

        pumpService = new PumpServiceImpl(
                pumpRepository,
                new PumpMapper(),
                entityLookupService,
                mock(AuthorizationService.class),
                meteringConsistencyService
        );
    }

    @Test
    void createUsesExplicitActiveStatus() {
        PumpRequest request = request(true, PumpStatus.ACTIVE);

        var response = pumpService.create(
                organizationId,
                stationId,
                request
        );

        assertTrue(response.isActive());
        assertEquals(PumpStatus.ACTIVE, response.getStatus());

        verify(meteringConsistencyService, never())
                .validateBeforeActivatingPump(any(Pump.class));
    }


    @Test
    void createActiveDispensingPointLevelWithoutPoints() {
        PumpRequest request = request(true, PumpStatus.ACTIVE);
        request.setMeteringLevel(MeteringLevel.DISPENSING_POINT);

        var response = pumpService.create(
                organizationId,
                stationId,
                request
        );

        assertTrue(response.isActive());
        assertEquals(PumpStatus.ACTIVE, response.getStatus());
    }

    @Test
    void createWithoutStatusKeepsInactiveDefault() {
        PumpRequest request = request(false, null);

        assertEquals(PumpStatus.INACTIVE, pumpService.create(
                organizationId,
                stationId,
                request
        ).getStatus());
    }

    @Test
    void updateChangesInactiveToActive() {
        Pump pump = existingPump(PumpStatus.INACTIVE);
        mockExistingPump(pump);

        assertEquals(PumpStatus.ACTIVE, pumpService.update(
                organizationId,
                stationId,
                pump.getId(),
                request(false, PumpStatus.ACTIVE)
        ).getStatus());
    }

    @Test
    void updateChangesActiveToMaintenance() {
        Pump pump = existingPump(PumpStatus.ACTIVE);
        mockExistingPump(pump);

        assertEquals(PumpStatus.MAINTENANCE, pumpService.update(
                organizationId,
                stationId,
                pump.getId(),
                request(false, PumpStatus.MAINTENANCE)
        ).getStatus());
    }

    @Test
    void updateWithoutStatusKeepsExistingStatus() {
        Pump pump = existingPump(PumpStatus.ACTIVE);
        mockExistingPump(pump);

        assertEquals(PumpStatus.ACTIVE, pumpService.update(
                organizationId,
                stationId,
                pump.getId(),
                request(false, null)
        ).getStatus());
    }

    private PumpRequest request(Boolean active, PumpStatus status) {
        PumpRequest request = new PumpRequest();
        request.setCode("PUMP_01");
        request.setName("Pompe 1");
        request.setPumpNumber(1);
        request.setMeteringLevel(MeteringLevel.PUMP);
        request.setDisplayOrder(1);
        request.setActive(active);
        request.setStatus(status);
        return request;
    }

    private Pump existingPump(PumpStatus status) {
        Pump pump = Pump.builder()
                .id(UUID.randomUUID())
                .station(station)
                .code("PUMP_01")
                .name("Pompe 1")
                .pumpNumber(1)
                .meteringLevel(MeteringLevel.PUMP)
                .status(status)
                .displayOrder(1)
                .active(false)
                .build();
        pump.prePersist();
        pump.setStatus(status);
        return pump;
    }

    private void mockExistingPump(Pump pump) {
        when(pumpRepository.findByStationAndId(station, pump.getId()))
                .thenReturn(Optional.of(pump));
    }
}
