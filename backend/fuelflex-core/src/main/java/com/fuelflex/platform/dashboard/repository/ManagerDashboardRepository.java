package com.fuelflex.platform.dashboard.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.fuelflex.platform.operations.entity.OperationalDaySummary;

public interface ManagerDashboardRepository extends Repository<OperationalDaySummary, UUID> {
    interface DailyProjection {
        LocalDate getBusinessDate();
        BigDecimal getCash();
        BigDecimal getCredit();
        BigDecimal getExpenses();
        BigDecimal getInternalVolume();
        BigDecimal getInternalAmount();
    }

    @Query(value = """
            select day.business_date as "businessDate",
                   coalesce(sum(summary.cash_amount), 0) as "cash",
                   coalesce(sum(summary.credit_amount), 0) as "credit",
                   coalesce(sum(summary.disbursed_expense_amount), 0) as "expenses",
                   coalesce(sum(summary.internal_consumption_volume), 0) as "internalVolume",
                   coalesce(sum(summary.internal_consumption_amount), 0) as "internalAmount"
              from operational_day_summaries summary
              join operational_days day on day.id = summary.operational_day_id
             where day.organization_id = :organizationId
               and day.station_id = :stationId
               and day.status = 'CLOSED'
               and day.business_date between :fromDate and :toDate
             group by day.business_date
             order by day.business_date
            """, nativeQuery = true)
    List<DailyProjection> findDailySnapshots(
            @Param("organizationId") UUID organizationId,
            @Param("stationId") UUID stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
