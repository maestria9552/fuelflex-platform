package com.fuelflex.platform.supplier.repository;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.fuelflex.platform.supplier.entity.OrganizationSupplier;
public interface OrganizationSupplierRepository extends JpaRepository<OrganizationSupplier, UUID> {
    boolean existsByOrganizationIdAndSupplierId(UUID organizationId, UUID supplierId);
    Optional<OrganizationSupplier> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<OrganizationSupplier> findByOrganizationIdOrderBySupplierDisplayNameAsc(UUID organizationId);
    List<OrganizationSupplier> findByOrganizationIdAndActiveTrueAndSupplierActiveTrueOrderBySupplierDisplayNameAsc(UUID organizationId);
    @Query("select os from OrganizationSupplier os where os.organization.id=:org and os.active=true and os.supplier.active=true and exists (select m.id from SupplierUserMembership m join m.user u join u.roles r where m.supplier=os.supplier and m.active=true and u.enabled=true and r.code='SUPPLIER_USER' and r.active=true)")
    List<OrganizationSupplier> findIntegratedByOrganizationId(@Param("org") UUID organizationId);
}
