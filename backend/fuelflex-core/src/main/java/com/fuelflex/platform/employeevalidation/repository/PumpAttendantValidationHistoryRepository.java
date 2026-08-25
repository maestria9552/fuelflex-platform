package com.fuelflex.platform.employeevalidation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationHistory;

public interface PumpAttendantValidationHistoryRepository
        extends JpaRepository<PumpAttendantValidationHistory, UUID> {

    List<PumpAttendantValidationHistory> findByRequestIdOrderByPerformedAtAscIdAsc(
            UUID requestId
    );
}
