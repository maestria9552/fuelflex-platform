package com.fuelflex.platform.operations.controller;

import static com.fuelflex.platform.operations.dto.OperationalDtos.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.operations.service.OperationalDayService;
import com.fuelflex.platform.operations.service.OperationalReadService;
import com.fuelflex.platform.station.dto.response.StationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
public class ManagerOperationalDayController {

    private final OperationalDayService service;
    private final OperationalReadService operationalReadService;

    @GetMapping("/stations")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('operational-day:view')")
    public List<StationResponse> accessibleStations() {
        return operationalReadService.accessibleStations();
    }

    @GetMapping("/stations/{stationId}/eligible-pump-attendants")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('shift-assignment:create')")
    public List<EligiblePumpAttendantResponse> eligiblePumpAttendants(
            @PathVariable UUID stationId
    ) {
        return operationalReadService.eligiblePumpAttendants(stationId);
    }

    @GetMapping("/operational-days/{id}/available-fuel-meters")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('shift-assignment:create')")
    public List<AvailableFuelMeterResponse> availableFuelMeters(@PathVariable UUID id) {
        return operationalReadService.availableFuelMeters(id);
    }

    @PostMapping("/operational-days")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('operational-day:open')")
    public DayResponse open(@Valid @RequestBody OpenDayRequest request) {
        return service.open(request);
    }

    @GetMapping("/operational-days")
    @PreAuthorize("(hasAuthority('MANAGER') or hasAuthority('SUPERVISOR')) and hasAuthority('operational-day:view')")
    public List<DayResponse> all() {
        return service.findAll();
    }

    @GetMapping("/operational-days/{id}")
    @PreAuthorize("(hasAuthority('MANAGER') or hasAuthority('SUPERVISOR')) and hasAuthority('operational-day:view')")
    public DayResponse one(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping("/operational-days/{id}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('shift-assignment:create')")
    public AssignmentResponse assign(
            @PathVariable UUID id,
            @Valid @RequestBody OpenAssignmentRequest request
    ) {
        return service.assign(id, request);
    }

    @GetMapping("/operational-days/{id}/assignments")
    @PreAuthorize("(hasAuthority('MANAGER') or hasAuthority('SUPERVISOR')) and hasAuthority('shift-assignment:view')")
    public List<AssignmentResponse> assignments(@PathVariable UUID id) {
        return service.assignments(id);
    }

    @GetMapping("/shift-assignments/{id}")
    @PreAuthorize("(hasAuthority('MANAGER') or hasAuthority('SUPERVISOR')) and hasAuthority('shift-assignment:view')")
    public AssignmentResponse assignment(@PathVariable UUID id) {
        return service.assignment(id);
    }

    @PostMapping("/shift-assignments/{id}/close")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('shift-assignment:close')")
    public AssignmentResponse closeAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody CloseAssignmentRequest request
    ) {
        return service.closeAssignment(id, request);
    }

    @PostMapping("/operational-days/{id}/close")
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('operational-day:close')")
    public DayResponse close(
            @PathVariable UUID id,
            @Valid @RequestBody CloseDayRequest request
    ) {
        return service.close(id, request);
    }
}
