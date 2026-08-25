package com.fuelflex.platform.sale.service;
import java.util.*; import com.fuelflex.platform.sale.dto.PosSaleDtos.*;
public interface PosFuelSaleService{SaleResponse create(CreateSaleRequest request);SaleResponse createCredit(CreateCreditSaleRequest request);List<SaleResponse> findCurrentOperationalDaySales();SaleResponse findById(UUID saleId);SaleResponse reverse(UUID saleId,ReverseSaleRequest request);}
