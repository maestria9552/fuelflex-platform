package com.fuelflex.platform.station.service;

import java.util.Set;
import java.util.UUID;
import com.fuelflex.platform.user.entity.User;

public interface StationAccessService {
    Set<UUID> getAccessibleStationIds(User user);
    boolean canAccessStation(User user, UUID stationId);
    void checkStationAccess(User user, UUID stationId);
}
