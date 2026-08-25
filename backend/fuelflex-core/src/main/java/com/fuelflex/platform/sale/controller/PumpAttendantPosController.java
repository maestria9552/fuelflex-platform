package com.fuelflex.platform.sale.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.fuelflex.platform.operations.dto.OperationalDtos.PosOperationalContext;
import com.fuelflex.platform.operations.service.PumpAttendantOperationalContextService;
import com.fuelflex.platform.sale.dto.PosSaleDtos.CreateSaleRequest;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleResponse;
import com.fuelflex.platform.sale.service.PosFuelSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pump-attendant/pos")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PUMP_ATTENDANT')")
public class PumpAttendantPosController {
    private final PumpAttendantOperationalContextService contextService;
    private final PosFuelSaleService saleService;

    @GetMapping("/context")
    @PreAuthorize("hasAuthority('PUMP_ATTENDANT') and hasAuthority('pos-sale:view')")
    public PosOperationalContext context() { return contextService.requireCurrentContext(); }

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PUMP_ATTENDANT') and hasAuthority('pos-sale:create')")
    public SaleResponse create(@Valid @RequestBody CreateSaleRequest request) { return saleService.create(request); }

    @GetMapping("/sales")
    @PreAuthorize("hasAuthority('PUMP_ATTENDANT') and hasAuthority('pos-sale:view')")
    public List<SaleResponse> findAll() { return saleService.findCurrentOperationalDaySales(); }

    @GetMapping("/sales/{saleId}")
    @PreAuthorize("hasAuthority('PUMP_ATTENDANT') and hasAuthority('pos-sale:view')")
    public SaleResponse findById(@PathVariable UUID saleId) { return saleService.findById(saleId); }
}
