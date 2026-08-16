import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCircle, Building2, CheckCircle2, CircleGauge, LoaderCircle, Pencil, Plus, RefreshCw, Search, Warehouse } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import TankModal from "../../features/tank/components/TankModal";
import { formatNumber, getLocaleForLanguage } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import { getDepots } from "../../services/depot/depotService";
import { getProducts } from "../../services/product/productService";
import { getStations } from "../../services/station/stationService";
import { getTanks } from "../../services/tank/tankService";
import "./TanksPage.css";

function PageState({ icon, title, text, action, compact = false }) {
  const Icon = icon === "station" ? Building2 : icon === "depot" ? Warehouse : icon === "tank" ? CircleGauge : icon === "search" ? Search : null;
  return <section className={`tanks-page-state ${title ? "empty" : ""} ${compact ? "compact" : ""}`}>{icon === "loading" ? <LoaderCircle className="tanks-page-spinner" size={30} /> : Icon ? <Icon size={32} /> : null}{title && <h2>{title}</h2>}<p>{text}</p>{action}</section>;
}

function ErrorState({ message, onRetry, retryLabel }) {
  return <div className="tanks-page-alert error" role="alert"><AlertCircle size={18} /><span>{message}</span><button type="button" onClick={onRetry}><RefreshCw size={15} />{retryLabel}</button></div>;
}

function TanksPage() {
  const { t, i18n } = useTranslation(["tanks", "common"]);
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [selectedStationId, setSelectedStationId] = useState("");
  const [depots, setDepots] = useState([]);
  const [selectedDepotId, setSelectedDepotId] = useState("");
  const [products, setProducts] = useState([]);
  const [tanks, setTanks] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [tankModal, setTankModal] = useState(undefined);
  const [isLoadingStations, setIsLoadingStations] = useState(Boolean(organizationId));
  const [isLoadingDepots, setIsLoadingDepots] = useState(false);
  const [isLoadingProducts, setIsLoadingProducts] = useState(Boolean(organizationId));
  const [isLoadingTanks, setIsLoadingTanks] = useState(false);
  const [stationsError, setStationsError] = useState(organizationId ? null : { key: "tanks:feedback.organizationMissing" });
  const [depotsError, setDepotsError] = useState(null);
  const [productsError, setProductsError] = useState(null);
  const [tanksError, setTanksError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [stationsAttempt, setStationsAttempt] = useState(0);
  const [depotsAttempt, setDepotsAttempt] = useState(0);
  const [productsAttempt, setProductsAttempt] = useState(0);
  const [tanksAttempt, setTanksAttempt] = useState(0);
  const language = i18n.resolvedLanguage;
  const locale = getLocaleForLanguage(language);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";
  const formatLiters = (value) => t("tanks:page.liters", { value: formatNumber(Number(value) || 0, { language, maximumFractionDigits: 3 }) });

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingStations(true); setStationsError(null); return getStations(organizationId, { signal: controller.signal }); })
      .then((result) => { const loaded = Array.isArray(result) ? result : []; setStations(loaded); setSelectedStationId((current) => loaded.length === 1 ? loaded[0].id : loaded.some(({ id }) => id === current) ? current : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setStationsError(error?.message ? { text: error.message } : { key: "tanks:feedback.stationsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingStations(false); });
    return () => controller.abort();
  }, [organizationId, stationsAttempt]);

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingProducts(true); setProductsError(null); return getProducts(organizationId, { signal: controller.signal }); })
      .then((result) => setProducts(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setProductsError(error?.message ? { text: error.message } : { key: "tanks:feedback.productsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingProducts(false); });
    return () => controller.abort();
  }, [organizationId, productsAttempt]);

  useEffect(() => {
    if (!organizationId || !selectedStationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingDepots(true); setDepotsError(null); return getDepots(organizationId, selectedStationId, { signal: controller.signal }); })
      .then((result) => { const loaded = Array.isArray(result) ? result : []; setDepots(loaded); setSelectedDepotId((current) => loaded.length === 1 ? loaded[0].id : loaded.some(({ id }) => id === current) ? current : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setDepotsError(error?.message ? { text: error.message } : { key: "tanks:feedback.depotsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingDepots(false); });
    return () => controller.abort();
  }, [depotsAttempt, organizationId, selectedStationId]);

  useEffect(() => {
    if (!organizationId || !selectedStationId || !selectedDepotId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingTanks(true); setTanksError(null); return getTanks(organizationId, selectedStationId, selectedDepotId, { signal: controller.signal }); })
      .then((result) => setTanks(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setTanksError(error?.message ? { text: error.message } : { key: "tanks:feedback.tanksLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingTanks(false); });
    return () => controller.abort();
  }, [organizationId, selectedDepotId, selectedStationId, tanksAttempt]);

  const selectedStation = stations.find(({ id }) => id === selectedStationId) || null;
  const selectedDepot = depots.find(({ id }) => id === selectedDepotId) || null;
  const activeProducts = useMemo(() => products.filter((product) => product.active), [products]);
  const modalProducts = useMemo(() => {
    if (!tankModal?.productId) return activeProducts;
    const current = products.find(({ id }) => id === tankModal.productId);
    return current && !current.active ? [...activeProducts, current] : activeProducts;
  }, [activeProducts, products, tankModal]);
  const filteredTanks = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase(locale);
    return query ? tanks.filter((tank) => [tank.name, tank.code, tank.productName, tank.location].some((value) => value?.toLocaleLowerCase(locale).includes(query))) : tanks;
  }, [locale, searchTerm, tanks]);

  const handleStationChange = (event) => { setSelectedStationId(event.target.value); setSelectedDepotId(""); setDepots([]); setTanks([]); setSearchTerm(""); setTankModal(undefined); setSuccessMessage(null); setDepotsError(null); setTanksError(null); };
  const handleDepotChange = (event) => { setSelectedDepotId(event.target.value); setTanks([]); setSearchTerm(""); setTankModal(undefined); setSuccessMessage(null); setTanksError(null); };
  const handleTankSaved = (_, wasUpdate) => { setTankModal(undefined); setSuccessMessage({ key: wasUpdate ? "tanks:feedback.updated" : "tanks:feedback.created" }); setTanksAttempt((attempt) => attempt + 1); };
  const openCreate = () => { setSuccessMessage(null); setTankModal(null); };
  const canCreate = selectedStation && selectedDepot && !isLoadingProducts && !productsError && activeProducts.length > 0;
  const retryLabel = t("common:actions.retry");

  return <SupervisorLayout><main className="tanks-page">
    <header className="tanks-page-header"><div><span>{t("tanks:page.eyebrow")}</span><h1>{t("tanks:page.title")}</h1><p>{t("tanks:page.description")}</p></div>{canCreate && <button type="button" className="tanks-page-primary" onClick={openCreate}><Plus size={17} />{t("tanks:page.add")}</button>}</header>
    {successMessage && <div className="tanks-page-alert success" role="status"><CheckCircle2 size={18} />{renderMessage(successMessage)}</div>}
    {stationsError && <div className="tanks-page-alert error" role="alert"><AlertCircle size={18} /><span>{renderMessage(stationsError)}</span>{organizationId && <button type="button" onClick={() => setStationsAttempt((value) => value + 1)}><RefreshCw size={15} />{retryLabel}</button>}</div>}
    {isLoadingStations ? <PageState icon="loading" text={t("tanks:page.loadingStations")} /> : !stationsError && stations.length === 0 ? <PageState icon="station" title={t("tanks:page.noStationTitle")} text={t("tanks:page.noStationDescription")} /> : !stationsError && <>
      <section className="tanks-page-selectors"><label><span>{t("tanks:page.station")}</span><select value={selectedStationId} onChange={handleStationChange}><option value="">{t("tanks:page.selectStation")}</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label><label><span>{t("tanks:page.depot")}</span><select value={selectedDepotId} onChange={handleDepotChange} disabled={!selectedStationId || isLoadingDepots || Boolean(depotsError)}><option value="">{isLoadingDepots ? t("tanks:page.loading") : t("tanks:page.selectDepot")}</option>{depots.map((depot) => <option key={depot.id} value={depot.id}>{depot.name} — {depot.code}</option>)}</select></label></section>
      {!selectedStationId ? <PageState title={t("tanks:page.noStationSelectedTitle")} text={t("tanks:page.noStationSelectedDescription")} /> : isLoadingDepots ? <PageState icon="loading" text={t("tanks:page.loadingDepots")} /> : depotsError ? <ErrorState message={renderMessage(depotsError)} onRetry={() => setDepotsAttempt((value) => value + 1)} retryLabel={retryLabel} /> : depots.length === 0 ? <PageState icon="depot" title={t("tanks:page.noDepotTitle")} text={t("tanks:page.noDepotDescription")} /> : !selectedDepotId ? <PageState icon="depot" title={t("tanks:page.noDepotSelectedTitle")} text={t("tanks:page.noDepotSelectedDescription")} /> : <>
        {isLoadingProducts ? <div className="tanks-page-inline-state"><LoaderCircle className="tanks-page-spinner" size={18} />{t("tanks:page.loadingProducts")}</div> : productsError ? <div className="tanks-page-alert error" role="alert"><AlertCircle size={18} /><span>{renderMessage(productsError)}</span><button type="button" onClick={() => setProductsAttempt((value) => value + 1)}><RefreshCw size={15} />{retryLabel}</button></div> : activeProducts.length === 0 && <div className="tanks-page-alert warning" role="status"><AlertCircle size={18} />{t("tanks:page.noActiveProduct")}</div>}
        {tanksError && <ErrorState message={renderMessage(tanksError)} onRetry={() => setTanksAttempt((value) => value + 1)} retryLabel={retryLabel} />}
        {!tanksError && !isLoadingTanks && tanks.length > 0 && <div className="tanks-page-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder={t("tanks:page.searchPlaceholder")} aria-label={t("tanks:page.searchAriaLabel")} /></label><span>{t("tanks:page.count", { count: filteredTanks.length })}</span></div>}
        {isLoadingTanks ? <PageState icon="loading" text={t("tanks:page.loadingTanks")} /> : !tanksError && (tanks.length === 0 ? <PageState icon="tank" title={t("tanks:page.emptyTitle")} text={t("tanks:page.emptyDescription", { depotName: selectedDepot?.name || t("tanks:page.selectedDepotFallback") })} action={canCreate ? <button type="button" className="tanks-page-primary" onClick={openCreate}><Plus size={17} />{t("tanks:page.add")}</button> : null} /> : filteredTanks.length === 0 ? <PageState icon="search" title={t("tanks:page.noResultTitle")} text={t("tanks:page.noResultDescription")} compact /> : <section className="tanks-page-grid">{filteredTanks.map((tank) => <article key={tank.id} className={!tank.active ? "inactive" : ""}><div className="tanks-page-card-heading"><span><CircleGauge size={20} /></span><div><small>{tank.code}</small><h2>{tank.name}</h2><p>{tank.productName}</p></div></div><div className="tanks-page-levels"><strong>{formatLiters(tank.capacityLiters)}</strong><span>{t("tanks:page.minimumShort")} {formatLiters(tank.minimumLevelLiters)} · {t("tanks:page.maximumShort")} {formatLiters(tank.maximumLevelLiters)}</span></div><div className="tanks-page-badges"><em className={`status ${(tank.status || "").toLowerCase()}`}>{t(`tanks:status.${tank.status}`, { defaultValue: tank.status })}</em><em className={tank.active ? "active" : "inactive"}>{t(tank.active ? "tanks:availability.active" : "tanks:availability.inactive")}</em></div><div className="tanks-page-actions"><button type="button" onClick={() => { setSuccessMessage(null); setTankModal(tank); }}><Pencil size={15} />{t("tanks:page.edit")}</button></div></article>)}</section>)}</>}
    </>}
  </main>{tankModal !== undefined && selectedStation && selectedDepot && <TankModal key={tankModal?.id || "new-tank"} isOpen organizationId={organizationId} stationId={selectedStation.id} depots={[selectedDepot]} products={modalProducts} tank={tankModal} fixedDepotId={selectedDepot.id} onClose={() => setTankModal(undefined)} onSaved={handleTankSaved} />}</SupervisorLayout>;
}

export default TanksPage;
