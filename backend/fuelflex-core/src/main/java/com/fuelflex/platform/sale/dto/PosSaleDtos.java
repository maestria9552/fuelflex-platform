package com.fuelflex.platform.sale.dto;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID; import com.fuelflex.platform.sale.entity.*; import jakarta.validation.constraints.*;
public final class PosSaleDtos {private PosSaleDtos(){}
 public record CreateSaleRequest(@NotNull @DecimalMin(value="0.000",inclusive=false) @Digits(integer=16,fraction=3) BigDecimal quantity,@NotNull VehicleType vehicleType,@Size(max=40) String licensePlate){}
 public record CreateCreditSaleRequest(@NotNull UUID creditCustomerId,@NotNull @DecimalMin(value="0.000",inclusive=false) @Digits(integer=16,fraction=3) BigDecimal quantity,@NotNull VehicleType vehicleType,@Size(max=40) String licensePlate){}
 public record ReverseSaleRequest(@NotBlank @Size(max=1000) String reason){}
 public record ReferenceSummary(UUID id,String name){} public record PumpAttendantSummary(UUID id,String firstName,String lastName,String operationalCode){}
 public record SaleResponse(UUID id,String saleNumber,UUID organizationId,ReferenceSummary station,UUID operationalDayId,UUID shiftAssignmentId,PumpAttendantSummary pumpAttendant,ReferenceSummary pump,ReferenceSummary dispensingPoint,ReferenceSummary fuelMeter,ReferenceSummary tank,ReferenceSummary product,ReferenceSummary tariffCategory,SaleType saleType,SaleStatus status,ReferenceSummary creditCustomer,BigDecimal quantity,BigDecimal unitPrice,BigDecimal totalAmount,VehicleType vehicleType,String licensePlate,OffsetDateTime soldAt,OffsetDateTime reversedAt,String reversalReason){}
}
