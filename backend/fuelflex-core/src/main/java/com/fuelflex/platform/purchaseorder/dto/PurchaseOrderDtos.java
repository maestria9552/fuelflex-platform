package com.fuelflex.platform.purchaseorder.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.fuelflex.platform.purchaseorder.model.PurchaseOrderAction;
import com.fuelflex.platform.purchaseorder.model.PurchaseOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public final class PurchaseOrderDtos {
    private PurchaseOrderDtos() {}
    public record ItemRequest(@NotNull UUID productId, @NotNull @DecimalMin(value="0.0", inclusive=false) @Digits(integer=16, fraction=3) BigDecimal quantity) {}
    public record CreateRequest(@NotNull UUID stationId, UUID organizationSupplierId, @NotNull @Valid List<ItemRequest> items) {}
    public record UpdateRequest(@NotNull UUID stationId, UUID organizationSupplierId, @NotNull @Valid List<ItemRequest> items) {}
    public record DecisionRequest(@Size(max=1000) String comment) {}
    public record StationSummary(UUID id, String code, String name) {}
    public record OrganizationSummary(UUID id, String name, String tradeName, String registrationNumber, String nationalId, String taxNumber, String logoUrl, String address, String city, String province, String country) {}
    public record StationDocumentSummary(UUID id, String code, String name, String address, String city, String province, String country) {}
    public record SupplierSummary(UUID organizationSupplierId, UUID supplierId, String displayName, boolean integrated) {}
    public record UserSummary(UUID id, String firstName, String lastName) {}
    public record AttachmentResponse(UUID id, String displayName, String originalFilename, String contentType, long fileSize, UserSummary uploadedBy, OffsetDateTime uploadedAt) {}
    public record ItemResponse(UUID id, UUID productId, String productCode, String productName, String unit, BigDecimal quantity) {}
    public record Response(UUID id, String orderNumber, OrganizationSummary organization, StationDocumentSummary station, SupplierSummary supplier,
            PurchaseOrderStatus status, List<ItemResponse> items, UserSummary createdBy, OffsetDateTime createdAt,
            OffsetDateTime updatedAt, OffsetDateTime submittedAt, UserSummary supervisorReviewedBy,
            OffsetDateTime supervisorReviewedAt, UserSummary supplierReviewedBy, OffsetDateTime supplierReviewedAt, long version, List<AttachmentResponse> attachments) {}
    public record HistoryResponse(UUID id, PurchaseOrderAction action, PurchaseOrderStatus fromStatus,
            PurchaseOrderStatus toStatus, UserSummary performedBy, OffsetDateTime performedAt, String comment) {}
}
