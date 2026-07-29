package com.fuelflex.platform.tank.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fuelflex.platform.tank.dto.request.TankRequest;
import com.fuelflex.platform.tank.dto.response.TankResponse;
import com.fuelflex.platform.tank.service.TankService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/stations/{stationId}/depots/{depotId}/tanks")
public class TankController {

    private final TankService tankService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TankResponse create(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId,
            @Valid @RequestBody TankRequest request
    ) {
        return tankService.create(
                organizationId,
                stationId,
                depotId,
                request
        );
    }

    @GetMapping
    public List<TankResponse> findAll(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId
    ) {
        return tankService.findAllByDepot(
                organizationId,
                stationId,
                depotId
        );
    }

    @GetMapping("/active")
    public List<TankResponse> findActive(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId
    ) {
        return tankService.findActiveByDepot(
                organizationId,
                stationId,
                depotId
        );
    }

    @GetMapping("/{tankId}")
    public TankResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId,
            @PathVariable UUID tankId
    ) {
        return tankService.findById(
                organizationId,
                stationId,
                depotId,
                tankId
        );
    }

    @PutMapping("/{tankId}")
    public TankResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId,
            @PathVariable UUID tankId,
            @Valid @RequestBody TankRequest request
    ) {
        return tankService.update(
                organizationId,
                stationId,
                depotId,
                tankId,
                request
        );
    }

    @DeleteMapping("/{tankId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId,
            @PathVariable UUID tankId
    ) {
        tankService.delete(
                organizationId,
                stationId,
                depotId,
                tankId
        );
    }
}