package com.fuelflex.platform.operations.entity;
import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
import com.fuelflex.platform.organization.entity.Organization; import com.fuelflex.platform.station.entity.Station; import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="daily_expenses") @Getter @Setter @NoArgsConstructor
public class DailyExpense {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="station_id",nullable=false) private Station station;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="operational_day_id",nullable=false) private OperationalDay operationalDay;
 @Column(nullable=false,length=180) private String label; @Column(nullable=false,precision=19,scale=3) private BigDecimal amount;
 @Column(length=100) private String reference; @Column(length=1000) private String comment;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="created_by",nullable=false) private User createdBy;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
 @PrePersist void create(){if(createdAt==null)createdAt=OffsetDateTime.now();}
}
