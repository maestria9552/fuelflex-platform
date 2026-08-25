import { useCallback, useEffect, useState } from "react";
import { AlertCircle, ArrowLeft, ArrowRightLeft, Building2, CalendarDays, CheckCircle2, ChevronLeft, ChevronRight, LoaderCircle, MapPinPlus, Pencil, RefreshCw, UserRoundCog, UserX } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import ConfirmationModal from "../../components/modal/ConfirmationModal";
import AssignmentModal from "../../features/employee/components/AssignmentModal";
import EmployeeFormModal from "../../features/employee/components/EmployeeFormModal";
import { getEmployeePermissions } from "../../features/employee/employeePermissions";
import { formatDate, formatDateTime } from "../../i18n/formatters";
import { getEmployeeAssignments } from "../../services/employee/employeeAssignmentService";
import { getAssignableEmployeeRoles, getEmployee, resendEmployeeInvitation, updateEmployeeStatus } from "../../services/employee/employeeService";
import { getStoredUser } from "../../services/auth/authStorage";
import { getActiveStations } from "../../services/station/stationService";
import "./EmployeesPage.css";
import "./EmployeeDetailPage.css";

function EmployeeDetailPage() {
  const { employeeId } = useParams();
  const { t, i18n } = useTranslation(["employees", "common", "pumpAttendantValidation"]);
  const navigate = useNavigate();
  const permissions = getEmployeePermissions();
  const organizationId = getStoredUser()?.organizationId;
  const [employee, setEmployee] = useState(null);
  const [roles, setRoles] = useState([]);
  const [stations, setStations] = useState([]);
  const [assignments, setAssignments] = useState({ content: [], page: 0, totalPages: 0, first: true, last: true });
  const [activeAssignments, setActiveAssignments] = useState([]);
  const [assignmentPage, setAssignmentPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [editOpen, setEditOpen] = useState(false);
  const [assignmentAction, setAssignmentAction] = useState(null);
  const [statusConfirm, setStatusConfirm] = useState(false);
  const [statusLoading, setStatusLoading] = useState(false);
  const [statusError, setStatusError] = useState("");
  const [invitationLoading, setInvitationLoading] = useState(false);
  const [reload, setReload] = useState(0);

  const load = useCallback(async (signal) => {
    void reload;
    setLoading(true); setError("");
    try {
      const [loadedEmployee, loadedRoles, loadedAssignments, loadedActiveAssignments, loadedStations] = await Promise.all([
        getEmployee(employeeId, { signal }),
        getAssignableEmployeeRoles({ signal }),
        permissions.canViewAssignments ? getEmployeeAssignments(employeeId, { status: statusFilter, page: assignmentPage, size: 10 }, { signal }) : null,
        permissions.canViewAssignments ? getEmployeeAssignments(employeeId, { status: "ACTIVE", page: 0, size: 100 }, { signal }) : null,
        organizationId ? getActiveStations(organizationId, { signal }) : [],
      ]);
      setEmployee(loadedEmployee); setRoles(loadedRoles || []);
      if (permissions.canViewAssignments) {
        setAssignments(loadedAssignments);
        setActiveAssignments(loadedActiveAssignments?.content || []);
      }
      setStations(loadedStations || []);
    } catch (requestError) { if (requestError?.name !== "AbortError") setError(requestError?.message || t("employees:errors.detailLoad")); }
    finally { if (!signal.aborted) setLoading(false); }
  }, [assignmentPage, employeeId, organizationId, permissions.canViewAssignments, reload, statusFilter, t]);

  useEffect(() => { const controller = new AbortController(); Promise.resolve().then(() => load(controller.signal)); return () => controller.abort(); }, [load]);
  const date = (value) => value ? formatDateTime(value, { language: i18n.resolvedLanguage }) : "—";
  const dateOnly = (value) => value ? formatDate([value, "T00:00:00"].join(""), { language: i18n.resolvedLanguage, dateStyle: "medium" }) : "—";
  const pumpAttendantReady = employee?.roleCode !== "PUMP_ATTENDANT"
    || employee?.pumpAttendantValidationStatus === "VALIDATED";

  const handleActionSaved = (mode) => { setAssignmentAction(null); setSuccess(t(`employees:feedback.${mode === "create" ? "assigned" : mode === "end" ? "ended" : "transferred"}`)); setReload((value) => value + 1); };
  const resendInvitation = async () => {
    setInvitationLoading(true);
    try {
      const result = await resendEmployeeInvitation(employee.id);
      setSuccess(t(result?.invitationSent === false ? "employees:feedback.invitationNotSent" : "employees:feedback.invitationResent"));
    } catch (requestError) { setError(requestError?.message || t("employees:errors.invitationResend")); }
    finally { setInvitationLoading(false); }
  };
  const handleStatus = async () => { setStatusLoading(true); setStatusError(""); try { const saved = await updateEmployeeStatus(employee.id, !employee.enabled); setEmployee(saved); setStatusConfirm(false); setSuccess(t(saved.enabled ? "employees:feedback.enabled" : "employees:feedback.disabled")); setReload((value) => value + 1); } catch (requestError) { setStatusError(requestError?.status === 409 ? t("employees:errors.statusConflict") : requestError?.message || t("employees:errors.status")); } finally { setStatusLoading(false); } };

  return <SupervisorLayout><main className="employee-detail">
    <button type="button" className="employee-back" onClick={() => navigate("/superviseur/employes")}><ArrowLeft size={17} />{t("employees:detail.back")}</button>
    {success && <div className="employees-alert success" role="status"><CheckCircle2 size={18} />{success}</div>}
    {error && <div className="employees-alert error" role="alert"><AlertCircle size={18} />{error}<button type="button" onClick={() => setReload((value) => value + 1)}><RefreshCw size={15} />{t("common:actions.retry")}</button></div>}
    {loading ? <section className="employees-state"><LoaderCircle className="employee-spin" size={28} />{t("employees:detail.loading")}</section> : employee && <>
      <header className="employee-detail-header"><div className="employee-avatar"><UserRoundCog size={27} /></div><div><span className={`employees-badge ${employee.enabled ? "active" : "inactive"}`}>{t(!pumpAttendantReady
  ? `pumpAttendantValidation:candidateStatus.${employee.pumpAttendantValidationStatus}`
  : (employee.invitationPending ? "employees:status.pending" : (employee.enabled ? "employees:status.active" : "employees:status.inactive")))}</span><h1>{employee.firstName} {employee.lastName}</h1><p>{t(`employees:roles.${employee.roleCode}`)} · {employee.email}</p></div><div className="employee-detail-actions">{permissions.canUpdate && pumpAttendantReady && employee.invitationPending && <button type="button" onClick={resendInvitation} disabled={invitationLoading}><RefreshCw size={16} />{t("employees:actions.resendInvitation")}</button>}{permissions.canUpdate && pumpAttendantReady && <button type="button" onClick={() => setEditOpen(true)}><Pencil size={16} />{t("employees:actions.edit")}</button>}{permissions.canDisable && pumpAttendantReady && <button type="button" className={employee.enabled ? "danger" : "success"} onClick={() => setStatusConfirm(true)}><UserX size={16} />{t(employee.enabled ? "employees:actions.disable" : "employees:actions.enable")}</button>}</div></header>
      <section className="employee-panel"><header><div><h2>{t("employees:detail.information")}</h2><p>{t("employees:detail.informationDescription")}</p></div></header><dl className="employee-info-grid"><div><dt>{t("employees:fields.firstName")}</dt><dd>{employee.firstName}</dd></div><div><dt>{t("employees:fields.lastName")}</dt><dd>{employee.lastName}</dd></div>{employee.roleCode === "PUMP_ATTENDANT" && <><div><dt>{t("employees:fields.postName")}</dt><dd>{employee.postName || "—"}</dd></div><div><dt>{t("employees:fields.gender")}</dt><dd>{employee.gender ? t("employees:gender." + employee.gender) : "—"}</dd></div><div><dt>{t("employees:fields.birthPlace")}</dt><dd>{employee.birthPlace || "—"}</dd></div><div><dt>{t("employees:fields.birthDate")}</dt><dd>{dateOnly(employee.birthDate)}</dd></div><div><dt>{t("employees:fields.address")}</dt><dd>{employee.address || "—"}</dd></div></>}<div><dt>{t("employees:fields.email")}</dt><dd>{employee.email}</dd></div><div><dt>{t("employees:fields.phone")}</dt><dd>{employee.phoneNumber}</dd></div><div><dt>{t("employees:fields.role")}</dt><dd>{t(`employees:roles.${employee.roleCode}`)}</dd></div>{employee.operationalCode && <div><dt>{t("employees:fields.operationalCode")}</dt><dd>{employee.operationalCode}</dd></div>}{employee.roleCode === "PUMP_ATTENDANT" && <div><dt>{t("employees:fields.validationStatus")}</dt><dd>{t(`pumpAttendantValidation:candidateStatus.${employee.pumpAttendantValidationStatus}`)}</dd></div>}<div><dt>{t("employees:fields.createdAt")}</dt><dd>{date(employee.createdAt)}</dd></div></dl></section>
      {permissions.canViewAssignments && <section className="employee-panel"><header><div><h2>{t("employees:assignments.title")}</h2><p>{t("employees:assignments.description")}</p></div>{permissions.canCreateAssignment && employee.enabled && pumpAttendantReady && <button type="button" className="employees-primary" onClick={() => setAssignmentAction({ mode: "create" })}><MapPinPlus size={16} />{t("employees:assignments.assign")}</button>}</header>
        {(!employee.enabled || !pumpAttendantReady) && <div className="employee-disabled-note"><AlertCircle size={17} />{t(!pumpAttendantReady ? "employees:assignments.validationRequiredNote" : "employees:assignments.disabledNote")}</div>}
        <div className="employee-active-grid">{activeAssignments.length === 0 ? <div className="employee-empty-inline"><Building2 size={25} />{t("employees:assignments.noActive")}</div> : activeAssignments.map((assignment) => <article key={assignment.id}><div><strong>{assignment.stationName}</strong><small>{assignment.stationCode}</small></div><p><CalendarDays size={14} />{t("employees:assignments.since", { date: date(assignment.validFrom) })}</p>{assignment.reason && <p>{assignment.reason}</p>}<div>{permissions.canEndAssignment && <button type="button" onClick={() => setAssignmentAction({ mode: "end", assignment })}>{t("employees:assignments.end")}</button>}{permissions.canTransfer && employee.enabled && pumpAttendantReady && <button type="button" onClick={() => setAssignmentAction({ mode: "transfer", assignment })}><ArrowRightLeft size={14} />{t("employees:assignments.transfer")}</button>}</div></article>)}</div>
        <div className="employee-history-heading"><h3>{t("employees:assignments.history")}</h3><select value={statusFilter} onChange={(event) => { setAssignmentPage(0); setStatusFilter(event.target.value); }} aria-label={t("employees:assignments.filterStatus")}><option value="ALL">{t("employees:assignments.all")}</option><option value="ACTIVE">{t("employees:assignments.active")}</option><option value="ENDED">{t("employees:assignments.ended")}</option></select></div>
        <div className="employee-history-list">{assignments.content.length === 0 ? <div className="employee-empty-inline">{t("employees:assignments.noHistory")}</div> : assignments.content.map((assignment) => <article key={assignment.id}><div><strong>{assignment.stationName}</strong><small>{assignment.stationCode}</small></div><span className={`employees-badge ${assignment.active ? "active" : "inactive"}`}>{t(assignment.active ? "employees:assignments.active" : "employees:assignments.ended")}</span><p>{date(assignment.validFrom)} → {date(assignment.validUntil)}</p><p>{assignment.reason || "—"}</p></article>)}</div>
        {assignments.totalPages > 1 && <nav className="employees-pagination"><span>{t("employees:list.page", { current: assignments.page + 1, total: assignments.totalPages })}</span><div><button type="button" disabled={assignments.first} onClick={() => setAssignmentPage((value) => value - 1)}><ChevronLeft size={17} /></button><button type="button" disabled={assignments.last} onClick={() => setAssignmentPage((value) => value + 1)}><ChevronRight size={17} /></button></div></nav>}
      </section>}
    </>}
  </main>
  {editOpen && <EmployeeFormModal employee={{ ...employee, stationId: activeAssignments[0]?.stationId || "" }} roles={roles} stations={stations} onClose={() => setEditOpen(false)} onSaved={(saved) => { setEmployee(saved); setEditOpen(false); setSuccess(t("employees:feedback.updated")); }} />}
  {assignmentAction && <AssignmentModal mode={assignmentAction.mode} employee={employee} assignment={assignmentAction.assignment} stations={stations} onClose={() => setAssignmentAction(null)} onSaved={handleActionSaved} />}
  <ConfirmationModal isOpen={statusConfirm} variant={employee?.enabled ? "danger" : "default"} title={t(employee?.enabled ? "employees:statusConfirm.disableTitle" : "employees:statusConfirm.enableTitle")} description={t(employee?.enabled ? "employees:statusConfirm.disableDescription" : "employees:statusConfirm.enableDescription", { name: `${employee?.firstName || ""} ${employee?.lastName || ""}`.trim() })} isLoading={statusLoading} errorMessage={statusError} onConfirm={handleStatus} onClose={() => setStatusConfirm(false)} />
  </SupervisorLayout>;
}

export default EmployeeDetailPage;
