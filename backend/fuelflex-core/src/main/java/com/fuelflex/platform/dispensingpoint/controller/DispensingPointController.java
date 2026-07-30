package com.fuelflex.platform.dispensingpoint.controller;

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

import com.fuelflex.platform.dispensingpoint.dto.request.DispensingPointRequest;
import com.fuelflex.platform.dispensingpoint.dto.response.DispensingPointResponse;
import com.fuelflex.platform.dispensingpoint.service.DispensingPointService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}"
                + "/stations/{stationId}"
                + "/pumps/{pumpId}"
                + "/dispensing-points"
)
@RequiredArgsConstructor
@Validated
public class DispensingPointController {

    private final DispensingPointService dispensingPointService;

    @PostMapping
    public ResponseEntity<DispensingPointResponse> create(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @Valid @RequestBody DispensingPointRequest request
    ) {
        DispensingPointResponse response =
                dispensingPointService.create(
                        organizationId,
                        stationId,
                        pumpId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DispensingPointResponse>> findAllByPump(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId
    ) {
        List<DispensingPointResponse> response =
                dispensingPointService.findAllByPump(
                        organizationId,
                        stationId,
                        pumpId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<DispensingPointResponse>> findActiveByPump(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId
    ) {
        List<DispensingPointResponse> response =
                dispensingPointService.findActiveByPump(
                        organizationId,
                        stationId,
                        pumpId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{dispensingPointId}")
    public ResponseEntity<DispensingPointResponse> findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @PathVariable UUID dispensingPointId
    ) {
        DispensingPointResponse response =
                dispensingPointService.findById(
                        organizationId,
                        stationId,
                        pumpId,
                        dispensingPointId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{dispensingPointId}")
    public ResponseEntity<DispensingPointResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @PathVariable UUID dispensingPointId,
            @Valid @RequestBody DispensingPointRequest request
    ) {
        DispensingPointResponse response =
                dispensingPointService.update(
                        organizationId,
                        stationId,
                        pumpId,
                        dispensingPointId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{dispensingPointId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @PathVariable UUID dispensingPointId
    ) {
        dispensingPointService.delete(
                organizationId,
                stationId,
                pumpId,
                dispensingPointId
        );

        return ResponseEntity.noContent().build();
    }
}