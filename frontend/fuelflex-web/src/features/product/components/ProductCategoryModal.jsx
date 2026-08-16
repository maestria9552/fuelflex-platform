import { useState } from "react";
import { useTranslation } from "react-i18next";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { createProductCategory, updateProductCategory } from "../../../services/product/productCategoryService";
import "./ProductFormModal.css";

function ProductCategoryModal({ isOpen, organizationId, category, onClose, onSaved }) {
  const { t } = useTranslation(["products", "common"]);
  const [formData, setFormData] = useState(() => ({ code: category?.code || "", name: category?.name || "", description: category?.description || "", active: category?.active ?? true }));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const isEditing = Boolean(category?.id);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((currentData) => ({ ...currentData, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (isSaving) return;
    setIsSaving(true); setErrorMessage(null);
    const payload = { code: formData.code.trim(), name: formData.name.trim(), description: formData.description.trim() || null, active: isEditing ? formData.active : true };
    try {
      const savedCategory = isEditing ? await updateProductCategory(organizationId, category.id, payload) : await createProductCategory(organizationId, payload);
      onSaved?.(savedCategory, isEditing);
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: isEditing ? "products:categoryModal.updateFailed" : "products:categoryModal.createFailed" });
    } finally { setIsSaving(false); }
  };

  const footer = <><button type="button" className="product-form-modal-cancel" onClick={onClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="product-category-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />{t("products:commonForm.saving")}</> : <><Save size={18} />{t("common:actions.save")}</>}</button></>;

  return <AppModal isOpen={isOpen} title={t(isEditing ? "products:categoryModal.editTitle" : "products:categoryModal.createTitle")} description={t(isEditing ? "products:categoryModal.editDescription" : "products:categoryModal.createDescription")} size="md" footer={footer} closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={onClose}>
    <form id="product-category-form" className="product-form-modal-form" onSubmit={handleSubmit}>
      {errorMessage && <div className="product-form-modal-alert" role="alert">{renderMessage(errorMessage)}</div>}
      <div className="product-form-modal-grid">
        <label><span>{t("products:commonForm.name")} *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={120} required autoFocus /></label>
        <label><span>{t("products:commonForm.code")} *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
      </div>
      <details className="product-form-modal-section"><summary>{t("products:commonForm.additionalInformation")}</summary><div className="product-form-modal-grid"><label className="product-form-modal-field-full"><span>{t("products:commonForm.description")}</span><textarea name="description" value={formData.description} onChange={handleChange} maxLength={500} rows={4} /></label></div></details>
      {isEditing && <details className="product-form-modal-section product-form-modal-administration"><summary>{t("products:commonForm.administration")}</summary><div className="product-form-modal-grid"><label className="product-form-modal-checkbox product-form-modal-field-full"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>{t("products:categoryModal.activeUsable")}</span></label></div></details>}
    </form>
  </AppModal>;
}

export default ProductCategoryModal;
