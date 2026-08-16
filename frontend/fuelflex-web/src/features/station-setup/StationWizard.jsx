import { useEffect, useMemo, useState } from "react";
import { LoaderCircle } from "lucide-react";
import { useTranslation } from "react-i18next";

import DepotModal from "../depot/components/DepotModal";
import TankModal from "../tank/components/TankModal";
import PumpModal from "../pump/components/PumpModal";
import DispensingPointModal from "../dispensing-point/components/DispensingPointModal";
import FuelMeterModal from "../fuel-meter/components/FuelMeterModal";
import { getDispensingPoints } from "../../services/dispensingPoint/dispensingPointService";
import { getDepots } from "../../services/depot/depotService";
import { getDispensingPointFuelMeters, getPumpFuelMeters } from "../../services/fuelMeter/fuelMeterService";
import { getPumps } from "../../services/pump/pumpService";
import { getActiveProducts } from "../../services/product/productService";
import {
  createStationProduct,
  deactivateStationProduct,
  getStationProducts,
  updateStationProduct,
} from "../../services/stationProduct/stationProductService";
import { createStation, getStationById, updateStation, validateStationConfiguration } from "../../services/station/stationService";
import { getTanks } from "../../services/tank/tankService";
import StationSetupStepper from "./components/StationSetupStepper";
import DepotStep from "./steps/DepotStep";
import DispensingPointStep from "./steps/DispensingPointStep";
import FuelMeterStep from "./steps/FuelMeterStep";
import CommissioningStep from "./steps/CommissioningStep";
import ReviewStep from "./steps/ReviewStep";
import ProductsStep from "./steps/ProductsStep";
import StationStep from "./steps/StationStep";
import PumpStep from "./steps/PumpStep";
import TankStep from "./steps/TankStep";
import { clearStationSetupDraft, getStationSetupDraft, saveStationSetupDraft } from "./stationSetupStorage";
import "./StationWizard.css";

const AVAILABLE_STEPS = new Set(["station", "products", "depots", "tanks", "pumps", "dispensing-points", "fuel-meters", "review", "commissioning"]);

function StationWizard({ organizationId, onBackToPreparation }) {
  const { t } = useTranslation(["stationSetup", "stations", "depots", "tanks", "pumps", "dispensingPoints", "fuelMeters"]);
  const [initialDraft] = useState(() => getStationSetupDraft(organizationId));
  const [activeStep, setActiveStep] = useState(AVAILABLE_STEPS.has(initialDraft?.activeStep) ? initialDraft.activeStep : "station");
  const [station, setStation] = useState(null);
  const [isRestoring, setIsRestoring] = useState(Boolean(initialDraft?.stationId));
  const [isSavingStation, setIsSavingStation] = useState(false);
  const [stationError, setStationError] = useState("");
  const [stationSuccess, setStationSuccess] = useState("");
  const [products, setProducts] = useState([]);
  const [isLoadingProducts, setIsLoadingProducts] = useState(false);
  const [productsError, setProductsError] = useState("");
  const [productsAttempt, setProductsAttempt] = useState(0);
  const [selectedProductIds, setSelectedProductIds] = useState(initialDraft?.selectedProductIds || []);
  const [productsSavedMessage, setProductsSavedMessage] = useState("");
  const [stationProducts, setStationProducts] = useState([]);
  const [isSavingProducts, setIsSavingProducts] = useState(false);
  const [depots, setDepots] = useState([]);
  const [isLoadingDepots, setIsLoadingDepots] = useState(false);
  const [depotsError, setDepotsError] = useState("");
  const [depotsSuccess, setDepotsSuccess] = useState("");
  const [depotsAttempt, setDepotsAttempt] = useState(0);
  const [editingDepot, setEditingDepot] = useState(undefined);
  const [tanksByDepot, setTanksByDepot] = useState({});
  const [isLoadingTanks, setIsLoadingTanks] = useState(false);
  const [tanksError, setTanksError] = useState("");
  const [tanksSuccess, setTanksSuccess] = useState("");
  const [tanksAttempt, setTanksAttempt] = useState(0);
  const [tankModalState, setTankModalState] = useState(null);
  const [pumps, setPumps] = useState([]);
  const [isLoadingPumps, setIsLoadingPumps] = useState(false);
  const [pumpsError, setPumpsError] = useState("");
  const [pumpsSuccess, setPumpsSuccess] = useState("");
  const [pumpsAttempt, setPumpsAttempt] = useState(0);
  const [editingPump, setEditingPump] = useState(undefined);
  const [dispensingPointsByPump, setDispensingPointsByPump] = useState({});
  const [isLoadingDispensingPoints, setIsLoadingDispensingPoints] = useState(false);
  const [dispensingPointsError, setDispensingPointsError] = useState("");
  const [dispensingPointsSuccess, setDispensingPointsSuccess] = useState("");
  const [dispensingPointsAttempt, setDispensingPointsAttempt] = useState(0);
  const [dispensingPointModalState, setDispensingPointModalState] = useState(null);
  const [pumpMetersByPump, setPumpMetersByPump] = useState({});
  const [pointMetersByPoint, setPointMetersByPoint] = useState({});
  const [isLoadingFuelMeters, setIsLoadingFuelMeters] = useState(false);
  const [fuelMetersError, setFuelMetersError] = useState("");
  const [fuelMetersSuccess, setFuelMetersSuccess] = useState("");
  const [fuelMetersAttempt, setFuelMetersAttempt] = useState(0);
  const [fuelMeterModalState, setFuelMeterModalState] = useState(null);
  const [configurationValidation, setConfigurationValidation] = useState(null);
  const [isValidatingConfiguration, setIsValidatingConfiguration] = useState(false);
  const [configurationValidationError, setConfigurationValidationError] = useState("");
  const [configurationValidationAttempt, setConfigurationValidationAttempt] = useState(0);
  const [isCompletingCommissioning, setIsCompletingCommissioning] = useState(false);
  const [isCommissioningComplete, setIsCommissioningComplete] = useState(false);

  const tanks = useMemo(() => Object.values(tanksByDepot).flat(), [tanksByDepot]);

  const selectedProducts = useMemo(() => {
    const selectedIds = new Set(selectedProductIds);
    return products.filter((product) => selectedIds.has(product.id));
  }, [products, selectedProductIds]);

  const saveDraft = (updates = {}) => saveStationSetupDraft(organizationId, {
    stationId: station?.id || initialDraft?.stationId || null,
    activeStep,
    selectedProductIds,
    depotIds: depots.map((depot) => depot.id),
    pumpIds: pumps.map((pump) => pump.id),
    dispensingPointIds: Object.values(dispensingPointsByPump).flat().map((point) => point.id),
    fuelMeterIds: [...Object.values(pumpMetersByPump).flat(), ...Object.values(pointMetersByPoint).flat()].map((meter) => meter.id),
    ...updates,
  });

  useEffect(() => {
    if (!initialDraft?.stationId) return undefined;
    const controller = new AbortController();
    getStationById(organizationId, initialDraft.stationId, { signal: controller.signal })
      .then(setStation)
      .catch((error) => {
        if (error?.name === "AbortError") return;
        clearStationSetupDraft();
        setActiveStep("station");
        setSelectedProductIds([]);
        setStationError(t("wizard.draftUnavailable"));
      })
      .finally(() => { if (!controller.signal.aborted) setIsRestoring(false); });
    return () => controller.abort();
  }, [initialDraft, organizationId, t]);

  useEffect(() => {
    if (!station?.id) return undefined;
    const controller = new AbortController();
    Promise.resolve()
      .then(() => {
        setIsLoadingProducts(true);
        setProductsError("");
        return Promise.all([
          getActiveProducts(organizationId, { signal: controller.signal }),
          getStationProducts(organizationId, station.id, { signal: controller.signal }),
        ]);
      })
      .then(([catalogResult, stationProductResult]) => {
        const activeProducts = Array.isArray(catalogResult) ? catalogResult : [];
        const loadedStationProducts = Array.isArray(stationProductResult) ? stationProductResult : [];
        setProducts(activeProducts);
        setStationProducts(loadedStationProducts);
        const availableIds = new Set(activeProducts.map((product) => product.id));
        const persistedIds = loadedStationProducts
          .filter((stationProduct) => stationProduct.active && availableIds.has(stationProduct.productId))
          .map((stationProduct) => stationProduct.productId);
        setSelectedProductIds(persistedIds);
        saveStationSetupDraft(organizationId, {
          stationId: station.id,
          activeStep,
          selectedProductIds: persistedIds,
        });
      })
      .catch((error) => { if (error?.name !== "AbortError") setProductsError(error?.message || t("wizard.productsLoadFailed")); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingProducts(false); });
    return () => controller.abort();
  }, [activeStep, organizationId, productsAttempt, station?.id, t]);

  useEffect(() => {
    if (!station?.id || !["depots", "tanks", "dispensing-points", "fuel-meters", "review"].includes(activeStep)) return undefined;
    const controller = new AbortController();
    Promise.resolve()
      .then(() => {
        setIsLoadingDepots(true);
        setDepotsError("");
        return getDepots(organizationId, station.id, { signal: controller.signal });
      })
      .then((result) => {
        const loadedDepots = Array.isArray(result) ? result : [];
        setDepots(loadedDepots);
        saveStationSetupDraft(organizationId, { stationId: station.id, activeStep, selectedProductIds, depotIds: loadedDepots.map((depot) => depot.id) });
      })
      .catch((error) => { if (error?.name !== "AbortError") setDepotsError(error?.message || t("depots:feedback.depotsLoadFailed")); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingDepots(false); });
    return () => controller.abort();
  }, [activeStep, depotsAttempt, organizationId, selectedProductIds, station?.id, t]);

  useEffect(() => {
    if (!["tanks", "dispensing-points", "review"].includes(activeStep) || depots.length === 0 || !station?.id) return undefined;
    const controller = new AbortController();
    Promise.resolve()
      .then(() => {
        setIsLoadingTanks(true);
        setTanksError("");
        return Promise.all(depots.map(async (depot) => [depot.id, await getTanks(organizationId, station.id, depot.id, { signal: controller.signal })]));
      })
      .then((entries) => setTanksByDepot(Object.fromEntries(entries.map(([id, tanks]) => [id, Array.isArray(tanks) ? tanks : []]))))
      .catch((error) => { if (error?.name !== "AbortError") setTanksError(error?.message || t("tanks:feedback.tanksLoadFailed")); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingTanks(false); });
    return () => controller.abort();
  }, [activeStep, depots, organizationId, station?.id, t, tanksAttempt]);

  useEffect(() => {
    if (!["pumps", "dispensing-points", "fuel-meters", "review"].includes(activeStep) || !station?.id) return undefined;
    const controller = new AbortController();
    Promise.resolve()
      .then(() => {
        setIsLoadingPumps(true);
        setPumpsError("");
        return getPumps(organizationId, station.id, { signal: controller.signal });
      })
      .then((result) => {
        const loadedPumps = Array.isArray(result) ? result : [];
        setPumps(loadedPumps);
        saveStationSetupDraft(organizationId, { stationId: station.id, activeStep, selectedProductIds, depotIds: depots.map((depot) => depot.id), pumpIds: loadedPumps.map((pump) => pump.id) });
      })
      .catch((error) => { if (error?.name !== "AbortError") setPumpsError(error?.message || t("pumps:feedback.pumpsLoadFailed")); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingPumps(false); });
    return () => controller.abort();
  }, [activeStep, depots, organizationId, pumpsAttempt, selectedProductIds, station?.id, t]);

  useEffect(() => {
    if (!["dispensing-points", "fuel-meters", "review"].includes(activeStep) || pumps.length === 0 || !station?.id) return undefined;
    const controller = new AbortController();
    Promise.resolve()
      .then(() => {
        setIsLoadingDispensingPoints(true);
        setDispensingPointsError("");
        return Promise.all(pumps.map(async (pump) => [pump.id, await getDispensingPoints(organizationId, station.id, pump.id, { signal: controller.signal })]));
      })
      .then((entries) => {
        const loadedPoints = Object.fromEntries(entries.map(([pumpId, points]) => [pumpId, Array.isArray(points) ? points : []]));
        setDispensingPointsByPump(loadedPoints);
        saveStationSetupDraft(organizationId, { stationId: station.id, activeStep, selectedProductIds, depotIds: depots.map((depot) => depot.id), pumpIds: pumps.map((pump) => pump.id), dispensingPointIds: Object.values(loadedPoints).flat().map((point) => point.id) });
      })
      .catch((error) => { if (error?.name !== "AbortError") setDispensingPointsError(error?.message || t("dispensingPoints:feedback.pointsLoadFailed")); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingDispensingPoints(false); });
    return () => controller.abort();
  }, [activeStep, depots, dispensingPointsAttempt, organizationId, pumps, selectedProductIds, station?.id, t]);

  useEffect(() => {
    if (!["fuel-meters", "review"].includes(activeStep) || pumps.length === 0 || !station?.id) return undefined;
    const controller = new AbortController();
    Promise.resolve()
      .then(async () => {
        setIsLoadingFuelMeters(true);
        setFuelMetersError("");
        const pumpEntries = await Promise.all(pumps.filter((pump) => pump.meteringLevel === "PUMP").map(async (pump) => [pump.id, await getPumpFuelMeters(organizationId, station.id, pump.id, { signal: controller.signal })]));
        const pointContexts = pumps.filter((pump) => pump.meteringLevel === "DISPENSING_POINT").flatMap((pump) => (dispensingPointsByPump[pump.id] || []).map((point) => ({ pump, point })));
        const pointEntries = await Promise.all(pointContexts.map(async ({ pump, point }) => [point.id, await getDispensingPointFuelMeters(organizationId, station.id, pump.id, point.id, { signal: controller.signal })]));
        return { pumpEntries, pointEntries };
      })
      .then(({ pumpEntries, pointEntries }) => {
        const loadedPumpMeters = Object.fromEntries(pumpEntries.map(([pumpId, meters]) => [pumpId, Array.isArray(meters) ? meters : []]));
        const loadedPointMeters = Object.fromEntries(pointEntries.map(([pointId, meters]) => [pointId, Array.isArray(meters) ? meters : []]));
        setPumpMetersByPump(loadedPumpMeters);
        setPointMetersByPoint(loadedPointMeters);
        const fuelMeterIds = [...Object.values(loadedPumpMeters).flat(), ...Object.values(loadedPointMeters).flat()].map((meter) => meter.id);
        saveStationSetupDraft(organizationId, { stationId: station.id, activeStep, selectedProductIds, depotIds: depots.map((depot) => depot.id), pumpIds: pumps.map((pump) => pump.id), dispensingPointIds: Object.values(dispensingPointsByPump).flat().map((point) => point.id), fuelMeterIds });
      })
      .catch((error) => { if (error?.name !== "AbortError") setFuelMetersError(error?.message || t("fuelMeters:feedback.configurationLoadFailed")); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingFuelMeters(false); });
    return () => controller.abort();
  }, [activeStep, depots, dispensingPointsByPump, fuelMetersAttempt, organizationId, pumps, selectedProductIds, station?.id, t]);

  useEffect(() => {
    if (!["review", "commissioning"].includes(activeStep) || !station?.id) return undefined;
    const controller = new AbortController();
    Promise.resolve()
      .then(() => {
        setIsValidatingConfiguration(true);
        setConfigurationValidationError("");
        setConfigurationValidation(null);
        return validateStationConfiguration(organizationId, station.id, { signal: controller.signal });
      })
      .then(setConfigurationValidation)
      .catch((error) => { if (error?.name !== "AbortError") { setConfigurationValidation(null); setConfigurationValidationError(error?.message || t("wizard.configurationValidationFailed")); } })
      .finally(() => { if (!controller.signal.aborted) setIsValidatingConfiguration(false); });
    return () => controller.abort();
  }, [activeStep, configurationValidationAttempt, organizationId, station?.id, t]);

  const handleStationSubmit = async (payload) => {
    setIsSavingStation(true); setStationError(""); setStationSuccess("");
    try {
      const savedStation = station?.id ? await updateStation(organizationId, station.id, payload) : await createStation(organizationId, payload);
      setStation(savedStation);
      setStationSuccess(station?.id ? t("stations:feedback.updated") : t("wizard.stationCreated"));
      setActiveStep("products");
      saveStationSetupDraft(organizationId, { stationId: savedStation.id, activeStep: "products", selectedProductIds, depotIds: depots.map((depot) => depot.id) });
    } catch (error) { setStationError(error?.message || t("wizard.stationSaveFailed")); }
    finally { setIsSavingStation(false); }
  };

  const handleProductToggle = (productId) => setSelectedProductIds((currentIds) => {
    setProductsSavedMessage("");
    const updatedIds = currentIds.includes(productId) ? currentIds.filter((id) => id !== productId) : [...currentIds, productId];
    saveDraft({ activeStep: "products", selectedProductIds: updatedIds });
    return updatedIds;
  });

  const handleProductsSave = async () => {
    if (!station?.id || selectedProductIds.length === 0 || isSavingProducts) return;
    setIsSavingProducts(true);
    setProductsError("");
    setProductsSavedMessage("");
    try {
      const selectedIds = new Set(selectedProductIds);
      const existingByProductId = new Map(
        stationProducts.map((stationProduct) => [stationProduct.productId, stationProduct])
      );

      for (const [index, productId] of selectedProductIds.entries()) {
        const existing = existingByProductId.get(productId);
        if (!existing) {
          await createStationProduct(organizationId, station.id, {
            productId,
            displayOrder: index + 1,
          });
        } else if (!existing.active) {
          await updateStationProduct(organizationId, station.id, existing.id, {
            productId,
            displayOrder: existing.displayOrder || index + 1,
            active: true,
          });
        }
      }

      for (const existing of stationProducts) {
        if (existing.active && !selectedIds.has(existing.productId)) {
          await deactivateStationProduct(organizationId, station.id, existing.id);
        }
      }

      const refreshed = await getStationProducts(organizationId, station.id);
      setStationProducts(Array.isArray(refreshed) ? refreshed : []);
      saveDraft({ activeStep: "depots", selectedProductIds });
      setProductsSavedMessage(t("wizard.productsSaved"));
      setActiveStep("depots");
    } catch (error) {
      setProductsError(error?.message || t("wizard.productsSaveFailed"));
    } finally {
      setIsSavingProducts(false);
    }
  };

  const goToStep = (step) => { setActiveStep(step); saveDraft({ activeStep: step }); };
  const handleDepotSaved = (savedDepot, wasUpdate) => {
    setEditingDepot(undefined);
    setDepotsSuccess(t(wasUpdate ? "depots:feedback.updated" : "depots:feedback.created"));
    saveDraft({ activeStep: "depots", depotIds: [...new Set([...depots.map((depot) => depot.id), savedDepot.id])] });
    setDepotsAttempt((attempt) => attempt + 1);
  };
  const handleTankSaved = (savedTank, wasUpdate) => {
    setTankModalState(null);
    setTanksSuccess(t(wasUpdate ? "tanks:feedback.updated" : "tanks:feedback.created"));
    setTanksAttempt((attempt) => attempt + 1);
  };
  const handlePumpSaved = (savedPump, wasUpdate) => {
    setEditingPump(undefined);
    setPumpsSuccess(t(wasUpdate ? "pumps:feedback.updated" : "pumps:feedback.created"));
    saveDraft({ activeStep: "pumps", pumpIds: [...new Set([...pumps.map((pump) => pump.id), savedPump.id])] });
    setPumpsAttempt((attempt) => attempt + 1);
  };
  const handleDispensingPointSaved = (savedPoint, wasUpdate) => {
    setDispensingPointModalState(null);
    setDispensingPointsSuccess(t(wasUpdate ? "dispensingPoints:feedback.updated" : "dispensingPoints:feedback.created"));
    const currentIds = Object.values(dispensingPointsByPump).flat().map((point) => point.id);
    saveDraft({ activeStep: "dispensing-points", dispensingPointIds: [...new Set([...currentIds, savedPoint.id])] });
    setDispensingPointsAttempt((attempt) => attempt + 1);
  };
  const handleFuelMeterSaved = (savedMeter, wasUpdate) => {
    setFuelMeterModalState(null);
    setFuelMetersSuccess(t(wasUpdate ? "fuelMeters:feedback.updated" : "fuelMeters:feedback.created"));
    const currentIds = [...Object.values(pumpMetersByPump).flat(), ...Object.values(pointMetersByPoint).flat()].map((meter) => meter.id);
    saveDraft({ activeStep: "fuel-meters", fuelMeterIds: [...new Set([...currentIds, savedMeter.id])] });
    setFuelMetersAttempt((attempt) => attempt + 1);
  };

  const handleFinishCommissioning = async () => {
    if (!station?.id || selectedProductIds.length === 0 || isCompletingCommissioning) return;
    setIsCompletingCommissioning(true);
    setConfigurationValidationError("");
    try {
      const latestValidation = await validateStationConfiguration(organizationId, station.id);
      setConfigurationValidation(latestValidation);
      if (!latestValidation?.valid) {
        setConfigurationValidationError(t("wizard.configurationChanged"));
        return;
      }
      clearStationSetupDraft();
      setIsCommissioningComplete(true);
    } catch (error) {
      setConfigurationValidationError(error?.message || t("wizard.commissioningFailed"));
    } finally {
      setIsCompletingCommissioning(false);
    }
  };

  if (isRestoring) return <section className="station-setup-state-card"><LoaderCircle className="station-setup-spinner" size={34} /><h2>{t("wizard.restoringTitle")}</h2><p>{t("wizard.restoringDescription")}</p></section>;

  return (
    <section className="station-wizard-shell">
      <StationSetupStepper activeStep={activeStep} />
      {activeStep === "station" && <StationStep key={station?.id || "new-station"} station={station} isSaving={isSavingStation} errorMessage={stationError} successMessage={stationSuccess} onBack={() => { saveDraft({ activeStep: "station" }); onBackToPreparation(); }} onSubmit={handleStationSubmit} />}
      {activeStep === "products" && <ProductsStep products={products} selectedProductIds={selectedProductIds} isLoading={isLoadingProducts} isSaving={isSavingProducts} errorMessage={productsError} savedMessage={productsSavedMessage} onToggle={handleProductToggle} onBack={() => goToStep("station")} onRetry={() => setProductsAttempt((attempt) => attempt + 1)} onContinue={handleProductsSave} />}
      {activeStep === "depots" && <DepotStep depots={depots} isLoading={isLoadingDepots} errorMessage={depotsError} successMessage={depotsSuccess} onCreate={() => setEditingDepot(null)} onEdit={setEditingDepot} onBack={() => goToStep("products")} onContinue={() => goToStep("tanks")} onRetry={() => setDepotsAttempt((attempt) => attempt + 1)} />}
      {activeStep === "tanks" && <TankStep depots={depots} tanksByDepot={tanksByDepot} isLoading={isLoadingDepots || isLoadingProducts || isLoadingTanks} errorMessage={depotsError || productsError || tanksError} successMessage={tanksSuccess} onCreate={(depotId) => setTankModalState({ initialDepotId: depotId })} onEdit={(tank) => setTankModalState({ tank })} onBack={() => goToStep("depots")} onContinue={() => goToStep("pumps")} onRetry={() => { setDepotsAttempt((attempt) => attempt + 1); setProductsAttempt((attempt) => attempt + 1); setTanksAttempt((attempt) => attempt + 1); }} />}
      {activeStep === "pumps" && <PumpStep pumps={pumps} isLoading={isLoadingPumps} errorMessage={pumpsError} successMessage={pumpsSuccess} onCreate={() => setEditingPump(null)} onEdit={setEditingPump} onBack={() => goToStep("tanks")} onContinue={() => goToStep("dispensing-points")} onRetry={() => setPumpsAttempt((attempt) => attempt + 1)} />}
      {activeStep === "dispensing-points" && <DispensingPointStep pumps={pumps} dispensingPointsByPump={dispensingPointsByPump} tanks={tanks} isLoading={isLoadingPumps || isLoadingDepots || isLoadingTanks || isLoadingDispensingPoints} errorMessage={pumpsError || depotsError || tanksError || dispensingPointsError} successMessage={dispensingPointsSuccess} onCreate={(pump) => setDispensingPointModalState({ pump })} onEdit={(pump, dispensingPoint) => setDispensingPointModalState({ pump, dispensingPoint })} onBack={() => goToStep("pumps")} onContinue={() => goToStep("fuel-meters")} onRetry={() => { setPumpsAttempt((attempt) => attempt + 1); setDepotsAttempt((attempt) => attempt + 1); setTanksAttempt((attempt) => attempt + 1); setDispensingPointsAttempt((attempt) => attempt + 1); }} />}
      {activeStep === "fuel-meters" && <FuelMeterStep pumps={pumps} dispensingPointsByPump={dispensingPointsByPump} pumpMetersByPump={pumpMetersByPump} pointMetersByPoint={pointMetersByPoint} isLoading={isLoadingPumps || isLoadingDispensingPoints || isLoadingFuelMeters} errorMessage={pumpsError || dispensingPointsError || fuelMetersError} successMessage={fuelMetersSuccess} onConfigurePumpMeter={(pump, fuelMeter) => setFuelMeterModalState({ parentType: "PUMP", pump, fuelMeter })} onConfigurePointMeter={(pump, dispensingPoint, fuelMeter) => setFuelMeterModalState({ parentType: "DISPENSING_POINT", pump, dispensingPoint, fuelMeter })} onBack={() => goToStep("dispensing-points")} onContinue={() => goToStep("review")} onRetry={() => { setPumpsAttempt((attempt) => attempt + 1); setDispensingPointsAttempt((attempt) => attempt + 1); setFuelMetersAttempt((attempt) => attempt + 1); }} />}
      {activeStep === "review" && <ReviewStep station={station} products={selectedProducts} depots={depots} tanksByDepot={tanksByDepot} pumps={pumps} dispensingPointsByPump={dispensingPointsByPump} pumpMetersByPump={pumpMetersByPump} pointMetersByPoint={pointMetersByPoint} validation={configurationValidation} isLoading={isLoadingProducts || isLoadingDepots || isLoadingTanks || isLoadingPumps || isLoadingDispensingPoints || isLoadingFuelMeters || isValidatingConfiguration} errorMessage={productsError || depotsError || tanksError || pumpsError || dispensingPointsError || fuelMetersError || configurationValidationError} onBack={() => goToStep("fuel-meters")} onRetry={() => { setProductsAttempt((attempt) => attempt + 1); setDepotsAttempt((attempt) => attempt + 1); setTanksAttempt((attempt) => attempt + 1); setPumpsAttempt((attempt) => attempt + 1); setDispensingPointsAttempt((attempt) => attempt + 1); setFuelMetersAttempt((attempt) => attempt + 1); setConfigurationValidationAttempt((attempt) => attempt + 1); }} onGoToStep={goToStep} onContinue={() => goToStep("commissioning")} />}
      {activeStep === "commissioning" && <CommissioningStep station={station} selectedProductIds={selectedProductIds} validation={configurationValidation} isLoading={isValidatingConfiguration} isCompleting={isCompletingCommissioning} isCompleted={isCommissioningComplete} errorMessage={configurationValidationError} onBack={() => goToStep("review")} onRetry={() => setConfigurationValidationAttempt((attempt) => attempt + 1)} onFinish={handleFinishCommissioning} />}
      {editingDepot !== undefined && <DepotModal key={editingDepot?.id || "new-depot"} isOpen organizationId={organizationId} stationId={station.id} depot={editingDepot} onClose={() => setEditingDepot(undefined)} onSaved={handleDepotSaved} />}
      {tankModalState && <TankModal key={tankModalState.tank?.id || `new-${tankModalState.initialDepotId || "tank"}`} isOpen organizationId={organizationId} stationId={station.id} depots={depots} products={selectedProducts} tank={tankModalState.tank} initialDepotId={tankModalState.initialDepotId} onClose={() => setTankModalState(null)} onSaved={handleTankSaved} />}
      {editingPump !== undefined && <PumpModal key={editingPump?.id || "new-pump"} isOpen organizationId={organizationId} stationId={station.id} pump={editingPump} onClose={() => setEditingPump(undefined)} onSaved={handlePumpSaved} />}
      {dispensingPointModalState && <DispensingPointModal key={dispensingPointModalState.dispensingPoint?.id || "new-dispensing-point"} isOpen organizationId={organizationId} stationId={station.id} pump={dispensingPointModalState.pump} tanks={tanks} dispensingPoint={dispensingPointModalState.dispensingPoint} onClose={() => setDispensingPointModalState(null)} onSaved={handleDispensingPointSaved} />}
      {fuelMeterModalState && <FuelMeterModal key={fuelMeterModalState.fuelMeter?.id || "new-fuel-meter"} isOpen organizationId={organizationId} stationId={station.id} parentType={fuelMeterModalState.parentType} pump={fuelMeterModalState.pump} dispensingPoint={fuelMeterModalState.dispensingPoint} fuelMeter={fuelMeterModalState.fuelMeter} onClose={() => setFuelMeterModalState(null)} onSaved={handleFuelMeterSaved} />}
    </section>
  );
}
export default StationWizard;
