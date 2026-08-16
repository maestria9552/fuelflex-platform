package com.fuelflex.platform.supplier.service.impl;

import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.common.exception.*;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.supplier.dto.SupplierDtos.*;
import com.fuelflex.platform.supplier.entity.*;
import com.fuelflex.platform.supplier.repository.*;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor @Transactional
public class SupplierServiceImpl implements com.fuelflex.platform.supplier.service.SupplierService {
    private final SupplierRepository suppliers;
    private final OrganizationSupplierRepository partnerships;
    private final SupplierUserMembershipRepository memberships;
    private final UserRepository users;
    private final RoleRepository roles;
    private final AuthorizationService authorization;

    public SupplierResponse create(SupplierRequest r) { Supplier s=new Supplier(); apply(s,r); return response(suppliers.save(s)); }
    @Transactional(readOnly=true) public List<SupplierResponse> findAll() { return suppliers.findAll().stream().map(this::response).toList(); }
    @Transactional(readOnly=true) public List<SupplierCatalogResponse> findCatalog() { return suppliers.findByActiveTrueOrderByDisplayNameAsc().stream().map(s -> new SupplierCatalogResponse(s.getId(), s.getDisplayName(), s.getLegalName(), s.isActive())).toList(); }
    @Transactional(readOnly=true) public SupplierResponse find(UUID id) { return response(suppliers.findById(id).orElseThrow(()->new ResourceNotFoundException("Fournisseur introuvable."))); }
    public SupplierResponse update(UUID id,SupplierRequest r) { Supplier s=suppliers.findById(id).orElseThrow(()->new ResourceNotFoundException("Fournisseur introuvable.")); apply(s,r); return response(suppliers.save(s)); }
    public OrganizationSupplierResponse createPartnership(OrganizationSupplierRequest r) { User u=current(); if(u.getOrganization()==null) throw new BusinessException("Organisation requise."); Supplier s=suppliers.findById(r.supplierId()).orElseThrow(()->new ResourceNotFoundException("Fournisseur introuvable.")); if(!s.isActive()) throw new BusinessException("Un fournisseur inactif ne peut pas être partenaire."); if(partnerships.existsByOrganizationIdAndSupplierId(u.getOrganization().getId(),s.getId())) throw new ConflictException("Ce partenariat existe déjà."); OrganizationSupplier p=new OrganizationSupplier(); p.setOrganization(u.getOrganization()); p.setSupplier(s); p.setInternalCode(r.internalCode()); p.setActive(r.active()==null||r.active()); return partnershipResponse(partnerships.save(p)); }
    @Transactional(readOnly=true) public List<OrganizationSupplierResponse> findPartnerships() { UUID org=orgId(); return partnerships.findByOrganizationIdOrderBySupplierDisplayNameAsc(org).stream().map(this::partnershipResponse).toList(); }
    public OrganizationSupplierResponse updatePartnership(UUID id,OrganizationSupplierRequest r) { OrganizationSupplier p=partnerships.findByIdAndOrganizationId(id,orgId()).orElseThrow(()->new ResourceNotFoundException("Partenariat introuvable.")); if(r.supplierId()!=null&&!p.getSupplier().getId().equals(r.supplierId())) throw new BusinessException("Le fournisseur d’un partenariat ne peut pas être remplacé."); if(r.internalCode()!=null) p.setInternalCode(r.internalCode()); if(r.active()!=null) p.setActive(r.active()); if(!p.isActive()) p.setPartnershipEndedAt(OffsetDateTime.now()); else if(p.getPartnershipEndedAt()!=null) p.setPartnershipEndedAt(null); return partnershipResponse(partnerships.save(p)); }
    @Transactional(readOnly=true) public List<OrganizationSupplierResponse> findSelectable() { return partnerships.findByOrganizationIdAndActiveTrueAndSupplierActiveTrueOrderBySupplierDisplayNameAsc(orgId()).stream().map(this::partnershipResponse).toList(); }
    public MembershipResponse addMembership(MembershipRequest r) { requireGlobalAdmin();  Supplier s=suppliers.findById(r.supplierId()).orElseThrow(()->new ResourceNotFoundException("Fournisseur introuvable.")); if(!s.isActive()) throw new BusinessException("Fournisseur inactif."); User u=users.findById(r.userId()).orElseThrow(()->new ResourceNotFoundException("Utilisateur introuvable.")); boolean supplierRole=u.getRoles().stream().anyMatch(x->x.isActive()&&x.getCode().equalsIgnoreCase("SUPPLIER_USER")); if(!supplierRole) throw new BusinessException("L’utilisateur doit avoir le rôle SUPPLIER_USER."); if(memberships.existsBySupplierIdAndUserIdAndActiveTrue(s.getId(),u.getId())) throw new ConflictException("Cette adhésion active existe déjà."); SupplierUserMembership m=new SupplierUserMembership(); m.setSupplier(s); m.setUser(u); return membershipResponse(memberships.save(m)); }
    public void endMembership(UUID id) { requireGlobalAdmin();  SupplierUserMembership m=memberships.findById(id).orElseThrow(()->new ResourceNotFoundException("Adhésion introuvable.")); m.setActive(false); m.setEndedAt(OffsetDateTime.now()); memberships.save(m); }
    private void requireGlobalAdmin(){ if(current().getRoles().stream().noneMatch(r -> r.isActive() && r.getCode().equalsIgnoreCase("SUPER_ADMIN"))) throw new ForbiddenException("Cette opération fournisseur est réservée à l’administration globale."); }
    private User current(){return authorization.getAuthenticatedUser();} private UUID orgId(){User u=current(); if(u.getOrganization()==null) throw new BusinessException("Organisation requise."); return u.getOrganization().getId();}
    private void apply(Supplier s,SupplierRequest r){s.setLegalName(r.legalName());s.setDisplayName(r.displayName());s.setEmail(r.email());s.setPhone(r.phone());s.setAddress(r.address());if(r.active()!=null)s.setActive(r.active());}
    private SupplierResponse response(Supplier s){return new SupplierResponse(s.getId(),s.getLegalName(),s.getDisplayName(),s.getEmail(),s.getPhone(),s.getAddress(),s.isActive(),s.getCreatedAt(),s.getUpdatedAt());}
    private OrganizationSupplierResponse partnershipResponse(OrganizationSupplier p){return new OrganizationSupplierResponse(p.getId(),p.getOrganization().getId(),p.getSupplier().getId(),p.getSupplier().getDisplayName(),p.getInternalCode(),p.isActive(),memberships.hasActivePortalUser(p.getSupplier().getId()),p.getPartnershipStartedAt(),p.getPartnershipEndedAt());}
    private MembershipResponse membershipResponse(SupplierUserMembership m){return new MembershipResponse(m.getId(),m.getSupplier().getId(),m.getUser().getId(),m.isActive(),m.getCreatedAt(),m.getEndedAt());}
}
