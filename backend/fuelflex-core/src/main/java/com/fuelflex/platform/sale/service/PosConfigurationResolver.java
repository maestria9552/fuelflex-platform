package com.fuelflex.platform.sale.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPointStatus;
import com.fuelflex.platform.dispensingpoint.repository.DispensingPointRepository;
import com.fuelflex.platform.fuelmeter.entity.FuelMeter;
import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.fuelmeter.service.MeteringConsistencyService;
import com.fuelflex.platform.operations.entity.PumpShiftAssignment;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.pump.entity.MeteringLevel;
import com.fuelflex.platform.pump.entity.Pump;
import com.fuelflex.platform.pump.entity.PumpStatus;
import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.stationproduct.repository.StationProductRepository;
import com.fuelflex.platform.stationproductprice.entity.StationProductPrice;
import com.fuelflex.platform.stationproductprice.repository.StationProductPriceRepository;
import com.fuelflex.platform.tank.entity.Tank;
import com.fuelflex.platform.tank.entity.TankStatus;
import com.fuelflex.platform.tariffcategory.config.DefaultTariffCategories;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.tariffcategory.repository.TariffCategoryRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PosConfigurationResolver {
    private final DispensingPointRepository dispensingPoints;
    private final MeteringConsistencyService meteringConsistency;
    private final StationProductRepository stationProducts;
    private final TariffCategoryRepository tariffCategories;
    private final StationProductPriceRepository prices;

    public ResolvedPosContext resolve(PumpShiftAssignment assignment) { return resolve(assignment, DefaultTariffCategories.CASH_CODE); }

    public ResolvedPosContext resolveCredit(PumpShiftAssignment assignment) { return resolve(assignment, DefaultTariffCategories.CREDIT_CODE); }

    public ResolvedPosContext resolveInternal(PumpShiftAssignment assignment) { return resolve(assignment, DefaultTariffCategories.INTERNAL_CODE); }

    private ResolvedPosContext resolve(PumpShiftAssignment assignment, String tariffCode) {
        FuelMeter meter = assignment.getFuelMeter();
        if (!meter.isActive() || meter.getStatus() != FuelMeterStatus.ACTIVE) {
            throw new BusinessException("Le compteur de l’affectation POS n’est pas actif.");
        }
        DispensingPoint point = meter.getDispensingPoint();
        Pump pump = point == null ? meter.getPump() : point.getPump();
        if (pump == null || !pump.isActive() || pump.getStatus() != PumpStatus.ACTIVE) {
            throw new BusinessException("La pompe de l’affectation POS n’est pas active.");
        }
        meteringConsistency.validateCompletePumpConfiguration(pump);
        Tank tank = resolveTank(meter, pump, point);
        if (!tank.isActive() || tank.getStatus() != TankStatus.ACTIVE) {
            throw new BusinessException("La cuve résolue pour le POS n’est pas active.");
        }
        if (!tank.getDepot().getStation().getId().equals(assignment.getOperationalDay().getStation().getId())) {
            throw new BusinessException("La cuve résolue n’appartient pas à la station opérationnelle.");
        }
        Product product = tank.getProduct();
        if (product == null || !product.isActive()
                || !product.getOrganization().getId().equals(assignment.getOperationalDay().getOrganization().getId())) {
            throw new BusinessException("Le produit de la cuve n’est pas disponible pour cette organisation.");
        }
        StationProduct stationProduct = stationProducts
                .findByStationIdAndProductId(assignment.getOperationalDay().getStation().getId(), product.getId())
                .orElseThrow(() -> new BusinessException("Le produit de la cuve n’est pas configuré dans cette station."));
        if (!stationProduct.isActive()) {
            throw new BusinessException("Le produit de la station est inactif.");
        }
        TariffCategory cash = tariffCategories.findByOrganizationIdAndCodeIgnoreCase(
                        assignment.getOperationalDay().getOrganization().getId(), tariffCode)
                .orElseThrow(() -> new BusinessException("La catégorie tarifaire " + tariffCode + " est introuvable."));
        if (!cash.isActive()) {
            throw new BusinessException("La catégorie tarifaire " + tariffCode + " est inactive.");
        }
        StationProductPrice price = prices.findByStationProductIdAndTariffCategoryId(stationProduct.getId(), cash.getId())
                .orElseThrow(() -> new BusinessException("Aucun tarif " + tariffCode + " n’est configuré pour ce produit."));
        if (!price.isActive() || price.getPrice() == null || price.getPrice().signum() <= 0) {
            throw new BusinessException("Le tarif " + tariffCode + " du produit n’est pas actif ou valide.");
        }
        return new ResolvedPosContext(assignment, pump, point, tank, product, stationProduct, cash,
                price.getPrice().setScale(3, RoundingMode.UNNECESSARY));
    }

    private Tank resolveTank(FuelMeter meter, Pump pump, DispensingPoint point) {
        if (pump.getMeteringLevel() == MeteringLevel.DISPENSING_POINT) {
            if (point == null || meter.getPump() != null || !point.isActive()
                    || point.getStatus() != DispensingPointStatus.ACTIVE) {
                throw new BusinessException("La configuration du compteur par point de distribution est incohérente.");
            }
            return point.getTank();
        }
        if (pump.getMeteringLevel() != MeteringLevel.PUMP || meter.getPump() == null || point != null) {
            throw new BusinessException("La configuration du compteur global est incohérente.");
        }
        List<DispensingPoint> activePoints = dispensingPoints.findByPumpAndActiveTrueOrderByDisplayOrderAscNameAsc(pump);
        if (activePoints.isEmpty()) {
            throw new BusinessException("Aucun point de distribution actif ne permet de résoudre la cuve.");
        }
        Tank tank = activePoints.getFirst().getTank();
        boolean differentTank = activePoints.stream().anyMatch(candidate ->
                candidate.getTank() == null || !candidate.getTank().getId().equals(tank.getId()));
        if (differentTank) {
            throw new BusinessException("Les points actifs de la pompe globale ne ciblent pas une cuve unique.");
        }
        return tank;
    }
}
