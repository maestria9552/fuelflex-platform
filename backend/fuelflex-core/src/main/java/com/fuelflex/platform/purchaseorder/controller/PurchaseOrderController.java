package com.fuelflex.platform.purchaseorder.controller;
import java.util.*; import org.springframework.data.domain.*;
import org.springframework.http.*; import org.springframework.web.multipart.MultipartFile; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
import com.fuelflex.platform.purchaseorder.dto.PurchaseOrderDtos.*; import com.fuelflex.platform.purchaseorder.service.PurchaseOrderService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class PurchaseOrderController { private final PurchaseOrderService service;
 @PostMapping("/manager/orders") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('order:create')") public Response create(@Valid @RequestBody CreateRequest r){return service.create(r);}
 @GetMapping("/manager/orders") @PreAuthorize("hasAuthority('order:view')") public Page<Response> managerOrders(Pageable p){return service.managerOrders(p);}
 @GetMapping("/manager/orders/{id}") @PreAuthorize("hasAuthority('order:view')") public Response managerOrder(@PathVariable UUID id){return service.managerOrder(id);}
 @PutMapping("/manager/orders/{id}") @PreAuthorize("hasAuthority('order:update')") public Response update(@PathVariable UUID id,@Valid @RequestBody UpdateRequest r){return service.update(id,r);}
 @PostMapping("/manager/orders/{id}/submit") @PreAuthorize("hasAuthority('order:submit')") public Response submit(@PathVariable UUID id){return service.submit(id);}
 @GetMapping("/manager/orders/{id}/history") @PreAuthorize("hasAuthority('order:view')") public List<HistoryResponse> managerHistory(@PathVariable UUID id){return service.managerHistory(id);}
 @PostMapping(value="/manager/orders/{id}/attachments", consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasAuthority('order:update')") public AttachmentResponse uploadAttachment(@PathVariable UUID id,@RequestPart("displayName") String displayName,@RequestPart("file") MultipartFile file){return service.uploadAttachment(id,displayName,file);}
 @DeleteMapping("/manager/orders/{id}/attachments/{attachmentId}") @PreAuthorize("hasAuthority('order:update')") public void deleteAttachment(@PathVariable UUID id,@PathVariable UUID attachmentId){service.deleteAttachment(id,attachmentId);}
 @GetMapping("/supervisor/orders/pending-count") @PreAuthorize("hasAuthority('order:view')") public Map<String, Long> supervisorPendingCount(){return Map.of("count", service.supervisorPendingCount());}
 @GetMapping("/supervisor/orders/pending") @PreAuthorize("hasAuthority('order:view')") public Page<Response> supervisorPendingOrders(Pageable p){return service.supervisorPendingOrders(p);}
 @GetMapping("/supervisor/orders") @PreAuthorize("hasAuthority('order:view')") public Page<Response> supervisorOrders(Pageable p){return service.supervisorOrders(p);}
 @GetMapping("/orders/{id}/attachments") @PreAuthorize("hasAuthority('order:view')") public List<AttachmentResponse> attachments(@PathVariable UUID id){return service.attachments(id);}
 @GetMapping("/orders/{id}/attachments/{attachmentId}/download") @PreAuthorize("hasAuthority('order:view')") public ResponseEntity<byte[]> downloadAttachment(@PathVariable UUID id,@PathVariable UUID attachmentId){var file=service.downloadAttachment(id,attachmentId);return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType())).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+file.filename().replace("\"","_").replace("\\","_")+"\"").body(file.bytes());}
 @GetMapping("/supervisor/orders/{id}") @PreAuthorize("hasAuthority('order:view')") public Response supervisorOrder(@PathVariable UUID id){return service.supervisorOrder(id);}
 @PostMapping("/supervisor/orders/{id}/approve") @PreAuthorize("hasAuthority('order:supervisor_approve')") public Response supervisorApprove(@PathVariable UUID id,@RequestBody(required=false) DecisionRequest r){return service.supervisorApprove(id,r);}
 @PostMapping("/supervisor/orders/{id}/reject") @PreAuthorize("hasAuthority('order:supervisor_reject')") public Response supervisorReject(@PathVariable UUID id,@RequestBody(required=false) DecisionRequest r){return service.supervisorReject(id,r);}
 @GetMapping("/supervisor/orders/{id}/history") @PreAuthorize("hasAuthority('order:view')") public List<HistoryResponse> supervisorHistory(@PathVariable UUID id){return service.supervisorHistory(id);}
 @GetMapping("/supplier/orders") @PreAuthorize("hasAuthority('order:view')") public Page<Response> supplierOrders(Pageable p){return service.supplierOrders(p);}
 @GetMapping("/supplier/orders/{id}") @PreAuthorize("hasAuthority('order:view')") public Response supplierOrder(@PathVariable UUID id){return service.supplierOrder(id);}
 @PostMapping("/supplier/orders/{id}/approve") @PreAuthorize("hasAuthority('order:supplier_approve')") public Response supplierApprove(@PathVariable UUID id,@RequestBody(required=false) DecisionRequest r){return service.supplierApprove(id,r);}
 @PostMapping("/supplier/orders/{id}/reject") @PreAuthorize("hasAuthority('order:supplier_reject')") public Response supplierReject(@PathVariable UUID id,@RequestBody(required=false) DecisionRequest r){return service.supplierReject(id,r);}
 @GetMapping("/supplier/orders/{id}/history") @PreAuthorize("hasAuthority('order:view')") public List<HistoryResponse> supplierHistory(@PathVariable UUID id){return service.supplierHistory(id);}
}
