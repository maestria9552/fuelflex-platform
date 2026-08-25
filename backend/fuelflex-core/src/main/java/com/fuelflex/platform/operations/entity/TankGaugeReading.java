package com.fuelflex.platform.operations.entity;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
import com.fuelflex.platform.organization.entity.Organization; import com.fuelflex.platform.station.entity.Station; import com.fuelflex.platform.tank.entity.Tank; import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="tank_gauge_readings",uniqueConstraints=@UniqueConstraint(name="uk_tank_gauge_day_tank",columnNames={"operational_day_id","tank_id"})) @Getter @Setter @NoArgsConstructor
public class TankGaugeReading {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="station_id",nullable=false) private Station station;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="operational_day_id",nullable=false) private OperationalDay operationalDay;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="tank_id",nullable=false) private Tank tank;
 @Column(name="theoretical_stock",nullable=false,precision=19,scale=3) private BigDecimal theoreticalStock;
 @Column(name="physical_stock",nullable=false,precision=19,scale=3) private BigDecimal physicalStock;
 @Column(name="stock_variance",nullable=false,precision=19,scale=3) private BigDecimal stockVariance;
 @Column(length=1000) private String comment; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="recorded_by",nullable=false) private User recordedBy;
 @Column(name="recorded_at",nullable=false) private OffsetDateTime recordedAt; @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
 @PrePersist void create(){var now=OffsetDateTime.now();if(recordedAt==null)recordedAt=now;if(createdAt==null)createdAt=now;}
}
