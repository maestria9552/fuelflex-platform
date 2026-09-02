import { Droplets } from "lucide-react";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";

function nowLocal() {
  const date = new Date();
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
}

export default function TankReturnModal({ shift, tanks, isLoading, errorMessage, onClose, onSubmit }) {
  const { t } = useTranslation(["operations", "common"]);
  const compatible = useMemo(() => tanks.filter((tank) => tank.productId === shift?.productId), [shift, tanks]);
  const [form, setForm] = useState({ tankId: compatible[0]?.id || "", quantity: "", reason: "", occurredAt: nowLocal() });
  const valid = form.tankId && Number(form.quantity) > 0 && form.occurredAt;
  const submit = (event) => {
    event.preventDefault();
    if (!valid || isLoading) return;
    onSubmit({ ...form, reason: form.reason.trim() || null, occurredAt: new Date(form.occurredAt).toISOString() });
  };

  return <AppModal isOpen={Boolean(shift)} title={t("operations:tankReturns.formTitle")} description={t("operations:tankReturns.formDescription")} headerIcon={Droplets} size="md" isLoading={isLoading} closeOnEscape={!isLoading} closeOnOverlay={!isLoading} onClose={onClose} footer={<><button className="app-modal-action app-modal-action-no" type="button" onClick={onClose} disabled={isLoading}>{t("common:actions.no")}</button><button className="app-modal-action operations-manager-form-action" type="submit" form="tank-return-form" disabled={isLoading || !valid}>{isLoading ? t("common:actions.saving") : t("operations:tankReturns.save")}</button></>}>
    <div className="operations-readonly-grid">
      <div><span>{t("operations:fields.pumpAttendant")}</span><strong>{shift?.pumpAttendant?.firstName} {shift?.pumpAttendant?.lastName}</strong></div>
      <div><span>{t("operations:fields.pump")}</span><strong>{shift?.pump?.name || "—"}</strong></div>
      <div><span>{t("operations:fields.fuelMeter")}</span><strong>{shift?.fuelMeter?.name || "—"}</strong></div>
      <div><span>{t("operations:fields.product")}</span><strong>{shift?.productName || "—"}</strong></div>
      <div><span>{t("operations:fields.sourceTank")}</span><strong>{shift?.sourceTankName || "—"}</strong></div>
      <div><span>{t("operations:fields.businessDate")}</span><strong>{shift?.operationalDay?.businessDate || "—"}</strong></div>
    </div>
    <form className="operations-form" id="tank-return-form" onSubmit={submit}>
      <label><span>{t("operations:fields.destinationTank")}</span><select value={form.tankId} onChange={(event) => setForm((value) => ({ ...value, tankId: event.target.value }))} required><option value="">{t("operations:tankReturns.chooseTank")}</option>{compatible.map((tank) => <option key={tank.id} value={tank.id}>{tank.name} · {tank.productName}</option>)}</select></label>
      {!compatible.length && <p className="operations-form-error">{t("operations:tankReturns.noCompatibleTank")}</p>}
      <label><span>{t("operations:fields.returnedQuantity")}</span><div className="operations-input-unit"><input type="number" min="0.001" step="0.001" value={form.quantity} onChange={(event) => setForm((value) => ({ ...value, quantity: event.target.value }))} required/><b>L</b></div></label>
      <label><span>{t("operations:fields.occurredAt")}</span><input type="datetime-local" value={form.occurredAt} onChange={(event) => setForm((value) => ({ ...value, occurredAt: event.target.value }))} required/></label>
      <label><span>{t("operations:fields.reason")}</span><textarea rows="3" maxLength="1000" value={form.reason} onChange={(event) => setForm((value) => ({ ...value, reason: event.target.value }))}/></label>
      {errorMessage && <p className="operations-form-error" role="alert">{errorMessage}</p>}
    </form>
  </AppModal>;
}
