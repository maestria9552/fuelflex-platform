package com.fuelflex.platform.sale.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.operations.entity.OperationalDay;
import com.fuelflex.platform.operations.entity.PumpShiftAssignment;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fuel_sales")
@Getter @Setter @NoArgsConstructor
public class FuelSale {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "sale_number", nullable = false, unique = true, updatable = false, length = 30) private String saleNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organization_id", nullable = false) private Organization organization;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "station_id", nullable = false) private Station station;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "operational_day_id", nullable = false) private OperationalDay operationalDay;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "shift_assignment_id", nullable = false) private PumpShiftAssignment shiftAssignment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "pump_attendant_id", nullable = false) private User pumpAttendant;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "fuel_meter_id", nullable = false) private FuelMeter fuelMeter;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "dispensing_point_id") private DispensingPoint dispensingPoint;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tank_id", nullable = false) private Tank tank;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false) private Product product;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "tariff_category_id", nullable = false) private TariffCategory tariffCategory;
    @Column(nullable = false, precision = 19, scale = 3) private BigDecimal quantity;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 3) private BigDecimal unitPrice;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 3) private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) @Column(name = "vehicle_type", nullable = false, length = 30) private VehicleType vehicleType;
    @Column(name = "license_plate", length = 40) private String licensePlate;
    @Column(name = "sold_at", nullable = false) private OffsetDateTime soldAt;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @PrePersist void prePersist() { if (soldAt == null) soldAt = OffsetDateTime.now(); if (createdAt == null) createdAt = soldAt; }
}
