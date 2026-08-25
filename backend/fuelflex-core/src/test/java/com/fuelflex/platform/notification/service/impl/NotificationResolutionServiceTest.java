package com.fuelflex.platform.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.notification.entity.Notification;
import com.fuelflex.platform.notification.entity.NotificationCategory;
import com.fuelflex.platform.notification.mapper.NotificationMapper;
import com.fuelflex.platform.notification.repository.NotificationRepository;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.organization.repository.OrganizationRepository;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationResolutionServiceTest {

    @Mock
    private NotificationRepository notifications;
    @Mock
    private UserRepository users;
    @Mock
    private OrganizationRepository organizations;
    @Mock
    private StationRepository stations;
    @Mock
    private AuthorizationService authorization;

    private NotificationServiceImpl service;
    private Organization organization;
    private User supervisor;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(
                notifications, users, organizations, stations,
                authorization, new NotificationMapper());
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        supervisor = user(organization);
    }

    @Test
    void businessResolutionPreservesReadStateAndHistoricalCategory() {
        UUID resourceId = UUID.randomUUID();
        Notification action = action(resourceId);
        when(users.findById(supervisor.getId()))
                .thenReturn(Optional.of(supervisor));
        when(organizations.findById(organization.getId()))
                .thenReturn(Optional.of(organization));
        when(notifications
                .findByOrganizationIdAndResourceTypeAndResourceIdAndRequiresActionTrueAndResolvedAtIsNull(
                        organization.getId(), "RECEPTION", resourceId))
                .thenReturn(List.of(action));

        int resolved = service.resolveRequiredActions(
                organization.getId(), "reception", resourceId,
                supervisor.getId());

        assertThat(resolved).isOne();
        assertThat(action.isRequiresAction()).isFalse();
        assertThat(action.getResolvedAt()).isNotNull();
        assertThat(action.getResolvedBy()).isSameAs(supervisor);
        assertThat(action.isRead()).isFalse();
        assertThat(action.getReadAt()).isNull();
        assertThat(action.getCategory())
                .isEqualTo(NotificationCategory.ACTION_REQUIRED);
        verify(notifications).saveAll(List.of(action));
    }

    @Test
    void alreadyReadActionRemainsReadWhenItIsResolved() {
        UUID resourceId = UUID.randomUUID();
        Notification action = action(resourceId);
        OffsetDateTime readAt = OffsetDateTime.now().minusMinutes(4);
        action.setRead(true);
        action.setReadAt(readAt);
        arrangeResolution(resourceId, List.of(action));

        service.resolveRequiredActions(
                organization.getId(), "RECEPTION", resourceId,
                supervisor.getId());

        assertThat(action.isRead()).isTrue();
        assertThat(action.getReadAt()).isEqualTo(readAt);
        assertThat(action.getResolvedAt()).isNotNull();
    }

    @Test
    void resolutionIsIdempotentAndDoesNotTouchInformationNotifications() {
        UUID resourceId = UUID.randomUUID();
        Notification information = action(resourceId);
        information.setCategory(NotificationCategory.INFORMATION);
        information.setRequiresAction(false);
        arrangeResolution(resourceId, List.of());

        int first = service.resolveRequiredActions(
                organization.getId(), "RECEPTION", resourceId,
                supervisor.getId());
        int second = service.resolveRequiredActions(
                organization.getId(), "RECEPTION", resourceId,
                supervisor.getId());

        assertThat(first).isZero();
        assertThat(second).isZero();
        assertThat(information.getResolvedAt()).isNull();
        verify(notifications, never()).saveAll(any());
    }

    @Test
    void resolverFromAnotherOrganizationCannotResolveTenantActions() {
        Organization other = new Organization();
        other.setId(UUID.randomUUID());
        supervisor.setOrganization(other);
        when(users.findById(supervisor.getId()))
                .thenReturn(Optional.of(supervisor));
        when(organizations.findById(organization.getId()))
                .thenReturn(Optional.of(organization));

        assertThatThrownBy(() -> service.resolveRequiredActions(
                organization.getId(), "RECEPTION", UUID.randomUUID(),
                supervisor.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("resolver does not belong");

        verify(notifications, never())
                .findByOrganizationIdAndResourceTypeAndResourceIdAndRequiresActionTrueAndResolvedAtIsNull(
                        any(), any(), any());
    }

    private void arrangeResolution(
            UUID resourceId,
            List<Notification> pending
    ) {
        when(users.findById(supervisor.getId()))
                .thenReturn(Optional.of(supervisor));
        when(organizations.findById(organization.getId()))
                .thenReturn(Optional.of(organization));
        when(notifications
                .findByOrganizationIdAndResourceTypeAndResourceIdAndRequiresActionTrueAndResolvedAtIsNull(
                        organization.getId(), "RECEPTION", resourceId))
                .thenReturn(pending);
    }

    private Notification action(UUID resourceId) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setOrganization(organization);
        notification.setRecipient(supervisor);
        notification.setCategory(NotificationCategory.ACTION_REQUIRED);
        notification.setRequiresAction(true);
        notification.setResourceType("RECEPTION");
        notification.setResourceId(resourceId);
        return notification;
    }

    private User user(Organization userOrganization) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setOrganization(userOrganization);
        return user;
    }
}
