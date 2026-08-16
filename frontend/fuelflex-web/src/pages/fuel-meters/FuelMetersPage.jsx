import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCircle, Building2, CheckCircle2, Fuel, Gauge, LoaderCircle, Pencil, Plus, RefreshCw, Search } from "lucide-react";
import { Link } from "react-router-dom";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import FuelMeterModal from "../../features/fuel-meter/components/FuelMeterModal";
import { formatNumber, getLocaleForLanguage } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import { getDispensingPoints } from "../../services/dispensingPoint/dispensingPointService";
import { getDispensingPointFuelMeters, getPumpFuelMeters } from "../../services/fuelMeter/fuelMeterService";
import { getPumps } from "../../services/pump/pumpService";
import { getStations } from "../../services/station/stationService";
import "./FuelMetersPage.css";


function PageState({ icon: Icon = Gauge, title, text, action, compact = false }) {
  return <section className={`fuel-meters-page-state ${title ? "empty" : ""} ${compact ? "compact" : ""}`}><Icon className={Icon === LoaderCircle ? "fuel-meters-page-spinner" : ""} size={30} />{title && <h2>{title}</h2>}<p>{text}</p>{action}</section>;
}

function ErrorState({ message, onRetry, retryLabel }) {
  return <div className="fuel-meters-page-alert error" role="alert"><AlertCircle size={18} /><span>{message}</span>{onRetry && <button type="button" onClick={onRetry}><RefreshCw size={15} />{retryLabel}</button>}</div>;
}

function MeterDetails({ meter, t, language }) {
  return <div className="fuel-meters-page-meter-details"><div><small>{t("fuelMeters:page.code")}</small><strong>{meter.code}</strong></div><div><small>{t("fuelMeters:page.technology")}</small><strong>{t(`fuelMeters:technology.${meter.technology}`, { defaultValue: meter.technology })}</strong></div><div><small>{t("fuelMeters:page.currentIndex")}</small><strong>{formatNumber(Number(meter.currentIndex) || 0, { language, minimumFractionDigits: 3, maximumFractionDigits: 3 })}</strong></div></div>;
}

function FuelMetersPage() {
  const { t, i18n } = useTranslation(["fuelMeters", "common"]);
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [stationId, setStationId] = useState("");
  const [pumps, setPumps] = useState([]);
  const [pumpId, setPumpId] = useState("");
  const [points, setPoints] = useState([]);
  const [pumpMeters, setPumpMeters] = useState([]);
  const [pointMeters, setPointMeters] = useState({});
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState(null);
  const [loading, setLoading] = useState({ stations: Boolean(organizationId), pumps: false, points: false, meters: false });
  const [errors, setErrors] = useState({ stations: organizationId ? null : { key: "fuelMeters:feedback.organizationMissing" }, pumps: null, points: null, meters: null });
  const [attempts, setAttempts] = useState({ stations: 0, pumps: 0, data: 0 });
  const [success, setSuccess] = useState(null);
  const setBusy = (key, value) => setLoading((current) => ({ ...current, [key]: value }));
  const setError = (key, value) => setErrors((current) => ({ ...current, [key]: value }));
  const retry = (key) => setAttempts((current) => ({ ...current, [key]: current[key] + 1 }));
  const language = i18n.resolvedLanguage;
  const locale = getLocaleForLanguage(language);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";
  const retryLabel = t("common:actions.retry");

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("stations", true); setError("stations", null); return getStations(organizationId, { signal: controller.signal }); })
      .then((value) => { const loaded = Array.isArray(value) ? value : []; setStations(loaded); setStationId(loaded.length === 1 ? loaded[0].id : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setError("stations", error?.message ? { text: error.message } : { key: "fuelMeters:feedback.stationsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setBusy("stations", false); });
    return () => controller.abort();
  }, [attempts.stations, organizationId]);

  useEffect(() => {
    if (!organizationId || !stationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("pumps", true); setError("pumps", null); return getPumps(organizationId, stationId, { signal: controller.signal }); })
      .then((value) => { const loaded = Array.isArray(value) ? value : []; setPumps(loaded); setPumpId(loaded.length === 1 ? loaded[0].id : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setError("pumps", error?.message ? { text: error.message } : { key: "fuelMeters:feedback.pumpsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setBusy("pumps", false); });
    return () => controller.abort();
  }, [attempts.pumps, organizationId, stationId]);

  const pump = pumps.find(({ id }) => id === pumpId) || null;

  useEffect(() => {
    if (!organizationId || !stationId || !pump) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(async () => {
      setError("points", null); setError("meters", null); setPumpMeters([]); setPointMeters({});
      if (pump.meteringLevel === "PUMP") {
        setBusy("meters", true);
        const meters = await getPumpFuelMeters(organizationId, stationId, pump.id, { signal: controller.signal });
        return { pumpMeters: Array.isArray(meters) ? meters : [], points: [], pointMeters: {} };
      }
      setBusy("points", true); setBusy("meters", true);
      const loadedPoints = await getDispensingPoints(organizationId, stationId, pump.id, { signal: controller.signal });
      const safePoints = Array.isArray(loadedPoints) ? loadedPoints : [];
      setBusy("points", false);
      const entries = await Promise.all(safePoints.map(async (point) => [point.id, await getDispensingPointFuelMeters(organizationId, stationId, pump.id, point.id, { signal: controller.signal })]));
      return { pumpMeters: [], points: safePoints, pointMeters: Object.fromEntries(entries.map(([id, meters]) => [id, Array.isArray(meters) ? meters : []])) };
    }).then((result) => { setPumpMeters(result.pumpMeters); setPoints(result.points); setPointMeters(result.pointMeters); })
      .catch((error) => { if (error?.name !== "AbortError") { const key = pump.meteringLevel === "PUMP" ? "meters" : "points"; setError(key, error?.message ? { text: error.message } : { key: "fuelMeters:feedback.configurationLoadFailed" }); } })
      .finally(() => { if (!controller.signal.aborted) { setBusy("points", false); setBusy("meters", false); } });
    return () => controller.abort();
  }, [attempts.data, organizationId, pump, stationId]);

  const activePumpMeter = pumpMeters.find((meter) => meter.active) || null;
  const filteredPoints = useMemo(() => { const query = search.trim().toLocaleLowerCase(locale); if (!query) return points; return points.filter((point) => { const meter = (pointMeters[point.id] || []).find((item) => item.active); return [point.name, point.code, String(point.nozzleNumber), meter?.code, meter?.name].some((value) => value?.toLocaleLowerCase(locale).includes(query)); }); }, [locale, pointMeters, points, search]);
  const changeStation = (event) => { setStationId(event.target.value); setPumps([]); setPumpId(""); setPoints([]); setPumpMeters([]); setPointMeters({}); setSearch(""); setModal(null); setSuccess(null); setErrors((current) => ({ ...current, pumps: null, points: null, meters: null })); };
  const changePump = (event) => { setPumpId(event.target.value); setPoints([]); setPumpMeters([]); setPointMeters({}); setSearch(""); setModal(null); setSuccess(null); setErrors((current) => ({ ...current, points: null, meters: null })); };
  const openPumpModal = (meter = null) => setModal({ parentType: "PUMP", fuelMeter: meter });
  const openPointModal = (point, meter = null) => setModal({ parentType: "DISPENSING_POINT", dispensingPoint: point, fuelMeter: meter });
  const saved = (_, updated) => { setModal(null); setSuccess({ key: updated ? "fuelMeters:feedback.updated" : "fuelMeters:feedback.created" }); retry("data"); };

  return <SupervisorLayout><main className="fuel-meters-page">
    <header className="fuel-meters-page-header"><div><span>{t("fuelMeters:page.eyebrow")}</span><h1>{t("fuelMeters:page.title")}</h1><p>{t("fuelMeters:page.description")}</p></div></header>
    {success && <div className="fuel-meters-page-alert success" role="status"><CheckCircle2 size={18} />{renderMessage(success)}</div>}
    {errors.stations && <ErrorState message={renderMessage(errors.stations)} onRetry={organizationId ? () => retry("stations") : undefined} retryLabel={retryLabel} />}
    {loading.stations ? <PageState icon={LoaderCircle} text={t("fuelMeters:page.loadingStations")} /> : !errors.stations && stations.length === 0 ? <PageState icon={Building2} title={t("fuelMeters:page.noStationTitle")} text={t("fuelMeters:page.noStationDescription")} /> : !errors.stations && <>
      <section className="fuel-meters-page-selectors"><label><span>{t("fuelMeters:page.station")}</span><select value={stationId} onChange={changeStation}><option value="">{t("fuelMeters:page.selectStation")}</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label><label><span>{t("fuelMeters:page.pump")}</span><select value={pumpId} onChange={changePump} disabled={!stationId || loading.pumps || Boolean(errors.pumps)}><option value="">{loading.pumps ? t("fuelMeters:page.loading") : t("fuelMeters:page.selectPump")}</option>{pumps.map((item) => <option key={item.id} value={item.id}>{item.name} — {item.code}</option>)}</select></label></section>
      {!stationId ? <PageState icon={Building2} title={t("fuelMeters:page.noStationSelectedTitle")} text={t("fuelMeters:page.noStationSelectedDescription")} /> : loading.pumps ? <PageState icon={LoaderCircle} text={t("fuelMeters:page.loadingPumps")} /> : errors.pumps ? <ErrorState message={renderMessage(errors.pumps)} onRetry={() => retry("pumps")} retryLabel={retryLabel} /> : pumps.length === 0 ? <PageState icon={Fuel} title={t("fuelMeters:page.noPumpTitle")} text={t("fuelMeters:page.noPumpDescription")} /> : !pumpId ? <PageState icon={Fuel} title={t("fuelMeters:page.noPumpSelectedTitle")} text={t("fuelMeters:page.noPumpSelectedDescription")} /> : <>
        <section className={`fuel-meters-page-mode ${pump.meteringLevel === "DISPENSING_POINT" ? "point" : ""}`}><Gauge size={21} /><div><small>{pump.code} · {t("fuelMeters:page.pumpNumber", { number: pump.pumpNumber })}</small><strong>{t(`fuelMeters:meteringLevel.${pump.meteringLevel}.title`, { defaultValue: pump.meteringLevel })}</strong><p>{t(`fuelMeters:meteringLevel.${pump.meteringLevel}.detail`, { defaultValue: "" })}</p></div></section>
        {errors.points && <ErrorState message={renderMessage(errors.points)} onRetry={() => retry("data")} retryLabel={retryLabel} />}{errors.meters && <ErrorState message={renderMessage(errors.meters)} onRetry={() => retry("data")} retryLabel={retryLabel} />}
        {!errors.points && !errors.meters && pump.meteringLevel === "PUMP" && (loading.meters ? <PageState icon={LoaderCircle} text={t("fuelMeters:page.loadingGlobalMeter")} /> : activePumpMeter ? <section className="fuel-meters-page-global-card"><div className="fuel-meters-page-card-heading"><span><Gauge size={22} /></span><div><small>{t("fuelMeters:page.globalConfigured")}</small><h2>{activePumpMeter.name}</h2></div></div><MeterDetails meter={activePumpMeter} t={t} language={language} /><div className="fuel-meters-page-badges"><em className={`status ${(activePumpMeter.status || "").toLowerCase()}`}>{t(`fuelMeters:status.${activePumpMeter.status}`, { defaultValue: activePumpMeter.status })}</em><em className={activePumpMeter.active ? "active" : "inactive"}>{t(activePumpMeter.active ? "fuelMeters:availability.active" : "fuelMeters:availability.inactive")}</em></div><button type="button" className="fuel-meters-page-edit" onClick={() => openPumpModal(activePumpMeter)}><Pencil size={15} />{t("fuelMeters:page.edit")}</button></section> : <PageState title={t("fuelMeters:page.globalMissingTitle")} text={t("fuelMeters:page.globalMissingDescription")} action={<button type="button" className="fuel-meters-page-primary" onClick={() => openPumpModal()}><Plus size={17} />{t("fuelMeters:page.addMeter")}</button>} />)}
        {!errors.points && !errors.meters && pump.meteringLevel === "DISPENSING_POINT" && (loading.points ? <PageState icon={LoaderCircle} text={t("fuelMeters:page.loadingPoints")} /> : points.length === 0 ? <PageState title={t("fuelMeters:page.noPointsTitle")} text={t("fuelMeters:page.noPointsDescription")} action={<Link className="fuel-meters-page-primary" to="/superviseur/pistolets">{t("fuelMeters:page.managePoints")}</Link>} /> : loading.meters ? <PageState icon={LoaderCircle} text={t("fuelMeters:page.loadingIndividualMeters")} /> : <><div className="fuel-meters-page-toolbar"><label><Search size={17} /><input type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder={t("fuelMeters:page.searchPlaceholder")} aria-label={t("fuelMeters:page.searchAriaLabel")} /></label><span>{t("fuelMeters:page.pointCount", { count: filteredPoints.length })}</span></div>{filteredPoints.length === 0 ? <PageState icon={Search} title={t("fuelMeters:page.noResultTitle")} text={t("fuelMeters:page.noResultDescription")} compact /> : <section className="fuel-meters-page-point-grid">{filteredPoints.map((point) => { const meter = (pointMeters[point.id] || []).find((item) => item.active) || null; return <article key={point.id} className={!point.active ? "inactive" : ""}><header><div><small>{point.code} · {t("fuelMeters:page.nozzleNumber", { number: point.nozzleNumber })}</small><h2>{point.name}</h2><p>{t("fuelMeters:page.tank", { name: point.tankName || point.tankCode || t("fuelMeters:page.tankUnknown") })}</p></div><em>{t(point.active ? "fuelMeters:availability.active" : "fuelMeters:availability.inactive")}</em></header>{meter ? <><strong className="fuel-meters-page-configured">{t("fuelMeters:page.configured")}</strong><h3>{meter.name}</h3><MeterDetails meter={meter} t={t} language={language} /><div className="fuel-meters-page-badges"><em className={`status ${(meter.status || "").toLowerCase()}`}>{t(`fuelMeters:status.${meter.status}`, { defaultValue: meter.status })}</em><em className="active">{t("fuelMeters:availability.active")}</em></div><button type="button" className="fuel-meters-page-edit" onClick={() => openPointModal(point, meter)}><Pencil size={15} />{t("fuelMeters:page.edit")}</button></> : <div className="fuel-meters-page-missing"><Gauge size={24} /><strong>{t("fuelMeters:page.missing")}</strong><p>{t("fuelMeters:page.missingDescription")}</p>{point.active && <button type="button" className="fuel-meters-page-primary" onClick={() => openPointModal(point)}><Plus size={16} />{t("fuelMeters:page.addIndividualMeter")}</button>}</div>}</article>; })}</section>}</>)}
      </>}
    </>}
  </main>{modal && pump && <FuelMeterModal key={modal.fuelMeter?.id || `${modal.parentType}-${modal.dispensingPoint?.id || pump.id}`} isOpen organizationId={organizationId} stationId={stationId} parentType={modal.parentType} pump={pump} dispensingPoint={modal.dispensingPoint} fuelMeter={modal.fuelMeter} onClose={() => setModal(null)} onSaved={saved} />}</SupervisorLayout>;
}

export default FuelMetersPage;
