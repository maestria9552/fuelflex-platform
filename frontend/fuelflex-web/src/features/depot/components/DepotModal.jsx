import { useState } from "react";
import { useTranslation } from "react-i18next";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { createDepot, updateDepot } from "../../../services/depot/depotService";
import "../../product/components/ProductFormModal.css";

function getInitialForm(depot) {
  return { name: depot?.name || "", code: depot?.code || "", description: depot?.description || "", location: depot?.location || "", displayOrder: depot?.displayOrder || 1, active: depot ? depot.active : true };
}

function DepotModal({ isOpen, organizationId, stationId, depot, onClose, onSaved }) {
  const { t } = useTranslation(["depots", "common"]);
  const [formData, setFormData] = useState(() => getInitialForm(depot));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const isEditing = Boolean(depot?.id);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    setIsSaving(true); setErrorMessage(null);
    const payload = {
      name: formData.name.trim(), code: formData.code.trim(), description: formData.description.trim() || null,
      location: formData.location.trim() || null, displayOrder: Number(formData.displayOrder) || 1,
      active: isEditing ? formData.active : true,
    };
    try {
      const savedDepot = isEditing ? await updateDepot(organizationId, stationId, depot.id, payload) : await createDepot(organizationId, stationId, payload);
      onSaved?.(savedDepot, isEditing);
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: "depots:feedback.saveFailed" });
    } finally { setIsSaving(false); }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return <AppModal isOpen={isOpen} title={t(isEditing ? "depots:modal.editTitle" : "depots:modal.createTitle")} description={t("depots:modal.description")} size="md" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="depot-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />{t("depots:modal.saving")}</> : <><Save size={18} />{t("common:actions.save")}</>}</button></>}>
    <form id="depot-form" className="product-form-modal-form" onSubmit={handleSubmit}>
      {errorMessage && <div className="product-form-modal-alert" role="alert">{renderMessage(errorMessage)}</div>}
      <div className="product-form-modal-grid">
        <label><span>{t("depots:modal.name")} *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required autoFocus /></label>
        <label><span>{t("depots:modal.code")} *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
      </div>
      <details className="product-form-modal-section"><summary>{t("depots:modal.additionalInformation")}</summary><div className="product-form-modal-grid">
        <label className="product-form-modal-field-full"><span>{t("depots:modal.descriptionLabel")}</span><textarea name="description" value={formData.description} onChange={handleChange} maxLength={255} rows={3} /></label>
        <label className="product-form-modal-field-full"><span>{t("depots:modal.location")}</span><input name="location" value={formData.location} onChange={handleChange} maxLength={255} /></label>
        <label><span>{t("depots:modal.displayOrder")}</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="1" /></label>
      </div></details>
      {isEditing && <details className="product-form-modal-section product-form-modal-administration"><summary>{t("depots:modal.administration")}</summary><div className="product-form-modal-grid"><label className="product-form-modal-checkbox product-form-modal-field-full"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>{t("depots:modal.activeUsable")}</span></label></div></details>}
    </form>
  </AppModal>;
}

export default DepotModal;
