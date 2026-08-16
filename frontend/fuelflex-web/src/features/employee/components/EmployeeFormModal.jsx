import { useState } from "react";
import { AlertCircle, LoaderCircle } from "lucide-react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";
import { createEmployee, updateEmployee } from "../../../services/employee/employeeService";
import "./EmployeeModals.css";

const EMPTY_FORM = { firstName: "", lastName: "", email: "", phoneNumber: "", roleCode: "" };

function EmployeeFormModal({ employee, roles, onClose, onSaved }) {
  const { t } = useTranslation(["employees", "common"]);
  const isEditing = Boolean(employee);
  const [form, setForm] = useState(() => employee ? {
    firstName: employee.firstName || "",
    lastName: employee.lastName || "",
    email: employee.email || "",
    phoneNumber: employee.phoneNumber || "",
    roleCode: employee.roleCode || "",
  } : { ...EMPTY_FORM, roleCode: roles[0]?.code || "" });
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");

  const updateField = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }));

  const submit = async (event) => {
    event.preventDefault();
    setIsSaving(true);
    setError("");
    const payload = {
      firstName: form.firstName.trim(), lastName: form.lastName.trim(),
      phoneNumber: form.phoneNumber.trim(), roleCode: form.roleCode,
      ...(!isEditing ? { email: form.email.trim() } : {}),
    };
    try {
      const saved = isEditing
        ? await updateEmployee(employee.id, payload)
        : await createEmployee(payload);
      onSaved(saved, isEditing);
    } catch (requestError) {
      setError(requestError?.status === 409 ? t("employees:errors.conflict") : requestError?.message || t("employees:errors.save"));
    } finally {
      setIsSaving(false);
    }
  };

  return <AppModal isOpen title={t(isEditing ? "employees:form.editTitle" : "employees:form.createTitle")} description={t(isEditing ? "employees:form.editDescription" : "employees:form.createDescription")} size="md" closeOnEscape={!isSaving} closeOnOverlay={!isSaving} onClose={onClose} footer={<><button type="button" className="employee-modal-secondary" onClick={onClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="employee-form" className="employee-modal-primary" disabled={isSaving}>{isSaving && <LoaderCircle className="employee-spin" size={17} />}{t("common:actions.save")}</button></>}>
    <form id="employee-form" className="employee-form" onSubmit={submit}>
      {error && <div className="employee-modal-error" role="alert"><AlertCircle size={17} />{error}</div>}
      <div className="employee-form-grid">
        <label><span>{t("employees:fields.firstName")}</span><input name="firstName" value={form.firstName} onChange={updateField} maxLength={100} required autoFocus /></label>
        <label><span>{t("employees:fields.lastName")}</span><input name="lastName" value={form.lastName} onChange={updateField} maxLength={100} required /></label>
      </div>
      <label><span>{t("employees:fields.email")}</span><input name="email" type="email" value={form.email} onChange={updateField} maxLength={180} required disabled={isEditing} aria-describedby={isEditing ? "employee-email-help" : undefined} />{isEditing && <small id="employee-email-help">{t("employees:form.emailImmutable")}</small>}</label>
      <label><span>{t("employees:fields.phone")}</span><input name="phoneNumber" value={form.phoneNumber} onChange={updateField} maxLength={30} required /></label>
      <label><span>{t("employees:fields.role")}</span><select name="roleCode" value={form.roleCode} onChange={updateField} required><option value="" disabled>{t("employees:form.chooseRole")}</option>{roles.map((role) => <option key={role.code} value={role.code}>{t(`employees:roles.${role.code}`, { defaultValue: role.name })}</option>)}</select></label>
      {!isEditing && <p className="employee-form-note">{t("employees:form.accessNote")}</p>}
    </form>
  </AppModal>;
}

export default EmployeeFormModal;
