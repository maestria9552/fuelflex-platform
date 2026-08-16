import { useState } from "react";
import { LoaderCircle, Save } from "lucide-react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";
import {
  createDispensingPointFuelMeter,
  createPumpFuelMeter,
  updateDispensingPointFuelMeter,
  updatePumpFuelMeter,
} from "../../../services/fuelMeter/fuelMeterService";
import {
  FUEL_METER_STATUSES,
  METER_TECHNOLOGIES,
} from "../../station-setup/stationSetup.constants";
import "../../product/components/ProductFormModal.css";

const PARENT_TYPES = Object.freeze({
  PUMP: "PUMP",
  DISPENSING_POINT: "DISPENSING_POINT",
});


function getInitialForm(fuelMeter) {
  return {
    code: fuelMeter?.code || "",
    name: fuelMeter?.name || "",
    technology: fuelMeter?.technology || METER_TECHNOLOGIES.MECHANICAL,
    currentIndex: fuelMeter?.currentIndex ?? "0.000",
    status: fuelMeter?.status || FUEL_METER_STATUSES.ACTIVE,
    displayOrder: fuelMeter?.displayOrder || 1,
    active: fuelMeter ? fuelMeter.active : true,
  };
}

function FuelMeterModal({ isOpen, organizationId, stationId, parentType, pump, dispensingPoint, fuelMeter, onClose, onSaved }) {
  const { t } = useTranslation(["fuelMeters", "common"]);
  const [formData, setFormData] = useState(() => getInitialForm(fuelMeter));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";
  const isPumpParent = parentType === PARENT_TYPES.PUMP;

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    if (!isPumpParent && parentType !== PARENT_TYPES.DISPENSING_POINT) return setErrorMessage({ key: "fuelMeters:validation.invalidParentType" });
    if (!pump?.id || (!isPumpParent && !dispensingPoint?.id)) return setErrorMessage({ key: "fuelMeters:validation.parentMissing" });

    const currentIndex = Number(formData.currentIndex);
    if (!Number.isFinite(currentIndex) || currentIndex < 0) return setErrorMessage({ key: "fuelMeters:validation.nonNegativeIndex" });
    if (fuelMeter && currentIndex < Number(fuelMeter.currentIndex)) return setErrorMessage({ key: "fuelMeters:validation.indexRegression" });

    setIsSaving(true);
    setErrorMessage(null);
    const payload = {
      pumpId: isPumpParent ? pump.id : null,
      dispensingPointId: isPumpParent ? null : dispensingPoint.id,
      code: formData.code.trim(),
      name: formData.name.trim(),
      technology: formData.technology,
      currentIndex,
      status: fuelMeter ? formData.status : FUEL_METER_STATUSES.ACTIVE,
      displayOrder: Number(formData.displayOrder) || 1,
      active: fuelMeter ? formData.active : true,
    };

    try {
      let savedMeter;
      if (isPumpParent) {
        savedMeter = fuelMeter?.id
          ? await updatePumpFuelMeter(organizationId, stationId, pump.id, fuelMeter.id, payload)
          : await createPumpFuelMeter(organizationId, stationId, pump.id, payload);
      } else {
        savedMeter = fuelMeter?.id
          ? await updateDispensingPointFuelMeter(organizationId, stationId, pump.id, dispensingPoint.id, fuelMeter.id, payload)
          : await createDispensingPointFuelMeter(organizationId, stationId, pump.id, dispensingPoint.id, payload);
      }
      onSaved?.(savedMeter, Boolean(fuelMeter?.id));
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: "fuelMeters:feedback.saveFailed" });
    } finally {
      setIsSaving(false);
    }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };
  const parentName = isPumpParent ? pump?.name : dispensingPoint?.name;
  const parentDescription = isPumpParent ? `${pump?.code} · ${t("fuelMeters:modal.globalMeter")}` : `${dispensingPoint?.code} · ${pump?.name}`;

  return (
    <AppModal isOpen={isOpen} title={t(fuelMeter ? "fuelMeters:modal.editTitle" : "fuelMeters:modal.createTitle")} description={t(isPumpParent ? "fuelMeters:modal.pumpDescription" : "fuelMeters:modal.pointDescription")} size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="fuel-meter-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />{t("fuelMeters:modal.saving")}</> : <><Save size={18} />{t("common:actions.save")}</>}</button></>}>
      <form id="fuel-meter-form" className="product-form-modal-form" onSubmit={handleSubmit}>
        {errorMessage && <div className="product-form-modal-alert" role="alert">{renderMessage(errorMessage)}</div>}
        <div className="dispensing-point-parent-card"><span>{t(isPumpParent ? "fuelMeters:modal.pump" : "fuelMeters:modal.point")}</span><strong>{parentName}</strong><small>{parentDescription}</small></div>
        <div className="product-form-modal-grid">
          <label><span>{t("fuelMeters:modal.name")} *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
          <label><span>{t("fuelMeters:modal.code")} *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
          <label><span>{t("fuelMeters:modal.technology")} *</span><select name="technology" value={formData.technology} onChange={handleChange} required>{Object.values(METER_TECHNOLOGIES).map((technology) => <option key={technology} value={technology}>{t(`fuelMeters:technology.${technology}`)}</option>)}</select></label>
          <label><span>{t(fuelMeter ? "fuelMeters:modal.currentIndex" : "fuelMeters:modal.initialIndex")} *</span><input type="number" name="currentIndex" value={formData.currentIndex} onChange={handleChange} min={fuelMeter?.currentIndex ?? "0"} step="0.001" required /></label>
          <details className="product-form-modal-details">
            <summary>{t("fuelMeters:modal.additionalInformation")}</summary>
            <label><span>{t("fuelMeters:modal.displayOrder")}</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
          </details>
          {fuelMeter && <>
            <label><span>{t("fuelMeters:modal.status")}</span><select name="status" value={formData.status} onChange={handleChange}>{Object.values(FUEL_METER_STATUSES).map((status) => <option key={status} value={status}>{t(`fuelMeters:status.${status}`)}</option>)}</select></label>
            <label className="product-form-modal-checkbox"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>{t("fuelMeters:modal.activeUsable")}</span></label>
          </>}
        </div>
      </form>
    </AppModal>
  );
}

export default FuelMeterModal;
