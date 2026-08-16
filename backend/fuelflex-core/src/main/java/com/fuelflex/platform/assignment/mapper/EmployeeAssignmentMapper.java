package com.fuelflex.platform.assignment.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.assignment.dto.response.EmployeeAssignmentResponse;
import com.fuelflex.platform.assignment.entity.UserStationAssignment;

@Component
public class EmployeeAssignmentMapper {

    public EmployeeAssignmentResponse toResponse(UserStationAssignment assignment) {
        return EmployeeAssignmentResponse.builder()
                .id(assignment.getId())
                .employeeId(assignment.getUser().getId())
                .stationId(assignment.getStation().getId())
                .stationName(assignment.getStation().getName())
                .stationCode(assignment.getStation().getCode())
                .validFrom(assignment.getValidFrom())
                .validUntil(assignment.getValidUntil())
                .active(assignment.isActive())
                .createdById(assignment.getCreatedBy().getId())
                .createdAt(assignment.getCreatedAt())
                .endedById(assignment.getEndedBy() == null ? null : assignment.getEndedBy().getId())
                .endedAt(assignment.getEndedAt())
                .reason(assignment.getReason())
                .build();
    }
}
