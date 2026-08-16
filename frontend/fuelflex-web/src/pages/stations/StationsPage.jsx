import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { AlertCircle, Building2, CheckCircle2, ExternalLink, LoaderCircle, MapPin, Pencil, Plus, RefreshCw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import StationModal from "../../features/station/components/StationModal";
import { getLocaleForLanguage } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import { getStations } from "../../services/station/stationService";
import "./StationsPage.css";

function StationsPage() {
  const { t, i18n } = useTranslation(["stations", "common"]);
  const navigate = useNavigate();
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [editingStation, setEditingStation] = useState(null);
  const [isLoading, setIsLoading] = useState(Boolean(organizationId));
  const [errorMessage, setErrorMessage] = useState(organizationId ? null : { key: "stations:feedback.organizationMissing" });
  const [successMessage, setSuccessMessage] = useState(null);
  const [loadAttempt, setLoadAttempt] = useState(0);
  const locale = getLocaleForLanguage(i18n.resolvedLanguage);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => {
      setIsLoading(true);
      setErrorMessage(null);
      return getStations(organizationId, { signal: controller.signal });
    }).then((loadedStations) => setStations(Array.isArray(loadedStations) ? loadedStations : []))
      .catch((error) => {
        if (error?.name !== "AbortError") {
          setErrorMessage(error?.message ? { text: error.message } : { key: "stations:feedback.loadFailed" });
        }
      })
      .finally(() => { if (!controller.signal.aborted) setIsLoading(false); });
    return () => controller.abort();
  }, [loadAttempt, organizationId]);

  const filteredStations = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase(locale);
    if (!query) return stations;
    return stations.filter((station) => [station.name, station.code, station.city]
      .some((value) => value?.toLocaleLowerCase(locale).includes(query)));
  }, [locale, searchTerm, stations]);

  const handleStationSaved = () => {
    setEditingStation(null);
    setSuccessMessage({ key: "stations:feedback.updated" });
    setLoadAttempt((attempt) => attempt + 1);
  };

  return <SupervisorLayout>
    <main className="stations-page">
      <header className="stations-page-header">
        <div><span>{t("stations:page.eyebrow")}</span><h1>{t("stations:page.title")}</h1><p>{t("stations:page.description")}</p></div>
        <button type="button" className="stations-page-primary" onClick={() => navigate("/superviseur/stations/nouvelle")}><Plus size={17} />{t("stations:page.new")}</button>
      </header>

      {successMessage && <div className="stations-page-alert success" role="status"><CheckCircle2 size={18} />{renderMessage(successMessage)}</div>}
      {errorMessage && <div className="stations-page-alert error" role="alert"><AlertCircle size={18} /><span>{renderMessage(errorMessage)}</span>{organizationId && <button type="button" onClick={() => setLoadAttempt((attempt) => attempt + 1)}><RefreshCw size={15} />{t("common:actions.retry")}</button>}</div>}

      {!errorMessage && <div className="stations-page-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder={t("stations:page.searchPlaceholder")} aria-label={t("stations:page.searchAriaLabel")} /></label><span>{t("stations:page.count", { count: filteredStations.length })}</span></div>}

      {isLoading ? <section className="stations-page-loading"><LoaderCircle className="stations-page-spinner" size={30} />{t("stations:page.loading")}</section> : !errorMessage && (stations.length === 0 ? <section className="stations-page-empty"><Building2 size={34} /><h2>{t("stations:page.emptyTitle")}</h2><p>{t("stations:page.emptyDescription")}</p><button type="button" className="stations-page-primary" onClick={() => navigate("/superviseur/stations/nouvelle")}><Plus size={17} />{t("stations:page.create")}</button></section> : filteredStations.length === 0 ? <section className="stations-page-empty compact"><Search size={30} /><h2>{t("stations:page.noResultTitle")}</h2><p>{t("stations:page.noResultDescription")}</p></section> : <section className="stations-page-grid">{filteredStations.map((station) => {
        const location = [station.city, station.province].filter(Boolean).join(" · ");
        return <article key={station.id} className={!station.active ? "inactive" : ""}>
          <div className="stations-page-card-top"><span className="stations-page-icon"><Building2 size={21} /></span><div><small>{station.code}</small><h2>{station.name}</h2><p>{t(`stations:types.${station.type}`, { defaultValue: station.type })}</p></div></div>
          {location && <p className="stations-page-location"><MapPin size={15} />{location}</p>}
          <div className="stations-page-badges"><span className={`status ${String(station.status).toLowerCase()}`}>{t(`stations:status.${station.status}`, { defaultValue: station.status })}</span><span className={station.active ? "active" : "inactive"}>{t(station.active ? "stations:availability.active" : "stations:availability.inactive")}</span></div>
          <div className="stations-page-actions"><button type="button" className="open" disabled title={t("stations:page.openUnavailable")}><ExternalLink size={15} />{t("stations:page.open")}</button><button type="button" onClick={() => { setSuccessMessage(null); setEditingStation(station); }}><Pencil size={15} />{t("stations:page.edit")}</button></div>
        </article>;
      })}</section>)}
    </main>
    {editingStation && <StationModal key={editingStation.id} isOpen organizationId={organizationId} station={editingStation} onClose={() => setEditingStation(null)} onSaved={handleStationSaved} />}
  </SupervisorLayout>;
}

export default StationsPage;
