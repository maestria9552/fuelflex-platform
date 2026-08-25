package com.fuelflex.platform.creditcustomer.entity;
import java.time.OffsetDateTime; import java.util.UUID;
import com.fuelflex.platform.organization.entity.Organization;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="credit_customers",uniqueConstraints={@UniqueConstraint(name="uk_credit_customer_org_code",columnNames={"organization_id","code"}),@UniqueConstraint(name="uk_credit_customer_org_name",columnNames={"organization_id","name"})})
@Getter @Setter @NoArgsConstructor
public class CreditCustomer {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
 @Column(nullable=false,length=50) private String code; @Column(nullable=false,length=180) private String name;
 @Column(length=30) private String phone; @Column(length=180) private String email; @Column(nullable=false) private boolean active=true;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt; @Version private long version;
 @PrePersist void create(){var now=OffsetDateTime.now();createdAt=now;updatedAt=now;normalize();} @PreUpdate void update(){updatedAt=OffsetDateTime.now();normalize();}
 private void normalize(){if(code!=null)code=code.trim().toUpperCase().replaceAll("[^A-Z0-9]+","_").replaceAll("^_+|_+$","");if(name!=null)name=name.trim().replaceAll("\\s+"," ");if(phone!=null)phone=phone.trim();if(email!=null)email=email.trim().toLowerCase();}
}
