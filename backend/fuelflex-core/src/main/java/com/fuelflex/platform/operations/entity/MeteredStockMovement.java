package com.fuelflex.platform.operations.entity;
import java.math.BigDecimal;import java.time.OffsetDateTime;import java.util.UUID;
import com.fuelflex.platform.product.entity.Product;import com.fuelflex.platform.station.entity.Station;import com.fuelflex.platform.tank.entity.Tank;import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*;import lombok.*;
@Entity @Table(name="metered_stock_movements",uniqueConstraints=@UniqueConstraint(name="uk_metered_movement_shift",columnNames="shift_assignment_id")) @Getter @Setter @NoArgsConstructor
public class MeteredStockMovement{
 @Id @GeneratedValue(strategy=GenerationType.UUID)private UUID id;
 @OneToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="shift_assignment_id",nullable=false)private PumpShiftAssignment shiftAssignment;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="station_id",nullable=false)private Station station;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="tank_id",nullable=false)private Tank tank;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="product_id",nullable=false)private Product product;
 @Column(nullable=false,precision=19,scale=3)private BigDecimal quantity;
 @Column(name="movement_type",nullable=false,length=30)private String movementType="METERED_OUTBOUND";
 @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="created_by",nullable=false)private User createdBy;
 @Column(name="created_at",nullable=false,updatable=false)private OffsetDateTime createdAt;
 @PrePersist void create(){if(createdAt==null)createdAt=OffsetDateTime.now();}
}
