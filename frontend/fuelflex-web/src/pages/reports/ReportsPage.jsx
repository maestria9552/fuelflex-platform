import { Eye, FileText, Printer, RotateCcw } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";

import ManagerLayout from "../../components/layout/ManagerLayout";
import SupervisorLayout from "../../components/layout/SupervisorLayout";
import { getOperationalPermissions } from "../../services/auth/permissionService";
import { getOrganizationById } from "../../services/organization/organizationService";
import { getOperationalDaysForRole, getShiftAssignmentsForRole, getShiftReconciliations } from "../../services/operations/operationalService";
import "./Reports.css";

const INITIAL_FILTERS = { stationId: "", startDate: "", endDate: "", type: "all", attendantId: "" };
const personName = (person) => [person?.firstName, person?.lastName].filter(Boolean).join(" ") || "—";
const isWithinDates = (date, startDate, endDate) => (!startDate || date >= startDate) && (!endDate || date <= endDate);

function ReportsContent({ role }) {
  const { t } = useTranslation(["reports", "common"]);
  const permissions = getOperationalPermissions();
  const routeBase = role === "manager" ? "/gerant/operations" : "/superviseur/operations";
  const [days, setDays] = useState([]);
  const [sheets, setSheets] = useState([]);
  const [currencies, setCurrencies] = useState({});
  const [filters, setFilters] = useState(INITIAL_FILTERS);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const loadedDays = await getOperationalDaysForRole(role);
      const safeDays = Array.isArray(loadedDays) ? loadedDays : [];
      const organizationIds = [...new Set(safeDays.map((day) => day.organizationId).filter(Boolean))];
      const organizations = await Promise.all(organizationIds.map((id) => getOrganizationById(id)));
      const currencyByOrganization = Object.fromEntries(organizations.map((organization) => [organization.id, organization.defaultCurrency]));
      let availableSheets = [];
      if (permissions.canViewReconciliations && safeDays.length) {
        const rows = await Promise.all(safeDays.map(async (day) => {
          const [assignments, reconciliations] = await Promise.all([
            getShiftAssignmentsForRole(role, day.id),
            getShiftReconciliations(role, day.id),
          ]);
          const reconciliationByAssignment = new Map((Array.isArray(reconciliations) ? reconciliations : []).map((item) => [item.shiftAssignmentId, item]));
          return (Array.isArray(assignments) ? assignments : [])
            .filter((assignment) => assignment.status === "CLOSED" && reconciliationByAssignment.has(assignment.id))
            .map((assignment) => ({ assignment, reconciliation: reconciliationByAssignment.get(assignment.id), day }));
        }));
        availableSheets = rows.flat();
      }
      setDays(safeDays);
      setSheets(availableSheets);
      setCurrencies(currencyByOrganization);
    } catch (requestError) {
      setError(requestError.message || t("reports:states.loadError"));
    } finally {
      setLoading(false);
    }
  }, [permissions.canViewReconciliations, role, t]);

  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => { load(); }, [load]);

  const stations = useMemo(() => Array.from(new Map(days.filter((day) => day.station?.id).map((day) => [day.station.id, day.station])).values()), [days]);
  const attendants = useMemo(() => Array.from(new Map(sheets.map(({ assignment }) => [assignment.pumpAttendant?.id, assignment.pumpAttendant])).values()).filter(Boolean), [sheets]);
  const filteredDays = useMemo(() => days
    .filter((day) => !filters.stationId || day.station?.id === filters.stationId)
    .filter((day) => isWithinDates(day.businessDate, filters.startDate, filters.endDate))
    .sort((left, right) => String(right.businessDate).localeCompare(String(left.businessDate))), [days, filters.endDate, filters.startDate, filters.stationId]);
  const filteredSheets = useMemo(() => sheets
    .filter(({ day }) => !filters.stationId || day.station?.id === filters.stationId)
    .filter(({ day }) => isWithinDates(day.businessDate, filters.startDate, filters.endDate))
    .filter(({ assignment }) => !filters.attendantId || assignment.pumpAttendant?.id === filters.attendantId)
    .sort((left, right) => String(right.day.businessDate).localeCompare(String(left.day.businessDate))), [filters.attendantId, filters.endDate, filters.startDate, filters.stationId, sheets]);

  const showRjv = filters.type !== "sheet";
  const showSheets = filters.type !== "rjv";
  const hasAnyReport = days.length > 0 || sheets.length > 0;
  const hasFilteredReport = (showRjv && permissions.canViewRjv && filteredDays.length > 0) || (showSheets && permissions.canViewReconciliations && filteredSheets.length > 0);
  const updateFilter = (name) => (event) => setFilters((current) => ({ ...current, [name]: event.target.value }));

  return <section className="reports-page">
    <header className="reports-header"><div><p className="reports-eyebrow">{t("reports:eyebrow")}</p><h1>{t("reports:title")}</h1><p>{t("reports:description")}</p></div></header>
    <div className="reports-filters" aria-label={t("reports:filters.title")}>
      <label><span>{t("reports:fields.station")}</span><select value={filters.stationId} onChange={updateFilter("stationId")}><option value="">{t("reports:filters.allStations")}</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name}</option>)}</select></label>
      <label><span>{t("reports:fields.startDate")}</span><input type="date" value={filters.startDate} onChange={updateFilter("startDate")} /></label>
      <label><span>{t("reports:fields.endDate")}</span><input type="date" value={filters.endDate} onChange={updateFilter("endDate")} /></label>
      <label><span>{t("reports:fields.type")}</span><select value={filters.type} onChange={updateFilter("type")}><option value="all">{t("reports:types.all")}</option><option value="rjv">{t("reports:types.rjv")}</option><option value="sheet">{t("reports:types.sheet")}</option></select></label>
      <label><span>{t("reports:fields.attendant")}</span><select value={filters.attendantId} onChange={updateFilter("attendantId")} disabled={filters.type === "rjv"}><option value="">{t("reports:filters.allAttendants")}</option>{attendants.map((attendant) => <option key={attendant.id} value={attendant.id}>{personName(attendant)}</option>)}</select></label>
      <button type="button" className="reports-reset" onClick={() => setFilters(INITIAL_FILTERS)}><RotateCcw size={16} />{t("reports:actions.reset")}</button>
    </div>
    {loading ? <div className="reports-state">{t("reports:states.loading")}</div>
      : error ? <div className="reports-state reports-state-error"><p>{t("reports:states.loadError")}</p><small>{error}</small><button type="button" onClick={load}>{t("common:actions.retry")}</button></div>
      : stations.length === 0 ? <ReportState text={t("reports:states.noStation")} />
      : !hasAnyReport ? <ReportState text={t("reports:states.empty")} />
      : !hasFilteredReport ? <ReportState text={t("reports:states.noResults")} />
      : <>
        {showRjv && permissions.canViewRjv && filteredDays.length > 0 && <ReportSection title={t("reports:sections.rjv")}><table><thead><tr><th>{t("reports:fields.date")}</th><th>{t("reports:fields.station")}</th><th>{t("reports:fields.reference")}</th><th>{t("reports:fields.status")}</th><th>{t("reports:fields.currency")}</th><th>{t("reports:fields.actions")}</th></tr></thead><tbody>{filteredDays.map((day) => { const path = `${routeBase}/${day.id}/rjv/print`; return <tr key={day.id}><td>{day.businessDate}</td><td>{day.station?.name || "—"}</td><td className="reports-reference">{day.id}</td><td><span className={`reports-status reports-status-${day.status}`}>{t(`reports:statuses.${day.status}`)}</span></td><td>{currencies[day.organizationId] || "—"}</td><td><ReportActions path={path} t={t} /></td></tr>; })}</tbody></table></ReportSection>}
        {showSheets && permissions.canViewReconciliations && filteredSheets.length > 0 && <ReportSection title={t("reports:sections.sheets")}><table><thead><tr><th>{t("reports:fields.date")}</th><th>{t("reports:fields.station")}</th><th>{t("reports:fields.attendant")}</th><th>{t("reports:fields.attendantCode")}</th><th>{t("reports:fields.pump")}</th><th>{t("reports:fields.product")}</th><th>{t("reports:fields.reference")}</th><th>{t("reports:fields.actions")}</th></tr></thead><tbody>{filteredSheets.map(({ assignment, reconciliation, day }) => { const path = `${routeBase}/${day.id}/assignments/${assignment.id}/print`; return <tr key={assignment.id}><td>{day.businessDate}</td><td>{day.station?.name || "—"}</td><td>{reconciliation.pumpAttendantName || personName(assignment.pumpAttendant)}</td><td className="reports-reference">{assignment.pumpAttendant?.id || "—"}</td><td>{reconciliation.pumpName || assignment.pump?.name || "—"}</td><td>{reconciliation.productName || assignment.productName || "—"}</td><td>{assignment.operationalCode || assignment.id}</td><td><ReportActions path={path} t={t} /></td></tr>; })}</tbody></table></ReportSection>}
      </>}
  </section>;
}

function ReportState({ text }) { return <div className="reports-state"><FileText size={34} /><h2>{text}</h2></div>; }
function ReportSection({ title, children }) { return <section className="reports-section"><header><h2>{title}</h2></header><div className="reports-table-wrap">{children}</div></section>; }
function ReportActions({ path, t }) { return <div className="reports-actions"><Link to={path}><Eye size={15} />{t("reports:actions.view")}</Link><Link className="reports-action-primary" to={`${path}?print=1`}><Printer size={15} />{t("reports:actions.printPdf")}</Link></div>; }

export default function ReportsPage({ role }) {
  const Layout = role === "manager" ? ManagerLayout : SupervisorLayout;
  return <Layout><ReportsContent role={role} /></Layout>;
}
