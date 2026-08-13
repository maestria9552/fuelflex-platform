package com.fuelflex.platform.station.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StationConfigurationValidationResponse {

    private boolean valid;

    private UUID stationId;

    private List<ConfigurationIssue> issues;

    private ConfigurationSummary summary;

    @Getter
    @Builder
    public static class ConfigurationIssue {

        private String code;

        private String step;

        private String objectType;

        private UUID objectId;

        private String objectName;

        private String message;
    }

    @Getter
    @Builder
    public static class ConfigurationSummary {

        private int depots;

        private int tanks;

        private int pumps;

        private int dispensingPoints;

        private int fuelMeters;
    }
}
