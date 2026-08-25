import { useState } from "react";
import { AlertCircle, LoaderCircle } from "lucide-react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";
import { createEmployee, updateEmployee } from "../../../services/employee/employeeService";
import "./EmployeeModals.css";

const EMPTY_FORM = {
  firstName: "",
  lastName: "",
  postName: "",
  gender: "",
  birthPlace: "",
  birthDate: "",
  address: "",
  email: "",
  phoneNumber: "",
  roleCode: "",
  stationId: "",
};

const yesterday = new Date();
yesterday.setDate(yesterday.getDate() - 1);
const MAX_BIRTH_DATE = yesterday.toISOString().slice(0, 10);

function EmployeeFormModal({ employee, roles, stations = [], onClose, onSaved }) {
  const { t } = useTranslation(["employees", "common"]);
  const isEditing = Boolean(employee);
  const existingPumpAttendant = employee?.roleCode === "PUMP_ATTENDANT";
  const [form, setForm] = useState(() => employee ? {
    firstName: employee.firstName || "",
    lastName: employee.lastName || "",
    postName: employee.postName || "",
    gender: employee.gender || "",
    birthPlace: employee.birthPlace || "",
    birthDate: employee.birthDate || "",
    address: employee.address || "",
    email: employee.email || "",
    phoneNumber: employee.phoneNumber || "",
    roleCode: employee.roleCode || "",
    stationId: employee.stationId || "",
  } : { ...EMPTY_FORM, roleCode: roles[0]?.code || "" });
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");
  const isPumpAttendant = form.roleCode === "PUMP_ATTENDANT";
  const stationRequired = isPumpAttendant && (!isEditing || !existingPumpAttendant);

  const updateField = (event) => setForm((current) => ({
    ...current,
    [event.target.name]: event.target.value,
  }));

  const submit = async (event) => {
    event.preventDefault();
    setIsSaving(true);
    setError("");
    const payload = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      phoneNumber: form.phoneNumber.trim(),
      roleCode: form.roleCode,
      ...(!isEditing ? { email: form.email.trim() } : {}),
      ...(isPumpAttendant ? {
        postName: form.postName.trim(),
        gender: form.gender,
        birthPlace: form.birthPlace.trim(),
        birthDate: form.birthDate,
        address: form.address.trim(),
        ...(form.stationId ? { stationId: form.stationId } : {}),
      } : {}),
    };
    try {
      const saved = isEditing
        ? await updateEmployee(employee.id, payload)
        : await createEmployee(payload);
      onSaved(saved, isEditing);
    } catch (requestError) {
      setError(requestError?.status === 409
        ? t("employees:errors.conflict")
        : requestError?.message || t("employees:errors.save"));
    } finally {
      setIsSaving(false);
    }
  };

  return <AppModal isOpen title={t(isEditing ? "employees:form.editTitle" : "employees:form.createTitle")} description={t(isEditing ? "employees:form.editDescription" : "employees:form.createDescription")} size="lg" closeOnEscape={!isSaving} closeOnOverlay={!isSaving} onClose={onClose} footer={<><button type="button" className="employee-modal-secondary" onClick={onClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="employee-form" className="employee-modal-primary" disabled={isSaving}>{isSaving && <LoaderCircle className="employee-spin" size={17} />}{t("common:actions.save")}</button></>}>
    <form id="employee-form" className="employee-form" onSubmit={submit}>
      {error && <div className="employee-modal-error" role="alert"><AlertCircle size={17} />{error}</div>}
      <div className="employee-form-grid">
        <label><span>{t("employees:fields.lastName")}</span><input name="lastName" value={form.lastName} onChange={updateField} maxLength={100} required autoFocus /></label>
        {isPumpAttendant && <label><span>{t("employees:fields.postName")}</span><input name="postName" value={form.postName} onChange={updateField} maxLength={100} required /></label>}
        <label><span>{t("employees:fields.firstName")}</span><input name="firstName" value={form.firstName} onChange={updateField} maxLength={100} required /></label>
        {isPumpAttendant && <label><span>{t("employees:fields.gender")}</span><select name="gender" value={form.gender} onChange={updateField} required><option value="" disabled>{t("employees:form.chooseGender")}</option><option value="MALE">{t("employees:gender.MALE")}</option><option value="FEMALE">{t("employees:gender.FEMALE")}</option></select></label>}
      </div>
      {isPumpAttendant && <div className="employee-form-grid">
        <label><span>{t("employees:fields.birthPlace")}</span><input name="birthPlace" value={form.birthPlace} onChange={updateField} maxLength={150} required /></label>
        <label><span>{t("employees:fields.birthDate")}</span><input name="birthDate" type="date" value={form.birthDate} max={MAX_BIRTH_DATE} onChange={updateField} required /></label>
      </div>}
      {isPumpAttendant && <label><span>{t("employees:fields.address")}</span><textarea name="address" value={form.address} onChange={updateField} maxLength={500} rows={3} required /></label>}
      <label><span>{t("employees:fields.email")}</span><input name="email" type="email" value={form.email} onChange={updateField} maxLength={180} required disabled={isEditing} aria-describedby={isEditing ? "employee-email-help" : undefined} />{isEditing && <small id="employee-email-help">{t("employees:form.emailImmutable")}</small>}</label>
      <label><span>{t("employees:fields.phone")}</span><input name="phoneNumber" value={form.phoneNumber} onChange={updateField} maxLength={30} required /></label>
      <label><span>{t("employees:fields.role")}</span><select name="roleCode" value={form.roleCode} onChange={updateField} required><option value="" disabled>{t("employees:form.chooseRole")}</option>{roles.map((role) => <option key={role.code} value={role.code}>{t(`employees:roles.${role.code}`, { defaultValue: role.name })}</option>)}</select></label>
      {isPumpAttendant && <label><span>{t("employees:fields.station")}</span><select name="stationId" value={form.stationId} onChange={updateField} required={stationRequired}><option value="" disabled={stationRequired}>{t(stationRequired ? "employees:form.chooseStation" : "employees:form.keepStation")}</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name}</option>)}</select></label>}
      {isPumpAttendant && isEditing && existingPumpAttendant && <p className="employee-form-note">{t("employees:form.stationChangeRecorded")}</p>}
      {!isEditing && <p className="employee-form-note">{t("employees:form.accessNote")}</p>}
    </form>
  </AppModal>;
}

export default EmployeeFormModal;
