import { useState } from "react";

import { STATION_STATUSES, STATION_TYPES } from "../../station-setup/stationSetup.constants";
import "./StationForm.css";

const TYPE_LABELS = {
  SERVICE_STATION: "Station-service", DEPOT: "Dépôt", AIRPORT: "Aéroport", PORT: "Port", MINE: "Mine",
  LOGISTICS_CENTER: "Centre logistique", DISTRIBUTION_CENTER: "Centre de distribution",
};

const STATUS_LABELS = {
  ACTIVE: "Active", INACTIVE: "Inactive", MAINTENANCE: "Maintenance", SUSPENDED: "Suspendue", CLOSED: "Fermée",
};

function getInitialForm(station) {
  return {
    name: station?.name || "", code: station?.code || "", type: station?.type || STATION_TYPES.SERVICE_STATION,
    shortName: station?.shortName || "", address: station?.address || "", city: station?.city || "",
    province: station?.province || "", country: station?.country || "République démocratique du Congo",
    phoneNumber: station?.phoneNumber || "", email: station?.email || "", latitude: station?.latitude || "",
    longitude: station?.longitude || "", displayOrder: station?.displayOrder || 1,
    status: station?.status || STATION_STATUSES.ACTIVE, active: station ? station.active : true,
  };
}

function nullableText(value) { return value.trim() || null; }

function StationForm({ station, formId = "station-form", isSaving = false, onSubmit, children }) {
  const [formData, setFormData] = useState(() => getInitialForm(station));
  const isEditing = Boolean(station?.id);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    onSubmit?.({
      name: formData.name.trim(), code: formData.code.trim(), type: formData.type,
      shortName: nullableText(formData.shortName), address: nullableText(formData.address),
      city: nullableText(formData.city), province: nullableText(formData.province), country: nullableText(formData.country),
      phoneNumber: nullableText(formData.phoneNumber), email: nullableText(formData.email)?.toLowerCase() || null,
      latitude: nullableText(formData.latitude), longitude: nullableText(formData.longitude),
      displayOrder: Number(formData.displayOrder) || 1,
      status: isEditing ? formData.status : STATION_STATUSES.ACTIVE,
      active: isEditing ? formData.active : true,
    });
  };

  return (
    <form id={formId} className="station-form" onSubmit={handleSubmit}>
      <div className="station-form-grid station-form-main-fields">
        <label><span>Nom *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus disabled={isSaving} /></label>
        <label><span>Code *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required disabled={isSaving} /></label>
        <label className="station-form-field-full"><span>Type *</span><select name="type" value={formData.type} onChange={handleChange} required disabled={isSaving}>{Object.values(STATION_TYPES).map((type) => <option key={type} value={type}>{TYPE_LABELS[type]}</option>)}</select></label>
      </div>

      <details className="station-form-section">
        <summary>Informations complémentaires</summary>
        <div className="station-form-grid">
          <label><span>Nom court</span><input name="shortName" value={formData.shortName} onChange={handleChange} maxLength={100} disabled={isSaving} /></label>
          <label><span>Ville</span><input name="city" value={formData.city} onChange={handleChange} maxLength={100} disabled={isSaving} /></label>
          <label className="station-form-field-full"><span>Adresse</span><input name="address" value={formData.address} onChange={handleChange} maxLength={255} disabled={isSaving} /></label>
          <label><span>Province</span><input name="province" value={formData.province} onChange={handleChange} maxLength={100} disabled={isSaving} /></label>
          <label><span>Pays</span><input name="country" value={formData.country} onChange={handleChange} maxLength={100} disabled={isSaving} /></label>
          <label><span>Téléphone</span><input type="tel" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} maxLength={30} disabled={isSaving} /></label>
          <label><span>Adresse e-mail</span><input type="email" name="email" value={formData.email} onChange={handleChange} maxLength={150} disabled={isSaving} /></label>
          <label><span>Latitude</span><input name="latitude" value={formData.latitude} onChange={handleChange} maxLength={30} disabled={isSaving} /></label>
          <label><span>Longitude</span><input name="longitude" value={formData.longitude} onChange={handleChange} maxLength={30} disabled={isSaving} /></label>
          <label><span>Ordre d’affichage</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" disabled={isSaving} /></label>
        </div>
      </details>

      {isEditing && <details className="station-form-section station-form-administration">
        <summary>Administration</summary>
        <div className="station-form-grid">
          <label><span>Statut</span><select name="status" value={formData.status} onChange={handleChange} disabled={isSaving}>{Object.values(STATION_STATUSES).map((status) => <option key={status} value={status}>{STATUS_LABELS[status]}</option>)}</select></label>
          <label className="station-form-active-field"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} disabled={isSaving} /><span>Station active et utilisable</span></label>
        </div>
      </details>}
      {children}
    </form>
  );
}

export default StationForm;
