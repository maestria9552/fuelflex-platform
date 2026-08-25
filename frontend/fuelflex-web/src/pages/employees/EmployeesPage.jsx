import { useCallback, useEffect, useState } from "react";
import { AlertCircle, CheckCircle2, ChevronLeft, ChevronRight, Eye, LoaderCircle, Pencil, Plus, RefreshCw, Search, UserRoundCog } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import ConfirmationModal from "../../components/modal/ConfirmationModal";
import EmployeeFormModal from "../../features/employee/components/EmployeeFormModal";
import { getEmployeePermissions } from "../../features/employee/employeePermissions";
import { getStoredUser } from "../../services/auth/authStorage";
import { getAssignableEmployeeRoles, getEmployees, updateEmployeeStatus } from "../../services/employee/employeeService";
import { getActiveStations } from "../../services/station/stationService";
import "./EmployeesPage.css";

const PAGE_SIZE = 10;

function EmployeesPage() {
  const { t, i18n } = useTranslation(["employees", "common", "pumpAttendantValidation"]);
  const navigate = useNavigate();
  const permissions = getEmployeePermissions();
  const organizationId = getStoredUser()?.organizationId;
  const [pageData, setPageData] = useState({ content: [], page: 0, totalElements: 0, totalPages: 0, first: true, last: true });
  const [roles, setRoles] = useState([]);
  const [stations, setStations] = useState([]);
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [roleCode, setRoleCode] = useState("");
  const [enabled, setEnabled] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [formEmployee, setFormEmployee] = useState(undefined);
  const [formOpen, setFormOpen] = useState(false);
  const [statusEmployee, setStatusEmployee] = useState(null);
  const [statusLoading, setStatusLoading] = useState(false);
  const [statusError, setStatusError] = useState("");
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const timer = window.setTimeout(() => { setPage(0); setSearch(searchInput.trim()); }, 350);
    return () => window.clearTimeout(timer);
  }, [searchInput]);

  const load = useCallback(async (signal) => {
    void reload;
    setIsLoading(true); setError("");
    try {
      const [employees, assignableRoles, activeStations] = await Promise.all([
        getEmployees({ page, size: PAGE_SIZE, search, roleCode, enabled }, { signal }),
        getAssignableEmployeeRoles({ signal }),
        organizationId ? getActiveStations(organizationId, { signal }) : [],
      ]);
      setPageData(employees);
      setRoles(Array.isArray(assignableRoles) ? assignableRoles : []);
      setStations(Array.isArray(activeStations) ? activeStations : []);
    } catch (requestError) {
      if (requestError?.name !== "AbortError") setError(requestError?.message || t("employees:errors.load"));
    } finally { if (!signal.aborted) setIsLoading(false); }
  }, [enabled, organizationId, page, reload, roleCode, search, t]);

  useEffect(() => { const controller = new AbortController(); Promise.resolve().then(() => load(controller.signal)); return () => controller.abort(); }, [load]);

  const handleSaved = (saved, editing) => {
    setFormOpen(false); setFormEmployee(undefined);
    setSuccess(t(editing ? "employees:feedback.updated" : (saved?.invitationSent === false ? "employees:feedback.createdNoEmail" : "employees:feedback.created")));
    setReload((value) => value + 1);
  };

  const handleStatus = async () => {
    setStatusLoading(true); setStatusError("");
    try {
      await updateEmployeeStatus(statusEmployee.id, !statusEmployee.enabled);
      setSuccess(t(statusEmployee.enabled ? "employees:feedback.disabled" : "employees:feedback.enabled"));
      setStatusEmployee(null); setReload((value) => value + 1);
    } catch (requestError) { setStatusError(requestError?.status === 409 ? t("employees:errors.statusConflict") : requestError?.message || t("employees:errors.status")); }
    finally { setStatusLoading(false); }
  };

  return <SupervisorLayout><main className="employees-page">
    <header className="employees-header"><div><span>{t("employees:list.eyebrow")}</span><h1>{t("employees:list.title")}</h1><p>{t("employees:list.description")}</p></div>{permissions.canCreate && <button type="button" className="employees-primary" onClick={() => { setFormEmployee(undefined); setFormOpen(true); }}><Plus size={17} />{t("employees:list.new")}</button>}</header>
    {success && <div className="employees-alert success" role="status"><CheckCircle2 size={18} />{success}<button type="button" onClick={() => setSuccess("")} aria-label={t("employees:actions.dismiss")}>×</button></div>}
    {error && <div className="employees-alert error" role="alert"><AlertCircle size={18} /><span>{error}</span><button type="button" onClick={() => setReload((value) => value + 1)}><RefreshCw size={15} />{t("common:actions.retry")}</button></div>}
    <section className="employees-toolbar" aria-label={t("employees:list.filters")}>
      <label className="employees-search"><Search size={17} /><input type="search" value={searchInput} onChange={(event) => setSearchInput(event.target.value)} placeholder={t("employees:list.search")} aria-label={t("employees:list.search")} /></label>
      <label><span>{t("employees:fields.role")}</span><select value={roleCode} onChange={(event) => { setPage(0); setRoleCode(event.target.value); }}><option value="">{t("employees:list.allRoles")}</option>{roles.map((role) => <option key={role.code} value={role.code}>{t(`employees:roles.${role.code}`, { defaultValue: role.name })}</option>)}</select></label>
      <label><span>{t("employees:fields.status")}</span><select value={enabled} onChange={(event) => { setPage(0); setEnabled(event.target.value); }}><option value="">{t("employees:list.allStatuses")}</option><option value="true">{t("employees:status.active")}</option><option value="false">{t("employees:status.inactive")}</option></select></label>
    </section>
    {isLoading ? <section className="employees-state"><LoaderCircle className="employee-spin" size={28} />{t("employees:list.loading")}</section> : !error && pageData.content.length === 0 ? <section className="employees-state empty"><UserRoundCog size={35} /><h2>{t("employees:list.emptyTitle")}</h2><p>{t("employees:list.emptyDescription")}</p></section> : !error && <><div className="employees-table-wrap"><table><thead><tr><th>{t("employees:fields.employee")}</th><th>{t("employees:fields.phone")}</th><th>{t("employees:fields.role")}</th><th>{t("employees:fields.status")}</th><th>{t("employees:fields.createdAt")}</th><th><span className="sr-only">{t("employees:fields.actions")}</span></th></tr></thead><tbody>{pageData.content.map((employee) => <tr key={employee.id}><td><strong>{employee.firstName} {employee.lastName}</strong><small>{employee.email}</small></td><td>{employee.phoneNumber}</td><td><span className="employees-badge role">{t(`employees:roles.${employee.roleCode}`)}</span></td><td><span className={`employees-badge ${employee.enabled ? "active" : "inactive"}`}>{t(employee.roleCode === "PUMP_ATTENDANT" && employee.pumpAttendantValidationStatus !== "VALIDATED"
  ? `pumpAttendantValidation:candidateStatus.${employee.pumpAttendantValidationStatus}`
  : (employee.invitationPending ? "employees:status.pending" : (employee.enabled ? "employees:status.active" : "employees:status.inactive")))}</span></td><td>{employee.createdAt ? new Intl.DateTimeFormat(i18n.resolvedLanguage === "en" ? "en" : "fr-CD", { dateStyle: "medium" }).format(new Date(employee.createdAt)) : "—"}</td><td><div className="employees-actions"><button type="button" onClick={() => navigate(`/superviseur/employes/${employee.id}`)} title={t("employees:actions.view")}><Eye size={16} /></button>{permissions.canUpdate && (employee.roleCode !== "PUMP_ATTENDANT" || employee.pumpAttendantValidationStatus === "VALIDATED") && <button type="button" onClick={() => { setFormEmployee(employee); setFormOpen(true); }} title={t("employees:actions.edit")}><Pencil size={16} /></button>}{permissions.canDisable && (employee.roleCode !== "PUMP_ATTENDANT" || employee.pumpAttendantValidationStatus === "VALIDATED") && <button type="button" className={employee.enabled ? "danger" : "success"} onClick={() => setStatusEmployee(employee)}>{t(employee.enabled ? "employees:actions.disable" : "employees:actions.enable")}</button>}</div></td></tr>)}</tbody></table></div>
      <nav className="employees-pagination" aria-label={t("employees:list.pagination")}><span>{t("employees:list.total", { count: pageData.totalElements })}</span><div><button type="button" disabled={pageData.first} onClick={() => setPage((value) => value - 1)} aria-label={t("employees:list.previous")}><ChevronLeft size={17} /></button><span>{t("employees:list.page", { current: pageData.page + 1, total: Math.max(pageData.totalPages, 1) })}</span><button type="button" disabled={pageData.last} onClick={() => setPage((value) => value + 1)} aria-label={t("employees:list.next")}><ChevronRight size={17} /></button></div></nav></>}
  </main>
  {formOpen && <EmployeeFormModal employee={formEmployee} roles={roles} stations={stations} onClose={() => setFormOpen(false)} onSaved={handleSaved} />}
  <ConfirmationModal isOpen={Boolean(statusEmployee)} variant={statusEmployee?.enabled ? "danger" : "default"} title={t(statusEmployee?.enabled ? "employees:statusConfirm.disableTitle" : "employees:statusConfirm.enableTitle")} description={t(statusEmployee?.enabled ? "employees:statusConfirm.disableDescription" : "employees:statusConfirm.enableDescription", { name: `${statusEmployee?.firstName || ""} ${statusEmployee?.lastName || ""}`.trim() })} confirmLabel={t(statusEmployee?.enabled ? "employees:actions.disable" : "employees:actions.enable")} isLoading={statusLoading} errorMessage={statusError} onClose={() => { setStatusEmployee(null); setStatusError(""); }} onConfirm={handleStatus} />
  </SupervisorLayout>;
}

export default EmployeesPage;
