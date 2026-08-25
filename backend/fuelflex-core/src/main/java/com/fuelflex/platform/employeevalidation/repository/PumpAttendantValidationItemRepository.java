package com.fuelflex.platform.employeevalidation.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fuelflex.platform.employeevalidation.entity.PumpAttendantValidationItem;

public interface PumpAttendantValidationItemRepository
        extends JpaRepository<PumpAttendantValidationItem, UUID> {

    @Query("""
            select item from PumpAttendantValidationItem item
              join fetch item.pumpAttendant
             where item.request.id = :requestId
             order by item.firstNameSnapshot, item.lastNameSnapshot, item.id
            """)
    List<PumpAttendantValidationItem> findByRequestId(
            @Param("requestId") UUID requestId
    );

    Optional<PumpAttendantValidationItem> findByPumpAttendantId(UUID pumpAttendantId);

    List<PumpAttendantValidationItem> findByPumpAttendantIdIn(
            Collection<UUID> pumpAttendantIds
    );
}
