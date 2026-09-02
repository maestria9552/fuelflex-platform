package com.fuelflex.platform.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ManagerDashboardDtos {
    private ManagerDashboardDtos() {}

    public record PeriodTotals(
            BigDecimal revenue,
            BigDecimal cash,
            BigDecimal credit,
            BigDecimal disbursedExpenses,
            BigDecimal internalVolume,
            BigDecimal internalAmount,
            BigDecimal cashAfterExpenses
    ) {}

    public record DailySales(LocalDate date, BigDecimal cash, BigDecimal credit) {}

    public record TankStock(UUID tankId, String tankName, BigDecimal stockQuantity,
                            BigDecimal capacity, BigDecimal fillPercentage) {}

    public record ProductStock(UUID productId, String productName, BigDecimal stockQuantity,
                               BigDecimal totalCapacity, BigDecimal fillPercentage,
                               List<TankStock> tanks) {}

    public record Response(
            UUID stationId,
            String stationName,
            String currency,
            LocalDate currentPeriodStart,
            LocalDate currentPeriodEnd,
            LocalDate previousPeriodStart,
            LocalDate previousPeriodEnd,
            PeriodTotals current,
            PeriodTotals previous,
            List<DailySales> dailySales,
            List<ProductStock> products
    ) {}
}
