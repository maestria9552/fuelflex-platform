package com.fuelflex.platform.supplier.repository;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.fuelflex.platform.supplier.entity.SupplierUserMembership;
public interface SupplierUserMembershipRepository extends JpaRepository<SupplierUserMembership, UUID> {
    boolean existsBySupplierIdAndUserIdAndActiveTrue(UUID supplierId, UUID userId);
    @Query("select count(m)>0 from SupplierUserMembership m join m.user u join u.roles r where m.supplier.id=:supplier and m.active=true and u.enabled=true and r.code='SUPPLIER_USER' and r.active=true")
    boolean hasActivePortalUser(@Param("supplier") UUID supplierId);
}
