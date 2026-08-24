package com.fuelflex.platform.operations.entity;
import java.time.OffsetDateTime; import java.util.UUID; import com.fuelflex.platform.user.entity.User; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="operational_history") @Getter @Setter @NoArgsConstructor
public class OperationalHistory {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="resource_type",nullable=false,length=40) private String resourceType; @Column(name="resource_id",nullable=false) private UUID resourceId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=50) private OperationalAction action;
 @Column(name="old_status",length=20) private String oldStatus; @Column(name="new_status",nullable=false,length=20) private String newStatus;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="performed_by",nullable=false) private User performedBy;
 @Column(name="performed_at",nullable=false) private OffsetDateTime performedAt; @Column(length=1000) private String details;
}
