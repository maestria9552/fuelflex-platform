import { useState } from "react";
import { useTranslation } from "react-i18next";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { createDispensingPoint, updateDispensingPoint } from "../../../services/dispensingPoint/dispensingPointService";
import { DISPENSING_POINT_STATUSES } from "../../station-setup/stationSetup.constants";
import "../../product/components/ProductFormModal.css";

function getInitialForm(point, tanks) {
  return {
    tankId: point?.tankId || (tanks.length === 1 ? tanks[0].id : ""),
    code: point?.code || "",
    name: point?.name || "",
    nozzleNumber: point?.nozzleNumber || 1,
    status: point?.status || DISPENSING_POINT_STATUSES.ACTIVE,
    displayOrder: point?.displayOrder || 1,
    active: point ? point.active : true,
  };
}

function DispensingPointModal({ isOpen, organizationId, stationId, pump, tanks, dispensingPoint, onClose, onSaved }) {
  const { t } = useTranslation(["dispensingPoints", "common"]);
  const [formData, setFormData] = useState(() => getInitialForm(dispensingPoint, tanks));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    if (!formData.tankId) return setErrorMessage({ key: "dispensingPoints:validation.tankRequired" });
    setIsSaving(true);
    setErrorMessage(null);
    const payload = {
      tankId: formData.tankId,
      code: formData.code.trim(),
      name: formData.name.trim(),
      nozzleNumber: Number(formData.nozzleNumber),
      status: dispensingPoint ? formData.status : DISPENSING_POINT_STATUSES.ACTIVE,
      displayOrder: Number(formData.displayOrder) || 1,
      active: dispensingPoint ? formData.active : true,
    };
    try {
      const savedPoint = dispensingPoint?.id
        ? await updateDispensingPoint(organizationId, stationId, pump.id, dispensingPoint.id, payload)
        : await createDispensingPoint(organizationId, stationId, pump.id, payload);
      onSaved?.(savedPoint, Boolean(dispensingPoint?.id));
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: "dispensingPoints:feedback.saveFailed" });
    } finally {
      setIsSaving(false);
    }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return (
    <AppModal isOpen={isOpen} title={t(dispensingPoint ? "dispensingPoints:modal.editTitle" : "dispensingPoints:modal.createTitle")} description={t("dispensingPoints:modal.description")} size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="dispensing-point-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />{t("dispensingPoints:modal.saving")}</> : <><Save size={18} />{t("common:actions.save")}</>}</button></>}>
      <form id="dispensing-point-form" className="product-form-modal-form" onSubmit={handleSubmit}>
        {errorMessage && <div className="product-form-modal-alert" role="alert">{renderMessage(errorMessage)}</div>}
        <div className="dispensing-point-parent-card"><span>{t("dispensingPoints:modal.pump")}</span><strong>{pump.name}</strong><small>{pump.code}{pump.pumpNumber ? ` · ${t("dispensingPoints:modal.pumpNumber", { number: pump.pumpNumber })}` : ""}</small></div>
        <div className="product-form-modal-grid">
          <label><span>{t("dispensingPoints:modal.name")} *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
          <label><span>{t("dispensingPoints:modal.code")} *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
          <label><span>{t("dispensingPoints:modal.nozzleNumber")} *</span><input type="number" name="nozzleNumber" value={formData.nozzleNumber} onChange={handleChange} min="1" required /></label>
          <label><span>{t("dispensingPoints:modal.tank")} *</span><select name="tankId" value={formData.tankId} onChange={handleChange} required><option value="">{t("dispensingPoints:modal.selectTank")}</option>{tanks.map((tank) => <option key={tank.id} value={tank.id}>{tank.name} — {tank.code}{tank.productName ? ` — ${tank.productName}` : ""}</option>)}</select></label>
          <label><span>{t("dispensingPoints:modal.displayOrder")}</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
          <label><span>{t("dispensingPoints:modal.status")}</span><select name="status" value={formData.status} onChange={handleChange} disabled={!dispensingPoint}>{Object.values(DISPENSING_POINT_STATUSES).map((status) => <option key={status} value={status}>{t(`dispensingPoints:status.${status}`)}</option>)}</select></label>
          <label className="product-form-modal-checkbox"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} disabled={!dispensingPoint} /><span>{t("dispensingPoints:modal.activeUsable")}</span></label>
        </div>
      </form>
    </AppModal>
  );
}

export default DispensingPointModal;
