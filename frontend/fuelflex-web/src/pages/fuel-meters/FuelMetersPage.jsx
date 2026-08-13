import { useEffect, useMemo, useState } from "react";
import { AlertCircle, Building2, CheckCircle2, Fuel, Gauge, LoaderCircle, Pencil, Plus, RefreshCw, Search } from "lucide-react";
import { Link } from "react-router-dom";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import FuelMeterModal from "../../features/fuel-meter/components/FuelMeterModal";
import { getStoredUser } from "../../services/auth/authStorage";
import { getDispensingPoints } from "../../services/dispensingPoint/dispensingPointService";
import { getDispensingPointFuelMeters, getPumpFuelMeters } from "../../services/fuelMeter/fuelMeterService";
import { getPumps } from "../../services/pump/pumpService";
import { getStations } from "../../services/station/stationService";
import "./FuelMetersPage.css";

const TECHNOLOGY_LABELS = { MECHANICAL: "Mécanique", ELECTRONIC: "Électronique", MANUAL: "Manuel" };
const STATUS_LABELS = { ACTIVE: "Actif", INACTIVE: "Inactif", MAINTENANCE: "Maintenance", OUT_OF_SERVICE: "Hors service" };
const METERING = {
  PUMP: ["Comptage global à la pompe", "Un seul compteur actif suit l’ensemble des distributions de cette pompe."],
  DISPENSING_POINT: ["Comptage par pistolet", "Chaque pistolet actif dispose de son propre compteur individuel."],
};
const formatIndex = (value) => new Intl.NumberFormat("fr-FR", { minimumFractionDigits: 3, maximumFractionDigits: 3 }).format(Number(value) || 0);

function PageState({ icon: Icon = Gauge, title, text, action, compact = false }) {
  return <section className={`fuel-meters-page-state ${title ? "empty" : ""} ${compact ? "compact" : ""}`}><Icon className={Icon === LoaderCircle ? "fuel-meters-page-spinner" : ""} size={30} />{title && <h2>{title}</h2>}<p>{text}</p>{action}</section>;
}

function ErrorState({ message, onRetry }) {
  return <div className="fuel-meters-page-alert error" role="alert"><AlertCircle size={18} /><span>{message}</span>{onRetry && <button type="button" onClick={onRetry}><RefreshCw size={15} />Réessayer</button>}</div>;
}

function MeterDetails({ meter }) {
  return <div className="fuel-meters-page-meter-details"><div><small>Code</small><strong>{meter.code}</strong></div><div><small>Technologie</small><strong>{TECHNOLOGY_LABELS[meter.technology] || meter.technology}</strong></div><div><small>Index courant</small><strong>{formatIndex(meter.currentIndex)}</strong></div></div>;
}

function FuelMetersPage() {
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
  const [errors, setErrors] = useState({ stations: organizationId ? "" : "Aucune société n’est associée à ce compte.", pumps: "", points: "", meters: "" });
  const [attempts, setAttempts] = useState({ stations: 0, pumps: 0, data: 0 });
  const [success, setSuccess] = useState("");
  const setBusy = (key, value) => setLoading((current) => ({ ...current, [key]: value }));
  const setError = (key, value) => setErrors((current) => ({ ...current, [key]: value }));
  const retry = (key) => setAttempts((current) => ({ ...current, [key]: current[key] + 1 }));

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("stations", true); setError("stations", ""); return getStations(organizationId, { signal: controller.signal }); })
      .then((value) => { const loaded = Array.isArray(value) ? value : []; setStations(loaded); setStationId(loaded.length === 1 ? loaded[0].id : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setError("stations", error?.message || "Impossible de charger les stations."); })
      .finally(() => { if (!controller.signal.aborted) setBusy("stations", false); });
    return () => controller.abort();
  }, [attempts.stations, organizationId]);

  useEffect(() => {
    if (!organizationId || !stationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("pumps", true); setError("pumps", ""); return getPumps(organizationId, stationId, { signal: controller.signal }); })
      .then((value) => { const loaded = Array.isArray(value) ? value : []; setPumps(loaded); setPumpId(loaded.length === 1 ? loaded[0].id : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setError("pumps", error?.message || "Impossible de charger les pompes."); })
      .finally(() => { if (!controller.signal.aborted) setBusy("pumps", false); });
    return () => controller.abort();
  }, [attempts.pumps, organizationId, stationId]);

  const pump = pumps.find(({ id }) => id === pumpId) || null;

  useEffect(() => {
    if (!organizationId || !stationId || !pump) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(async () => {
      setError("points", ""); setError("meters", ""); setPumpMeters([]); setPointMeters({});
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
      .catch((error) => { if (error?.name !== "AbortError") { const key = pump.meteringLevel === "PUMP" ? "meters" : "points"; setError(key, error?.message || "Impossible de charger la configuration des compteurs."); } })
      .finally(() => { if (!controller.signal.aborted) { setBusy("points", false); setBusy("meters", false); } });
    return () => controller.abort();
  }, [attempts.data, organizationId, pump, stationId]);

  const activePumpMeter = pumpMeters.find((meter) => meter.active) || null;
  const filteredPoints = useMemo(() => { const query = search.trim().toLocaleLowerCase("fr"); if (!query) return points; return points.filter((point) => { const meter = (pointMeters[point.id] || []).find((item) => item.active); return [point.name, point.code, String(point.nozzleNumber), meter?.code, meter?.name].some((value) => value?.toLocaleLowerCase("fr").includes(query)); }); }, [pointMeters, points, search]);
  const changeStation = (event) => { setStationId(event.target.value); setPumps([]); setPumpId(""); setPoints([]); setPumpMeters([]); setPointMeters({}); setSearch(""); setModal(null); setSuccess(""); setErrors((current) => ({ ...current, pumps: "", points: "", meters: "" })); };
  const changePump = (event) => { setPumpId(event.target.value); setPoints([]); setPumpMeters([]); setPointMeters({}); setSearch(""); setModal(null); setSuccess(""); setErrors((current) => ({ ...current, points: "", meters: "" })); };
  const openPumpModal = (meter = null) => setModal({ parentType: "PUMP", fuelMeter: meter });
  const openPointModal = (point, meter = null) => setModal({ parentType: "DISPENSING_POINT", dispensingPoint: point, fuelMeter: meter });
  const saved = (_, updated) => { setModal(null); setSuccess(updated ? "Le compteur a été modifié avec succès." : "Le compteur a été créé avec succès."); retry("data"); };
  const metering = pump ? METERING[pump.meteringLevel] : null;

  return <SupervisorLayout><main className="fuel-meters-page">
    <header className="fuel-meters-page-header"><div><span>CONFIGURATION DU RÉSEAU</span><h1>Compteurs</h1><p>Gérez les compteurs selon le mode défini sur chaque pompe.</p></div></header>
    {success && <div className="fuel-meters-page-alert success" role="status"><CheckCircle2 size={18} />{success}</div>}
    {errors.stations && <ErrorState message={errors.stations} onRetry={organizationId ? () => retry("stations") : undefined} />}
    {loading.stations ? <PageState icon={LoaderCircle} text="Chargement des stations..." /> : !errors.stations && stations.length === 0 ? <PageState icon={Building2} title="Aucune station configurée" text="Créez d’abord une station avant de gérer ses compteurs." /> : !errors.stations && <>
      <section className="fuel-meters-page-selectors"><label><span>Station</span><select value={stationId} onChange={changeStation}><option value="">Sélectionner une station</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label><label><span>Pompe</span><select value={pumpId} onChange={changePump} disabled={!stationId || loading.pumps || Boolean(errors.pumps)}><option value="">{loading.pumps ? "Chargement..." : "Sélectionner une pompe"}</option>{pumps.map((item) => <option key={item.id} value={item.id}>{item.name} — {item.code}</option>)}</select></label></section>
      {!stationId ? <PageState icon={Building2} title="Aucune station sélectionnée" text="Choisissez la station dont vous souhaitez gérer les compteurs." /> : loading.pumps ? <PageState icon={LoaderCircle} text="Chargement des pompes..." /> : errors.pumps ? <ErrorState message={errors.pumps} onRetry={() => retry("pumps")} /> : pumps.length === 0 ? <PageState icon={Fuel} title="Aucune pompe configurée" text="Créez d’abord une pompe dans cette station." /> : !pumpId ? <PageState icon={Fuel} title="Aucune pompe sélectionnée" text="Choisissez la pompe dont vous souhaitez gérer les compteurs." /> : <>
        <section className={`fuel-meters-page-mode ${pump.meteringLevel === "DISPENSING_POINT" ? "point" : ""}`}><Gauge size={21} /><div><small>{pump.code} · Pompe n° {pump.pumpNumber}</small><strong>{metering?.[0] || pump.meteringLevel}</strong><p>{metering?.[1]}</p></div></section>
        {errors.points && <ErrorState message={errors.points} onRetry={() => retry("data")} />}{errors.meters && <ErrorState message={errors.meters} onRetry={() => retry("data")} />}
        {!errors.points && !errors.meters && pump.meteringLevel === "PUMP" && (loading.meters ? <PageState icon={LoaderCircle} text="Chargement du compteur global..." /> : activePumpMeter ? <section className="fuel-meters-page-global-card"><div className="fuel-meters-page-card-heading"><span><Gauge size={22} /></span><div><small>COMPTEUR GLOBAL CONFIGURÉ</small><h2>{activePumpMeter.name}</h2></div></div><MeterDetails meter={activePumpMeter} /><div className="fuel-meters-page-badges"><em className={`status ${(activePumpMeter.status || "").toLowerCase()}`}>{STATUS_LABELS[activePumpMeter.status] || activePumpMeter.status}</em><em className={activePumpMeter.active ? "active" : "inactive"}>{activePumpMeter.active ? "Actif" : "Inactif"}</em></div><button type="button" className="fuel-meters-page-edit" onClick={() => openPumpModal(activePumpMeter)}><Pencil size={15} />Modifier</button></section> : <PageState title="Compteur global non configuré" text="Ajoutez l’unique compteur global attendu pour cette pompe." action={<button type="button" className="fuel-meters-page-primary" onClick={() => openPumpModal()}><Plus size={17} />Ajouter le compteur</button>} />)}
        {!errors.points && !errors.meters && pump.meteringLevel === "DISPENSING_POINT" && (loading.points ? <PageState icon={LoaderCircle} text="Chargement des pistolets..." /> : points.length === 0 ? <PageState title="Aucun pistolet n’est configuré pour cette pompe." text="Configurez d’abord les pistolets avant d’ajouter leurs compteurs." action={<Link className="fuel-meters-page-primary" to="/superviseur/pistolets">Gérer les pistolets</Link>} /> : loading.meters ? <PageState icon={LoaderCircle} text="Chargement des compteurs individuels..." /> : <><div className="fuel-meters-page-toolbar"><label><Search size={17} /><input type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Rechercher par pistolet ou compteur" aria-label="Rechercher un pistolet ou un compteur" /></label><span>{filteredPoints.length} pistolet{filteredPoints.length > 1 ? "s" : ""}</span></div>{filteredPoints.length === 0 ? <PageState icon={Search} title="Aucun résultat" text="Essayez un autre nom, code ou numéro." compact /> : <section className="fuel-meters-page-point-grid">{filteredPoints.map((point) => { const meter = (pointMeters[point.id] || []).find((item) => item.active) || null; return <article key={point.id} className={!point.active ? "inactive" : ""}><header><div><small>{point.code} · Pistolet n° {point.nozzleNumber}</small><h2>{point.name}</h2><p>Citerne : {point.tankName || point.tankCode || "Non renseignée"}</p></div><em>{point.active ? "Actif" : "Inactif"}</em></header>{meter ? <><strong className="fuel-meters-page-configured">Compteur configuré</strong><h3>{meter.name}</h3><MeterDetails meter={meter} /><div className="fuel-meters-page-badges"><em className={`status ${(meter.status || "").toLowerCase()}`}>{STATUS_LABELS[meter.status] || meter.status}</em><em className="active">Actif</em></div><button type="button" className="fuel-meters-page-edit" onClick={() => openPointModal(point, meter)}><Pencil size={15} />Modifier</button></> : <div className="fuel-meters-page-missing"><Gauge size={24} /><strong>Compteur non configuré</strong><p>Aucun compteur actif n’est associé à ce pistolet.</p>{point.active && <button type="button" className="fuel-meters-page-primary" onClick={() => openPointModal(point)}><Plus size={16} />Ajouter un compteur</button>}</div>}</article>; })}</section>}</>)}
      </>}
    </>}
  </main>{modal && pump && <FuelMeterModal key={modal.fuelMeter?.id || `${modal.parentType}-${modal.dispensingPoint?.id || pump.id}`} isOpen organizationId={organizationId} stationId={stationId} parentType={modal.parentType} pump={pump} dispensingPoint={modal.dispensingPoint} fuelMeter={modal.fuelMeter} onClose={() => setModal(null)} onSaved={saved} />}</SupervisorLayout>;
}

export default FuelMetersPage;
