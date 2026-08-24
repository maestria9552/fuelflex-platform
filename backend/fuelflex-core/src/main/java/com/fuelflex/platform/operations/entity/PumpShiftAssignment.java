package com.fuelflex.platform.operations.entity;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter; import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="pump_shift_assignments") @Getter @Setter @NoArgsConstructor
public class PumpShiftAssignment {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="operational_day_id",nullable=false) private OperationalDay operationalDay;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="pump_attendant_id",nullable=false) private User pumpAttendant;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="fuel_meter_id",nullable=false) private FuelMeter fuelMeter;
 @Column(name="opening_index",nullable=false,precision=19,scale=3) private BigDecimal openingIndex;
 @Column(name="closing_index",precision=19,scale=3) private BigDecimal closingIndex;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private OperationalStatus status;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="opened_by",nullable=false) private User openedBy;
 @Column(name="opened_at",nullable=false) private OffsetDateTime openedAt;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="closed_by") private User closedBy;
 @Column(name="closed_at") private OffsetDateTime closedAt;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
 @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @Version @Column(nullable=false) private long version;
 @Transient public BigDecimal getMeteredVolume(){return closingIndex==null?null:closingIndex.subtract(openingIndex);}
 @PrePersist void create(){var now=OffsetDateTime.now();createdAt=now;updatedAt=now;} @PreUpdate void update(){updatedAt=OffsetDateTime.now();}
}
