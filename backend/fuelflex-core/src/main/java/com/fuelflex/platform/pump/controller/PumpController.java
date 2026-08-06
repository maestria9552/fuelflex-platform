package com.fuelflex.platform.pump.controller;

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

import com.fuelflex.platform.pump.dto.request.PumpRequest;
import com.fuelflex.platform.pump.dto.response.PumpResponse;
import com.fuelflex.platform.pump.service.PumpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}"
                + "/stations/{stationId}"
                + "/pumps"
)
@RequiredArgsConstructor
@Validated
public class PumpController {

    private final PumpService pumpService;

    @PostMapping
    public ResponseEntity<PumpResponse> create(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @Valid @RequestBody PumpRequest request
    ) {
        PumpResponse response = pumpService.create(
                organizationId,
                stationId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PumpResponse>> findAllByStation(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId
    ) {
        List<PumpResponse> response =
                pumpService.findAllByStation(
                        organizationId,
                        stationId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PumpResponse>> findActiveByStation(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId
    ) {
        List<PumpResponse> response =
                pumpService.findActiveByStation(
                        organizationId,
                        stationId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pumpId}")
    public ResponseEntity<PumpResponse> findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId
    ) {
        PumpResponse response = pumpService.findById(
                organizationId,
                stationId,
                pumpId
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{pumpId}")
    public ResponseEntity<PumpResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @Valid @RequestBody PumpRequest request
    ) {
        PumpResponse response = pumpService.update(
                organizationId,
                stationId,
                pumpId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{pumpId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId
    ) {
        pumpService.delete(
                organizationId,
                stationId,
                pumpId
        );

        return ResponseEntity.noContent().build();
    }
}
