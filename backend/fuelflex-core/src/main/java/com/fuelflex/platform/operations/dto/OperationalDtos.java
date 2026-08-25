package com.fuelflex.platform.operations.dto;
import java.math.BigDecimal; import java.time.*; import java.util.UUID; import jakarta.validation.constraints.*;
import com.fuelflex.platform.operations.entity.OperationalStatus;
public final class OperationalDtos { private OperationalDtos(){}
 public record OpenDayRequest(@NotNull UUID stationId,@NotNull LocalDate businessDate){}
 public record OpenAssignmentRequest(@NotNull UUID pumpAttendantId,@NotNull UUID fuelMeterId){}
 public record CloseAssignmentRequest(@NotNull @DecimalMin("0.000") @Digits(integer=16,fraction=3) BigDecimal closingIndex){}
 public record UserSummary(UUID id,String firstName,String lastName){}
 public record StationSummary(UUID id,String name){}
 public record PumpSummary(UUID id,String name){}
 public record DispensingPointSummary(UUID id,String name){}
 public record FuelMeterSummary(UUID id,String name){}
 public record DayResponse(UUID id,UUID organizationId,StationSummary station,LocalDate businessDate,OperationalStatus status,UserSummary openedBy,OffsetDateTime openedAt,OffsetDateTime createdAt,OffsetDateTime updatedAt){}
 public record AssignmentResponse(UUID id,OperationalStatus status,DayResponse operationalDay,UserSummary pumpAttendant,String operationalCode,StationSummary station,PumpSummary pump,DispensingPointSummary dispensingPoint,FuelMeterSummary fuelMeter,BigDecimal openingIndex,BigDecimal closingIndex,BigDecimal meteredVolume,OffsetDateTime openedAt,OffsetDateTime closedAt,UserSummary openedBy,UserSummary closedBy){}
 public record PosOperationalContext(UserSummary pumpAttendant,String operationalCode,UUID operationalDayId,UUID assignmentId,UUID organizationId,UUID stationId,String stationName,UUID pumpId,String pumpName,UUID dispensingPointId,UUID fuelMeterId,String fuelMeterName,UUID tankId,String tankName,UUID productId,String productName,BigDecimal openingIndex,BigDecimal cashUnitPrice){}
}
