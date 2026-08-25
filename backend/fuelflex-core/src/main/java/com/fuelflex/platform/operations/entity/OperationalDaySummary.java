package com.fuelflex.platform.operations.entity;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID; import com.fuelflex.platform.user.entity.User; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="operational_day_summaries",uniqueConstraints=@UniqueConstraint(name="uk_operational_day_summary_day",columnNames="operational_day_id")) @Getter @Setter @NoArgsConstructor
public class OperationalDaySummary {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="operational_day_id",nullable=false) private OperationalDay operationalDay;
 @Column(name="cash_volume",nullable=false,precision=19,scale=3) private BigDecimal cashVolume; @Column(name="cash_amount",nullable=false,precision=19,scale=3) private BigDecimal cashAmount;
 @Column(name="credit_volume",nullable=false,precision=19,scale=3) private BigDecimal creditVolume; @Column(name="credit_amount",nullable=false,precision=19,scale=3) private BigDecimal creditAmount;
 @Column(name="metered_volume",nullable=false,precision=19,scale=3) private BigDecimal meteredVolume; @Column(name="sold_volume",nullable=false,precision=19,scale=3) private BigDecimal soldVolume;
 @Column(name="volume_variance",nullable=false,precision=19,scale=3) private BigDecimal volumeVariance; @Column(name="expense_amount",nullable=false,precision=19,scale=3) private BigDecimal expenseAmount;
 @Column(name="theoretical_stock",nullable=false,precision=19,scale=3) private BigDecimal theoreticalStock; @Column(name="physical_stock",nullable=false,precision=19,scale=3) private BigDecimal physicalStock;
 @Column(name="stock_variance",nullable=false,precision=19,scale=3) private BigDecimal stockVariance; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="closed_by",nullable=false) private User closedBy;
 @Column(name="closed_at",nullable=false) private OffsetDateTime closedAt; @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @PrePersist void create(){if(createdAt==null)createdAt=OffsetDateTime.now();}
}
