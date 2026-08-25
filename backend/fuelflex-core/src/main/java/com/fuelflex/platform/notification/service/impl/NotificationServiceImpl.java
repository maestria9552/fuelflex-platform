package com.fuelflex.platform.notification.service.impl;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.notification.dto.request.CreateNotificationCommand;
import com.fuelflex.platform.notification.dto.response.NotificationPageResponse;
import com.fuelflex.platform.notification.dto.response.NotificationResponse;
import com.fuelflex.platform.notification.dto.response.UnreadNotificationCountResponse;
import com.fuelflex.platform.notification.entity.Notification;
import com.fuelflex.platform.notification.entity.NotificationCategory;
import com.fuelflex.platform.notification.mapper.NotificationMapper;
import com.fuelflex.platform.notification.repository.NotificationRepository;
import com.fuelflex.platform.notification.service.NotificationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.organization.repository.OrganizationRepository;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final StationRepository stationRepository;
    private final AuthorizationService authorizationService;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponse create(CreateNotificationCommand command) {
        validateCreateCommand(command);

        Organization organization = organizationRepository
                .findById(command.getOrganizationId())
                .orElseThrow(() -> new BusinessException(
                        "Notification organization was not found."
                ));
        User recipient = getUser(command.getRecipientId(), "recipient");
        requireSameOrganization(recipient, organization, "recipient");

        Station station = command.getStationId() == null
                ? null
                : stationRepository
                        .findByIdAndOrganizationId(
                                command.getStationId(),
                                organization.getId()
                        )
                        .orElseThrow(() -> new BusinessException(
                                "Notification station was not found in the organization."
                        ));

        User actor = command.getActorId() == null
                ? null
                : getUser(command.getActorId(), "actor");
        if (actor != null) {
            requireSameOrganization(actor, organization, "actor");
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setOrganization(organization);
        notification.setStation(station);
        notification.setActor(actor);
        notification.setEventType(normalizeCode(command.getEventType()));
        notification.setCategory(command.getCategory());
        notification.setTitleKey(command.getTitleKey().trim());
        notification.setMessageKey(command.getMessageKey().trim());
        notification.setResourceType(normalizeNullableCode(
                command.getResourceType()
        ));
        notification.setResourceId(command.getResourceId());
        notification.setRequiresAction(command.isRequiresAction());
        notification.setRead(false);
        notification.setReadAt(null);
        notification.setResolvedAt(null);
        notification.setResolvedBy(null);

        return notificationMapper.toResponse(
                notificationRepository.save(notification)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse findMine(int page, int size) {
        User currentUser = getCurrentUserWithOrganization();
        PageRequest pageable = PageRequest.of(
                validatePage(page),
                validateSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<NotificationResponse> notifications = notificationRepository
                .findRequiringAttention(
                        currentUser.getId(),
                        currentUser.getOrganization().getId(),
                        pageable
                )
                .map(notificationMapper::toResponse);

        return NotificationPageResponse.from(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse countMineUnread() {
        User currentUser = getCurrentUserWithOrganization();
        long unread = countUnread(currentUser);
        long nonOrder = notificationRepository.countUnreadExcludingOrderSubmitted(currentUser.getId(), currentUser.getOrganization().getId());
        long actions = notificationRepository.countByRecipientIdAndOrganizationIdAndRequiresActionTrueAndResolvedAtIsNull(currentUser.getId(), currentUser.getOrganization().getId());
        long attention = notificationRepository.countRequiringAttention(currentUser.getId(), currentUser.getOrganization().getId());
        return new UnreadNotificationCountResponse(unread, nonOrder, actions, attention);
    }
    @Override
    public NotificationResponse markMineAsRead(UUID notificationId) {
        if (notificationId == null) {
            throw new BusinessException("Notification id is required.");
        }

        User currentUser = getCurrentUserWithOrganization();
        Notification notification = notificationRepository
                .findByIdAndRecipientIdAndOrganizationId(
                        notificationId,
                        currentUser.getId(),
                        currentUser.getOrganization().getId()
                )
                .orElseThrow(() -> new BusinessException(
                        "Notification was not found."
                ));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(OffsetDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    public UnreadNotificationCountResponse markAllMineAsRead() {
        User currentUser = getCurrentUserWithOrganization();
        notificationRepository.markAllAsRead(
                currentUser.getId(),
                currentUser.getOrganization().getId(),
                OffsetDateTime.now()
        );
        long actions = notificationRepository.countByRecipientIdAndOrganizationIdAndRequiresActionTrueAndResolvedAtIsNull(currentUser.getId(), currentUser.getOrganization().getId());
        return new UnreadNotificationCountResponse(0, 0, actions, actions);
    }

    @Override
    public int resolveRequiredActions(
            UUID organizationId,
            String resourceType,
            UUID resourceId,
            UUID resolvedById
    ) {
        requireId(organizationId, "organization");
        requireId(resourceId, "resource");
        requireId(resolvedById, "resolver");
        requireText(resourceType, "resource type");

        User resolver = getUser(resolvedById, "resolver");
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException(
                        "Notification organization was not found."));
        requireSameOrganization(resolver, organization, "resolver");

        java.util.List<Notification> pending = notificationRepository
                .findByOrganizationIdAndResourceTypeAndResourceIdAndRequiresActionTrueAndResolvedAtIsNull(
                        organizationId,
                        normalizeCode(resourceType),
                        resourceId
                );
        if (pending.isEmpty()) {
            return 0;
        }

        OffsetDateTime resolvedAt = OffsetDateTime.now();
        pending.forEach(notification -> {
            notification.setRequiresAction(false);
            notification.setResolvedAt(resolvedAt);
            notification.setResolvedBy(resolver);
        });
        notificationRepository.saveAll(pending);
        return pending.size();
    }

    private void validateCreateCommand(CreateNotificationCommand command) {
        if (command == null) {
            throw new BusinessException("Notification data is required.");
        }
        requireId(command.getRecipientId(), "recipient");
        requireId(command.getOrganizationId(), "organization");
        requireText(command.getEventType(), "event type");
        requireText(command.getTitleKey(), "title key");
        requireText(command.getMessageKey(), "message key");
        if (command.getCategory() == null) {
            throw new BusinessException("Notification category is required.");
        }
        boolean actionCategory = command.getCategory()
                == NotificationCategory.ACTION_REQUIRED;
        if (command.isRequiresAction() != actionCategory) {
            throw new BusinessException(
                    "Notification category and requiresAction are inconsistent."
            );
        }
        boolean hasResourceType = command.getResourceType() != null
                && !command.getResourceType().isBlank();
        boolean hasResourceId = command.getResourceId() != null;
        if (hasResourceType != hasResourceId) {
            throw new BusinessException(
                    "Notification resource type and id must be provided together."
            );
        }
    }

    private User getCurrentUserWithOrganization() {
        User currentUser = authorizationService.getAuthenticatedUser();
        if (currentUser.getOrganization() == null
                || currentUser.getOrganization().getId() == null) {
            throw new BusinessException(
                    "Authenticated user has no organization."
            );
        }
        return currentUser;
    }

    private long countUnread(User user) {
        return notificationRepository
                .countByRecipientIdAndOrganizationIdAndReadFalse(
                        user.getId(),
                        user.getOrganization().getId()
                );
    }

    private User getUser(UUID id, String relation) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Notification " + relation + " was not found."
                ));
    }

    private void requireSameOrganization(
            User user,
            Organization organization,
            String relation
    ) {
        if (user.getOrganization() == null
                || !organization.getId().equals(
                        user.getOrganization().getId()
                )) {
            throw new BusinessException(
                    "Notification " + relation
                            + " does not belong to the organization."
            );
        }
    }

    private int validatePage(int page) {
        if (page < 0) {
            throw new BusinessException("Page must be zero or greater.");
        }
        return page;
    }

    private int validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    "Page size must be between 1 and " + MAX_PAGE_SIZE + "."
            );
        }
        return size;
    }

    private void requireId(UUID value, String field) {
        if (value == null) {
            throw new BusinessException(
                    "Notification " + field + " id is required."
            );
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    "Notification " + field + " is required."
            );
        }
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String normalizeNullableCode(String value) {
        return value == null || value.isBlank()
                ? null
                : normalizeCode(value);
    }
}
