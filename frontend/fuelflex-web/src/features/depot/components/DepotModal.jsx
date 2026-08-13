import { useState } from "react";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { createDepot, updateDepot } from "../../../services/depot/depotService";
import "../../product/components/ProductFormModal.css";

function getInitialForm(depot) {
  return {
    name: depot?.name || "", code: depot?.code || "", description: depot?.description || "",
    location: depot?.location || "", displayOrder: depot?.displayOrder || 1,
    active: depot ? depot.active : true,
  };
}

function DepotModal({ isOpen, organizationId, stationId, depot, onClose, onSaved }) {
  const [formData, setFormData] = useState(() => getInitialForm(depot));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const isEditing = Boolean(depot?.id);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    setIsSaving(true); setErrorMessage("");
    const payload = {
      name: formData.name.trim(), code: formData.code.trim(),
      description: formData.description.trim() || null, location: formData.location.trim() || null,
      displayOrder: Number(formData.displayOrder) || 1, active: isEditing ? formData.active : true,
    };
    try {
      const savedDepot = isEditing
        ? await updateDepot(organizationId, stationId, depot.id, payload)
        : await createDepot(organizationId, stationId, payload);
      onSaved?.(savedDepot, isEditing);
    } catch (error) {
      setErrorMessage(error?.message || "Impossible d’enregistrer le dépôt.");
    } finally { setIsSaving(false); }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return <AppModal isOpen={isOpen} title={isEditing ? "Modifier le dépôt" : "Créer un dépôt"} description="Renseignez les informations d’organisation physique du dépôt." size="md" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>Annuler</button><button type="submit" form="depot-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />Enregistrement...</> : <><Save size={18} />Enregistrer</>}</button></>}>
    <form id="depot-form" className="product-form-modal-form" onSubmit={handleSubmit}>
      {errorMessage && <div className="product-form-modal-alert" role="alert">{errorMessage}</div>}
      <div className="product-form-modal-grid">
        <label><span>Nom *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
        <label><span>Code *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
      </div>
      <details className="product-form-modal-section">
        <summary>Informations complémentaires</summary>
        <div className="product-form-modal-grid">
          <label className="product-form-modal-field-full"><span>Description</span><textarea name="description" value={formData.description} onChange={handleChange} maxLength={255} rows={3} /></label>
          <label className="product-form-modal-field-full"><span>Emplacement</span><input name="location" value={formData.location} onChange={handleChange} maxLength={255} /></label>
          <label><span>Ordre d’affichage</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
        </div>
      </details>
      {isEditing && <details className="product-form-modal-section product-form-modal-administration">
        <summary>Administration</summary>
        <div className="product-form-modal-grid"><label className="product-form-modal-checkbox product-form-modal-field-full"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>Dépôt actif et utilisable</span></label></div>
      </details>}
    </form>
  </AppModal>;
}

export default DepotModal;
