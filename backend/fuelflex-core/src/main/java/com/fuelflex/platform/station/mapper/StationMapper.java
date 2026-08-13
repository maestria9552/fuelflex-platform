package com.fuelflex.platform.station.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.station.dto.request.StationRequest;
import com.fuelflex.platform.station.dto.response.StationResponse;
import com.fuelflex.platform.station.entity.Station;

@Component
public class StationMapper {

    public Station toEntity(
            StationRequest request
    ) {
        if (request == null) {
            return null;
        }

        return Station.builder()
                .code(request.getCode())
                .name(request.getName())
                .shortName(request.getShortName())
                .type(request.getType())
                .status(request.getStatus())
                .address(request.getAddress())
                .city(request.getCity())
                .province(request.getProvince())
                .country(request.getCountry())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .displayOrder(request.getDisplayOrder())
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();
    }

    public void updateEntity(
            Station station,
            StationRequest request
    ) {
        if (station == null || request == null) {
            return;
        }

        station.setCode(request.getCode());
        station.setName(request.getName());
        station.setShortName(request.getShortName());
        station.setType(request.getType());
        station.setStatus(request.getStatus());
        station.setAddress(request.getAddress());
        station.setCity(request.getCity());
        station.setProvince(request.getProvince());
        station.setCountry(request.getCountry());
        station.setPhoneNumber(request.getPhoneNumber());
        station.setEmail(request.getEmail());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setDisplayOrder(request.getDisplayOrder());

        if (request.getActive() != null) {
            station.setActive(request.getActive());
        }
    }

    public StationResponse toResponse(
            Station station
    ) {
        if (station == null) {
            return null;
        }

        return StationResponse.builder()
                .id(station.getId())
                .organizationId(
                        station.getOrganization() == null
                                ? null
                                : station.getOrganization().getId()
                )
                .code(station.getCode())
                .name(station.getName())
                .shortName(station.getShortName())
                .type(station.getType())
                .status(station.getStatus())
                .address(station.getAddress())
                .city(station.getCity())
                .province(station.getProvince())
                .country(station.getCountry())
                .phoneNumber(station.getPhoneNumber())
                .email(station.getEmail())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .displayOrder(station.getDisplayOrder())
                .active(station.isActive())
                .createdAt(station.getCreatedAt())
                .updatedAt(station.getUpdatedAt())
                .build();
    }
}