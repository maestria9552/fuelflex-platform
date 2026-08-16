import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Gauge, LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { createPump, updatePump } from "../../../services/pump/pumpService";
import { METERING_LEVELS, PUMP_STATUSES } from "../../station-setup/stationSetup.constants";
import "../../product/components/ProductFormModal.css";
import "./PumpModal.css";

const METERING_VALUES = [METERING_LEVELS.PUMP, METERING_LEVELS.DISPENSING_POINT];

function getInitialForm(pump) {
  return {
    code: pump?.code || "",
    name: pump?.name || "",
    pumpNumber: pump?.pumpNumber || 1,
    meteringLevel: pump?.meteringLevel || METERING_LEVELS.PUMP,
    manufacturer: pump?.manufacturer || "",
    model: pump?.model || "",
    serialNumber: pump?.serialNumber || "",
    location: pump?.location || "",
    displayOrder: pump?.displayOrder || 1,
    status: pump?.status || PUMP_STATUSES.ACTIVE,
    active: pump ? pump.active : true,
  };
}

function PumpModal({ isOpen, organizationId, stationId, pump, onClose, onSaved }) {
  const { t } = useTranslation(["pumps", "common"]);
  const [formData, setFormData] = useState(() => getInitialForm(pump));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((currentData) => ({ ...currentData, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    setIsSaving(true);
    setErrorMessage(null);

    const payload = {
      code: formData.code.trim(),
      name: formData.name.trim(),
      pumpNumber: Number(formData.pumpNumber),
      meteringLevel: formData.meteringLevel,
      manufacturer: formData.manufacturer.trim() || null,
      model: formData.model.trim() || null,
      serialNumber: formData.serialNumber.trim() || null,
      location: formData.location.trim() || null,
      displayOrder: Number(formData.displayOrder) || 1,
      status: pump ? formData.status : PUMP_STATUSES.ACTIVE,
      active: pump ? formData.active : true,
    };

    try {
      const savedPump = pump?.id
        ? await updatePump(organizationId, stationId, pump.id, payload)
        : await createPump(organizationId, stationId, payload);
      onSaved?.(savedPump, Boolean(pump?.id));
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: "pumps:feedback.saveFailed" });
    } finally {
      setIsSaving(false);
    }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return (
    <AppModal isOpen={isOpen} title={t(pump ? "pumps:modal.editTitle" : "pumps:modal.createTitle")} description={t("pumps:modal.description")} size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="pump-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />{t("pumps:modal.saving")}</> : <><Save size={18} />{t("common:actions.save")}</>}</button></>}>
      <form id="pump-form" className="product-form-modal-form" onSubmit={handleSubmit}>
        {errorMessage && <div className="product-form-modal-alert" role="alert">{renderMessage(errorMessage)}</div>}
        <div className="product-form-modal-grid">
          <label><span>{t("pumps:modal.name")} *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
          <label><span>{t("pumps:modal.code")} *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
          <label><span>{t("pumps:modal.pumpNumber")} *</span><input type="number" name="pumpNumber" value={formData.pumpNumber} onChange={handleChange} min="1" required /></label>
          <fieldset className="pump-modal-metering product-form-modal-field-full">
            <legend>{t("pumps:modal.meteringMode")} *</legend>
            {METERING_VALUES.map((value) => <label key={value} className={formData.meteringLevel === value ? "selected" : ""}><input type="radio" name="meteringLevel" value={value} checked={formData.meteringLevel === value} onChange={handleChange} /><Gauge size={20} /><span><strong>{t(`pumps:meteringLevel.${value}.title`)}</strong><small>{t(`pumps:meteringLevel.${value}.description`)}</small><em>{t(`pumps:meteringLevel.${value}.help`)}</em></span></label>)}
          </fieldset>
        </div>
        {pump && <p className="pump-modal-metering-warning">{t("pumps:modal.meteringWarning")}</p>}

        <details className="product-form-modal-section">
          <summary>{t("pumps:modal.additionalInformation")}</summary>
          <div className="product-form-modal-grid">
            <label><span>{t("pumps:modal.manufacturer")}</span><input name="manufacturer" value={formData.manufacturer} onChange={handleChange} maxLength={100} /></label>
            <label><span>{t("pumps:modal.model")}</span><input name="model" value={formData.model} onChange={handleChange} maxLength={100} /></label>
            <label><span>{t("pumps:modal.serialNumber")}</span><input name="serialNumber" value={formData.serialNumber} onChange={handleChange} maxLength={100} /></label>
            <label><span>{t("pumps:modal.location")}</span><input name="location" value={formData.location} onChange={handleChange} maxLength={255} /></label>
            <label><span>{t("pumps:modal.displayOrder")}</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
          </div>
        </details>

        {pump && <details className="product-form-modal-section product-form-modal-administration">
          <summary>{t("pumps:modal.administration")}</summary>
          <div className="product-form-modal-grid">
            <label><span>{t("pumps:modal.status")}</span><select name="status" value={formData.status} onChange={handleChange}>{Object.values(PUMP_STATUSES).map((status) => <option key={status} value={status}>{t(`pumps:status.${status}`)}</option>)}</select></label>
            <label className="product-form-modal-checkbox"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>{t("pumps:modal.activeUsable")}</span></label>
          </div>
        </details>}
      </form>
    </AppModal>
  );
}
export default PumpModal;
