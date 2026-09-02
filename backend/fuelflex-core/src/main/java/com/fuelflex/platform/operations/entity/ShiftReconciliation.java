package com.fuelflex.platform.operations.entity;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID; import jakarta.persistence.*; import lombok.*;
import com.fuelflex.platform.product.entity.Product;import com.fuelflex.platform.tank.entity.Tank;
@Entity @Table(name="shift_reconciliations",uniqueConstraints=@UniqueConstraint(name="uk_shift_reconciliation_assignment",columnNames="shift_assignment_id")) @Getter @Setter @NoArgsConstructor
public class ShiftReconciliation {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="shift_assignment_id",nullable=false) private PumpShiftAssignment shiftAssignment;
 @Column(name="opening_index",nullable=false,precision=19,scale=3) private BigDecimal openingIndex; @Column(name="closing_index",nullable=false,precision=19,scale=3) private BigDecimal closingIndex;
 @Column(name="metered_volume",nullable=false,precision=19,scale=3) private BigDecimal meteredVolume; @Column(name="cash_volume",nullable=false,precision=19,scale=3) private BigDecimal cashVolume;
 @Column(name="credit_volume",nullable=false,precision=19,scale=3) private BigDecimal creditVolume; @Column(name="total_sold_volume",nullable=false,precision=19,scale=3) private BigDecimal totalSoldVolume;
 @Column(name="tank_return_volume",nullable=false,precision=19,scale=3) private BigDecimal tankReturnVolume; @Column(name="accounted_volume",nullable=false,precision=19,scale=3) private BigDecimal accountedVolume;
 @Column(name="internal_consumption_volume",nullable=false,precision=19,scale=3)private BigDecimal internalConsumptionVolume;@Column(name="internal_consumption_amount",nullable=false,precision=19,scale=3)private BigDecimal internalConsumptionAmount;
 @Column(name="internal_unit_price",nullable=false,precision=19,scale=3)private BigDecimal internalUnitPrice;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="source_tank_id",nullable=false)private Tank sourceTank;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="product_id",nullable=false)private Product product;
 @Column(name="cash_unit_price",nullable=false,precision=19,scale=3)private BigDecimal cashUnitPrice;@Column(name="credit_unit_price",nullable=false,precision=19,scale=3)private BigDecimal creditUnitPrice;
 @Column(name="cash_amount",nullable=false,precision=19,scale=3)private BigDecimal cashAmount;@Column(name="credit_amount",nullable=false,precision=19,scale=3)private BigDecimal creditAmount;@Column(name="turnover",nullable=false,precision=19,scale=3)private BigDecimal turnover;
 @Column(name="volume_variance",nullable=false,precision=19,scale=3) private BigDecimal volumeVariance; @Column(name="calculated_at",nullable=false) private OffsetDateTime calculatedAt;
}
