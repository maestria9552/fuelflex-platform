package com.fuelflex.platform.sale.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.sale.dto.PosSaleDtos.PumpAttendantSummary;
import com.fuelflex.platform.sale.dto.PosSaleDtos.ReferenceSummary;
import com.fuelflex.platform.sale.dto.PosSaleDtos.SaleResponse;
import com.fuelflex.platform.sale.entity.FuelSale;

@Component
public class FuelSaleResponseMapper {

    public SaleResponse toResponse(FuelSale sale) {
        var point = sale.getDispensingPoint();
        Pump pump = point == null ? sale.getFuelMeter().getPump() : point.getPump();
        return new SaleResponse(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getOrganization().getId(),
                new ReferenceSummary(sale.getStation().getId(), sale.getStation().getName()),
                sale.getOperationalDay().getId(),
                sale.getShiftAssignment().getId(),
                new PumpAttendantSummary(
                        sale.getPumpAttendant().getId(),
                        sale.getPumpAttendant().getFirstName(),
                        sale.getPumpAttendant().getLastName(),
                        sale.getPumpAttendant().getOperationalCode()
                ),
                new ReferenceSummary(pump.getId(), pump.getName()),
                point == null ? null : new ReferenceSummary(point.getId(), point.getName()),
                new ReferenceSummary(sale.getFuelMeter().getId(), sale.getFuelMeter().getName()),
                new ReferenceSummary(sale.getTank().getId(), sale.getTank().getName()),
                new ReferenceSummary(sale.getProduct().getId(), sale.getProduct().getName()),
                new ReferenceSummary(
                        sale.getTariffCategory().getId(),
                        sale.getTariffCategory().getName()
                ),
                sale.getSaleType(),
                sale.getStatus(),
                sale.getCreditCustomer() == null
                        ? null
                        : new ReferenceSummary(
                                sale.getCreditCustomer().getId(),
                                sale.getCreditCustomer().getName()
                        ),
                sale.getQuantity(),
                sale.getUnitPrice(),
                sale.getTotalAmount(),
                sale.getVehicleType(),
                sale.getLicensePlate(),
                sale.getSoldAt(),
                sale.getReversedAt(),
                sale.getReversalReason()
        );
    }
}
