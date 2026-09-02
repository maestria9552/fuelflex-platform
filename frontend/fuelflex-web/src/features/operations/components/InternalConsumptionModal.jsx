import { Fuel } from "lucide-react";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import AppModal from "../../../components/modal/AppModal";

export default function InternalConsumptionModal({ shift, isLoading, errorMessage, onClose, onSubmit }) {
  const { t } = useTranslation(["operations", "common"]);
  const [form, setForm] = useState({ quantity: "", usageBeneficiary: "", observation: "" });
  const valid = Number(form.quantity) > 0 && form.usageBeneficiary.trim();
  const submit = (event) => { event.preventDefault(); if (valid && !isLoading) onSubmit({ quantity: form.quantity, usageBeneficiary: form.usageBeneficiary.trim(), observation: form.observation.trim() || null }); };
  const value = (field) => shift?.[field] || shift?.[field.replace("Name", "")]?.name || "—";
  return <AppModal isOpen={Boolean(shift)} title={t("operations:internal.title")} description={t("operations:internal.description")} headerIcon={Fuel} size="md" isLoading={isLoading} closeOnEscape={!isLoading} closeOnOverlay={!isLoading} onClose={onClose} footer={<><button className="app-modal-action app-modal-action-no" type="button" onClick={onClose} disabled={isLoading}>{t("common:actions.no")}</button><button className="app-modal-action operations-manager-form-action" type="submit" form="internal-consumption-form" disabled={isLoading || !valid}>{isLoading ? t("common:actions.saving") : t("operations:internal.save")}</button></>}>
    <div className="operations-readonly-grid">
      <div><span>{t("operations:fields.pumpAttendant")}</span><strong>{shift?.pumpAttendant ? `${shift.pumpAttendant.firstName} ${shift.pumpAttendant.lastName}` : "—"}</strong></div>
      <div><span>{t("operations:fields.pump")}</span><strong>{shift?.pump?.name || "—"}</strong></div>
      <div><span>{t("operations:fields.fuelMeter")}</span><strong>{shift?.fuelMeter?.name || "—"}</strong></div>
      <div><span>{t("operations:fields.product")}</span><strong>{shift?.productName || value("productName")}</strong></div>
      <div><span>{t("operations:fields.sourceTank")}</span><strong>{shift?.sourceTankName || value("sourceTankName")}</strong></div>
      <div><span>{t("operations:fields.businessDate")}</span><strong>{shift?.operationalDay?.businessDate || "—"}</strong></div>
    </div>
    <form className="operations-form" id="internal-consumption-form" onSubmit={submit}>
      <label><span>{t("operations:internal.quantity")}</span><div className="operations-input-unit"><input type="number" min="0.001" step="0.001" value={form.quantity} onChange={(e) => setForm((v) => ({ ...v, quantity: e.target.value }))} required /><b>L</b></div></label>
      <label><span>{t("operations:internal.usageBeneficiary")}</span><input maxLength="180" value={form.usageBeneficiary} onChange={(e) => setForm((v) => ({ ...v, usageBeneficiary: e.target.value }))} required /></label>
      <label><span>{t("operations:internal.observation")}</span><textarea rows="3" maxLength="1000" value={form.observation} onChange={(e) => setForm((v) => ({ ...v, observation: e.target.value }))} /></label>
      {errorMessage && <p className="operations-form-error" role="alert">{errorMessage}</p>}
    </form>
  </AppModal>;
}
