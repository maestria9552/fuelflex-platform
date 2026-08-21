package com.fuelflex.platform.notification.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fuelflex.platform.notification.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    @Query(value = "select notification from Notification notification where notification.recipient.id = :recipientId and notification.organization.id = :organizationId and (notification.read = false or notification.requiresAction = true)",
            countQuery = "select count(notification) from Notification notification where notification.recipient.id = :recipientId and notification.organization.id = :organizationId and (notification.read = false or notification.requiresAction = true)")
    Page<Notification> findRequiringAttention(
            @Param("recipientId") UUID recipientId,
            @Param("organizationId") UUID organizationId,
            Pageable pageable
    );

    long countByRecipientIdAndOrganizationIdAndReadFalse(UUID recipientId, UUID organizationId);

    long countByRecipientIdAndOrganizationIdAndRequiresActionTrue(UUID recipientId, UUID organizationId);

    @Query("select count(notification) from Notification notification where notification.recipient.id = :recipientId and notification.organization.id = :organizationId and (notification.read = false or notification.requiresAction = true)")
    long countRequiringAttention(@Param("recipientId") UUID recipientId, @Param("organizationId") UUID organizationId);

    @Query("select count(notification) from Notification notification where notification.recipient.id = :recipientId and notification.organization.id = :organizationId and notification.read = false and notification.eventType <> 'ORDER_SUBMITTED'")
    long countUnreadExcludingOrderSubmitted(@Param("recipientId") UUID recipientId, @Param("organizationId") UUID organizationId);

    boolean existsByRecipientIdAndEventTypeAndResourceId(UUID recipientId, String eventType, UUID resourceId);

    List<Notification> findByOrganizationIdAndResourceTypeAndResourceIdAndRequiresActionTrue(
            UUID organizationId, String resourceType, UUID resourceId);

    Optional<Notification> findByIdAndRecipientIdAndOrganizationId(
            UUID id,
            UUID recipientId,
            UUID organizationId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification notification
               set notification.read = true,
                   notification.readAt = :readAt
             where notification.recipient.id = :recipientId
               and notification.organization.id = :organizationId
               and notification.read = false
            """)
    int markAllAsRead(
            @Param("recipientId") UUID recipientId,
            @Param("organizationId") UUID organizationId,
            @Param("readAt") OffsetDateTime readAt
    );
}
