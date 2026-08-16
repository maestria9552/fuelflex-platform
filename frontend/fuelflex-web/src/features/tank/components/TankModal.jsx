import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { createTank, updateTank } from "../../../services/tank/tankService";
import { TANK_STATUSES } from "../../station-setup/stationSetup.constants";
import "../../product/components/ProductFormModal.css";
import "./TankModal.css";

const STATUS_VALUES = [
  TANK_STATUSES.ACTIVE,
  TANK_STATUSES.INACTIVE,
  TANK_STATUSES.MAINTENANCE,
  TANK_STATUSES.OUT_OF_SERVICE,
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
  const { t } = useTranslation(["tanks", "common"]);
  const initialForm = useMemo(() => getInitialForm(tank, fixedDepotId, initialDepotId, depots, products), [tank, fixedDepotId, initialDepotId, depots, products]);
  const [formData, setFormData] = useState(initialForm);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const isEditing = Boolean(tank?.id);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  const handleChange = (event) => {
    const { checked, name, type, value } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    if (!formData.depotId) return setErrorMessage({ key: "tanks:validation.depotRequired" });
    if (!formData.productId) return setErrorMessage({ key: "tanks:validation.productRequired" });

    const capacity = Number(formData.capacityLiters);
    const minimum = nullableNumber(formData.minimumLevelLiters);
    const maximum = nullableNumber(formData.maximumLevelLiters);
    if (!Number.isFinite(capacity) || capacity <= 0) return setErrorMessage({ key: "tanks:validation.capacityPositive" });
    if (minimum !== null && minimum < 0) return setErrorMessage({ key: "tanks:validation.minimumNonNegative" });
    if (maximum !== null && maximum <= 0) return setErrorMessage({ key: "tanks:validation.maximumPositive" });
    const effectiveMinimum = minimum ?? 0;
    const effectiveMaximum = maximum ?? capacity;
    if (effectiveMinimum > effectiveMaximum) return setErrorMessage({ key: "tanks:validation.minimumBeforeMaximum" });
    if (effectiveMaximum > capacity) return setErrorMessage({ key: "tanks:validation.maximumWithinCapacity" });

    setIsSaving(true);
    setErrorMessage(null);
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
      setErrorMessage(error?.message ? { text: error.message } : { key: "tanks:feedback.saveFailed" });
    } finally {
      setIsSaving(false);
    }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return <AppModal isOpen={isOpen} title={t(isEditing ? "tanks:modal.editTitle" : "tanks:modal.createTitle")} description={t("tanks:modal.description")} size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="tank-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />{t("tanks:modal.saving")}</> : <><Save size={18} />{t("common:actions.save")}</>}</button></>}>
    <form id="tank-form" className="product-form-modal-form" onSubmit={handleSubmit}>
      {errorMessage && <div className="product-form-modal-alert" role="alert">{renderMessage(errorMessage)}</div>}
      <div className="product-form-modal-grid">
        {!fixedDepotId && <label><span>{t("tanks:modal.depot")} *</span><select name="depotId" value={formData.depotId} onChange={handleChange} required disabled={isEditing}><option value="">{t("tanks:modal.select")}</option>{depots.map((depot) => <option key={depot.id} value={depot.id}>{depot.name}</option>)}</select></label>}
        <label><span>{t("tanks:modal.product")} *</span><select name="productId" value={formData.productId} onChange={handleChange} required><option value="">{t("tanks:modal.select")}</option>{products.map((product) => <option key={product.id} value={product.id}>{product.name} ({product.code}){!product.active ? ` — ${t("tanks:modal.inactiveProduct")}` : ""}</option>)}</select></label>
        <label><span>{t("tanks:modal.name")} *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
        <label><span>{t("tanks:modal.code")} *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
        <label><span>{t("tanks:modal.capacity")} *</span><input type="number" name="capacityLiters" value={formData.capacityLiters} onChange={handleChange} min="0.001" step="0.001" required /></label>
      </div>

      <section className="tank-modal-levels">
        <div className="tank-modal-levels-heading"><strong>{t("tanks:modal.levels")}</strong><span>{t("tanks:modal.levelsRule")}</span></div>
        <div className="product-form-modal-grid">
          <label><span>{t("tanks:modal.minimumLevel")}</span><input type="number" name="minimumLevelLiters" value={formData.minimumLevelLiters} onChange={handleChange} min="0" step="0.001" placeholder={t("tanks:modal.minimumPlaceholder")} /></label>
          <label><span>{t("tanks:modal.maximumLevel")}</span><input type="number" name="maximumLevelLiters" value={formData.maximumLevelLiters} onChange={handleChange} min="0.001" step="0.001" placeholder={t("tanks:modal.maximumPlaceholder")} /></label>
        </div>
      </section>

      <details className="product-form-modal-section">
        <summary>{t("tanks:modal.additionalInformation")}</summary>
        <div className="product-form-modal-grid">
          <label className="product-form-modal-field-full"><span>{t("tanks:modal.location")}</span><input name="location" value={formData.location} onChange={handleChange} maxLength={255} /></label>
          <label><span>{t("tanks:modal.displayOrder")}</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
        </div>
      </details>

      {isEditing && <details className="product-form-modal-section product-form-modal-administration">
        <summary>{t("tanks:modal.administration")}</summary>
        <div className="product-form-modal-grid">
          <label><span>{t("tanks:modal.status")}</span><select name="status" value={formData.status} onChange={handleChange}>{STATUS_VALUES.map((value) => <option key={value} value={value}>{t(`tanks:status.${value}`)}</option>)}</select></label>
          <label className="product-form-modal-checkbox"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>{t("tanks:modal.activeUsable")}</span></label>
        </div>
      </details>}
    </form>
  </AppModal>;
}

export default TankModal;
