import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCircle, Building2, CheckCircle2, LoaderCircle, MapPin, Pencil, Plus, RefreshCw, Search, Warehouse } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import DepotModal from "../../features/depot/components/DepotModal";
import { getLocaleForLanguage } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import { getDepots } from "../../services/depot/depotService";
import { getStations } from "../../services/station/stationService";
import "./DepotsPage.css";

function DepotsPage() {
  const { t, i18n } = useTranslation(["depots", "common"]);
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [selectedStationId, setSelectedStationId] = useState("");
  const [depots, setDepots] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [depotModal, setDepotModal] = useState(undefined);
  const [isLoadingStations, setIsLoadingStations] = useState(Boolean(organizationId));
  const [isLoadingDepots, setIsLoadingDepots] = useState(false);
  const [stationsError, setStationsError] = useState(organizationId ? null : { key: "depots:feedback.organizationMissing" });
  const [depotsError, setDepotsError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [stationsAttempt, setStationsAttempt] = useState(0);
  const [depotsAttempt, setDepotsAttempt] = useState(0);
  const locale = getLocaleForLanguage(i18n.resolvedLanguage);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingStations(true); setStationsError(null); return getStations(organizationId, { signal: controller.signal }); })
      .then((result) => {
        const loadedStations = Array.isArray(result) ? result : [];
        setStations(loadedStations);
        setSelectedStationId((currentId) => loadedStations.length === 1 ? loadedStations[0].id : loadedStations.some((station) => station.id === currentId) ? currentId : "");
      }).catch((error) => { if (error?.name !== "AbortError") setStationsError(error?.message ? { text: error.message } : { key: "depots:feedback.stationsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingStations(false); });
    return () => controller.abort();
  }, [organizationId, stationsAttempt]);

  useEffect(() => {
    if (!organizationId || !selectedStationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setIsLoadingDepots(true); setDepotsError(null); return getDepots(organizationId, selectedStationId, { signal: controller.signal }); })
      .then((result) => setDepots(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setDepotsError(error?.message ? { text: error.message } : { key: "depots:feedback.depotsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setIsLoadingDepots(false); });
    return () => controller.abort();
  }, [depotsAttempt, organizationId, selectedStationId]);

  const selectedStation = stations.find((station) => station.id === selectedStationId) || null;
  const filteredDepots = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase(locale);
    if (!query) return depots;
    return depots.filter((depot) => [depot.name, depot.code, depot.location].some((value) => value?.toLocaleLowerCase(locale).includes(query)));
  }, [depots, locale, searchTerm]);

  const handleStationChange = (event) => { setSuccessMessage(null); setDepotModal(undefined); setDepots([]); setSearchTerm(""); setSelectedStationId(event.target.value); };
  const handleDepotSaved = (_, wasUpdate) => { setDepotModal(undefined); setSuccessMessage({ key: wasUpdate ? "depots:feedback.updated" : "depots:feedback.created" }); setDepotsAttempt((attempt) => attempt + 1); };

  return <SupervisorLayout>
    <main className="depots-page">
      <header className="depots-page-header"><div><span>{t("depots:page.eyebrow")}</span><h1>{t("depots:page.title")}</h1><p>{t("depots:page.description")}</p></div>{selectedStation && <button type="button" className="depots-page-primary" onClick={() => { setSuccessMessage(null); setDepotModal(null); }}><Plus size={17} />{t("depots:page.add")}</button>}</header>
      {successMessage && <div className="depots-page-alert success" role="status"><CheckCircle2 size={18} />{renderMessage(successMessage)}</div>}
      {stationsError && <div className="depots-page-alert error" role="alert"><AlertCircle size={18} /><span>{renderMessage(stationsError)}</span>{organizationId && <button type="button" onClick={() => setStationsAttempt((attempt) => attempt + 1)}><RefreshCw size={15} />{t("common:actions.retry")}</button>}</div>}

      {isLoadingStations ? <section className="depots-page-state"><LoaderCircle className="depots-page-spinner" size={30} />{t("depots:page.loadingStations")}</section> : !stationsError && stations.length === 0 ? <section className="depots-page-state empty"><Building2 size={32} /><h2>{t("depots:page.noStationTitle")}</h2><p>{t("depots:page.noStationDescription")}</p></section> : !stationsError && <>
        <section className="depots-page-station-selector"><label htmlFor="depot-station"><span>{t("depots:page.station")}</span><select id="depot-station" value={selectedStationId} onChange={handleStationChange}><option value="">{t("depots:page.selectStation")}</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label>{stations.length > 1 && !selectedStationId && <p>{t("depots:page.stationHelp")}</p>}</section>
        {!selectedStationId ? <section className="depots-page-state empty"><Warehouse size={32} /><h2>{t("depots:page.noSelectionTitle")}</h2><p>{t("depots:page.noSelectionDescription")}</p></section> : <>
          {depotsError && <div className="depots-page-alert error" role="alert"><AlertCircle size={18} /><span>{renderMessage(depotsError)}</span><button type="button" onClick={() => setDepotsAttempt((attempt) => attempt + 1)}><RefreshCw size={15} />{t("common:actions.retry")}</button></div>}
          {!depotsError && !isLoadingDepots && depots.length > 0 && <div className="depots-page-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder={t("depots:page.searchPlaceholder")} aria-label={t("depots:page.searchAriaLabel")} /></label><span>{t("depots:page.count", { count: filteredDepots.length })}</span></div>}
          {isLoadingDepots ? <section className="depots-page-state"><LoaderCircle className="depots-page-spinner" size={30} />{t("depots:page.loadingDepots")}</section> : !depotsError && (depots.length === 0 ? <section className="depots-page-state empty"><Warehouse size={32} /><h2>{t("depots:page.emptyTitle")}</h2><p>{t("depots:page.emptyDescription", { stationName: selectedStation?.name })}</p><button type="button" className="depots-page-primary" onClick={() => setDepotModal(null)}><Plus size={17} />{t("depots:page.add")}</button></section> : filteredDepots.length === 0 ? <section className="depots-page-state empty compact"><Search size={29} /><h2>{t("depots:page.noResultTitle")}</h2><p>{t("depots:page.noResultDescription")}</p></section> : <section className="depots-page-grid">{filteredDepots.map((depot) => <article key={depot.id} className={!depot.active ? "inactive" : ""}><div className="depots-page-card-heading"><span><Warehouse size={20} /></span><div><small>{depot.code}</small><h2>{depot.name}</h2></div></div>{depot.location && <p><MapPin size={15} />{depot.location}</p>}<em className={depot.active ? "active" : "inactive"}>{t(depot.active ? "depots:availability.active" : "depots:availability.inactive")}</em><div className="depots-page-actions"><button type="button" onClick={() => { setSuccessMessage(null); setDepotModal(depot); }}><Pencil size={15} />{t("depots:page.edit")}</button></div></article>)}</section>)}
        </>}
      </>}
    </main>
    {depotModal !== undefined && selectedStation && <DepotModal key={depotModal?.id || "new-depot"} isOpen organizationId={organizationId} stationId={selectedStation.id} depot={depotModal} onClose={() => setDepotModal(undefined)} onSaved={handleDepotSaved} />}
  </SupervisorLayout>;
}

export default DepotsPage;
