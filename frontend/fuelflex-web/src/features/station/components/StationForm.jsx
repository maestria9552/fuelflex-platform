import { useState } from "react";
import { useTranslation } from "react-i18next";

import { STATION_STATUSES, STATION_TYPES } from "../../station-setup/stationSetup.constants";
import "./StationForm.css";

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
  const { t } = useTranslation("stations");
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
        <label><span>{t("form.name")} *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus disabled={isSaving} /></label>
        <label><span>{t("form.code")} *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required disabled={isSaving} /></label>
        <label className="station-form-field-full"><span>{t("form.type")} *</span><select name="type" value={formData.type} onChange={handleChange} required disabled={isSaving}>{Object.values(STATION_TYPES).map((type) => <option key={type} value={type}>{t(`types.${type}`)}</option>)}</select></label>
      </div>

      <details className="station-form-section">
        <summary>{t("form.additionalInformation")}</summary>
        <div className="station-form-grid">
          <label><span>{t("form.shortName")}</span><input name="shortName" value={formData.shortName} onChange={handleChange} maxLength={100} disabled={isSaving} /></label>
          <label><span>{t("form.city")}</span><input name="city" value={formData.city} onChange={handleChange} maxLength={100} disabled={isSaving} /></label>
          <label className="station-form-field-full"><span>{t("form.address")}</span><input name="address" value={formData.address} onChange={handleChange} maxLength={255} disabled={isSaving} /></label>
          <label><span>{t("form.province")}</span><input name="province" value={formData.province} onChange={handleChange} maxLength={100} disabled={isSaving} /></label>
          <label><span>{t("form.country")}</span><input name="country" value={formData.country} onChange={handleChange} maxLength={100} disabled={isSaving} /></label>
          <label><span>{t("form.phone")}</span><input type="tel" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} maxLength={30} disabled={isSaving} /></label>
          <label><span>{t("form.email")}</span><input type="email" name="email" value={formData.email} onChange={handleChange} maxLength={150} disabled={isSaving} /></label>
          <label><span>{t("form.latitude")}</span><input name="latitude" value={formData.latitude} onChange={handleChange} maxLength={30} disabled={isSaving} /></label>
          <label><span>{t("form.longitude")}</span><input name="longitude" value={formData.longitude} onChange={handleChange} maxLength={30} disabled={isSaving} /></label>
          <label><span>{t("form.displayOrder")}</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" disabled={isSaving} /></label>
        </div>
      </details>

      {isEditing && <details className="station-form-section station-form-administration">
        <summary>{t("form.administration")}</summary>
        <div className="station-form-grid">
          <label><span>{t("form.status")}</span><select name="status" value={formData.status} onChange={handleChange} disabled={isSaving}>{Object.values(STATION_STATUSES).map((status) => <option key={status} value={status}>{t(`status.${status}`)}</option>)}</select></label>
          <label className="station-form-active-field"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} disabled={isSaving} /><span>{t("form.activeUsable")}</span></label>
        </div>
      </details>}
      {children}
    </form>
  );
}

export default StationForm;
