package com.fuelflex.platform.sale.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.depot.entity.Depot;
import com.fuelflex.platform.dispensingpoint.entity.*;
import com.fuelflex.platform.dispensingpoint.repository.DispensingPointRepository;
import com.fuelflex.platform.fuelmeter.entity.*;
import com.fuelflex.platform.fuelmeter.service.MeteringConsistencyService;
import com.fuelflex.platform.operations.entity.*;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.pump.entity.*;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.stationproduct.repository.StationProductRepository;
import com.fuelflex.platform.stationproductprice.entity.StationProductPrice;
import com.fuelflex.platform.stationproductprice.repository.StationProductPriceRepository;
import com.fuelflex.platform.tank.entity.*;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.tariffcategory.repository.TariffCategoryRepository;

@ExtendWith(MockitoExtension.class)
class PosConfigurationResolverTest {
    @Mock DispensingPointRepository points; @Mock MeteringConsistencyService consistency;
    @Mock StationProductRepository stationProducts; @Mock TariffCategoryRepository tariffs; @Mock StationProductPriceRepository prices;
    PosConfigurationResolver resolver; Organization org; Station station; Pump pump; DispensingPoint point; Tank tank;
    Product product; FuelMeter meter; PumpShiftAssignment assignment; StationProduct stationProduct; TariffCategory cash; StationProductPrice price;

    @BeforeEach void setup() {
        resolver = new PosConfigurationResolver(points, consistency, stationProducts, tariffs, prices);
        org = new Organization(); org.setId(UUID.randomUUID());
        station = new Station(); station.setId(UUID.randomUUID()); station.setOrganization(org);
        product = new Product(); product.setId(UUID.randomUUID()); product.setOrganization(org); product.setName("Diesel"); product.setActive(true);
        Depot depot = new Depot(); depot.setId(UUID.randomUUID()); depot.setStation(station);
        tank = new Tank(); tank.setId(UUID.randomUUID()); tank.setName("T1"); tank.setDepot(depot); tank.setProduct(product); tank.setActive(true); tank.setStatus(TankStatus.ACTIVE);
        pump = new Pump(); pump.setId(UUID.randomUUID()); pump.setStation(station); pump.setActive(true); pump.setStatus(PumpStatus.ACTIVE); pump.setMeteringLevel(MeteringLevel.DISPENSING_POINT);
        point = new DispensingPoint(); point.setId(UUID.randomUUID()); point.setPump(pump); point.setTank(tank); point.setActive(true); point.setStatus(DispensingPointStatus.ACTIVE);
        meter = new FuelMeter(); meter.setId(UUID.randomUUID()); meter.setDispensingPoint(point); meter.setActive(true); meter.setStatus(FuelMeterStatus.ACTIVE);
        OperationalDay day = new OperationalDay(); day.setId(UUID.randomUUID()); day.setOrganization(org); day.setStation(station); day.setStatus(OperationalStatus.OPEN);
        assignment = new PumpShiftAssignment(); assignment.setId(UUID.randomUUID()); assignment.setOperationalDay(day); assignment.setFuelMeter(meter); assignment.setStatus(OperationalStatus.OPEN);
        stationProduct = new StationProduct(); stationProduct.setId(UUID.randomUUID()); stationProduct.setStation(station); stationProduct.setProduct(product); stationProduct.setActive(true);
        cash = new TariffCategory(); cash.setId(UUID.randomUUID()); cash.setOrganization(org); cash.setCode("CASH"); cash.setName("Cash"); cash.setActive(true);
        price = new StationProductPrice(); price.setStationProduct(stationProduct); price.setTariffCategory(cash); price.setPrice(new BigDecimal("1.750")); price.setActive(true);
        lenient().when(stationProducts.findByStationIdAndProductId(station.getId(), product.getId())).thenReturn(Optional.of(stationProduct));
        lenient().when(tariffs.findByOrganizationIdAndCodeIgnoreCase(org.getId(), "CASH")).thenReturn(Optional.of(cash));
        lenient().when(prices.findByStationProductIdAndTariffCategoryId(stationProduct.getId(), cash.getId())).thenReturn(Optional.of(price));
    }

    @Test void derivesStationPumpMeterTankProductAndCashPrice() {
        ResolvedPosContext result = resolver.resolve(assignment);
        assertThat(result.pump()).isSameAs(pump); assertThat(result.dispensingPoint()).isSameAs(point);
        assertThat(result.tank()).isSameAs(tank); assertThat(result.product()).isSameAs(product);
        assertThat(result.cashUnitPrice()).isEqualByComparingTo("1.750");
    }
    @Test void resolvesGlobalPumpTankFromActivePoints() {
        pump.setMeteringLevel(MeteringLevel.PUMP); meter.setDispensingPoint(null); meter.setPump(pump);
        when(points.findByPumpAndActiveTrueOrderByDisplayOrderAscNameAsc(pump)).thenReturn(List.of(point));
        assertThat(resolver.resolve(assignment).tank()).isSameAs(tank);
    }
    @Test void rejectsInactiveStationProduct() { stationProduct.setActive(false); assertThatThrownBy(() -> resolver.resolve(assignment)).isInstanceOf(BusinessException.class).hasMessageContaining("inactif"); }
    @Test void rejectsMissingCashPrice() { when(prices.findByStationProductIdAndTariffCategoryId(any(), any())).thenReturn(Optional.empty()); assertThatThrownBy(() -> resolver.resolve(assignment)).isInstanceOf(BusinessException.class).hasMessageContaining("tarif CASH"); }
    @Test void rejectsInactiveCashPrice() { price.setActive(false); assertThatThrownBy(() -> resolver.resolve(assignment)).isInstanceOf(BusinessException.class).hasMessageContaining("tarif CASH"); }
}
