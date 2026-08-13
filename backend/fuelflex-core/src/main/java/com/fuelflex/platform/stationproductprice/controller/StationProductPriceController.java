package com.fuelflex.platform.stationproductprice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceRequest;
import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceUpdateRequest;
import com.fuelflex.platform.stationproductprice.dto.response.StationProductPriceResponse;
import com.fuelflex.platform.stationproductprice.service.StationProductPriceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/stations/{stationId}/station-products/{stationProductId}/prices")
@RequiredArgsConstructor
@Validated
public class StationProductPriceController {
    private final StationProductPriceService service;

    @PostMapping
    public ResponseEntity<StationProductPriceResponse> create(@PathVariable UUID organizationId,
            @PathVariable UUID stationId, @PathVariable UUID stationProductId,
            @Valid @RequestBody StationProductPriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(organizationId, stationId, stationProductId, request));
    }

    @GetMapping
    public ResponseEntity<List<StationProductPriceResponse>> findAll(@PathVariable UUID organizationId,
            @PathVariable UUID stationId, @PathVariable UUID stationProductId) {
        return ResponseEntity.ok(service.findAllByStationProduct(organizationId, stationId, stationProductId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<StationProductPriceResponse>> findActive(@PathVariable UUID organizationId,
            @PathVariable UUID stationId, @PathVariable UUID stationProductId) {
        return ResponseEntity.ok(service.findActiveByStationProduct(organizationId, stationId, stationProductId));
    }

    @GetMapping("/{stationProductPriceId}")
    public ResponseEntity<StationProductPriceResponse> findById(@PathVariable UUID organizationId,
            @PathVariable UUID stationId, @PathVariable UUID stationProductId,
            @PathVariable UUID stationProductPriceId) {
        return ResponseEntity.ok(service.findById(organizationId, stationId, stationProductId, stationProductPriceId));
    }

    @PutMapping("/{stationProductPriceId}")
    public ResponseEntity<StationProductPriceResponse> update(@PathVariable UUID organizationId,
            @PathVariable UUID stationId, @PathVariable UUID stationProductId,
            @PathVariable UUID stationProductPriceId,
            @Valid @RequestBody StationProductPriceUpdateRequest request) {
        return ResponseEntity.ok(service.update(organizationId, stationId, stationProductId,
                stationProductPriceId, request));
    }

    @DeleteMapping("/{stationProductPriceId}")
    public ResponseEntity<Void> delete(@PathVariable UUID organizationId, @PathVariable UUID stationId,
            @PathVariable UUID stationProductId, @PathVariable UUID stationProductPriceId) {
        service.delete(organizationId, stationId, stationProductId, stationProductPriceId);
        return ResponseEntity.noContent().build();
    }
}
