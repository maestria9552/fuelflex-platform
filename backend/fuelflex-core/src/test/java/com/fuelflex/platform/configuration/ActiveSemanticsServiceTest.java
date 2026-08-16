package com.fuelflex.platform.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fuelflex.platform.depot.dto.request.DepotRequest;
import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.depot.mapper.DepotMapper;
import com.fuelflex.platform.depot.repository.DepotRepository;
import com.fuelflex.platform.depot.service.impl.DepotServiceImpl;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.station.dto.request.StationRequest;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.entity.StationStatus;
import com.fuelflex.platform.station.entity.StationType;
import com.fuelflex.platform.station.mapper.StationMapper;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.impl.StationServiceImpl;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

class ActiveSemanticsServiceTest {

    private UUID organizationId;
    private UUID stationId;
    private Organization organization;
    private StationRepository stationRepository;
    private DepotRepository depotRepository;
    private StationServiceImpl stationService;
    private DepotServiceImpl depotService;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        stationId = UUID.randomUUID();

        organization = new Organization();
        organization.setId(organizationId);

        User user = new User();
        user.setEmail("supervisor@example.com");
        user.setOrganization(organization);

        UserRepository userRepository = mock(UserRepository.class);
        stationRepository = mock(StationRepository.class);
        depotRepository = mock(DepotRepository.class);

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        "password",
                        List.of()
                )
        );

        when(stationRepository.save(any(Station.class)))
                .thenAnswer(invocation -> {
                    Station station = invocation.getArgument(0);
                    if (station.getCreatedAt() == null) {
                        station.prePersist();
                    } else {
                        station.preUpdate();
                    }
                    return station;
                });

        when(depotRepository.save(any(Depot.class)))
                .thenAnswer(invocation -> {
                    Depot depot = invocation.getArgument(0);
                    if (depot.getCreatedAt() == null) {
                        depot.prePersist();
                    } else {
                        depot.preUpdate();
                    }
                    return depot;
                });

        stationService = new StationServiceImpl(
                stationRepository,
                new StationMapper(),
                userRepository,
                mock(EmployeeAssignmentService.class)
        );
        depotService = new DepotServiceImpl(
                depotRepository,
                stationRepository,
                userRepository,
                new DepotMapper()
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void stationCreateKeepsExplicitFalse() {
        assertFalse(stationService.create(
                organizationId,
                stationRequest(false)
        ).isActive());
    }

    @Test
    void stationCreateKeepsExplicitTrue() {
        assertTrue(stationService.create(
                organizationId,
                stationRequest(true)
        ).isActive());
    }

    @Test
    void stationUpdateChangesTrueToFalse() {
        Station station = station(true);
        when(stationRepository.findByIdAndOrganizationId(
                station.getId(),
                organizationId
        )).thenReturn(Optional.of(station));

        assertFalse(stationService.update(
                organizationId,
                station.getId(),
                stationRequest(false)
        ).isActive());
    }

    @Test
    void stationUpdateChangesFalseToTrue() {
        Station station = station(false);
        when(stationRepository.findByIdAndOrganizationId(
                station.getId(),
                organizationId
        )).thenReturn(Optional.of(station));

        assertTrue(stationService.update(
                organizationId,
                station.getId(),
                stationRequest(true)
        ).isActive());
    }

    @Test
    void stationCreateDefaultsNullToTrue() {
        assertTrue(stationService.create(
                organizationId,
                stationRequest(null)
        ).isActive());
    }

    @Test
    void depotCreateKeepsExplicitFalse() {
        mockParentStation();

        assertFalse(depotService.create(
                organizationId,
                stationId,
                depotRequest(false)
        ).isActive());
    }

    @Test
    void depotCreateKeepsExplicitTrue() {
        mockParentStation();

        assertTrue(depotService.create(
                organizationId,
                stationId,
                depotRequest(true)
        ).isActive());
    }

    @Test
    void depotUpdateChangesTrueToFalse() {
        Station station = mockParentStation();
        Depot depot = depot(station, true);
        when(depotRepository.findByIdAndStationId(
                depot.getId(),
                stationId
        )).thenReturn(Optional.of(depot));

        assertFalse(depotService.update(
                organizationId,
                stationId,
                depot.getId(),
                depotRequest(false)
        ).isActive());
    }

    @Test
    void depotUpdateChangesFalseToTrue() {
        Station station = mockParentStation();
        Depot depot = depot(station, false);
        when(depotRepository.findByIdAndStationId(
                depot.getId(),
                stationId
        )).thenReturn(Optional.of(depot));

        assertTrue(depotService.update(
                organizationId,
                stationId,
                depot.getId(),
                depotRequest(true)
        ).isActive());
    }

    @Test
    void depotCreateDefaultsNullToTrue() {
        mockParentStation();

        assertTrue(depotService.create(
                organizationId,
                stationId,
                depotRequest(null)
        ).isActive());
    }

    private StationRequest stationRequest(Boolean active) {
        StationRequest request = new StationRequest();
        request.setCode("STATION_01");
        request.setName("Station principale");
        request.setType(StationType.SERVICE_STATION);
        request.setStatus(StationStatus.INACTIVE);
        request.setActive(active);
        return request;
    }

    private DepotRequest depotRequest(Boolean active) {
        DepotRequest request = new DepotRequest();
        request.setCode("DEPOT_01");
        request.setName("Dépôt principal");
        request.setActive(active);
        return request;
    }

    private Station station(boolean active) {
        Station station = Station.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .code("STATION_01")
                .name("Station principale")
                .type(StationType.SERVICE_STATION)
                .status(StationStatus.INACTIVE)
                .displayOrder(1)
                .active(active)
                .build();
        station.prePersist();
        station.setActive(active);
        return station;
    }

    private Station mockParentStation() {
        Station station = station(true);
        station.setId(stationId);
        when(stationRepository.findByIdAndOrganizationId(
                stationId,
                organizationId
        )).thenReturn(Optional.of(station));
        return station;
    }

    private Depot depot(Station station, boolean active) {
        Depot depot = Depot.builder()
                .id(UUID.randomUUID())
                .station(station)
                .code("DEPOT_01")
                .name("Dépôt principal")
                .displayOrder(1)
                .active(active)
                .build();
        depot.prePersist();
        depot.setActive(active);
        return depot;
    }
}
