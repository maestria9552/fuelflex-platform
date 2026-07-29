package com.fuelflex.platform.station.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.station.dto.request.StationRequest;
import com.fuelflex.platform.station.dto.response.StationResponse;
import com.fuelflex.platform.station.service.StationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody StationRequest request
    ) {
        return stationService.create(
                organizationId,
                request
        );
    }

    @GetMapping
    public List<StationResponse> findAll(
            @PathVariable UUID organizationId
    ) {
        return stationService.findAllByOrganization(
                organizationId
        );
    }

    @GetMapping("/active")
    public List<StationResponse> findActive(
            @PathVariable UUID organizationId
    ) {
        return stationService.findActiveByOrganization(
                organizationId
        );
    }

    @GetMapping("/{stationId}")
    public StationResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId
    ) {
        return stationService.findById(
                organizationId,
                stationId
        );
    }

    @PutMapping("/{stationId}")
    public StationResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @Valid @RequestBody StationRequest request
    ) {
        return stationService.update(
                organizationId,
                stationId,
                request
        );
    }

    @DeleteMapping("/{stationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId
    ) {
        stationService.delete(
                organizationId,
                stationId
        );
    }
}