package com.fuelflex.platform.operations.blocktwo;
import static org.assertj.core.api.Assertions.assertThat; import java.nio.file.*; import org.junit.jupiter.api.Test;
class BlockTwoMigrationContractTest{
 @Test void v12CompletesBlockWithoutChangingAppliedMigrations()throws Exception{String sql=Files.readString(Path.of("src/main/resources/db/migration/V12__complete_operational_block_two.sql"));assertThat(sql).contains("CREATE TABLE credit_customers","CREATE TABLE daily_expenses","CREATE TABLE tank_gauge_readings","CREATE TABLE shift_reconciliations","CREATE TABLE operational_day_summaries","movement_type","REVERSAL","total_amount > 0","total_amount = ROUND(quantity * unit_price, 3)","WHERE NOT EXISTS").doesNotContain("DELETE FROM role_permissions");}
 @Test void v10AndV11RemainSeparatedFromBlockTwo()throws Exception{assertThat(Files.readString(Path.of("src/main/resources/db/migration/V10__create_operational_days_and_shift_assignments.sql"))).doesNotContain("credit_customers","daily_expenses");assertThat(Files.readString(Path.of("src/main/resources/db/migration/V11__create_pos_fuel_sales.sql"))).doesNotContain("sale_type","REVERSAL");}
 @Test void v23AlignsDatabaseAccountingConstraintWithValidatedInternalFormula()throws Exception{String sql=Files.readString(Path.of("src/main/resources/db/migration/V23__include_internal_consumption_in_shift_accounting.sql"));assertThat(sql).contains("DROP CONSTRAINT ck_shift_reconciliation_accounted","total_sold_volume + tank_return_volume + internal_consumption_volume");}

 @Test void v24AlignsDaySnapshotConstraintWithValidatedInternalFormula()throws Exception{String sql=Files.readString(Path.of("src/main/resources/db/migration/V24__include_internal_consumption_in_day_summary.sql"));assertThat(sql).contains("DROP CONSTRAINT ck_day_summary_accounted","sold_volume + tank_return_volume + internal_consumption_volume");}

}
