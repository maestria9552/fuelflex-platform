import { useEffect, useMemo, useState } from "react";
import { AlertCircle, Building2, CheckCircle2, Fuel, Gauge, LoaderCircle, Pencil, Plus, RefreshCw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import DispensingPointModal from "../../features/dispensing-point/components/DispensingPointModal";
import { getStoredUser } from "../../services/auth/authStorage";
import { getDepots } from "../../services/depot/depotService";
import { getDispensingPoints } from "../../services/dispensingPoint/dispensingPointService";
import { getPumps } from "../../services/pump/pumpService";
import { getStations } from "../../services/station/stationService";
import { getTanks } from "../../services/tank/tankService";
import "../pumps/PumpsPage.css";

const METERING = {
  PUMP: ["Comptage global à la pompe", "Cette pompe utilise un compteur global. Les pistolets doivent respecter la configuration commune de la pompe."],
  DISPENSING_POINT: ["Comptage par pistolet", "Les compteurs individuels seront gérés dans le sous-menu Compteurs."],
};
const STATUS_LABELS = { ACTIVE: "Actif", INACTIVE: "Inactif", MAINTENANCE: "Maintenance", OUT_OF_SERVICE: "Hors service" };

function PageState({ icon: Icon = Fuel, title, text, action, compact = false }) {
  return <section className={`pumps-page-state ${title ? "empty" : ""} ${compact ? "compact" : ""}`}><Icon className={Icon === LoaderCircle ? "pumps-page-spinner" : ""} size={30} />{title && <h2>{title}</h2>}<p>{text}</p>{action}</section>;
}

function ErrorState({ message, onRetry }) {
  return <div className="pumps-page-alert error" role="alert"><AlertCircle size={18} /><span>{message}</span>{onRetry && <button type="button" onClick={onRetry}><RefreshCw size={15} />Réessayer</button>}</div>;
}

function DispensingPointsPage() {
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
  const [errors, setErrors] = useState({ stations: organizationId ? "" : "Aucune société n’est associée à ce compte.", pumps: "", tanks: "", points: "" });
  const [attempts, setAttempts] = useState({ stations: 0, pumps: 0, tanks: 0, points: 0 });
  const [success, setSuccess] = useState("");
  const setBusy = (key, value) => setLoading((current) => ({ ...current, [key]: value }));
  const setError = (key, value) => setErrors((current) => ({ ...current, [key]: value }));
  const retry = (key) => setAttempts((current) => ({ ...current, [key]: current[key] + 1 }));

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("stations", true); setError("stations", ""); return getStations(organizationId, { signal: controller.signal }); }).then((value) => { const loaded = Array.isArray(value) ? value : []; setStations(loaded); setStationId(loaded.length === 1 ? loaded[0].id : ""); }).catch((error) => { if (error?.name !== "AbortError") setError("stations", error?.message || "Impossible de charger les stations."); }).finally(() => { if (!controller.signal.aborted) setBusy("stations", false); });
    return () => controller.abort();
  }, [attempts.stations, organizationId]);

  useEffect(() => {
    if (!organizationId || !stationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("pumps", true); setError("pumps", ""); return getPumps(organizationId, stationId, { signal: controller.signal }); }).then((value) => { const loaded = Array.isArray(value) ? value : []; setPumps(loaded); setPumpId(loaded.length === 1 ? loaded[0].id : ""); }).catch((error) => { if (error?.name !== "AbortError") setError("pumps", error?.message || "Impossible de charger les pompes."); }).finally(() => { if (!controller.signal.aborted) setBusy("pumps", false); });
    return () => controller.abort();
  }, [attempts.pumps, organizationId, stationId]);

  useEffect(() => {
    if (!organizationId || !stationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("tanks", true); setError("tanks", ""); return getDepots(organizationId, stationId, { signal: controller.signal }); }).then((value) => Promise.all((Array.isArray(value) ? value : []).map((depot) => getTanks(organizationId, stationId, depot.id, { signal: controller.signal })))).then((groups) => setTanks(groups.flatMap((group) => Array.isArray(group) ? group : []))).catch((error) => { if (error?.name !== "AbortError") setError("tanks", error?.message || "Impossible de charger les citernes de la station."); }).finally(() => { if (!controller.signal.aborted) setBusy("tanks", false); });
    return () => controller.abort();
  }, [attempts.tanks, organizationId, stationId]);

  useEffect(() => {
    if (!organizationId || !stationId || !pumpId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setBusy("points", true); setError("points", ""); return getDispensingPoints(organizationId, stationId, pumpId, { signal: controller.signal }); }).then((value) => setPoints(Array.isArray(value) ? value : [])).catch((error) => { if (error?.name !== "AbortError") setError("points", error?.message || "Impossible de charger les pistolets."); }).finally(() => { if (!controller.signal.aborted) setBusy("points", false); });
    return () => controller.abort();
  }, [attempts.points, organizationId, pumpId, stationId]);

  const station = stations.find(({ id }) => id === stationId) || null;
  const pump = pumps.find(({ id }) => id === pumpId) || null;
  const tankMap = useMemo(() => new Map(tanks.map((tank) => [tank.id, tank])), [tanks]);
  const modalTanks = useMemo(() => tanks.filter((tank) => tank.active || tank.id === modal?.tankId), [modal, tanks]);
  const filtered = useMemo(() => { const query = search.trim().toLocaleLowerCase("fr"); return query ? points.filter((point) => [point.name, point.code, String(point.nozzleNumber), point.tankName, tankMap.get(point.tankId)?.name].some((value) => value?.toLocaleLowerCase("fr").includes(query))) : points; }, [points, search, tankMap]);
  const changeStation = (event) => { setStationId(event.target.value); setPumps([]); setPumpId(""); setTanks([]); setPoints([]); setSearch(""); setModal(undefined); setSuccess(""); setErrors((current) => ({ ...current, pumps: "", tanks: "", points: "" })); };
  const changePump = (event) => { setPumpId(event.target.value); setPoints([]); setSearch(""); setModal(undefined); setSuccess(""); setError("points", ""); };
  const saved = (_, updated) => { setModal(undefined); setSuccess(updated ? "Le pistolet a été modifié avec succès." : "Le pistolet a été créé avec succès."); retry("points"); };
  const canCreate = pump && !loading.tanks && !errors.tanks && modalTanks.length > 0;
  const metering = pump ? METERING[pump.meteringLevel] : null;

  return <SupervisorLayout><main className="pumps-page">
    <header className="pumps-page-header"><div><span>CONFIGURATION DU RÉSEAU</span><h1>Pistolets</h1><p>Gérez les pistolets rattachés aux pompes de vos stations.</p></div>{canCreate && <button type="button" className="pumps-page-primary" onClick={() => setModal(null)}><Plus size={17} />Ajouter un pistolet</button>}</header>
    {success && <div className="pumps-page-alert success" role="status"><CheckCircle2 size={18} />{success}</div>}
    {errors.stations && <ErrorState message={errors.stations} onRetry={organizationId ? () => retry("stations") : undefined} />}
    {loading.stations ? <PageState icon={LoaderCircle} text="Chargement des stations..." /> : !errors.stations && stations.length === 0 ? <PageState icon={Building2} title="Aucune station configurée" text="Créez d’abord une station avant de gérer ses pistolets." /> : !errors.stations && <>
      <section className="pumps-page-station-selector"><label><span>Station</span><select value={stationId} onChange={changeStation}><option value="">Sélectionner une station</option>{stations.map((item) => <option key={item.id} value={item.id}>{item.name} — {item.code}</option>)}</select></label><label><span>Pompe</span><select value={pumpId} onChange={changePump} disabled={!stationId || loading.pumps || Boolean(errors.pumps)}><option value="">{loading.pumps ? "Chargement..." : "Sélectionner une pompe"}</option>{pumps.map((item) => <option key={item.id} value={item.id}>{item.name} — {item.code}</option>)}</select></label></section>
      {!stationId ? <PageState icon={Building2} title="Aucune station sélectionnée" text="Choisissez la station dont vous souhaitez gérer les pistolets." /> : loading.pumps ? <PageState icon={LoaderCircle} text="Chargement des pompes..." /> : errors.pumps ? <ErrorState message={errors.pumps} onRetry={() => retry("pumps")} /> : pumps.length === 0 ? <PageState title="Aucune pompe configurée" text="Créez d’abord une pompe dans cette station avant d’ajouter un pistolet." /> : !pumpId ? <PageState title="Aucune pompe sélectionnée" text="Choisissez la pompe dont vous souhaitez gérer les pistolets." /> : <>
        {metering && <div className={`pumps-page-metering ${pump.meteringLevel === "DISPENSING_POINT" ? "point" : ""}`}><Gauge size={18} /><div><strong>{metering[0]}</strong><span>{metering[1]}</span></div></div>}
        {loading.tanks ? <PageState icon={LoaderCircle} text="Chargement des citernes..." compact /> : errors.tanks ? <ErrorState message={errors.tanks} onRetry={() => retry("tanks")} /> : modalTanks.length === 0 && <div className="pumps-page-alert error" role="status"><AlertCircle size={18} />Aucune citerne active n’est disponible dans cette station.</div>}
        {errors.points && <ErrorState message={errors.points} onRetry={() => retry("points")} />}
        {!errors.points && !loading.points && points.length > 0 && <div className="pumps-page-toolbar"><label><Search size={17} /><input type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Rechercher par nom, code, numéro ou citerne" /></label><span>{filtered.length} pistolet{filtered.length > 1 ? "s" : ""}</span></div>}
        {loading.points ? <PageState icon={LoaderCircle} text="Chargement des pistolets..." /> : !errors.points && (points.length === 0 ? <PageState title="Aucun pistolet configuré" text={`Ajoutez le premier pistolet de la pompe ${pump.name}.`} action={canCreate ? <button type="button" className="pumps-page-primary" onClick={() => setModal(null)}><Plus size={17} />Ajouter un pistolet</button> : null} /> : filtered.length === 0 ? <PageState icon={Search} title="Aucun pistolet trouvé" text="Essayez un autre nom, code, numéro ou nom de citerne." compact /> : <section className="pumps-page-grid">{filtered.map((point) => { const tank = tankMap.get(point.tankId); return <article key={point.id} className={!point.active ? "inactive" : ""}><div className="pumps-page-card-heading"><span><Fuel size={20} /></span><div><small>{point.code}</small><h2>{point.name}</h2><p>Pistolet n° {point.nozzleNumber}</p></div></div><div className="pumps-page-metering"><div><strong>{point.tankName || tank?.name}</strong><span>{point.tankCode || tank?.code}{tank?.productName ? ` · ${tank.productName}` : ""}</span></div></div><div className="pumps-page-badges"><em className={`status ${(point.status || "").toLowerCase()}`}>{STATUS_LABELS[point.status] || point.status}</em><em className={point.active ? "active" : "inactive"}>{point.active ? "Actif" : "Inactif"}</em></div><div className="pumps-page-actions"><button type="button" onClick={() => setModal(point)}><Pencil size={15} />Modifier</button></div></article>; })}</section>)}</>}
    </>}
  </main>{modal !== undefined && station && pump && <DispensingPointModal key={modal?.id || "new-point"} isOpen organizationId={organizationId} stationId={station.id} pump={pump} tanks={modalTanks} dispensingPoint={modal} onClose={() => setModal(undefined)} onSaved={saved} />}</SupervisorLayout>;
}

export default DispensingPointsPage;
