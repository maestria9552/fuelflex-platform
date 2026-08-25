package com.fuelflex.platform.sale.service;

import java.util.List;
import java.util.UUID;
import com.fuelflex.platform.sale.dto.PosSaleDtos.CreateSaleRequest;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleResponse;

public interface PosFuelSaleService {
    SaleResponse create(CreateSaleRequest request);
    List<SaleResponse> findCurrentOperationalDaySales();
    SaleResponse findById(UUID saleId);
}
