package com.fuelflex.platform.operations.dto;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.*; import jakarta.validation.constraints.*; import com.fuelflex.platform.sale.entity.SaleType;
public final class DailyOperationsDtos{private DailyOperationsDtos(){}
 public record ExpenseRequest(@NotBlank @Size(max=180)String label,@NotNull @DecimalMin(value="0.000",inclusive=false)@Digits(integer=16,fraction=3)BigDecimal amount,@Size(max=100)String reference,@Size(max=1000)String comment){}
 public record ExpenseResponse(UUID id,String label,BigDecimal amount,String reference,String comment,UUID createdBy,OffsetDateTime createdAt){}
 public record GaugeRequest(@NotNull UUID tankId,@NotNull @DecimalMin("0.000")@Digits(integer=16,fraction=3)BigDecimal physicalStock,@Size(max=1000)String comment){}
 public record GaugeResponse(UUID id,UUID tankId,String tankName,BigDecimal theoreticalStock,BigDecimal physicalStock,BigDecimal stockVariance,UUID recordedBy,OffsetDateTime recordedAt,String comment){}
 public record ReconciliationResponse(UUID id,UUID shiftAssignmentId,UUID pumpAttendantId,String pumpAttendantName,UUID fuelMeterId,String fuelMeterName,BigDecimal openingIndex,BigDecimal closingIndex,BigDecimal meteredVolume,BigDecimal cashVolume,BigDecimal creditVolume,BigDecimal totalSoldVolume,BigDecimal volumeVariance,OffsetDateTime calculatedAt){}
 public record AggregateRow(UUID id,String name,BigDecimal cashVolume,BigDecimal creditVolume,BigDecimal totalVolume,BigDecimal cashAmount,BigDecimal creditAmount,BigDecimal totalAmount){}
 public record StockRow(UUID tankId,String tankName,UUID productId,String productName,BigDecimal theoreticalStock,BigDecimal physicalStock,BigDecimal stockVariance){}
 public record RjvResponse(UUID operationalDayId,UUID organizationId,UUID stationId,String stationName,java.time.LocalDate businessDate,String status,BigDecimal cashVolume,BigDecimal cashAmount,BigDecimal creditVolume,BigDecimal creditAmount,BigDecimal totalSoldVolume,BigDecimal totalSalesAmount,BigDecimal meteredVolume,BigDecimal volumeVariance,BigDecimal expenseAmount,BigDecimal theoreticalStock,BigDecimal physicalStock,BigDecimal stockVariance,List<AggregateRow> byProduct,List<AggregateRow> byPumpAttendant,List<AggregateRow> byFuelMeter,List<ReconciliationResponse> reconciliations,List<ExpenseResponse> expenses,List<StockRow> stocks){}
}
