package com.fuelflex.platform.fuelmeter.controller;

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

import com.fuelflex.platform.fuelmeter.dto.request.FuelMeterRequest;
import com.fuelflex.platform.fuelmeter.dto.response.FuelMeterResponse;
import com.fuelflex.platform.fuelmeter.service.FuelMeterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}"
                + "/stations/{stationId}"
                + "/pumps/{pumpId}"
                + "/fuel-meters"
)
@RequiredArgsConstructor
@Validated
public class PumpFuelMeterController {

    private final FuelMeterService fuelMeterService;

    @PostMapping
    public ResponseEntity<FuelMeterResponse> create(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @Valid @RequestBody FuelMeterRequest request
    ) {
        FuelMeterResponse response = fuelMeterService.create(
                organizationId,
                stationId,
                pumpId,
                null,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FuelMeterResponse>> findAll(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId
    ) {
        return ResponseEntity.ok(
                fuelMeterService.findAll(
                        organizationId,
                        stationId,
                        pumpId,
                        null
                )
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<FuelMeterResponse>> findActive(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId
    ) {
        return ResponseEntity.ok(
                fuelMeterService.findActive(
                        organizationId,
                        stationId,
                        pumpId,
                        null
                )
        );
    }

    @GetMapping("/{fuelMeterId}")
    public ResponseEntity<FuelMeterResponse> findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @PathVariable UUID fuelMeterId
    ) {
        return ResponseEntity.ok(
                fuelMeterService.findById(
                        organizationId,
                        stationId,
                        pumpId,
                        null,
                        fuelMeterId
                )
        );
    }

    @PutMapping("/{fuelMeterId}")
    public ResponseEntity<FuelMeterResponse> update(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @PathVariable UUID fuelMeterId,
            @Valid @RequestBody FuelMeterRequest request
    ) {
        return ResponseEntity.ok(
                fuelMeterService.update(
                        organizationId,
                        stationId,
                        pumpId,
                        null,
                        fuelMeterId,
                        request
                )
        );
    }

    @DeleteMapping("/{fuelMeterId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID stationId,
            @PathVariable UUID pumpId,
            @PathVariable UUID fuelMeterId
    ) {
        fuelMeterService.delete(
                organizationId,
                stationId,
                pumpId,
                null,
                fuelMeterId
        );
        return ResponseEntity.noContent().build();
    }
}
