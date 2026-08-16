import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCircle, Building2, CheckCircle2, Fuel, Gauge, LoaderCircle, Pencil, Plus, RefreshCw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import DispensingPointModal from "../../features/dispensing-point/components/DispensingPointModal";
import { getLocaleForLanguage } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import { getDepots } from "../../services/depot/depotService";
import { getDispensingPoints } from "../../services/dispensingPoint/dispensingPointService";
import { getPumps } from "../../services/pump/pumpService";
import { getStations } from "../../services/station/stationService";
import { getTanks } from "../../services/tank/tankService";
import "../pumps/PumpsPage.css";

function PageState({ icon: Icon = Fuel, title, text, action, compact = false }) {
  return <section className={`pumps-page-state ${title ? "empty" : ""} ${compact ? "compact" : ""}`}><Icon className={Icon === LoaderCircle ? "pumps-page-spinner" : ""} size={30} />{title && <h2>{title}</h2>}<p>{text}</p>{action}</section>;
}

function ErrorState({ message, onRetry, retryLabel }) {
  return <div className="pumps-page-alert error" role="alert"><AlertCircle size={18} /><span>{message}</span>{onRetry && <button type="button" onClick={onRetry}><RefreshCw size={15} />{retryLabel}</button>}</div>;
}

function DispensingPointsPage() {
  const { t, i18n } = useTranslation(["dispensingPoints", "common"]);
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [stationId, setStationId] = useState("");
  const [pumps, setPumps] = useState([]);
  const [pumpId, setPumpId] = useState("");
  const [tanks, setTanks] = useState([]);
  const [points, setPoints] = useState([]);
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState(undefined);
  const [loading, setLoading] = useState({ stations: Boolean(organizationId), pumps: false, tanks: false, points: false });
  const [errors, setErrors] = useState({ stations: organizationId ? null : { key: "dispensingPoints:feedback.organizationMissing" }, pumps: null, tanks: null, points: null });
  const [attempts, setAttempts] = useState({ stations: 0, pumps: 0, tanks: 0, points: 0 });
  const [success, setSuccess] = useState(null);
  const locale = getLocaleForLanguage(i18n.resolvedLanguage);
  const setBusy = (key, value) => setLoading((current) => ({ ...current, [key]: value }));
  const setError = (key, value) => setErrors((current) => ({ ...current, [key]: value }));
  const retry = (key) => setAttempts((current) => ({ ...current, [key]: current[key] + 1 }));
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";
  const retryLabel = t("common:actions.retry");

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("stations", true); setError("stations", null); return getStations(organizationId, { signal: controller.signal }); }).then((value) => { const loaded = Array.isArray(value) ? value : []; setStations(loaded); setStationId(loaded.length === 1 ? loaded[0].id : ""); }).catch((error) => { if (error?.name !== "AbortError") setError("stations", error?.message ? { text: error.message } : { key: "dispensingPoints:feedback.stationsLoadFailed" }); }).finally(() => { if (!controller.signal.aborted) setBusy("stations", false); });
    return () => controller.abort();
  }, [attempts.stations, organizationId]);

  useEffect(() => {
    if (!organizationId || !stationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("pumps", true); setError("pumps", null); return getPumps(organizationId, stationId, { signal: controller.signal }); }).then((value) => { const loaded = Array.isArray(value) ? value : []; setPumps(loaded); setPumpId(loaded.length === 1 ? loaded[0].id : ""); }).catch((error) => { if (error?.name !== "AbortError") setError("pumps", error?.message ? { text: error.message } : { key: "dispensingPoints:feedback.pumpsLoadFailed" }); }).finally(() => { if (!controller.signal.aborted) setBusy("pumps", false); });
    return () => controller.abort();
  }, [attempts.pumps, organizationId, stationId]);

  useEffect(() => {
    if (!organizationId || !stationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("tanks", true); setError("tanks", null); return getDepots(organizationId, stationId, { signal: controller.signal }); }).then((value) => Promise.all((Array.isArray(value) ? value : []).map((depot) => getTanks(organizationId, stationId, depot.id, { signal: controller.signal })))).then((groups) => setTanks(groups.flatMap((group) => Array.isArray(group) ? group : []))).catch((error) => { if (error?.name !== "AbortError") setError("tanks", error?.message ? { text: error.message } : { key: "dispensingPoints:feedback.tanksLoadFailed" }); }).finally(() => { if (!controller.signal.aborted) setBusy("tanks", false); });
    return () => controller.abort();
  }, [attempts.tanks, organizationId, stationId]);

  useEffect(() => {
    if (!organizationId || !stationId || !pumpId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("points", true); setError("points", null); return getDispensingPoints(organizationId, stationId, pumpId, { signal: controller.signal }); }).then((value) => setPoints(Array.isArray(value) ? value : [])).catch((error) => { if (error?.name !== "AbortError") setError("points", error?.message ? { text: error.message } : { key: "dispensingPoints:feedback.pointsLoadFailed" }); }).finally(() => { if (!controller.signal.aborted) setBusy("points", false); });
    return () => controller.abort();
  }, [attempts.points, organizationId, pumpId, stationId]);

  const station = stations.find(({ id }) => id === stationId) || null;
  const pump = pumps.find(({ id }) => id === pumpId) || null;
  const tankMap = useMemo(() => new Map(tanks.map((tank) => [tank.id, tank])), [tanks]);
  const modalTanks = useMemo(() => tanks.filter((tank) => tank.active || tank.id === modal?.tankId), [modal, tanks]);
  const filtered = useMemo(() => { const query = search.trim().toLocaleLowerCase(locale); return query ? points.filter((point) => [point.name, point.code, String(point.nozzleNumber), point.tankName, tankMap.get(point.tankId)?.name].some((value) => value?.toLocaleLowerCase(locale).includes(query))) : points; }, [locale, points, search, tankMap]);
  const changeStation = (event) => { setStationId(event.target.value); setPumps([]); setPumpId(""); setTanks([]); setPoints([]); setSearch(""); setModal(undefined); setSuccess(null); setErrors((current) => ({ ...current, pumps: null, tanks: null, points: null })); };
  const changePump = (event) => { setPumpId(event.target.value); setPoints([]); setSearch(""); setModal(undefined); setSuccess(null); setError("points", null); };
  const saved = (_, updated) => { setModal(undefined); setSuccess({ key: updated ? "dispensingPoints:feedback.updated" : "dispensingPoints:feedback.created" }); retry("points"); };
  const canCreate = pump && !loading.tanks && !errors.tanks && modalTanks.length > 0;

  return <SupervisorLayout><main className="pumps-page">
    <header className="pumps-page-header"><div><span>{t("dispensingPoints:page.eyebrow")}</span><h1>{t("dispensingPoints:page.title")}</h1><p>{t("dispensingPoints:page.description")}</p></div>{canCreate && <button type="button" className="pumps-page-primary" onClick={() => setModal(null)}><Plus size={17} />{t("dispensingPoints:page.add")}</button>}</header>
    {success && <div className="pumps-page-alert success" role="status"><CheckCircle2 size={18} />{renderMessage(success)}</div>}
    {errors.stations && <ErrorState message={renderMessage(errors.stations)} onRetry={organizationId ? () => retry("stations") : undefined} retryLabel={retryLabel} />}
    {loading.stations ? <PageState icon={LoaderCircle} text={t("dispensingPoints:page.loadingStations")} /> : !errors.stations && stations.length === 0 ? <PageState icon={Building2} title={t("dispensingPoints:page.noStationTitle")} text={t("dispensingPoints:page.noStationDescription")} /> : !errors.stations && <>
      <section className="pumps-page-station-selector"><label><span>{t("dispensingPoints:page.station")}</span><select value={stationId} onChange={changeStation}><option value="">{t("dispensingPoints:page.selectStation")}</option>{stations.map((item) => <option key={item.id} value={item.id}>{item.name} — {item.code}</option>)}</select></label><label><span>{t("dispensingPoints:page.pump")}</span><select value={pumpId} onChange={changePump} disabled={!stationId || loading.pumps || Boolean(errors.pumps)}><option value="">{loading.pumps ? t("dispensingPoints:page.loading") : t("dispensingPoints:page.selectPump")}</option>{pumps.map((item) => <option key={item.id} value={item.id}>{item.name} — {item.code}</option>)}</select></label></section>
      {!stationId ? <PageState icon={Building2} title={t("dispensingPoints:page.noStationSelectedTitle")} text={t("dispensingPoints:page.noStationSelectedDescription")} /> : loading.pumps ? <PageState icon={LoaderCircle} text={t("dispensingPoints:page.loadingPumps")} /> : errors.pumps ? <ErrorState message={renderMessage(errors.pumps)} onRetry={() => retry("pumps")} retryLabel={retryLabel} /> : pumps.length === 0 ? <PageState title={t("dispensingPoints:page.noPumpTitle")} text={t("dispensingPoints:page.noPumpDescription")} /> : !pumpId ? <PageState title={t("dispensingPoints:page.noPumpSelectedTitle")} text={t("dispensingPoints:page.noPumpSelectedDescription")} /> : <>
        <div className={`pumps-page-metering ${pump.meteringLevel === "DISPENSING_POINT" ? "point" : ""}`}><Gauge size={18} /><div><strong>{t(`dispensingPoints:meteringLevel.${pump.meteringLevel}.title`, { defaultValue: pump.meteringLevel })}</strong><span>{t(`dispensingPoints:meteringLevel.${pump.meteringLevel}.detail`, { defaultValue: "" })}</span></div></div>
        {loading.tanks ? <PageState icon={LoaderCircle} text={t("dispensingPoints:page.loadingTanks")} compact /> : errors.tanks ? <ErrorState message={renderMessage(errors.tanks)} onRetry={() => retry("tanks")} retryLabel={retryLabel} /> : modalTanks.length === 0 && <div className="pumps-page-alert error" role="status"><AlertCircle size={18} />{t("dispensingPoints:page.noActiveTank")}</div>}
        {errors.points && <ErrorState message={renderMessage(errors.points)} onRetry={() => retry("points")} retryLabel={retryLabel} />}
        {!errors.points && !loading.points && points.length > 0 && <div className="pumps-page-toolbar"><label><Search size={17} /><input type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder={t("dispensingPoints:page.searchPlaceholder")} aria-label={t("dispensingPoints:page.searchAriaLabel")} /></label><span>{t("dispensingPoints:page.count", { count: filtered.length })}</span></div>}
        {loading.points ? <PageState icon={LoaderCircle} text={t("dispensingPoints:page.loadingPoints")} /> : !errors.points && (points.length === 0 ? <PageState title={t("dispensingPoints:page.emptyTitle")} text={t("dispensingPoints:page.emptyDescription", { pumpName: pump.name })} action={canCreate ? <button type="button" className="pumps-page-primary" onClick={() => setModal(null)}><Plus size={17} />{t("dispensingPoints:page.add")}</button> : null} /> : filtered.length === 0 ? <PageState icon={Search} title={t("dispensingPoints:page.noResultTitle")} text={t("dispensingPoints:page.noResultDescription")} compact /> : <section className="pumps-page-grid">{filtered.map((point) => { const tank = tankMap.get(point.tankId); return <article key={point.id} className={!point.active ? "inactive" : ""}><div className="pumps-page-card-heading"><span><Fuel size={20} /></span><div><small>{point.code}</small><h2>{point.name}</h2><p>{t("dispensingPoints:page.nozzleNumber", { number: point.nozzleNumber })}</p></div></div><div className="pumps-page-metering"><div><strong>{point.tankName || tank?.name}</strong><span>{point.tankCode || tank?.code}{tank?.productName ? ` · ${tank.productName}` : ""}</span></div></div><div className="pumps-page-badges"><em className={`status ${(point.status || "").toLowerCase()}`}>{t(`dispensingPoints:status.${point.status}`, { defaultValue: point.status })}</em><em className={point.active ? "active" : "inactive"}>{t(point.active ? "dispensingPoints:availability.active" : "dispensingPoints:availability.inactive")}</em></div><div className="pumps-page-actions"><button type="button" onClick={() => setModal(point)}><Pencil size={15} />{t("dispensingPoints:page.edit")}</button></div></article>; })}</section>)}</>}
    </>}
  </main>{modal !== undefined && station && pump && <DispensingPointModal key={modal?.id || "new-point"} isOpen organizationId={organizationId} stationId={station.id} pump={pump} tanks={modalTanks} dispensingPoint={modal} onClose={() => setModal(undefined)} onSaved={saved} />}</SupervisorLayout>;
}

export default DispensingPointsPage;
