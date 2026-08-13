import { useEffect, useMemo, useState } from "react";
import { AlertCircle, Building2, CheckCircle2, CircleGauge, LoaderCircle, Pencil, Plus, RefreshCw, Search, Warehouse } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import TankModal from "../../features/tank/components/TankModal";
import { getStoredUser } from "../../services/auth/authStorage";
import { getDepots } from "../../services/depot/depotService";
import { getProducts } from "../../services/product/productService";
import { getStations } from "../../services/station/stationService";
import { getTanks } from "../../services/tank/tankService";
import "./TanksPage.css";

const STATUS_LABELS = { ACTIVE: "Active", INACTIVE: "Inactive", MAINTENANCE: "Maintenance", OUT_OF_SERVICE: "Hors service" };
const formatLiters = (value) => `${new Intl.NumberFormat("fr-FR", { maximumFractionDigits: 3 }).format(Number(value) || 0)} L`;

function PageState({ icon, title, text, action, compact = false }) {
  const Icon = icon === "station" ? Building2 : icon === "depot" ? Warehouse : icon === "tank" ? CircleGauge : icon === "search" ? Search : null;
  return <section className={`tanks-page-state ${title ? "empty" : ""} ${compact ? "compact" : ""}`}>{icon === "loading" ? <LoaderCircle className="tanks-page-spinner" size={30} /> : Icon ? <Icon size={32} /> : null}{title && <h2>{title}</h2>}<p>{text}</p>{action}</section>;
}

function ErrorState({ message, onRetry }) {
  return <div className="tanks-page-alert error" role="alert"><AlertCircle size={18} /><span>{message}</span><button type="button" onClick={onRetry}><RefreshCw size={15} />Réessayer</button></div>;
}

function TanksPage() {
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
  const [stationsError, setStationsError] = useState(organizationId ? "" : "Aucune société n’est associée à ce compte.");
  const [depotsError, setDepotsError] = useState("");
  const [productsError, setProductsError] = useState("");
  const [tanksError, setTanksError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [stationsAttempt, setStationsAttempt] = useState(0);
  const [depotsAttempt, setDepotsAttempt] = useState(0);
  const [productsAttempt, setProductsAttempt] = useState(0);
  const [tanksAttempt, setTanksAttempt] = useState(0);

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
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingProducts(true); setProductsError(""); return getProducts(organizationId, { signal: controller.signal }); })
      .then((result) => setProducts(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setProductsError(error?.message || "Impossible de charger les produits."); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingProducts(false); });
    return () => controller.abort();
  }, [organizationId, productsAttempt]);

  useEffect(() => {
    if (!organizationId || !selectedStationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingDepots(true); setDepotsError(""); return getDepots(organizationId, selectedStationId, { signal: controller.signal }); })
      .then((result) => { const loaded = Array.isArray(result) ? result : []; setDepots(loaded); setSelectedDepotId((current) => loaded.length === 1 ? loaded[0].id : loaded.some(({ id }) => id === current) ? current : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setDepotsError(error?.message || "Impossible de charger les dépôts."); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingDepots(false); });
    return () => controller.abort();
  }, [depotsAttempt, organizationId, selectedStationId]);

  useEffect(() => {
    if (!organizationId || !selectedStationId || !selectedDepotId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingTanks(true); setTanksError(""); return getTanks(organizationId, selectedStationId, selectedDepotId, { signal: controller.signal }); })
      .then((result) => setTanks(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setTanksError(error?.message || "Impossible de charger les citernes."); })
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
    const query = searchTerm.trim().toLocaleLowerCase("fr");
    return query ? tanks.filter((tank) => [tank.name, tank.code, tank.productName, tank.location].some((value) => value?.toLocaleLowerCase("fr").includes(query))) : tanks;
  }, [searchTerm, tanks]);

  const handleStationChange = (event) => { setSelectedStationId(event.target.value); setSelectedDepotId(""); setDepots([]); setTanks([]); setSearchTerm(""); setTankModal(undefined); setSuccessMessage(""); setDepotsError(""); setTanksError(""); };
  const handleDepotChange = (event) => { setSelectedDepotId(event.target.value); setTanks([]); setSearchTerm(""); setTankModal(undefined); setSuccessMessage(""); setTanksError(""); };
  const handleTankSaved = (_, wasUpdate) => { setTankModal(undefined); setSuccessMessage(wasUpdate ? "La citerne a été modifiée avec succès." : "La citerne a été créée avec succès."); setTanksAttempt((attempt) => attempt + 1); };
  const openCreate = () => { setSuccessMessage(""); setTankModal(null); };
  const canCreate = selectedStation && selectedDepot && !isLoadingProducts && !productsError && activeProducts.length > 0;

  return <SupervisorLayout><main className="tanks-page">
    <header className="tanks-page-header"><div><span>CONFIGURATION DU RÉSEAU</span><h1>Citernes</h1><p>Gérez les citernes rattachées aux dépôts de vos stations.</p></div>{canCreate && <button type="button" className="tanks-page-primary" onClick={openCreate}><Plus size={17} />Ajouter une citerne</button>}</header>
    {successMessage && <div className="tanks-page-alert success" role="status"><CheckCircle2 size={18} />{successMessage}</div>}
    {stationsError && <div className="tanks-page-alert error" role="alert"><AlertCircle size={18} /><span>{stationsError}</span>{organizationId && <button type="button" onClick={() => setStationsAttempt((value) => value + 1)}><RefreshCw size={15} />Réessayer</button>}</div>}
    {isLoadingStations ? <PageState icon="loading" text="Chargement des stations..." /> : !stationsError && stations.length === 0 ? <PageState icon="station" title="Aucune station configurée" text="Créez d’abord une station avant de gérer ses citernes." /> : !stationsError && <>
      <section className="tanks-page-selectors"><label><span>Station</span><select value={selectedStationId} onChange={handleStationChange}><option value="">Sélectionner une station</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label><label><span>Dépôt</span><select value={selectedDepotId} onChange={handleDepotChange} disabled={!selectedStationId || isLoadingDepots || Boolean(depotsError)}><option value="">{isLoadingDepots ? "Chargement..." : "Sélectionner un dépôt"}</option>{depots.map((depot) => <option key={depot.id} value={depot.id}>{depot.name} — {depot.code}</option>)}</select></label></section>
      {!selectedStationId ? <PageState title="Aucune station sélectionnée" text="Choisissez la station dont vous souhaitez gérer les citernes." /> : isLoadingDepots ? <PageState icon="loading" text="Chargement des dépôts..." /> : depotsError ? <ErrorState message={depotsError} onRetry={() => setDepotsAttempt((value) => value + 1)} /> : depots.length === 0 ? <PageState icon="depot" title="Aucun dépôt configuré" text="Aucun dépôt n’est configuré pour cette station. Créez d’abord un dépôt avant d’ajouter une citerne." /> : !selectedDepotId ? <PageState icon="depot" title="Aucun dépôt sélectionné" text="Choisissez le dépôt dont vous souhaitez gérer les citernes." /> : <>
        {isLoadingProducts ? <div className="tanks-page-inline-state"><LoaderCircle className="tanks-page-spinner" size={18} />Chargement des produits...</div> : productsError ? <div className="tanks-page-alert error" role="alert"><AlertCircle size={18} /><span>{productsError}</span><button type="button" onClick={() => setProductsAttempt((value) => value + 1)}><RefreshCw size={15} />Réessayer</button></div> : activeProducts.length === 0 && <div className="tanks-page-alert warning" role="status"><AlertCircle size={18} />Aucun produit actif n’est disponible. Activez ou créez un produit avant d’ajouter une citerne.</div>}
        {tanksError && <ErrorState message={tanksError} onRetry={() => setTanksAttempt((value) => value + 1)} />}
        {!tanksError && !isLoadingTanks && tanks.length > 0 && <div className="tanks-page-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder="Rechercher par nom, code, produit ou emplacement" aria-label="Rechercher une citerne" /></label><span>{filteredTanks.length} citerne{filteredTanks.length > 1 ? "s" : ""}</span></div>}
        {isLoadingTanks ? <PageState icon="loading" text="Chargement des citernes..." /> : !tanksError && (tanks.length === 0 ? <PageState icon="tank" title="Aucune citerne configurée" text={`Ajoutez la première citerne du dépôt ${selectedDepot?.name || "sélectionné"}.`} action={canCreate ? <button type="button" className="tanks-page-primary" onClick={openCreate}><Plus size={17} />Ajouter une citerne</button> : null} /> : filteredTanks.length === 0 ? <PageState icon="search" title="Aucune citerne trouvée" text="Essayez un autre nom, code, produit ou emplacement." compact /> : <section className="tanks-page-grid">{filteredTanks.map((tank) => <article key={tank.id} className={!tank.active ? "inactive" : ""}><div className="tanks-page-card-heading"><span><CircleGauge size={20} /></span><div><small>{tank.code}</small><h2>{tank.name}</h2><p>{tank.productName}</p></div></div><div className="tanks-page-levels"><strong>{formatLiters(tank.capacityLiters)}</strong><span>Min. {formatLiters(tank.minimumLevelLiters)} · Max. {formatLiters(tank.maximumLevelLiters)}</span></div><div className="tanks-page-badges"><em className={`status ${(tank.status || "").toLowerCase()}`}>{STATUS_LABELS[tank.status] || tank.status}</em><em className={tank.active ? "active" : "inactive"}>{tank.active ? "Actif" : "Inactif"}</em></div><div className="tanks-page-actions"><button type="button" onClick={() => { setSuccessMessage(""); setTankModal(tank); }}><Pencil size={15} />Modifier</button></div></article>)}</section>)}</>}
    </>}
  </main>{tankModal !== undefined && selectedStation && selectedDepot && <TankModal key={tankModal?.id || "new-tank"} isOpen organizationId={organizationId} stationId={selectedStation.id} depots={[selectedDepot]} products={modalProducts} tank={tankModal} fixedDepotId={selectedDepot.id} onClose={() => setTankModal(undefined)} onSaved={handleTankSaved} />}</SupervisorLayout>;
}

export default TanksPage;
