package com.fuelflex.platform.operations.entity;
import java.time.*; import java.util.UUID;
import com.fuelflex.platform.organization.entity.Organization; import com.fuelflex.platform.station.entity.Station; import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="operational_days") @Getter @Setter @NoArgsConstructor
public class OperationalDay {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="station_id",nullable=false) private Station station;
 @Column(name="business_date",nullable=false) private LocalDate businessDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private OperationalStatus status;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="opened_by",nullable=false) private User openedBy;
 @Column(name="opened_at",nullable=false) private OffsetDateTime openedAt;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
 @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @Version @Column(nullable=false) private long version;
 @PrePersist void create(){var now=OffsetDateTime.now();createdAt=now;updatedAt=now;} @PreUpdate void update(){updatedAt=OffsetDateTime.now();}
}
