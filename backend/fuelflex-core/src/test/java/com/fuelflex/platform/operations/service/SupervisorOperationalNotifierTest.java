package com.fuelflex.platform.operations.service;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fuelflex.platform.notification.service.NotificationService;
import com.fuelflex.platform.operations.entity.OperationalDay;
import com.fuelflex.platform.operations.repository.OperationalDayActivityNotificationRepository;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

class SupervisorOperationalNotifierTest {

    @Test
    void createsOneIndependentlyScopedAggregateForEveryEligibleSupervisor() {
        UserRepository users = mock(UserRepository.class);
        StationAccessService access = mock(StationAccessService.class);
        NotificationService genericNotifications = mock(NotificationService.class);
        OperationalDayActivityNotificationRepository aggregates = mock(OperationalDayActivityNotificationRepository.class);
        SupervisorOperationalNotifier notifier = new SupervisorOperationalNotifier(
                users, access, genericNotifications, aggregates);

        Organization organization = new Organization(); organization.setId(UUID.randomUUID());
        Station station = new Station(); station.setId(UUID.randomUUID()); station.setOrganization(organization);
        User actor = user(organization);
        User firstSupervisor = user(organization);
        User secondSupervisor = user(organization);
        OperationalDay day = new OperationalDay(); day.setId(UUID.randomUUID());
        day.setOrganization(organization); day.setStation(station);

        when(users.findEnabledByOrganizationIdAndRoleCode(organization.getId(), "SUPERVISOR"))
                .thenReturn(List.of(firstSupervisor, secondSupervisor));
        when(access.canAccessStation(any(), eq(station.getId()))).thenReturn(true);

        notifier.recordOperationalDayActivity(
                actor, day, OperationalDayActivityType.INTERNAL_CONSUMPTION_RECORDED);

        verify(aggregates).upsert(firstSupervisor.getId(), organization.getId(), station.getId(),
                actor.getId(), day.getId(), OperationalDayActivityType.INTERNAL_CONSUMPTION_RECORDED);
        verify(aggregates).upsert(secondSupervisor.getId(), organization.getId(), station.getId(),
                actor.getId(), day.getId(), OperationalDayActivityType.INTERNAL_CONSUMPTION_RECORDED);
        verifyNoInteractions(genericNotifications);
    }

    private User user(Organization organization) {
        User user = new User(); user.setId(UUID.randomUUID()); user.setOrganization(organization);
        return user;
    }
}
