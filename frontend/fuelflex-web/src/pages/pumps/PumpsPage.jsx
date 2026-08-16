import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCircle, Building2, CheckCircle2, Fuel, Gauge, LoaderCircle, MapPin, Pencil, Plus, RefreshCw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import PumpModal from "../../features/pump/components/PumpModal";
import { getLocaleForLanguage } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import { getPumps } from "../../services/pump/pumpService";
import { getStations } from "../../services/station/stationService";
import "./PumpsPage.css";

function PageState({ icon: Icon = Fuel, title, text, action, compact = false }) {
  return <section className={`pumps-page-state ${title ? "empty" : ""} ${compact ? "compact" : ""}`}>{Icon && <Icon className={Icon === LoaderCircle ? "pumps-page-spinner" : ""} size={30} />}{title && <h2>{title}</h2>}<p>{text}</p>{action}</section>;
}

function PumpsPage() {
  const { t, i18n } = useTranslation(["pumps", "common"]);
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [selectedStationId, setSelectedStationId] = useState("");
  const [pumps, setPumps] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [pumpModal, setPumpModal] = useState(undefined);
  const [isLoadingStations, setIsLoadingStations] = useState(Boolean(organizationId));
  const [isLoadingPumps, setIsLoadingPumps] = useState(false);
  const [stationsError, setStationsError] = useState(organizationId ? null : { key: "pumps:feedback.organizationMissing" });
  const [pumpsError, setPumpsError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [stationsAttempt, setStationsAttempt] = useState(0);
  const [pumpsAttempt, setPumpsAttempt] = useState(0);
  const locale = getLocaleForLanguage(i18n.resolvedLanguage);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingStations(true); setStationsError(null); return getStations(organizationId, { signal: controller.signal }); })
      .then((result) => { const loaded = Array.isArray(result) ? result : []; setStations(loaded); setSelectedStationId((current) => loaded.length === 1 ? loaded[0].id : loaded.some(({ id }) => id === current) ? current : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setStationsError(error?.message ? { text: error.message } : { key: "pumps:feedback.stationsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingStations(false); });
    return () => controller.abort();
  }, [organizationId, stationsAttempt]);

  useEffect(() => {
    if (!organizationId || !selectedStationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingPumps(true); setPumpsError(null); return getPumps(organizationId, selectedStationId, { signal: controller.signal }); })
      .then((result) => setPumps(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setPumpsError(error?.message ? { text: error.message } : { key: "pumps:feedback.pumpsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingPumps(false); });
    return () => controller.abort();
  }, [organizationId, pumpsAttempt, selectedStationId]);

  const selectedStation = stations.find(({ id }) => id === selectedStationId) || null;
  const filteredPumps = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase(locale);
    if (!query) return pumps;
    return pumps.filter((pump) => [pump.name, pump.code, String(pump.pumpNumber), pump.manufacturer, pump.model].some((value) => value?.toLocaleLowerCase(locale).includes(query)));
  }, [locale, pumps, searchTerm]);

  const handleStationChange = (event) => { setSelectedStationId(event.target.value); setPumps([]); setSearchTerm(""); setPumpModal(undefined); setSuccessMessage(null); setPumpsError(null); };
  const handlePumpSaved = (_, wasUpdate) => { setPumpModal(undefined); setSuccessMessage({ key: wasUpdate ? "pumps:feedback.updated" : "pumps:feedback.created" }); setPumpsAttempt((attempt) => attempt + 1); };
  const openCreate = () => { setSuccessMessage(null); setPumpModal(null); };
  const retryLabel = t("common:actions.retry");

  return <SupervisorLayout><main className="pumps-page">
    <header className="pumps-page-header"><div><span>{t("pumps:page.eyebrow")}</span><h1>{t("pumps:page.title")}</h1><p>{t("pumps:page.description")}</p></div>{selectedStation && <button type="button" className="pumps-page-primary" onClick={openCreate}><Plus size={17} />{t("pumps:page.add")}</button>}</header>
    {successMessage && <div className="pumps-page-alert success" role="status"><CheckCircle2 size={18} />{renderMessage(successMessage)}</div>}
    {stationsError && <div className="pumps-page-alert error" role="alert"><AlertCircle size={18} /><span>{renderMessage(stationsError)}</span>{organizationId && <button type="button" onClick={() => setStationsAttempt((value) => value + 1)}><RefreshCw size={15} />{retryLabel}</button>}</div>}
    {isLoadingStations ? <PageState icon={LoaderCircle} text={t("pumps:page.loadingStations")} /> : !stationsError && stations.length === 0 ? <PageState icon={Building2} title={t("pumps:page.noStationTitle")} text={t("pumps:page.noStationDescription")} /> : !stationsError && <>
      <section className="pumps-page-station-selector"><label><span>{t("pumps:page.station")}</span><select value={selectedStationId} onChange={handleStationChange}><option value="">{t("pumps:page.selectStation")}</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label>{stations.length > 1 && !selectedStationId && <p>{t("pumps:page.stationHelp")}</p>}</section>
      {!selectedStationId ? <PageState title={t("pumps:page.noSelectionTitle")} text={t("pumps:page.noSelectionDescription")} /> : <>
        {pumpsError && <div className="pumps-page-alert error" role="alert"><AlertCircle size={18} /><span>{renderMessage(pumpsError)}</span><button type="button" onClick={() => setPumpsAttempt((value) => value + 1)}><RefreshCw size={15} />{retryLabel}</button></div>}
        {!pumpsError && !isLoadingPumps && pumps.length > 0 && <div className="pumps-page-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder={t("pumps:page.searchPlaceholder")} aria-label={t("pumps:page.searchAriaLabel")} /></label><span>{t("pumps:page.count", { count: filteredPumps.length })}</span></div>}
        {isLoadingPumps ? <PageState icon={LoaderCircle} text={t("pumps:page.loadingPumps")} /> : !pumpsError && (pumps.length === 0 ? <PageState title={t("pumps:page.emptyTitle")} text={t("pumps:page.emptyDescription", { stationName: selectedStation?.name || t("pumps:page.stationFallback") })} action={<button type="button" className="pumps-page-primary" onClick={openCreate}><Plus size={17} />{t("pumps:page.add")}</button>} /> : filteredPumps.length === 0 ? <PageState icon={Search} title={t("pumps:page.noResultTitle")} text={t("pumps:page.noResultDescription")} compact /> : <section className="pumps-page-grid">{filteredPumps.map((pump) => <article key={pump.id} className={!pump.active ? "inactive" : ""}><div className="pumps-page-card-heading"><span><Fuel size={20} /></span><div><small>{pump.code}</small><h2>{pump.name}</h2><p>{t("pumps:page.pumpNumber", { number: pump.pumpNumber })}</p></div></div><div className={`pumps-page-metering ${pump.meteringLevel === "DISPENSING_POINT" ? "point" : "pump"}`}><Gauge size={18} /><div><strong>{t(`pumps:meteringLevel.${pump.meteringLevel}.title`, { defaultValue: pump.meteringLevel })}</strong><span>{t(`pumps:meteringLevel.${pump.meteringLevel}.detail`, { defaultValue: "" })}</span></div></div>{pump.location && <p className="pumps-page-location"><MapPin size={15} />{pump.location}</p>}<div className="pumps-page-badges"><em className={`status ${(pump.status || "").toLowerCase()}`}>{t(`pumps:status.${pump.status}`, { defaultValue: pump.status })}</em><em className={pump.active ? "active" : "inactive"}>{t(pump.active ? "pumps:availability.active" : "pumps:availability.inactive")}</em></div><div className="pumps-page-actions"><button type="button" onClick={() => { setSuccessMessage(null); setPumpModal(pump); }}><Pencil size={15} />{t("pumps:page.edit")}</button></div></article>)}</section>)}</>}
    </>}
  </main>{pumpModal !== undefined && selectedStation && <PumpModal key={pumpModal?.id || "new-pump"} isOpen organizationId={organizationId} stationId={selectedStation.id} pump={pumpModal} onClose={() => setPumpModal(undefined)} onSaved={handlePumpSaved} />}</SupervisorLayout>;
}

export default PumpsPage;
