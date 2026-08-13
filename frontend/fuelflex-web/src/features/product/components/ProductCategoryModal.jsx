import { useState } from "react";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import {
  createProductCategory,
  updateProductCategory,
} from "../../../services/product/productCategoryService";
import "./ProductFormModal.css";

function ProductCategoryModal({
  isOpen,
  organizationId,
  category,
  onClose,
  onSaved,
}) {
  const [formData, setFormData] = useState(() => ({
    code: category?.code || "",
    name: category?.name || "",
    description: category?.description || "",
    active: category?.active ?? true,
  }));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const isEditing = Boolean(category?.id);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((currentData) => ({
      ...currentData,
      [name]: type === "checkbox" ? checked : value,
    }));
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
      description: formData.description.trim() || null,
      active: isEditing ? formData.active : true,
    };

    try {
      const savedCategory = isEditing
        ? await updateProductCategory(organizationId, category.id, payload)
        : await createProductCategory(organizationId, payload);
      onSaved?.(savedCategory, isEditing);
    } catch (error) {
      setErrorMessage(error?.message || `Impossible de ${isEditing ? "modifier" : "créer"} la catégorie.`);
    } finally {
      setIsSaving(false);
    }
  };

  const footer = (
    <>
      <button type="button" className="product-form-modal-cancel" onClick={onClose} disabled={isSaving}>Annuler</button>
      <button type="submit" form="product-category-form" className="product-form-modal-save" disabled={isSaving}>
        {isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />Enregistrement...</> : <><Save size={18} />Enregistrer</>}
      </button>
    </>
  );

  return (
    <AppModal
      isOpen={isOpen}
      title={isEditing ? "Modifier la catégorie" : "Créer une catégorie"}
      description={isEditing ? "Mettez à jour cette catégorie du catalogue." : "Ajoutez une catégorie de produits à votre organisation."}
      size="md"
      footer={footer}
      closeOnOverlay={!isSaving}
      closeOnEscape={!isSaving}
      onClose={onClose}
    >
      <form id="product-category-form" className="product-form-modal-form" onSubmit={handleSubmit}>
        {errorMessage && <div className="product-form-modal-alert" role="alert">{errorMessage}</div>}
        <div className="product-form-modal-grid">
          <label><span>Nom *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={120} required autoFocus /></label>
          <label><span>Code *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={50} required /></label>
        </div>
        <details className="product-form-modal-section">
          <summary>Informations complémentaires</summary>
          <div className="product-form-modal-grid">
            <label className="product-form-modal-field-full"><span>Description</span><textarea name="description" value={formData.description} onChange={handleChange} maxLength={500} rows={4} /></label>
          </div>
        </details>
        {isEditing && <details className="product-form-modal-section product-form-modal-administration">
          <summary>Administration</summary>
          <div className="product-form-modal-grid">
            <label className="product-form-modal-checkbox product-form-modal-field-full"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>Catégorie active et utilisable</span></label>
          </div>
        </details>}
      </form>
    </AppModal>
  );
}

export default ProductCategoryModal;
