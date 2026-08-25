package com.fuelflex.platform.employeevalidation.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationAction;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.user.model.Gender;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class PumpAttendantValidationDtos {

    private PumpAttendantValidationDtos() {
    }

    public record CreateRequest(
            @NotNull UUID stationId,
            @NotEmpty @Size(max = 100) Set<@NotNull UUID> pumpAttendantIds
    ) {
    }

    public record ReviewRequest(
            @Size(max = 1000) String comment
    ) {
    }

    public record UserSummary(
            UUID id,
            String firstName,
            String lastName
    ) {
    }

    public record StationSummary(
            UUID id,
            String code,
            String name
    ) {
    }

    public record CandidateResponse(
            UUID id,
            String firstName,
            String lastName,
            String postName,
            Gender gender,
            String birthPlace,
            LocalDate birthDate,
            String address,
            String email,
            String phoneNumber,
            String operationalCode,
            StationSummary station,
            PumpAttendantValidationStatus validationStatus,
            UUID validationRequestId,
            String validationRequestNumber,
            PumpAttendantValidationRequestStatus validationRequestStatus,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }

    public record ItemResponse(
            UUID id,
            UUID pumpAttendantId,
            String firstName,
            String lastName,
            String postName,
            Gender gender,
            String birthPlace,
            LocalDate birthDate,
            String address,
            String email,
            String phoneNumber,
            String operationalCode,
            StationSummary station,
            PumpAttendantValidationStatus validationStatus,
            boolean invitationPending
    ) {
    }

    public record HistoryResponse(
            UUID id,
            PumpAttendantValidationAction action,
            PumpAttendantValidationRequestStatus oldStatus,
            PumpAttendantValidationRequestStatus newStatus,
            UserSummary performedBy,
            OffsetDateTime performedAt,
            String comment
    ) {
    }

    public record Response(
            UUID id,
            String requestNumber,
            UUID organizationId,
            StationSummary station,
            PumpAttendantValidationRequestStatus status,
            UserSummary createdBy,
            OffsetDateTime submittedAt,
            UserSummary reviewedBy,
            OffsetDateTime reviewedAt,
            String reviewComment,
            List<ItemResponse> pumpAttendants,
            List<HistoryResponse> history,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            long version
    ) {
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
        public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
            return new PageResponse<>(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast()
            );
        }
    }
}
