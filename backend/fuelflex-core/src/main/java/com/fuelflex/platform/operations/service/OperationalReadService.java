package com.fuelflex.platform.operations.service;

import java.util.List;
import java.util.UUID;

import com.fuelflex.platform.operations.dto.OperationalDtos.AvailableFuelMeterResponse;
import com.fuelflex.platform.operations.dto.OperationalDtos.EligiblePumpAttendantResponse;
import com.fuelflex.platform.station.dto.response.StationResponse;

public interface OperationalReadService {

    List<StationResponse> accessibleStations();

    List<EligiblePumpAttendantResponse> eligiblePumpAttendants(UUID stationId);

    List<AvailableFuelMeterResponse> availableFuelMeters(UUID operationalDayId);
}
