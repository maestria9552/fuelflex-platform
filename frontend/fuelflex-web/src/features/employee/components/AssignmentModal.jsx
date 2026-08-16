import { useMemo, useState } from "react";
import { AlertCircle, LoaderCircle } from "lucide-react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";
import { createEmployeeAssignment, endEmployeeAssignment, transferEmployee } from "../../../services/employee/employeeAssignmentService";
import "./EmployeeModals.css";

function toOffsetDateTime(localValue) {
  return localValue ? new Date(localValue).toISOString() : null;
}

function AssignmentModal({ mode, employee, assignment, stations, onClose, onSaved }) {
  const { t } = useTranslation(["employees", "common"]);
  const [stationId, setStationId] = useState("");
  const [effectiveAt, setEffectiveAt] = useState("");
  const [reason, setReason] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");
  const availableStations = useMemo(() => stations.filter((station) => station.id !== assignment?.stationId), [assignment, stations]);

  const submit = async (event) => {
    event.preventDefault(); setIsSaving(true); setError("");
    try {
      if (mode === "create") await createEmployeeAssignment(employee.id, { stationId, validFrom: toOffsetDateTime(effectiveAt), reason: reason.trim() || null });
      if (mode === "end") await endEmployeeAssignment(employee.id, assignment.id, { validUntil: toOffsetDateTime(effectiveAt), reason: reason.trim() || null });
      if (mode === "transfer") await transferEmployee(employee.id, { sourceAssignmentId: assignment.id, destinationStationId: stationId, effectiveAt: toOffsetDateTime(effectiveAt), reason: reason.trim() || null });
      onSaved(mode);
    } catch (requestError) {
      setError(requestError?.status === 409 ? t("employees:errors.assignmentConflict") : requestError?.message || t("employees:errors.assignmentSave"));
    } finally { setIsSaving(false); }
  };

  return <AppModal isOpen title={t(`employees:assignments.${mode}Title`)} description={assignment ? t("employees:assignments.source", { station: assignment.stationName }) : t("employees:assignments.createDescription")} size="md" closeOnEscape={!isSaving} closeOnOverlay={!isSaving} onClose={onClose} footer={<><button type="button" className="employee-modal-secondary" onClick={onClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="assignment-form" className="employee-modal-primary" disabled={isSaving}>{isSaving && <LoaderCircle className="employee-spin" size={17} />}{t("common:actions.confirm")}</button></>}>
    <form id="assignment-form" className="employee-form" onSubmit={submit}>
      {error && <div className="employee-modal-error" role="alert"><AlertCircle size={17} />{error}</div>}
      {mode !== "end" && <label><span>{t(mode === "transfer" ? "employees:assignments.destination" : "employees:assignments.station")}</span><select value={stationId} onChange={(event) => setStationId(event.target.value)} required autoFocus><option value="" disabled>{t("employees:assignments.chooseStation")}</option>{availableStations.map((station) => <option key={station.id} value={station.id}>{station.name} ({station.code})</option>)}</select></label>}
      <label><span>{t(mode === "end" ? "employees:assignments.endDate" : "employees:assignments.effectiveDate")}</span><input type="datetime-local" value={effectiveAt} onChange={(event) => setEffectiveAt(event.target.value)} /></label>
      <label><span>{t("employees:assignments.reason")}</span><textarea value={reason} onChange={(event) => setReason(event.target.value)} maxLength={500} rows={4} /></label>
      {mode === "transfer" && employee.roleCode === "MANAGER" && <p className="employee-form-note">{t("employees:assignments.managerTransferNote")}</p>}
    </form>
  </AppModal>;
}

export default AssignmentModal;
