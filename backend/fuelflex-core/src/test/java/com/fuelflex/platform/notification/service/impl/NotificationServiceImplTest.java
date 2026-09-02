package com.fuelflex.platform.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.notification.dto.request.CreateNotificationCommand;
import com.fuelflex.platform.notification.dto.response.NotificationPageResponse;
import com.fuelflex.platform.notification.dto.response.NotificationResponse;
import com.fuelflex.platform.notification.entity.Notification;
import com.fuelflex.platform.notification.entity.NotificationCategory;
import com.fuelflex.platform.notification.mapper.NotificationMapper;
import com.fuelflex.platform.notification.repository.NotificationRepository;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.organization.repository.OrganizationRepository;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private StationRepository stationRepository;
    @Mock
    private AuthorizationService authorizationService;

    private NotificationServiceImpl service;
    private Organization organization;
    private User recipient;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(
                notificationRepository,
                userRepository,
                organizationRepository,
                stationRepository,
                authorizationService,
                new NotificationMapper()
        );

        organization = new Organization();
        organization.setId(UUID.randomUUID());

        recipient = user(organization);
    }

    @Test
    void createStoresUnreadNotificationWithRecipientAndOrganization() {
        arrangeBaseCreation();
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        NotificationResponse response = service.create(informationCommand());

        ArgumentCaptor<Notification> captor = ArgumentCaptor
                .forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getRecipient()).isSameAs(recipient);
        assertThat(saved.getOrganization()).isSameAs(organization);
        assertThat(saved.isRead()).isFalse();
        assertThat(saved.getReadAt()).isNull();
        assertThat(saved.isRequiresAction()).isFalse();
        assertThat(response.getEventType()).isEqualTo("DAY_OPENED");
    }

    @Test
    void createSupportsOptionalStationActorAndResourceReference() {
        arrangeBaseCreation();
        Station station = new Station();
        station.setId(UUID.randomUUID());
        station.setOrganization(organization);
        User actor = user(organization);
        when(stationRepository.findByIdAndOrganizationId(
                station.getId(), organization.getId()
        )).thenReturn(Optional.of(station));
        when(userRepository.findById(actor.getId()))
                .thenReturn(Optional.of(actor));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0)));
        UUID resourceId = UUID.randomUUID();

        NotificationResponse response = service.create(
                CreateNotificationCommand.builder()
                        .recipientId(recipient.getId())
                        .organizationId(organization.getId())
                        .stationId(station.getId())
                        .actorId(actor.getId())
                        .eventType("order submitted for approval")
                        .category(NotificationCategory.ACTION_REQUIRED)
                        .titleKey("events.orderSubmitted.title")
                        .messageKey("events.orderSubmitted.message")
                        .resourceType("order")
                        .resourceId(resourceId)
                        .requiresAction(true)
                        .build()
        );

        assertThat(response.getStationId()).isEqualTo(station.getId());
        assertThat(response.getActorId()).isEqualTo(actor.getId());
        assertThat(response.getResourceType()).isEqualTo("ORDER");
        assertThat(response.getResourceId()).isEqualTo(resourceId);
        assertThat(response.isRequiresAction()).isTrue();
    }

    @Test
    void createAllowsStationAndActorToBeAbsent() {
        arrangeBaseCreation();
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        NotificationResponse response = service.create(informationCommand());

        assertThat(response.getStationId()).isNull();
        assertThat(response.getActorId()).isNull();
        verify(stationRepository, never())
                .findByIdAndOrganizationId(any(), any());
    }

    @Test
    void createRejectsCrossOrganizationRecipient() {
        Organization otherOrganization = new Organization();
        otherOrganization.setId(UUID.randomUUID());
        recipient.setOrganization(otherOrganization);
        when(organizationRepository.findById(organization.getId()))
                .thenReturn(Optional.of(organization));
        when(userRepository.findById(recipient.getId()))
                .thenReturn(Optional.of(recipient));

        assertThatThrownBy(() -> service.create(informationCommand()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("recipient does not belong");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createRequiresConsistentActionCategoryAndResourcePair() {
        CreateNotificationCommand inconsistent = CreateNotificationCommand
                .builder()
                .recipientId(recipient.getId())
                .organizationId(organization.getId())
                .eventType("ORDER_SUBMITTED_FOR_APPROVAL")
                .category(NotificationCategory.INFORMATION)
                .titleKey("title")
                .messageKey("message")
                .requiresAction(true)
                .resourceType("ORDER")
                .build();

        assertThatThrownBy(() -> service.create(inconsistent))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("requiresAction");
    }

    @Test
    void findMineUsesAuthenticatedRecipientOrganizationAndNewestFirst() {
        arrangeCurrentUser();
        Notification older = notification(recipient, OffsetDateTime.now().minusHours(2));
        Notification newer = notification(recipient, OffsetDateTime.now());
        when(notificationRepository.findRequiringAttention(
                any(), any(), any(Pageable.class)
        )).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(2);
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort().getOrderFor("updatedAt").isDescending())
                    .isTrue();
            return new PageImpl<>(List.of(newer, older), pageable, 2);
        });

        NotificationPageResponse response = service.findMine(0, 20);

        assertThat(response.getContent())
                .extracting(NotificationResponse::getId)
                .containsExactly(newer.getId(), older.getId());
        verify(notificationRepository).findRequiringAttention(
                eq(recipient.getId()), eq(organization.getId()), any(Pageable.class)
        );
    }

    @Test
    void countMineUnreadIsScopedToAuthenticatedUserAndOrganization() {
        arrangeCurrentUser();
        when(notificationRepository
                .countByRecipientIdAndOrganizationIdAndReadFalse(
                        recipient.getId(), organization.getId()
                )).thenReturn(4L);

        assertThat(service.countMineUnread().getUnreadCount()).isEqualTo(4);
    }

    @Test
    void markMineAsReadSetsReadAt() {
        arrangeCurrentUser();
        Notification notification = notification(recipient, OffsetDateTime.now());
        when(notificationRepository.findByIdAndRecipientIdAndOrganizationId(
                notification.getId(), recipient.getId(), organization.getId()
        )).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = service.markMineAsRead(notification.getId());

        assertThat(response.isRead()).isTrue();
        assertThat(response.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markMineAsReadIsIdempotent() {
        arrangeCurrentUser();
        Notification notification = notification(recipient, OffsetDateTime.now());
        OffsetDateTime originalReadAt = OffsetDateTime.now().minusMinutes(3);
        notification.setRead(true);
        notification.setReadAt(originalReadAt);
        when(notificationRepository.findByIdAndRecipientIdAndOrganizationId(
                notification.getId(), recipient.getId(), organization.getId()
        )).thenReturn(Optional.of(notification));

        NotificationResponse response = service.markMineAsRead(notification.getId());

        assertThat(response.getReadAt()).isEqualTo(originalReadAt);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void cannotReadAnotherUsersNotification() {
        arrangeCurrentUser();
        UUID foreignNotificationId = UUID.randomUUID();
        when(notificationRepository.findByIdAndRecipientIdAndOrganizationId(
                foreignNotificationId,
                recipient.getId(),
                organization.getId()
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markMineAsRead(foreignNotificationId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Notification was not found.");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllMineAsReadIsScopedAndReturnsZero() {
        arrangeCurrentUser();

        assertThat(service.markAllMineAsRead().getUnreadCount()).isZero();
        verify(notificationRepository).markAllAsRead(
                eq(recipient.getId()),
                eq(organization.getId()),
                any(OffsetDateTime.class)
        );
    }

    private void arrangeBaseCreation() {
        when(organizationRepository.findById(eq(organization.getId())))
                .thenReturn(Optional.of(organization));
        when(userRepository.findById(recipient.getId()))
                .thenReturn(Optional.of(recipient));
    }

    private void arrangeCurrentUser() {
        when(authorizationService.getAuthenticatedUser()).thenReturn(recipient);
    }

    private CreateNotificationCommand informationCommand() {
        return CreateNotificationCommand.builder()
                .recipientId(recipient.getId())
                .organizationId(organization.getId())
                .eventType("day opened")
                .category(NotificationCategory.INFORMATION)
                .titleKey("events.dayOpened.title")
                .messageKey("events.dayOpened.message")
                .requiresAction(false)
                .build();
    }

    private User user(Organization userOrganization) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setOrganization(userOrganization);
        return user;
    }

    private Notification notification(User user, OffsetDateTime createdAt) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setRecipient(user);
        notification.setOrganization(user.getOrganization());
        notification.setEventType("DAY_OPENED");
        notification.setCategory(NotificationCategory.INFORMATION);
        notification.setTitleKey("events.dayOpened.title");
        notification.setMessageKey("events.dayOpened.message");
        notification.setCreatedAt(createdAt);
        return notification;
    }

    private Notification persisted(Notification notification) {
        notification.setId(UUID.randomUUID());
        notification.setCreatedAt(OffsetDateTime.now());
        return notification;
    }
}
