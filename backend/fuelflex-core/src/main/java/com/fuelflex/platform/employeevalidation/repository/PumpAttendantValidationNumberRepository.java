package com.fuelflex.platform.employeevalidation.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationRequest;

public interface PumpAttendantValidationNumberRepository
        extends Repository<PumpAttendantValidationRequest, java.util.UUID> {

    @Query(value = "select nextval('pump_attendant_validation_request_number_seq')", nativeQuery = true)
    long nextValue();
}
