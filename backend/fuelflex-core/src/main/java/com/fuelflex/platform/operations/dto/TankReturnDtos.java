package com.fuelflex.platform.operations.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.validation.constraints.*;
public final class TankReturnDtos { private TankReturnDtos() {}
    public record CreateRequest(@NotNull UUID tankId,
            @NotNull @DecimalMin(value="0.000", inclusive=false) @Digits(integer=16, fraction=3) BigDecimal quantity,
            @Size(max=1000) String reason, @NotNull OffsetDateTime occurredAt) {}
    public record Response(UUID id, UUID organizationId, UUID operationalDayId, UUID shiftAssignmentId,
            UUID tankId, String tankName, UUID pumpAttendantId, String pumpAttendantName,
            UUID fuelMeterId, String fuelMeterName, UUID pumpId, String pumpName,
            UUID productId, String productName, BigDecimal quantity, String reason,
            OffsetDateTime occurredAt, UUID createdBy, OffsetDateTime createdAt) {}
}
