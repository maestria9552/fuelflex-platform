package com.fuelflex.platform.purchaseorder.service;
import java.util.List; import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import com.fuelflex.platform.purchaseorder.dto.PurchaseOrderDtos.*;
public interface PurchaseOrderService {
 Response create(CreateRequest request); Response update(UUID id, UpdateRequest request); Response submit(UUID id);
 Page<Response> managerOrders(Pageable pageable); Response managerOrder(UUID id); List<HistoryResponse> managerHistory(UUID id);
 Page<Response> supervisorOrders(Pageable pageable); Page<Response> supervisorPendingOrders(Pageable pageable); long supervisorPendingCount(); Response supervisorOrder(UUID id); List<HistoryResponse> supervisorHistory(UUID id);
 Response supervisorApprove(UUID id, DecisionRequest request); Response supervisorReject(UUID id, DecisionRequest request);
 Page<Response> supplierOrders(Pageable pageable); Response supplierOrder(UUID id); List<HistoryResponse> supplierHistory(UUID id);
 Response supplierApprove(UUID id, DecisionRequest request); Response supplierReject(UUID id, DecisionRequest request);
 AttachmentResponse uploadAttachment(UUID orderId, String displayName, MultipartFile file); List<AttachmentResponse> attachments(UUID orderId); AttachmentDownload downloadAttachment(UUID orderId, UUID attachmentId); void deleteAttachment(UUID orderId, UUID attachmentId); record AttachmentDownload(byte[] bytes, String contentType, String filename) {}
}
