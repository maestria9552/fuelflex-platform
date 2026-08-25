package com.fuelflex.platform.sale.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleReadFilter;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleResponse;

public interface SaleReadService {

    Page<SaleResponse> findForManager(SaleReadFilter filter, Pageable pageable);

    SaleResponse findForManager(UUID saleId);

    Page<SaleResponse> findForSupervisor(SaleReadFilter filter, Pageable pageable);

    SaleResponse findForSupervisor(UUID saleId);
}
