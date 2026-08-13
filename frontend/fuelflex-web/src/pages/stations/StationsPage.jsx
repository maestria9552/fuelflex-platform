import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AlertCircle, Building2, CheckCircle2, ExternalLink, LoaderCircle, MapPin, Pencil, Plus, RefreshCw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import StationModal from "../../features/station/components/StationModal";
import { getStoredUser } from "../../services/auth/authStorage";
import { getStations } from "../../services/station/stationService";
import "./StationsPage.css";

const TYPE_LABELS = {
  SERVICE_STATION: "Station-service", DEPOT: "Dépôt", AIRPORT: "Aéroport", PORT: "Port", MINE: "Mine",
  LOGISTICS_CENTER: "Centre logistique", DISTRIBUTION_CENTER: "Centre de distribution",
};
const STATUS_LABELS = { ACTIVE: "Active", INACTIVE: "Inactive", MAINTENANCE: "Maintenance", SUSPENDED: "Suspendue", CLOSED: "Fermée" };

function StationsPage() {
  const navigate = useNavigate();
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [editingStation, setEditingStation] = useState(null);
  const [isLoading, setIsLoading] = useState(Boolean(organizationId));
  const [errorMessage, setErrorMessage] = useState(organizationId ? "" : "Aucune société n’est associée à ce compte.");
  const [successMessage, setSuccessMessage] = useState("");
  const [loadAttempt, setLoadAttempt] = useState(0);

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => {
      setIsLoading(true); setErrorMessage("");
      return getStations(organizationId, { signal: controller.signal });
    }).then((loadedStations) => setStations(Array.isArray(loadedStations) ? loadedStations : []))
      .catch((error) => { if (error?.name !== "AbortError") setErrorMessage(error?.message || "Impossible de charger les stations."); })
      .finally(() => { if (!controller.signal.aborted) setIsLoading(false); });
    return () => controller.abort();
  }, [loadAttempt, organizationId]);

  const filteredStations = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase("fr");
    if (!query) return stations;
    return stations.filter((station) => [station.name, station.code, station.city].some((value) => value?.toLocaleLowerCase("fr").includes(query)));
  }, [searchTerm, stations]);

  const handleStationSaved = () => {
    setEditingStation(null);
    setSuccessMessage("La station a été modifiée avec succès.");
    setLoadAttempt((attempt) => attempt + 1);
  };

  return <SupervisorLayout>
    <main className="stations-page">
      <header className="stations-page-header">
        <div><span>CONFIGURATION DU RÉSEAU</span><h1>Stations</h1><p>Gérez les stations rattachées à votre organisation.</p></div>
        <button type="button" className="stations-page-primary" onClick={() => navigate("/superviseur/stations/nouvelle")}><Plus size={17} />Nouvelle station</button>
      </header>

      {successMessage && <div className="stations-page-alert success" role="status"><CheckCircle2 size={18} />{successMessage}</div>}
      {errorMessage && <div className="stations-page-alert error" role="alert"><AlertCircle size={18} /><span>{errorMessage}</span>{organizationId && <button type="button" onClick={() => setLoadAttempt((attempt) => attempt + 1)}><RefreshCw size={15} />Réessayer</button>}</div>}

      {!errorMessage && <div className="stations-page-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder="Rechercher par nom, code ou ville" aria-label="Rechercher une station" /></label><span>{filteredStations.length} station{filteredStations.length > 1 ? "s" : ""}</span></div>}

      {isLoading ? <section className="stations-page-loading"><LoaderCircle className="stations-page-spinner" size={30} />Chargement des stations...</section> : !errorMessage && (stations.length === 0 ? <section className="stations-page-empty"><Building2 size={34} /><h2>Aucune station configurée</h2><p>Créez votre première station avec l’assistant de configuration.</p><button type="button" className="stations-page-primary" onClick={() => navigate("/superviseur/stations/nouvelle")}><Plus size={17} />Créer une station</button></section> : filteredStations.length === 0 ? <section className="stations-page-empty compact"><Search size={30} /><h2>Aucune station trouvée</h2><p>Essayez un autre nom, code ou ville.</p></section> : <section className="stations-page-grid">{filteredStations.map((station) => {
        const location = [station.city, station.province].filter(Boolean).join(" · ");
        return <article key={station.id} className={!station.active ? "inactive" : ""}>
          <div className="stations-page-card-top"><span className="stations-page-icon"><Building2 size={21} /></span><div><small>{station.code}</small><h2>{station.name}</h2><p>{TYPE_LABELS[station.type] || station.type}</p></div></div>
          {location && <p className="stations-page-location"><MapPin size={15} />{location}</p>}
          <div className="stations-page-badges"><span className={`status ${String(station.status).toLowerCase()}`}>{STATUS_LABELS[station.status] || station.status}</span><span className={station.active ? "active" : "inactive"}>{station.active ? "Active" : "Inactive"}</span></div>
          <div className="stations-page-actions"><button type="button" className="open" disabled title="Le tableau de bord de station sera disponible dans un prochain bloc"><ExternalLink size={15} />Ouvrir</button><button type="button" onClick={() => { setSuccessMessage(""); setEditingStation(station); }}><Pencil size={15} />Modifier</button></div>
        </article>;
      })}</section>)}
    </main>
    {editingStation && <StationModal key={editingStation.id} isOpen organizationId={organizationId} station={editingStation} onClose={() => setEditingStation(null)} onSaved={handleStationSaved} />}
  </SupervisorLayout>;
}

export default StationsPage;
