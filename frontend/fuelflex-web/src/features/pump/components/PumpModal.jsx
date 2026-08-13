import { useState } from "react";
import { Gauge, LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { METERING_LEVELS, PUMP_STATUSES } from "../../station-setup/stationSetup.constants";
import { createPump, updatePump } from "../../../services/pump/pumpService";
import "../../product/components/ProductFormModal.css";
import "./PumpModal.css";

const METERING_OPTIONS = [
  {
    value: METERING_LEVELS.PUMP,
    label: "Comptage global à la pompe",
    description: "Un seul compteur suivra le volume distribué par cette pompe.",
    help: "Adapté lorsque les points de distribution utilisent la même citerne.",
  },
  {
    value: METERING_LEVELS.DISPENSING_POINT,
    label: "Comptage par pistolet",
    description: "Chaque pistolet disposera de son propre compteur.",
    help: "Nécessaire lorsque les points doivent être suivis indépendamment.",
  },
];

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
  const [formData, setFormData] = useState(() => getInitialForm(pump));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((currentData) => ({ ...currentData, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    setIsSaving(true);
    setErrorMessage("");

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
      setErrorMessage(error?.message || "Impossible d’enregistrer la pompe.");
    } finally {
      setIsSaving(false);
    }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return (
    <AppModal isOpen={isOpen} title={pump ? "Modifier la pompe" : "Créer une pompe"} description="Configurez le distributeur physique et son niveau de comptage." size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>Annuler</button><button type="submit" form="pump-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />Enregistrement...</> : <><Save size={18} />Enregistrer</>}</button></>}>
      <form id="pump-form" className="product-form-modal-form" onSubmit={handleSubmit}>
        {errorMessage && <div className="product-form-modal-alert" role="alert">{errorMessage}</div>}
        <div className="product-form-modal-grid">
          <label><span>Nom *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
          <label><span>Code *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
          <label><span>Numéro de pompe *</span><input type="number" name="pumpNumber" value={formData.pumpNumber} onChange={handleChange} min="1" required /></label>
          <fieldset className="pump-modal-metering product-form-modal-field-full">
            <legend>Mode de comptage *</legend>
            {METERING_OPTIONS.map((option) => <label key={option.value} className={formData.meteringLevel === option.value ? "selected" : ""}><input type="radio" name="meteringLevel" value={option.value} checked={formData.meteringLevel === option.value} onChange={handleChange} /><Gauge size={20} /><span><strong>{option.label}</strong><small>{option.description}</small><em>{option.help}</em></span></label>)}
          </fieldset>
        </div>
        {pump && <p className="pump-modal-metering-warning">Modifier le mode de comptage peut nécessiter d’adapter les compteurs existants.</p>}

        <details className="product-form-modal-section">
          <summary>Informations complémentaires</summary>
          <div className="product-form-modal-grid">
            <label><span>Fabricant</span><input name="manufacturer" value={formData.manufacturer} onChange={handleChange} maxLength={100} /></label>
            <label><span>Modèle</span><input name="model" value={formData.model} onChange={handleChange} maxLength={100} /></label>
            <label><span>Numéro de série</span><input name="serialNumber" value={formData.serialNumber} onChange={handleChange} maxLength={100} /></label>
            <label><span>Emplacement</span><input name="location" value={formData.location} onChange={handleChange} maxLength={255} /></label>
            <label><span>Ordre d’affichage</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
          </div>
        </details>

        {pump && <details className="product-form-modal-section product-form-modal-administration">
          <summary>Administration</summary>
          <div className="product-form-modal-grid">
            <label><span>Statut</span><select name="status" value={formData.status} onChange={handleChange}>{Object.values(PUMP_STATUSES).map((status) => <option key={status} value={status}>{status}</option>)}</select></label>
            <label className="product-form-modal-checkbox"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>Pompe active et utilisable</span></label>
          </div>
        </details>}
      </form>
    </AppModal>
  );
}
export default PumpModal;
