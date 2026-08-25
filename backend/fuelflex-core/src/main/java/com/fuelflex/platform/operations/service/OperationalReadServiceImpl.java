package com.fuelflex.platform.operations.service;

import static com.fuelflex.platform.operations.dto.OperationalDtos.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.assignment.entity.UserStationAssignment;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.fuelmeter.repository.FuelMeterRepository;
import com.fuelflex.platform.operations.entity.OperationalDay;
import com.fuelflex.platform.operations.entity.OperationalStatus;
import com.fuelflex.platform.operations.repository.OperationalDayRepository;
import com.fuelflex.platform.operations.repository.PumpShiftAssignmentRepository;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.dto.response.StationResponse;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.mapper.StationMapper;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationalReadServiceImpl implements OperationalReadService {

    private static final Comparator<String> TEXT_ORDER =
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);

    private final AuthorizationService authorizationService;
    private final StationAccessService stationAccessService;
    private final StationRepository stationRepository;
    private final StationMapper stationMapper;
    private final UserStationAssignmentRepository administrativeAssignments;
    private final OperationalDayRepository operationalDayRepository;
    private final PumpShiftAssignmentRepository shiftAssignmentRepository;
    private final FuelMeterRepository fuelMeterRepository;
    private final AssignmentMeterValidator assignmentMeterValidator;

    @Override
    public List<StationResponse> accessibleStations() {
        User manager = manager();
        Set<UUID> stationIds = stationAccessService.getAccessibleStationIds(manager);
        if (stationIds.isEmpty()) {
            return List.of();
        }
        return stationRepository
                .findByOrganizationIdAndIdInOrderByDisplayOrderAscNameAsc(
                        organizationId(manager),
                        stationIds
                )
                .stream()
                .map(stationMapper::toResponse)
                .toList();
    }

    @Override
    public List<EligiblePumpAttendantResponse> eligiblePumpAttendants(UUID stationId) {
        User manager = manager();
        UUID organizationId = organizationId(manager);
        Station station = stationRepository
                .findByIdAndOrganizationId(stationId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station was not found."));
        stationAccessService.checkStationAccess(manager, station.getId());

        Map<UUID, User> candidates = new LinkedHashMap<>();
        for (UserStationAssignment assignment : administrativeAssignments
                .findAllByStationIdAndOrganizationIdAndValidUntilIsNull(
                        station.getId(),
                        organizationId
                )) {
            User user = assignment.getUser();
            if (isEligibleAssignment(assignment, user, station, organizationId)) {
                candidates.putIfAbsent(user.getId(), user);
            }
        }

        Set<UUID> openAttendantIds = candidates.isEmpty()
                ? Set.of()
                : shiftAssignmentRepository
                        .findPumpAttendantIdsByStatusAndPumpAttendantIdIn(
                                OperationalStatus.OPEN,
                                candidates.keySet()
                        );

        return candidates.values().stream()
                .filter(user -> !openAttendantIds.contains(user.getId()))
                .sorted(Comparator.comparing(User::getLastName, TEXT_ORDER)
                        .thenComparing(User::getFirstName, TEXT_ORDER))
                .map(user -> new EligiblePumpAttendantResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getOperationalCode()
                ))
                .toList();
    }

    @Override
    public List<AvailableFuelMeterResponse> availableFuelMeters(UUID operationalDayId) {
        User manager = manager();
        UUID organizationId = organizationId(manager);
        OperationalDay day = operationalDayRepository
                .findByIdAndOrganizationId(operationalDayId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Operational day was not found."
                ));
        stationAccessService.checkStationAccess(manager, day.getStation().getId());
        if (day.getStatus() != OperationalStatus.OPEN) {
            throw new ConflictException(
                    "The operational day is closed and cannot receive assignments."
            );
        }

        List<FuelMeter> candidates = fuelMeterRepository.findActiveByStationId(
                day.getStation().getId(),
                FuelMeterStatus.ACTIVE
        );
        Set<UUID> meterIds = candidates.stream().map(FuelMeter::getId).collect(
                java.util.stream.Collectors.toSet()
        );
        Set<UUID> openMeterIds = meterIds.isEmpty()
                ? Set.of()
                : shiftAssignmentRepository.findFuelMeterIdsByStatusAndFuelMeterIdIn(
                        OperationalStatus.OPEN,
                        meterIds
                );

        return candidates.stream()
                .filter(meter -> !openMeterIds.contains(meter.getId()))
                .map(meter -> availableMeter(meter, day.getStation()))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator
                        .comparing(
                                (AvailableFuelMeterResponse response) -> response.pump().name(),
                                TEXT_ORDER
                        )
                        .thenComparing(
                                response -> response.dispensingPoint() == null
                                        ? null
                                        : response.dispensingPoint().name(),
                                TEXT_ORDER
                        )
                        .thenComparing(AvailableFuelMeterResponse::name, TEXT_ORDER))
                .toList();
    }

    private AvailableFuelMeterResponse availableMeter(FuelMeter meter, Station station) {
        try {
            Pump pump = assignmentMeterValidator.validate(meter, station);
            DispensingPoint point = meter.getDispensingPoint();
            return new AvailableFuelMeterResponse(
                    meter.getId(),
                    meter.getCode(),
                    meter.getName(),
                    meter.getCurrentIndex(),
                    pump.getMeteringLevel(),
                    new PumpSummary(pump.getId(), pump.getName()),
                    point == null
                            ? null
                            : new DispensingPointSummary(point.getId(), point.getName())
            );
        } catch (BusinessException | ForbiddenException exception) {
            return null;
        }
    }

    private boolean isEligibleAssignment(
            UserStationAssignment assignment,
            User user,
            Station station,
            UUID organizationId
    ) {
        return assignment != null
                && assignment.isActive()
                && assignment.getOrganization() != null
                && organizationId.equals(assignment.getOrganization().getId())
                && assignment.getStation() != null
                && station.getId().equals(assignment.getStation().getId())
                && user != null
                && user.getId() != null
                && user.isEnabled()
                && user.getOrganization() != null
                && organizationId.equals(user.getOrganization().getId())
                && hasRole(user, "PUMP_ATTENDANT");
    }

    private User manager() {
        User actor = authorizationService.getAuthenticatedUser();
        if (actor == null
                || !actor.isEnabled()
                || actor.getOrganization() == null
                || !hasRole(actor, "MANAGER")) {
            throw new ForbiddenException("Only a manager can access this resource.");
        }
        return actor;
    }

    private boolean hasRole(User user, String code) {
        return user.getRoles().stream()
                .filter(Role::isActive)
                .anyMatch(role -> code.equalsIgnoreCase(role.getCode()));
    }

    private UUID organizationId(User user) {
        return user.getOrganization().getId();
    }
}
