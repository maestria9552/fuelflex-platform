package com.fuelflex.platform.supplier.service;
import java.util.*;
import com.fuelflex.platform.supplier.dto.SupplierDtos.*;
public interface SupplierService {
    SupplierResponse create(SupplierRequest request);
    List<SupplierResponse> findAll();
    List<SupplierCatalogResponse> findCatalog();
    SupplierResponse find(UUID id);
    SupplierResponse update(UUID id, SupplierRequest request);
    OrganizationSupplierResponse createPartnership(OrganizationSupplierRequest request);
    List<OrganizationSupplierResponse> findPartnerships();
    OrganizationSupplierResponse updatePartnership(UUID id, OrganizationSupplierRequest request);
    List<OrganizationSupplierResponse> findSelectable();
    MembershipResponse addMembership(MembershipRequest request);
    void endMembership(UUID id);
}
