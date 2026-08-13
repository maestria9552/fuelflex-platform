import { useState } from "react";
import { LoaderCircle, Save } from "lucide-react";

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

const TECHNOLOGY_LABELS = {
  MECHANICAL: "Mécanique",
  ELECTRONIC: "Électronique",
  MANUAL: "Manuel",
};

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
  const [formData, setFormData] = useState(() => getInitialForm(fuelMeter));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const isPumpParent = parentType === PARENT_TYPES.PUMP;

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    if (!isPumpParent && parentType !== PARENT_TYPES.DISPENSING_POINT) return setErrorMessage("Le type de parent du compteur est invalide.");
    if (!pump?.id || (!isPumpParent && !dispensingPoint?.id)) return setErrorMessage("Le parent du compteur est introuvable.");

    const currentIndex = Number(formData.currentIndex);
    if (!Number.isFinite(currentIndex) || currentIndex < 0) return setErrorMessage("L’index actuel doit être un nombre positif ou nul.");
    if (fuelMeter && currentIndex < Number(fuelMeter.currentIndex)) return setErrorMessage("Le nouvel index ne peut pas être inférieur à l’index actuel.");

    setIsSaving(true);
    setErrorMessage("");
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
      setErrorMessage(error?.message || "Impossible d’enregistrer le compteur.");
    } finally {
      setIsSaving(false);
    }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };
  const parentName = isPumpParent ? pump?.name : dispensingPoint?.name;
  const parentDescription = isPumpParent ? `${pump?.code} · Compteur global` : `${dispensingPoint?.code} · ${pump?.name}`;

  return (
    <AppModal isOpen={isOpen} title={fuelMeter ? "Modifier le compteur" : "Configurer le compteur"} description={isPumpParent ? "Ce compteur sera rattaché directement à la pompe." : "Ce compteur sera rattaché au pistolet sélectionné."} size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>Annuler</button><button type="submit" form="fuel-meter-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />Enregistrement...</> : <><Save size={18} />Enregistrer</>}</button></>}>
      <form id="fuel-meter-form" className="product-form-modal-form" onSubmit={handleSubmit}>
        {errorMessage && <div className="product-form-modal-alert" role="alert">{errorMessage}</div>}
        <div className="dispensing-point-parent-card"><span>{isPumpParent ? "Pompe" : "Pistolet"}</span><strong>{parentName}</strong><small>{parentDescription}</small></div>
        <div className="product-form-modal-grid">
          <label><span>Nom *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
          <label><span>Code *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
          <label><span>Technologie *</span><select name="technology" value={formData.technology} onChange={handleChange} required>{Object.values(METER_TECHNOLOGIES).map((technology) => <option key={technology} value={technology}>{TECHNOLOGY_LABELS[technology]}</option>)}</select></label>
          <label><span>{fuelMeter ? "Index actuel *" : "Index initial *"}</span><input type="number" name="currentIndex" value={formData.currentIndex} onChange={handleChange} min={fuelMeter?.currentIndex ?? "0"} step="0.001" required /></label>
          <details className="product-form-modal-details">
            <summary>Informations complémentaires</summary>
            <label><span>Ordre d’affichage</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
          </details>
          {fuelMeter && <>
            <label><span>Statut</span><select name="status" value={formData.status} onChange={handleChange}>{Object.values(FUEL_METER_STATUSES).map((status) => <option key={status} value={status}>{status}</option>)}</select></label>
            <label className="product-form-modal-checkbox"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>Compteur actif et utilisable</span></label>
          </>}
        </div>
      </form>
    </AppModal>
  );
}

export default FuelMeterModal;
