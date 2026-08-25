package com.fuelflex.platform.sale.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleReadFilter;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleResponse;
import com.fuelflex.platform.sale.entity.SaleStatus;
import com.fuelflex.platform.sale.entity.SaleType;
import com.fuelflex.platform.sale.service.SaleReadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/supervisor/pos-sales")
@RequiredArgsConstructor
public class SupervisorSaleController {

    private final SaleReadService saleReadService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('pos-sale:view')")
    public Page<SaleResponse> findAll(
            @RequestParam(required = false) UUID stationId,
            @RequestParam(required = false) UUID operationalDayId,
            @RequestParam(required = false) UUID pumpAttendantId,
            @RequestParam(required = false) SaleType saleType,
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            Pageable pageable
    ) {
        return saleReadService.findForSupervisor(
                new SaleReadFilter(
                        stationId,
                        operationalDayId,
                        pumpAttendantId,
                        saleType,
                        status,
                        from,
                        to
                ),
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPERVISOR') and hasAuthority('pos-sale:view')")
    public SaleResponse findById(@PathVariable UUID id) {
        return saleReadService.findForSupervisor(id);
    }
}
