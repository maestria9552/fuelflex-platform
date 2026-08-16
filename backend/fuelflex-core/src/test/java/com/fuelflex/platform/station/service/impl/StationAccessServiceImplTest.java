package com.fuelflex.platform.station.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.user.entity.User;

@ExtendWith(MockitoExtension.class)
class StationAccessServiceImplTest {
    @Mock StationRepository stationRepository;
    @Mock UserStationAssignmentRepository assignmentRepository;
    StationAccessServiceImpl service;

    @BeforeEach void setUp() {
        service = new StationAccessServiceImpl(stationRepository, assignmentRepository);
    }

    @Test void supervisorAccountantAndAuditorHaveOrganizationWideAccess() {
        for (String role : Set.of("SUPERVISOR", "ACCOUNTANT", "AUDITOR")) {
            User user = user(role);
            UUID stationId = UUID.randomUUID();
            when(stationRepository.findActiveIdsByOrganizationId(user.getOrganization().getId()))
                    .thenReturn(Set.of(stationId));
            assertThat(service.getAccessibleStationIds(user)).containsExactly(stationId);
        }
    }

    @Test void managerAndPumpAttendantUseOnlyActiveAssignments() {
        for (String role : Set.of("MANAGER", "PUMP_ATTENDANT")) {
            User user = user(role);
            UUID stationId = UUID.randomUUID();
            when(assignmentRepository.findActiveStationIds(user.getId(), user.getOrganization().getId()))
                    .thenReturn(Set.of(stationId));
            assertThat(service.canAccessStation(user, stationId)).isTrue();
            assertThatThrownBy(() -> service.checkStationAccess(user, UUID.randomUUID()))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Test void disabledAndExternalUsersHaveNoImplicitAccess() {
        User disabled = user("MANAGER");
        disabled.setEnabled(false);
        assertThat(service.getAccessibleStationIds(disabled)).isEmpty();
        assertThat(service.getAccessibleStationIds(user("SUPPLIER_USER"))).isEmpty();
    }

    private User user(String roleCode) {
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        Role role = new Role();
        role.setCode(roleCode);
        role.setActive(true);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEnabled(true);
        user.setOrganization(organization);
        user.setRoles(Set.of(role));
        return user;
    }
}
