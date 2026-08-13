import { useState } from "react";
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
  const [formData, setFormData] = useState(() => getInitialForm(dispensingPoint, tanks));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    if (!formData.tankId) return setErrorMessage("Sélectionnez la citerne alimentant ce pistolet.");
    setIsSaving(true);
    setErrorMessage("");
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
      setErrorMessage(error?.message || "Impossible d’enregistrer le pistolet.");
    } finally {
      setIsSaving(false);
    }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return (
    <AppModal isOpen={isOpen} title={dispensingPoint ? "Modifier le pistolet" : "Ajouter un pistolet"} description="Associez ce point de distribution à une citerne de la station." size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>Annuler</button><button type="submit" form="dispensing-point-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />Enregistrement...</> : <><Save size={18} />Enregistrer</>}</button></>}>
      <form id="dispensing-point-form" className="product-form-modal-form" onSubmit={handleSubmit}>
        {errorMessage && <div className="product-form-modal-alert" role="alert">{errorMessage}</div>}
        <div className="dispensing-point-parent-card"><span>Pompe</span><strong>{pump.name}</strong><small>{pump.code}{pump.pumpNumber ? ` · Pompe n° ${pump.pumpNumber}` : ""}</small></div>
        <div className="product-form-modal-grid">
          <label><span>Nom *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
          <label><span>Code *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
          <label><span>Numéro du pistolet *</span><input type="number" name="nozzleNumber" value={formData.nozzleNumber} onChange={handleChange} min="1" required /></label>
          <label><span>Citerne *</span><select name="tankId" value={formData.tankId} onChange={handleChange} required><option value="">Sélectionner une citerne</option>{tanks.map((tank) => <option key={tank.id} value={tank.id}>{tank.name} — {tank.code}{tank.productName ? ` — ${tank.productName}` : ""}</option>)}</select></label>
          <label><span>Ordre d’affichage</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
          <label><span>Statut</span><select name="status" value={formData.status} onChange={handleChange} disabled={!dispensingPoint}>{Object.values(DISPENSING_POINT_STATUSES).map((status) => <option key={status} value={status}>{status}</option>)}</select></label>
          <label className="product-form-modal-checkbox"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} disabled={!dispensingPoint} /><span>Pistolet actif et utilisable</span></label>
        </div>
      </form>
    </AppModal>
  );
}

export default DispensingPointModal;
