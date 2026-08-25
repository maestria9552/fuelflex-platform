package com.fuelflex.platform.sale.entity;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID; import com.fuelflex.platform.product.entity.Product; import com.fuelflex.platform.station.entity.Station; import com.fuelflex.platform.tank.entity.Tank; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="sale_stock_movements",uniqueConstraints=@UniqueConstraint(name="uk_sale_stock_movement_sale_type",columnNames={"sale_id","movement_type"})) @Getter @Setter @NoArgsConstructor
public class SaleStockMovement {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="sale_id",nullable=false) private FuelSale sale;
 @Enumerated(EnumType.STRING) @Column(name="movement_type",nullable=false,length=20) private StockMovementType movementType=StockMovementType.OUTBOUND;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="station_id",nullable=false) private Station station; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="tank_id",nullable=false) private Tank tank;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id",nullable=false) private Product product; @Column(nullable=false,precision=19,scale=3) private BigDecimal quantity;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @PrePersist void create(){if(createdAt==null)createdAt=OffsetDateTime.now();if(movementType==null)movementType=StockMovementType.OUTBOUND;}
}
