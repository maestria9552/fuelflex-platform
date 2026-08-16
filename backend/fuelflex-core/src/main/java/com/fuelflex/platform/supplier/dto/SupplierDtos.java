package com.fuelflex.platform.supplier.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class SupplierDtos {
    private SupplierDtos() {}
    public record SupplierRequest(@NotBlank @Size(max=180) String legalName, @NotBlank @Size(max=180) String displayName, @Size(max=180) String email, @Size(max=30) String phone, @Size(max=500) String address, Boolean active) {}
    public record SupplierResponse(UUID id, String legalName, String displayName, String email, String phone, String address, boolean active, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
    public record SupplierCatalogResponse(UUID supplierId, String displayName, String legalName, boolean active) {}
    public record OrganizationSupplierRequest(UUID supplierId, @Size(max=100) String internalCode, Boolean active) {}
    public record OrganizationSupplierResponse(UUID id, UUID organizationId, UUID supplierId, String displayName, String internalCode, boolean active, boolean integrated, OffsetDateTime partnershipStartedAt, OffsetDateTime partnershipEndedAt) {}
    public record MembershipRequest(UUID supplierId, UUID userId) {}
    public record MembershipResponse(UUID id, UUID supplierId, UUID userId, boolean active, OffsetDateTime createdAt, OffsetDateTime endedAt) {}
}
