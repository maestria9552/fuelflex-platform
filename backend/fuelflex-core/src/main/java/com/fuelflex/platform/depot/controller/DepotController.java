package com.fuelflex.platform.depot.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.depot.dto.request.DepotRequest;
import com.fuelflex.platform.depot.dto.response.DepotResponse;
import com.fuelflex.platform.depot.service.DepotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/stations/{stationId}/depots"
)
@RequiredArgsConstructor
@Validated
public class DepotController {

    private final DepotService depotService;

    @PostMapping
    public ResponseEntity<DepotResponse> create(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @Valid @RequestBody DepotRequest request
    ) {
        DepotResponse response = depotService.create(
                organizationId,
                stationId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DepotResponse>> findAllByStation(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId
    ) {
        List<DepotResponse> response =
                depotService.findAllByStation(
                        organizationId,
                        stationId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<DepotResponse>> findActiveByStation(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId
    ) {
        List<DepotResponse> response =
                depotService.findActiveByStation(
                        organizationId,
                        stationId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{depotId}")
    public ResponseEntity<DepotResponse> findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId
    ) {
        DepotResponse response = depotService.findById(
                organizationId,
                stationId,
                depotId
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{depotId}")
    public ResponseEntity<DepotResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId,
            @Valid @RequestBody DepotRequest request
    ) {
        DepotResponse response = depotService.update(
                organizationId,
                stationId,
                depotId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{depotId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID depotId
    ) {
        depotService.delete(
                organizationId,
                stationId,
                depotId
        );

        return ResponseEntity.noContent().build();
    }
}