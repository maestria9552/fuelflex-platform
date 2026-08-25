import { AlertCircle, LoaderCircle } from "lucide-react";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";
import {
  createManagerPumpAttendant,
  updateManagerPumpAttendant,
} from "../../../services/employee/pumpAttendantValidationService";

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
  stationId: "",
};

const yesterday = new Date();
yesterday.setDate(yesterday.getDate() - 1);
const MAX_BIRTH_DATE = yesterday.toISOString().slice(0, 10);

function PumpAttendantDraftModal({ employee, stations = [], onClose, onSaved }) {
  const { t } = useTranslation(["pumpAttendantValidation", "common"]);
  const isEditing = Boolean(employee);
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
    stationId: employee.station?.id || employee.stationId || "",
  } : { ...EMPTY_FORM, stationId: stations[0]?.id || "" });
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const submit = async (event) => {
    event.preventDefault();
    setIsSaving(true);
    setError("");
    const payload = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      postName: form.postName.trim(),
      gender: form.gender,
      birthPlace: form.birthPlace.trim(),
      birthDate: form.birthDate,
      address: form.address.trim(),
      email: form.email.trim(),
      phoneNumber: form.phoneNumber.trim(),
      stationId: form.stationId,
    };
    try {
      const saved = isEditing
        ? await updateManagerPumpAttendant(employee.id, payload)
        : await createManagerPumpAttendant(payload);
      onSaved(saved, isEditing);
    } catch (requestError) {
      setError(requestError?.message
        || t("pumpAttendantValidation:errors.saveCandidate"));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <AppModal
      isOpen
      title={t(isEditing
        ? "pumpAttendantValidation:candidateForm.editTitle"
        : "pumpAttendantValidation:candidateForm.createTitle")}
      description={t(isEditing
        ? "pumpAttendantValidation:candidateForm.editDescription"
        : "pumpAttendantValidation:candidateForm.createDescription")}
      size="lg"
      closeOnEscape={!isSaving}
      closeOnOverlay={!isSaving}
      onClose={onClose}
      footer={(
        <>
          <button type="button" className="validation-secondary" onClick={onClose} disabled={isSaving}>{t("common:actions.cancel")}</button>
          <button type="submit" form="pump-attendant-draft-form" className="validation-primary" disabled={isSaving}>{isSaving && <LoaderCircle className="validation-spin" size={17} />}{t("common:actions.save")}</button>
        </>
      )}
    >
      <form id="pump-attendant-draft-form" className="validation-form" onSubmit={submit}>
        {error && <div className="validation-alert error" role="alert"><AlertCircle size={17} />{error}</div>}
        <div className="validation-form-grid">
          <label><span>{t("pumpAttendantValidation:fields.lastName")}</span><input name="lastName" value={form.lastName} onChange={updateField} maxLength={100} required autoFocus /></label>
          <label><span>{t("pumpAttendantValidation:fields.postName")}</span><input name="postName" value={form.postName} onChange={updateField} maxLength={100} required /></label>
          <label><span>{t("pumpAttendantValidation:fields.firstName")}</span><input name="firstName" value={form.firstName} onChange={updateField} maxLength={100} required /></label>
          <label><span>{t("pumpAttendantValidation:fields.gender")}</span><select name="gender" value={form.gender} onChange={updateField} required><option value="" disabled>{t("pumpAttendantValidation:candidateForm.chooseGender")}</option><option value="MALE">{t("pumpAttendantValidation:gender.MALE")}</option><option value="FEMALE">{t("pumpAttendantValidation:gender.FEMALE")}</option></select></label>
          <label><span>{t("pumpAttendantValidation:fields.birthPlace")}</span><input name="birthPlace" value={form.birthPlace} onChange={updateField} maxLength={150} required /></label>
          <label><span>{t("pumpAttendantValidation:fields.birthDate")}</span><input name="birthDate" type="date" value={form.birthDate} max={MAX_BIRTH_DATE} onChange={updateField} required /></label>
        </div>
        <label><span>{t("pumpAttendantValidation:fields.address")}</span><textarea name="address" value={form.address} onChange={updateField} maxLength={500} rows={3} required /></label>
        <div className="validation-form-grid">
          <label><span>{t("pumpAttendantValidation:fields.email")}</span><input name="email" type="email" value={form.email} onChange={updateField} maxLength={180} required /></label>
          <label><span>{t("pumpAttendantValidation:fields.phone")}</span><input name="phoneNumber" value={form.phoneNumber} onChange={updateField} maxLength={30} required /></label>
        </div>
        <label><span>{t("pumpAttendantValidation:fields.station")}</span><select name="stationId" value={form.stationId} onChange={updateField} required><option value="" disabled>{t("pumpAttendantValidation:candidateForm.chooseStation")}</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name}</option>)}</select></label>
        {!isEditing && <p className="validation-form-note">{t("pumpAttendantValidation:candidateForm.noAutomaticSubmission")}</p>}
      </form>
    </AppModal>
  );
}

export default PumpAttendantDraftModal;
