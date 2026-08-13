import { useEffect, useMemo, useState } from "react";
import { AlertCircle, Building2, CheckCircle2, Fuel, Gauge, LoaderCircle, MapPin, Pencil, Plus, RefreshCw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import PumpModal from "../../features/pump/components/PumpModal";
import { getStoredUser } from "../../services/auth/authStorage";
import { getPumps } from "../../services/pump/pumpService";
import { getStations } from "../../services/station/stationService";
import "./PumpsPage.css";

const METERING_LABELS = {
  PUMP: { title: "Comptage global à la pompe", detail: "Un compteur global pour toute la pompe" },
  DISPENSING_POINT: { title: "Comptage par pistolet", detail: "Un compteur individuel par pistolet" },
};
const STATUS_LABELS = { ACTIVE: "Active", INACTIVE: "Inactive", MAINTENANCE: "Maintenance", OUT_OF_SERVICE: "Hors service" };

function PageState({ icon: Icon = Fuel, title, text, action, compact = false }) {
  return <section className={`pumps-page-state ${title ? "empty" : ""} ${compact ? "compact" : ""}`}>{Icon && <Icon className={Icon === LoaderCircle ? "pumps-page-spinner" : ""} size={30} />}{title && <h2>{title}</h2>}<p>{text}</p>{action}</section>;
}

function PumpsPage() {
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [selectedStationId, setSelectedStationId] = useState("");
  const [pumps, setPumps] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [pumpModal, setPumpModal] = useState(undefined);
  const [isLoadingStations, setIsLoadingStations] = useState(Boolean(organizationId));
  const [isLoadingPumps, setIsLoadingPumps] = useState(false);
  const [stationsError, setStationsError] = useState(organizationId ? "" : "Aucune société n’est associée à ce compte.");
  const [pumpsError, setPumpsError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [stationsAttempt, setStationsAttempt] = useState(0);
  const [pumpsAttempt, setPumpsAttempt] = useState(0);

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingStations(true); setStationsError(""); return getStations(organizationId, { signal: controller.signal }); })
      .then((result) => { const loaded = Array.isArray(result) ? result : []; setStations(loaded); setSelectedStationId((current) => loaded.length === 1 ? loaded[0].id : loaded.some(({ id }) => id === current) ? current : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setStationsError(error?.message || "Impossible de charger les stations."); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingStations(false); });
    return () => controller.abort();
  }, [organizationId, stationsAttempt]);

  useEffect(() => {
    if (!organizationId || !selectedStationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingPumps(true); setPumpsError(""); return getPumps(organizationId, selectedStationId, { signal: controller.signal }); })
      .then((result) => setPumps(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setPumpsError(error?.message || "Impossible de charger les pompes."); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingPumps(false); });
    return () => controller.abort();
  }, [organizationId, pumpsAttempt, selectedStationId]);

  const selectedStation = stations.find(({ id }) => id === selectedStationId) || null;
  const filteredPumps = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase("fr");
    if (!query) return pumps;
    return pumps.filter((pump) => [pump.name, pump.code, String(pump.pumpNumber), pump.manufacturer, pump.model].some((value) => value?.toLocaleLowerCase("fr").includes(query)));
  }, [pumps, searchTerm]);

  const handleStationChange = (event) => { setSelectedStationId(event.target.value); setPumps([]); setSearchTerm(""); setPumpModal(undefined); setSuccessMessage(""); setPumpsError(""); };
  const handlePumpSaved = (_, wasUpdate) => { setPumpModal(undefined); setSuccessMessage(wasUpdate ? "La pompe a été modifiée avec succès." : "La pompe a été créée avec succès."); setPumpsAttempt((attempt) => attempt + 1); };
  const openCreate = () => { setSuccessMessage(""); setPumpModal(null); };

  return <SupervisorLayout><main className="pumps-page">
    <header className="pumps-page-header"><div><span>CONFIGURATION DU RÉSEAU</span><h1>Pompes</h1><p>Gérez les pompes installées dans vos différentes stations.</p></div>{selectedStation && <button type="button" className="pumps-page-primary" onClick={openCreate}><Plus size={17} />Ajouter une pompe</button>}</header>
    {successMessage && <div className="pumps-page-alert success" role="status"><CheckCircle2 size={18} />{successMessage}</div>}
    {stationsError && <div className="pumps-page-alert error" role="alert"><AlertCircle size={18} /><span>{stationsError}</span>{organizationId && <button type="button" onClick={() => setStationsAttempt((value) => value + 1)}><RefreshCw size={15} />Réessayer</button>}</div>}
    {isLoadingStations ? <PageState icon={LoaderCircle} text="Chargement des stations..." /> : !stationsError && stations.length === 0 ? <PageState icon={Building2} title="Aucune station configurée" text="Créez d’abord une station avant de gérer ses pompes." /> : !stationsError && <>
      <section className="pumps-page-station-selector"><label><span>Station</span><select value={selectedStationId} onChange={handleStationChange}><option value="">Sélectionner une station</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label>{stations.length > 1 && !selectedStationId && <p>Sélectionnez la station dont vous souhaitez gérer les pompes.</p>}</section>
      {!selectedStationId ? <PageState title="Aucune station sélectionnée" text="Choisissez une station dans le sélecteur ci-dessus." /> : <>
        {pumpsError && <div className="pumps-page-alert error" role="alert"><AlertCircle size={18} /><span>{pumpsError}</span><button type="button" onClick={() => setPumpsAttempt((value) => value + 1)}><RefreshCw size={15} />Réessayer</button></div>}
        {!pumpsError && !isLoadingPumps && pumps.length > 0 && <div className="pumps-page-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder="Rechercher par nom, code, numéro, fabricant ou modèle" aria-label="Rechercher une pompe" /></label><span>{filteredPumps.length} pompe{filteredPumps.length > 1 ? "s" : ""}</span></div>}
        {isLoadingPumps ? <PageState icon={LoaderCircle} text="Chargement des pompes..." /> : !pumpsError && (pumps.length === 0 ? <PageState title="Aucune pompe n’est encore configurée pour cette station." text={`Ajoutez la première pompe de ${selectedStation?.name || "cette station"}.`} action={<button type="button" className="pumps-page-primary" onClick={openCreate}><Plus size={17} />Ajouter une pompe</button>} /> : filteredPumps.length === 0 ? <PageState icon={Search} title="Aucune pompe trouvée" text="Essayez un autre nom, code, numéro, fabricant ou modèle." compact /> : <section className="pumps-page-grid">{filteredPumps.map((pump) => { const metering = METERING_LABELS[pump.meteringLevel]; return <article key={pump.id} className={!pump.active ? "inactive" : ""}><div className="pumps-page-card-heading"><span><Fuel size={20} /></span><div><small>{pump.code}</small><h2>{pump.name}</h2><p>Pompe n° {pump.pumpNumber}</p></div></div><div className={`pumps-page-metering ${pump.meteringLevel === "DISPENSING_POINT" ? "point" : "pump"}`}><Gauge size={18} /><div><strong>{metering?.title || pump.meteringLevel}</strong><span>{metering?.detail}</span></div></div>{pump.location && <p className="pumps-page-location"><MapPin size={15} />{pump.location}</p>}<div className="pumps-page-badges"><em className={`status ${(pump.status || "").toLowerCase()}`}>{STATUS_LABELS[pump.status] || pump.status}</em><em className={pump.active ? "active" : "inactive"}>{pump.active ? "Actif" : "Inactif"}</em></div><div className="pumps-page-actions"><button type="button" onClick={() => { setSuccessMessage(""); setPumpModal(pump); }}><Pencil size={15} />Modifier</button></div></article>; })}</section>)}</>}
    </>}
  </main>{pumpModal !== undefined && selectedStation && <PumpModal key={pumpModal?.id || "new-pump"} isOpen organizationId={organizationId} stationId={selectedStation.id} pump={pumpModal} onClose={() => setPumpModal(undefined)} onSaved={handlePumpSaved} />}</SupervisorLayout>;
}

export default PumpsPage;
