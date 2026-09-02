package com.fuelflex.platform.dashboard.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.dashboard.dto.ManagerDashboardDtos.Response;
import com.fuelflex.platform.dashboard.service.ManagerDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/manager/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {
    private final ManagerDashboardService service;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('operational-day:view')")
    public Response get(@RequestParam UUID stationId) {
        return service.get(stationId);
    }
}
