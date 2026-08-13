package com.fuelflex.platform.stationproduct.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fuelflex.platform.stationproduct.dto.request.StationProductRequest;
import com.fuelflex.platform.stationproduct.dto.response.StationProductResponse;
import com.fuelflex.platform.stationproduct.service.StationProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/stations/{stationId}/products")
@RequiredArgsConstructor
@Validated
public class StationProductController {
    private final StationProductService stationProductService;

    @PostMapping
    public ResponseEntity<StationProductResponse> create(@PathVariable UUID organizationId,
            @PathVariable UUID stationId, @Valid @RequestBody StationProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stationProductService.create(organizationId, stationId, request));
    }

    @GetMapping
    public ResponseEntity<List<StationProductResponse>> findAll(@PathVariable UUID organizationId,
            @PathVariable UUID stationId) {
        return ResponseEntity.ok(stationProductService.findAllByStation(organizationId, stationId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<StationProductResponse>> findActive(@PathVariable UUID organizationId,
            @PathVariable UUID stationId) {
        return ResponseEntity.ok(stationProductService.findActiveByStation(organizationId, stationId));
    }

    @GetMapping("/{stationProductId}")
    public ResponseEntity<StationProductResponse> findById(@PathVariable UUID organizationId,
            @PathVariable UUID stationId, @PathVariable UUID stationProductId) {
        return ResponseEntity.ok(stationProductService.findById(organizationId, stationId, stationProductId));
    }

    @PutMapping("/{stationProductId}")
    public ResponseEntity<StationProductResponse> update(@PathVariable UUID organizationId,
            @PathVariable UUID stationId, @PathVariable UUID stationProductId,
            @Valid @RequestBody StationProductRequest request) {
        return ResponseEntity.ok(stationProductService.update(organizationId, stationId, stationProductId, request));
    }

    @DeleteMapping("/{stationProductId}")
    public ResponseEntity<Void> delete(@PathVariable UUID organizationId, @PathVariable UUID stationId,
            @PathVariable UUID stationProductId) {
        stationProductService.delete(organizationId, stationId, stationProductId);
        return ResponseEntity.noContent().build();
    }
}
