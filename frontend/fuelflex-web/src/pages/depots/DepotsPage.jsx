import { useEffect, useMemo, useState } from "react";
import { AlertCircle, Building2, CheckCircle2, LoaderCircle, MapPin, Pencil, Plus, RefreshCw, Search, Warehouse } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import DepotModal from "../../features/depot/components/DepotModal";
import { getStoredUser } from "../../services/auth/authStorage";
import { getDepots } from "../../services/depot/depotService";
import { getStations } from "../../services/station/stationService";
import "./DepotsPage.css";

function DepotsPage() {
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [selectedStationId, setSelectedStationId] = useState("");
  const [depots, setDepots] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [depotModal, setDepotModal] = useState(undefined);
  const [isLoadingStations, setIsLoadingStations] = useState(Boolean(organizationId));
  const [isLoadingDepots, setIsLoadingDepots] = useState(false);
  const [stationsError, setStationsError] = useState(organizationId ? "" : "Aucune société n’est associée à ce compte.");
  const [depotsError, setDepotsError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [stationsAttempt, setStationsAttempt] = useState(0);
  const [depotsAttempt, setDepotsAttempt] = useState(0);

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => {
      setIsLoadingStations(true); setStationsError("");
      return getStations(organizationId, { signal: controller.signal });
    }).then((result) => {
      const loadedStations = Array.isArray(result) ? result : [];
      setStations(loadedStations);
      setSelectedStationId((currentId) => {
        if (loadedStations.length === 1) return loadedStations[0].id;
        return loadedStations.some((station) => station.id === currentId) ? currentId : "";
      });
    }).catch((error) => {
      if (error?.name !== "AbortError") setStationsError(error?.message || "Impossible de charger les stations.");
    }).finally(() => { if (!controller.signal.aborted) setIsLoadingStations(false); });
    return () => controller.abort();
  }, [organizationId, stationsAttempt]);

  useEffect(() => {
    if (!organizationId || !selectedStationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => {
      setIsLoadingDepots(true); setDepotsError("");
      return getDepots(organizationId, selectedStationId, { signal: controller.signal });
    }).then((result) => setDepots(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setDepotsError(error?.message || "Impossible de charger les dépôts."); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingDepots(false); });
    return () => controller.abort();
  }, [depotsAttempt, organizationId, selectedStationId]);

  const selectedStation = stations.find((station) => station.id === selectedStationId) || null;
  const filteredDepots = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase("fr");
    if (!query) return depots;
    return depots.filter((depot) => [depot.name, depot.code, depot.location].some((value) => value?.toLocaleLowerCase("fr").includes(query)));
  }, [depots, searchTerm]);

  const handleStationChange = (event) => {
    setSuccessMessage(""); setDepotModal(undefined); setDepots([]); setSearchTerm(""); setSelectedStationId(event.target.value);
  };
  const handleDepotSaved = (_, wasUpdate) => {
    setDepotModal(undefined);
    setSuccessMessage(wasUpdate ? "Le dépôt a été modifié avec succès." : "Le dépôt a été créé avec succès.");
    setDepotsAttempt((attempt) => attempt + 1);
  };

  return <SupervisorLayout>
    <main className="depots-page">
      <header className="depots-page-header"><div><span>CONFIGURATION DU RÉSEAU</span><h1>Dépôts</h1><p>Gérez les dépôts rattachés à vos différentes stations.</p></div>{selectedStation && <button type="button" className="depots-page-primary" onClick={() => { setSuccessMessage(""); setDepotModal(null); }}><Plus size={17} />Ajouter un dépôt</button>}</header>

      {successMessage && <div className="depots-page-alert success" role="status"><CheckCircle2 size={18} />{successMessage}</div>}
      {stationsError && <div className="depots-page-alert error" role="alert"><AlertCircle size={18} /><span>{stationsError}</span>{organizationId && <button type="button" onClick={() => setStationsAttempt((attempt) => attempt + 1)}><RefreshCw size={15} />Réessayer</button>}</div>}

      {isLoadingStations ? <section className="depots-page-state"><LoaderCircle className="depots-page-spinner" size={30} />Chargement des stations...</section> : !stationsError && stations.length === 0 ? <section className="depots-page-state empty"><Building2 size={32} /><h2>Aucune station configurée</h2><p>Créez d’abord une station avant de gérer ses dépôts.</p></section> : !stationsError && <>
        <section className="depots-page-station-selector"><label htmlFor="depot-station"><span>Station</span><select id="depot-station" value={selectedStationId} onChange={handleStationChange}><option value="">Sélectionner une station</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label>{stations.length > 1 && !selectedStationId && <p>Sélectionnez la station dont vous souhaitez gérer les dépôts.</p>}</section>

        {!selectedStationId ? <section className="depots-page-state empty"><Warehouse size={32} /><h2>Aucune station sélectionnée</h2><p>Choisissez une station dans le sélecteur ci-dessus.</p></section> : <>
          {depotsError && <div className="depots-page-alert error" role="alert"><AlertCircle size={18} /><span>{depotsError}</span><button type="button" onClick={() => setDepotsAttempt((attempt) => attempt + 1)}><RefreshCw size={15} />Réessayer</button></div>}
          {!depotsError && !isLoadingDepots && depots.length > 0 && <div className="depots-page-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder="Rechercher par nom, code ou emplacement" aria-label="Rechercher un dépôt" /></label><span>{filteredDepots.length} dépôt{filteredDepots.length > 1 ? "s" : ""}</span></div>}
          {isLoadingDepots ? <section className="depots-page-state"><LoaderCircle className="depots-page-spinner" size={30} />Chargement des dépôts...</section> : !depotsError && (depots.length === 0 ? <section className="depots-page-state empty"><Warehouse size={32} /><h2>Aucun dépôt n’est encore configuré pour cette station.</h2><p>Ajoutez le premier dépôt de {selectedStation?.name}.</p><button type="button" className="depots-page-primary" onClick={() => setDepotModal(null)}><Plus size={17} />Ajouter un dépôt</button></section> : filteredDepots.length === 0 ? <section className="depots-page-state empty compact"><Search size={29} /><h2>Aucun dépôt trouvé</h2><p>Essayez un autre nom, code ou emplacement.</p></section> : <section className="depots-page-grid">{filteredDepots.map((depot) => <article key={depot.id} className={!depot.active ? "inactive" : ""}><div className="depots-page-card-heading"><span><Warehouse size={20} /></span><div><small>{depot.code}</small><h2>{depot.name}</h2></div></div>{depot.location && <p><MapPin size={15} />{depot.location}</p>}<em className={depot.active ? "active" : "inactive"}>{depot.active ? "Actif" : "Inactif"}</em><div className="depots-page-actions"><button type="button" onClick={() => { setSuccessMessage(""); setDepotModal(depot); }}><Pencil size={15} />Modifier</button></div></article>)}</section>)}
        </>}
      </>}
    </main>
    {depotModal !== undefined && selectedStation && <DepotModal key={depotModal?.id || "new-depot"} isOpen organizationId={organizationId} stationId={selectedStation.id} depot={depotModal} onClose={() => setDepotModal(undefined)} onSaved={handleDepotSaved} />}
  </SupervisorLayout>;
}

export default DepotsPage;
