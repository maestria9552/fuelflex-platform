package com.fuelflex.platform.sale.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fuelflex.platform.sale.entity.FuelSale;

public interface FuelSaleRepository extends JpaRepository<FuelSale, UUID> {
    List<FuelSale> findByPumpAttendantIdAndOperationalDayIdOrderBySoldAtDesc(UUID pumpAttendantId, UUID operationalDayId);
    Optional<FuelSale> findByIdAndPumpAttendantIdAndOperationalDayIdAndOrganizationIdAndStationId(
            UUID id, UUID pumpAttendantId, UUID operationalDayId, UUID organizationId, UUID stationId);
}
