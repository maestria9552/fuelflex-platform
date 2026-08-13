import { useMemo, useState } from "react";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { TANK_STATUSES } from "../../station-setup/stationSetup.constants";
import { createTank, updateTank } from "../../../services/tank/tankService";
import "../../product/components/ProductFormModal.css";
import "./TankModal.css";

const STATUS_OPTIONS = [
  [TANK_STATUSES.ACTIVE, "Active"],
  [TANK_STATUSES.INACTIVE, "Inactive"],
  [TANK_STATUSES.MAINTENANCE, "Maintenance"],
  [TANK_STATUSES.OUT_OF_SERVICE, "Hors service"],
];

function getInitialForm(tank, fixedDepotId, initialDepotId, depots, products) {
  return {
    depotId: tank?.depotId || fixedDepotId || initialDepotId || (depots.length === 1 ? depots[0].id : ""),
    productId: tank?.productId || (products.length === 1 ? products[0].id : ""),
    code: tank?.code || "",
    name: tank?.name || "",
    capacityLiters: tank?.capacityLiters ?? "",
    minimumLevelLiters: tank?.minimumLevelLiters ?? "",
    maximumLevelLiters: tank?.maximumLevelLiters ?? "",
    location: tank?.location || "",
    displayOrder: tank?.displayOrder || 1,
    status: tank?.status || TANK_STATUSES.ACTIVE,
    active: tank ? Boolean(tank.active) : true,
  };
}

function nullableNumber(value) {
  return value === "" ? null : Number(value);
}

function TankModal({ isOpen, organizationId, stationId, depots, products, tank, fixedDepotId, initialDepotId, onClose, onSaved }) {
  const initialForm = useMemo(() => getInitialForm(tank, fixedDepotId, initialDepotId, depots, products), [tank, fixedDepotId, initialDepotId, depots, products]);
  const [formData, setFormData] = useState(initialForm);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const isEditing = Boolean(tank?.id);

  const handleChange = (event) => {
    const { checked, name, type, value } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    if (!formData.depotId) return setErrorMessage("Sélectionnez le dépôt parent.");
    if (!formData.productId) return setErrorMessage("Sélectionnez le produit de la citerne.");

    const capacity = Number(formData.capacityLiters);
    const minimum = nullableNumber(formData.minimumLevelLiters);
    const maximum = nullableNumber(formData.maximumLevelLiters);
    if (!Number.isFinite(capacity) || capacity <= 0) return setErrorMessage("La capacité doit être supérieure à zéro.");
    if (minimum !== null && minimum < 0) return setErrorMessage("Le niveau minimal ne peut pas être négatif.");
    if (maximum !== null && maximum <= 0) return setErrorMessage("Le niveau maximal doit être supérieur à zéro.");
    const effectiveMinimum = minimum ?? 0;
    const effectiveMaximum = maximum ?? capacity;
    if (effectiveMinimum > effectiveMaximum) return setErrorMessage("Le niveau minimal ne peut pas dépasser le niveau maximal.");
    if (effectiveMaximum > capacity) return setErrorMessage("Le niveau maximal ne peut pas dépasser la capacité.");

    setIsSaving(true);
    setErrorMessage("");
    const payload = {
      productId: formData.productId,
      code: formData.code.trim(),
      name: formData.name.trim(),
      capacityLiters: capacity,
      minimumLevelLiters: minimum,
      maximumLevelLiters: maximum,
      status: isEditing ? formData.status : TANK_STATUSES.ACTIVE,
      location: formData.location.trim() || null,
      displayOrder: Number(formData.displayOrder) || 1,
      active: isEditing ? formData.active : true,
    };

    try {
      const savedTank = isEditing
        ? await updateTank(organizationId, stationId, tank.depotId, tank.id, payload)
        : await createTank(organizationId, stationId, formData.depotId, payload);
      onSaved?.(savedTank, isEditing);
    } catch (error) {
      setErrorMessage(error?.message || "Impossible d’enregistrer la citerne.");
    } finally {
      setIsSaving(false);
    }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return <AppModal isOpen={isOpen} title={isEditing ? "Modifier la citerne" : "Créer une citerne"} description="Renseignez la capacité et le produit réellement stocké dans cette citerne." size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>Annuler</button><button type="submit" form="tank-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />Enregistrement...</> : <><Save size={18} />Enregistrer</>}</button></>}>
    <form id="tank-form" className="product-form-modal-form" onSubmit={handleSubmit}>
      {errorMessage && <div className="product-form-modal-alert" role="alert">{errorMessage}</div>}
      <div className="product-form-modal-grid">
        {!fixedDepotId && <label><span>Dépôt *</span><select name="depotId" value={formData.depotId} onChange={handleChange} required disabled={isEditing}><option value="">Sélectionner</option>{depots.map((depot) => <option key={depot.id} value={depot.id}>{depot.name}</option>)}</select></label>}
        <label><span>Produit *</span><select name="productId" value={formData.productId} onChange={handleChange} required><option value="">Sélectionner</option>{products.map((product) => <option key={product.id} value={product.id}>{product.name} ({product.code}){!product.active ? " — inactif" : ""}</option>)}</select></label>
        <label><span>Nom *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
        <label><span>Code *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
        <label><span>Capacité (L) *</span><input type="number" name="capacityLiters" value={formData.capacityLiters} onChange={handleChange} min="0.001" step="0.001" required /></label>
      </div>

      <section className="tank-modal-levels">
        <div className="tank-modal-levels-heading"><strong>Niveaux</strong><span>0 ≤ minimum ≤ maximum ≤ capacité</span></div>
        <div className="product-form-modal-grid">
          <label><span>Niveau minimum (L)</span><input type="number" name="minimumLevelLiters" value={formData.minimumLevelLiters} onChange={handleChange} min="0" step="0.001" placeholder="0 par défaut" /></label>
          <label><span>Niveau maximum (L)</span><input type="number" name="maximumLevelLiters" value={formData.maximumLevelLiters} onChange={handleChange} min="0.001" step="0.001" placeholder="Capacité par défaut" /></label>
        </div>
      </section>

      <details className="product-form-modal-section">
        <summary>Informations complémentaires</summary>
        <div className="product-form-modal-grid">
          <label className="product-form-modal-field-full"><span>Emplacement</span><input name="location" value={formData.location} onChange={handleChange} maxLength={255} /></label>
          <label><span>Ordre d’affichage</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
        </div>
      </details>

      {isEditing && <details className="product-form-modal-section product-form-modal-administration">
        <summary>Administration</summary>
        <div className="product-form-modal-grid">
          <label><span>Statut</span><select name="status" value={formData.status} onChange={handleChange}>{STATUS_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
          <label className="product-form-modal-checkbox"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>Citerne active</span></label>
        </div>
      </details>}
    </form>
  </AppModal>;
}

export default TankModal;
