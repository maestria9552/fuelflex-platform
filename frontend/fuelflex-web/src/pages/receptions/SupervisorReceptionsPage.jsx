import { Eye, RefreshCw } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import SupervisorLayout from "../../components/layout/SupervisorLayout";
import { getReceptionPermissions } from "../../services/auth/permissionService";
import { getSupervisorReceptions } from "../../services/reception/receptionService";
import "./ManagerReceptions.css";

const filters = { all: null, action: "PENDING_SUPERVISOR_APPROVAL", information: "INFORMATION" };
const date = (value, locale) => value ? new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "—";

export default function SupervisorReceptionsPage() {
  const { t, i18n } = useTranslation(["receptions", "common"]);
  const locale = i18n.language === "en" ? "en-US" : "fr-FR";
  const permissions = getReceptionPermissions();
  const [page, setPage] = useState({ content: [], totalPages: 0, number: 0 });
  const [filter, setFilter] = useState("action");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const load = useCallback(async () => { setLoading(true); setError(null); try { setPage(await getSupervisorReceptions({ size: 50 })); } catch (cause) { setError(cause); } finally { setLoading(false); } }, []);
  // This effect synchronizes the page with the protected API.
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => { load(); }, [load]);
  const rows = useMemo(() => (page.content || []).filter((reception) => {
    if (!filters[filter]) return true;
    if (filter === "information") return reception.status !== "PENDING_SUPERVISOR_APPROVAL";
    return reception.status === filters[filter];
  }), [filter, page.content]);

  return <SupervisorLayout><main className="receptions-page"><header className="receptions-header"><div><p className="receptions-eyebrow">{t("receptions:eyebrow")}</p><h1>{t("receptions:supervisor.list.title")}</h1><p>{t("receptions:supervisor.list.subtitle")}</p></div></header><div className="receptions-tabs">{["action", "information", "all"].map((key) => <button key={key} type="button" className={filter === key ? "receptions-tab active" : "receptions-tab"} onClick={() => setFilter(key)}>{t(`receptions:supervisor.filters.${key}`)}</button>)}</div>{loading ? <div className="receptions-state">{t("receptions:form.loading")}</div> : error ? <div className="receptions-state"><p>{error.status === 403 ? t("receptions:errors.forbidden") : error.message || t("receptions:errors.load")}</p><button type="button" className="receptions-secondary" onClick={load}><RefreshCw size={16}/>{t("common:actions.retry")}</button></div> : !permissions.canView || !rows.length ? <div className="receptions-empty">{t("receptions:supervisor.list.empty")}</div> : <table className="receptions-table"><thead><tr><th>{t("receptions:list.order")}</th><th>{t("receptions:list.station")}</th><th>{t("receptions:supervisor.list.manager")}</th><th>{t("receptions:list.status")}</th><th>{t("receptions:list.date")}</th><th/></tr></thead><tbody>{rows.map((reception) => <tr key={reception.id}><td><strong>{reception.receptionNumber}</strong><br/><small>{reception.purchaseOrderNumber}</small></td><td>{reception.stationName || "—"}</td><td>{reception.createdBy ? `${reception.createdBy.firstName} ${reception.createdBy.lastName}` : "—"}</td><td><span className={`receptions-status receptions-status-${reception.status}`}>{t(`receptions:statuses.${reception.status}`)}</span>{reception.status === "PENDING_SUPERVISOR_APPROVAL" && <small className="receptions-action-label">{t("receptions:supervisor.actionRequired")}</small>}</td><td>{date(reception.submittedAt || reception.updatedAt, locale)}</td><td><Link className="receptions-link" to={`/superviseur/receptions/${reception.id}`}><Eye size={15}/>{t("receptions:list.view")}</Link></td></tr>)}</tbody></table>}</main></SupervisorLayout>;
}
