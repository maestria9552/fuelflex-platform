package com.fuelflex.platform.supplier.controller;
import java.util.List; import java.util.UUID;
import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
import com.fuelflex.platform.supplier.dto.SupplierDtos.*; import com.fuelflex.platform.supplier.service.SupplierService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class SupplierController { private final SupplierService service;
 @PostMapping("/suppliers") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('SUPER_ADMIN')") public SupplierResponse create(@Valid @RequestBody SupplierRequest r){return service.create(r);}
 @GetMapping("/suppliers") @PreAuthorize("hasAuthority('SUPER_ADMIN')") public List<SupplierResponse> all(){return service.findAll();}
 @GetMapping("/suppliers/catalog") @PreAuthorize("hasAuthority('supplier:partnership_view')") public List<SupplierCatalogResponse> catalog(){return service.findCatalog();}
 @GetMapping("/suppliers/{id}") @PreAuthorize("hasAuthority('SUPER_ADMIN')") public SupplierResponse one(@PathVariable UUID id){return service.find(id);}
 @PutMapping("/suppliers/{id}") @PreAuthorize("hasAuthority('SUPER_ADMIN')") public SupplierResponse update(@PathVariable UUID id,@Valid @RequestBody SupplierRequest r){return service.update(id,r);}
 @PostMapping("/organization-suppliers") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('supplier:partnership_create')") public OrganizationSupplierResponse createPartnership(@Valid @RequestBody OrganizationSupplierRequest r){return service.createPartnership(r);}
 @GetMapping("/organization-suppliers") @PreAuthorize("hasAuthority('supplier:partnership_view')") public List<OrganizationSupplierResponse> partnerships(){return service.findPartnerships();}
 @PutMapping("/organization-suppliers/{id}") @PreAuthorize("hasAuthority('supplier:partnership_update')") public OrganizationSupplierResponse updatePartnership(@PathVariable UUID id,@Valid @RequestBody OrganizationSupplierRequest r){return service.updatePartnership(id,r);}
 @GetMapping("/manager/suppliers") @PreAuthorize("hasAuthority('supplier:view')") public List<OrganizationSupplierResponse> selectable(){return service.findSelectable();}
 @PostMapping("/supplier-memberships") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('SUPER_ADMIN')") public MembershipResponse addMembership(@RequestBody MembershipRequest r){return service.addMembership(r);}
 @DeleteMapping("/supplier-memberships/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAuthority('SUPER_ADMIN')") public void endMembership(@PathVariable UUID id){service.endMembership(id);}
}
