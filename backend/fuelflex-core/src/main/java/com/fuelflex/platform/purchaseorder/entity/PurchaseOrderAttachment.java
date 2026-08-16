package com.fuelflex.platform.purchaseorder.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_order_attachments")
@Getter @Setter @NoArgsConstructor
public class PurchaseOrderAttachment {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "purchase_order_id", nullable = false) private PurchaseOrder purchaseOrder;
    @Column(name = "display_name", nullable = false, length = 180) private String displayName;
    @Column(name = "original_filename", nullable = false, length = 255) private String originalFilename;
    @Column(name = "storage_key", nullable = false, unique = true, length = 500) private String storageKey;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "uploaded_by", nullable = false) private User uploadedBy;
    @Column(name = "uploaded_at", nullable = false) private OffsetDateTime uploadedAt;
    @PrePersist void onCreate() { if (uploadedAt == null) uploadedAt = OffsetDateTime.now(); }
}
