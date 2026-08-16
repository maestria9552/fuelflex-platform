package com.fuelflex.platform.station.service.impl;

import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.repository.StationRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationAccessServiceImpl implements StationAccessService {
    private static final Set<String> ORGANIZATION_WIDE = Set.of("SUPERVISOR", "ACCOUNTANT", "AUDITOR");
    private static final Set<String> ASSIGNMENT_SCOPED = Set.of("MANAGER", "PUMP_ATTENDANT");
    private final StationRepository stationRepository;
    private final UserStationAssignmentRepository assignmentRepository;

    @Override
    public Set<UUID> getAccessibleStationIds(User user) {
        if (user == null || !user.isEnabled() || user.getOrganization() == null) return Set.of();
        String role = primaryActiveRole(user);
        UUID organizationId = user.getOrganization().getId();
        if (ORGANIZATION_WIDE.contains(role)) {
            return stationRepository.findActiveIdsByOrganizationId(organizationId);
        }
        if (ASSIGNMENT_SCOPED.contains(role)) {
            return assignmentRepository.findActiveStationIds(user.getId(), organizationId);
        }
        return Set.of();
    }

    @Override
    public boolean canAccessStation(User user, UUID stationId) {
        return stationId != null && getAccessibleStationIds(user).contains(stationId);
    }

    @Override
    public void checkStationAccess(User user, UUID stationId) {
        if (!canAccessStation(user, stationId)) {
            throw new ForbiddenException("You are not authorized to access this station.");
        }
    }

    private String primaryActiveRole(User user) {
        return user.getRoles().stream().filter(Role::isActive).map(Role::getCode)
                .filter(code -> ORGANIZATION_WIDE.contains(code) || ASSIGNMENT_SCOPED.contains(code))
                .findFirst().orElse("");
    }
}
