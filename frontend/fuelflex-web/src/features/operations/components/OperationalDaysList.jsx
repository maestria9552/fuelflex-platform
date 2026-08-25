import { CalendarDays, ChevronRight, Plus, RefreshCw } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";

import AppModal from "../../../components/modal/AppModal";
import {
  getManagerStations,
  getOperationalDays,
  openOperationalDay,
} from "../../../services/operations/operationalService";
import { getOperationalPermissions } from "../../../services/auth/permissionService";

function localBusinessDate() {
  const now = new Date();
  const offset = now.getTimezoneOffset() * 60000;
  return new Date(now.getTime() - offset).toISOString().slice(0, 10);
}

export default function OperationalDaysList({ role }) {
  const { t, i18n } = useTranslation(["operations", "common"]);
  const navigate = useNavigate();
  const permissions = getOperationalPermissions();
  const isManager = role === "manager";
  const routeBase = isManager ? "/gerant/operations" : "/superviseur/operations";
  const locale = i18n.language === "en" ? "en-US" : "fr-CD";
  const [days, setDays] = useState([]);
  const [stations, setStations] = useState([]);
  const [stationId, setStationId] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [formError, setFormError] = useState("");
  const [openModal, setOpenModal] = useState(false);
  const [businessDate, setBusinessDate] = useState(localBusinessDate);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [loadedDays, managerStations] = await Promise.all([
        getOperationalDays(),
        isManager ? getManagerStations() : Promise.resolve([]),
      ]);
      const safeDays = Array.isArray(loadedDays) ? loadedDays : [];
      const accessibleStations = isManager
        ? (Array.isArray(managerStations) ? managerStations : [])
        : Array.from(
            new Map(
              safeDays
                .filter((day) => day.station?.id)
                .map((day) => [day.station.id, day.station])
            ).values()
          );
      setDays(safeDays);
      setStations(accessibleStations);
      setStationId((current) =>
        current && accessibleStations.some((station) => station.id === current)
          ? current
          : accessibleStations[0]?.id || ""
      );
    } catch (requestError) {
      setError(requestError.message || t("operations:errors.loadDays"));
    } finally {
      setLoading(false);
    }
  }, [isManager, t]);

  // Synchronise la page avec les read-models protégés.
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load();
  }, [load]);

  const filteredDays = useMemo(
    () =>
      days
        .filter((day) => !stationId || day.station?.id === stationId)
        .sort((left, right) =>
          String(right.businessDate).localeCompare(String(left.businessDate))
        ),
    [days, stationId]
  );

  const currentOpenDay = filteredDays.find((day) => day.status === "OPEN");

  const submitOpenDay = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setFormError("");
    try {
      const created = await openOperationalDay({ stationId, businessDate });
      setOpenModal(false);
      navigate(`${routeBase}/${created.id}`);
    } catch (requestError) {
      setFormError(requestError.message || t("operations:errors.openDay"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="operations-page">
      <header className="operations-page-header">
        <div>
          <p className="operations-eyebrow">{t("operations:eyebrow")}</p>
          <h1>{t("operations:days.title")}</h1>
          <p>{t(isManager ? "operations:days.managerSubtitle" : "operations:days.supervisorSubtitle")}</p>
        </div>
        <div className="operations-header-actions">
          <button className="ops-button ops-button-secondary" type="button" onClick={load} disabled={loading}>
            <RefreshCw size={17} className={loading ? "ops-spin" : ""} />
            {t("common:actions.refresh")}
          </button>
          {isManager && permissions.canOpenDay && (
            <button
              className="ops-button ops-button-primary"
              type="button"
              onClick={() => {
                setFormError("");
                setBusinessDate(localBusinessDate());
                setOpenModal(true);
              }}
              disabled={!stationId || Boolean(currentOpenDay)}
              title={currentOpenDay ? t("operations:days.openAlready") : undefined}
            >
              <Plus size={17} />
              {t("operations:days.openAction")}
            </button>
          )}
        </div>
      </header>

      <div className="operations-toolbar">
        <label>
          <span>{t("operations:fields.station")}</span>
          <select value={stationId} onChange={(event) => setStationId(event.target.value)}>
            {stations.length === 0 && <option value="">{t("operations:days.noAccessibleStation")}</option>}
            {stations.map((station) => (
              <option value={station.id} key={station.id}>{station.name}</option>
            ))}
          </select>
        </label>
        {currentOpenDay && (
          <Link className="operations-current-day" to={`${routeBase}/${currentOpenDay.id}`}>
            <CalendarDays size={20} />
            <span>
              <small>{t("operations:days.current")}</small>
              <strong>{currentOpenDay.businessDate}</strong>
            </span>
            <ChevronRight size={18} />
          </Link>
        )}
      </div>

      {loading ? (
        <div className="operations-state">{t("common:feedback.loading")}</div>
      ) : error ? (
        <div className="operations-state operations-state-error">
          <p>{error}</p>
          <button className="ops-button ops-button-secondary" type="button" onClick={load}>
            {t("common:actions.retry")}
          </button>
        </div>
      ) : filteredDays.length === 0 ? (
        <div className="operations-empty">
          <CalendarDays size={34} />
          <h2>{t("operations:days.emptyTitle")}</h2>
          <p>{t("operations:days.emptyDescription")}</p>
        </div>
      ) : (
        <div className="operations-table-wrap">
          <table className="operations-table">
            <thead>
              <tr>
                <th>{t("operations:fields.businessDate")}</th>
                <th>{t("operations:fields.station")}</th>
                <th>{t("operations:fields.status")}</th>
                <th>{t("operations:fields.openedAt")}</th>
                <th>{t("operations:fields.openedBy")}</th>
                <th aria-label={t("common:actions.view")} />
              </tr>
            </thead>
            <tbody>
              {filteredDays.map((day) => (
                <tr key={day.id}>
                  <td><strong>{day.businessDate}</strong></td>
                  <td>{day.station?.name || "—"}</td>
                  <td><span className={`ops-status ops-status-${day.status}`}>{t(`operations:statuses.${day.status}`)}</span></td>
                  <td>{day.openedAt ? new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(day.openedAt)) : "—"}</td>
                  <td>{[day.openedBy?.firstName, day.openedBy?.lastName].filter(Boolean).join(" ") || "—"}</td>
                  <td><Link className="operations-row-link" to={`${routeBase}/${day.id}`}>{t("common:actions.view")}<ChevronRight size={16} /></Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <AppModal
        isOpen={openModal}
        title={t("operations:openDay.title")}
        description={t("operations:openDay.description")}
        size="sm"
        onClose={() => !submitting && setOpenModal(false)}
        footer={
          <>
            <button className="ops-button ops-button-secondary" type="button" onClick={() => setOpenModal(false)} disabled={submitting}>
              {t("common:actions.cancel")}
            </button>
            <button className="ops-button ops-button-primary" type="submit" form="open-operational-day-form" disabled={submitting || !stationId || !businessDate}>
              {submitting ? t("common:actions.saving") : t("operations:days.openAction")}
            </button>
          </>
        }
      >
        <form className="operations-form" id="open-operational-day-form" onSubmit={submitOpenDay}>
          <label>
            <span>{t("operations:fields.station")}</span>
            <select value={stationId} onChange={(event) => setStationId(event.target.value)} required>
              {stations.map((station) => <option value={station.id} key={station.id}>{station.name}</option>)}
            </select>
          </label>
          <label>
            <span>{t("operations:fields.businessDate")}</span>
            <input type="date" value={businessDate} onChange={(event) => setBusinessDate(event.target.value)} required />
          </label>
          {formError && <p className="operations-form-error" role="alert">{formError}</p>}
        </form>
      </AppModal>
    </section>
  );
}
