package com.fuelflex.platform.operations.service;
import java.util.*; import com.fuelflex.platform.operations.dto.DailyOperationsDtos.*;
public interface DailyOperationsService{ExpenseResponse addExpense(UUID dayId,ExpenseRequest request);List<ExpenseResponse> expenses(UUID dayId);GaugeResponse addGauge(UUID dayId,GaugeRequest request);List<GaugeResponse> gauges(UUID dayId);List<ReconciliationResponse> reconciliations(UUID dayId);RjvResponse rjv(UUID dayId);}
