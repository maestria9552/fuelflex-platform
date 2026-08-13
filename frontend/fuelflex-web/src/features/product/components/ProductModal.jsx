import { useState } from "react";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { createProduct, updateProduct } from "../../../services/product/productService";
import "./ProductFormModal.css";

const PRODUCT_UNITS = ["LITRE", "KILOGRAM", "PIECE", "BIDON", "BOUTEILLE", "CARTON", "BARIL"];

function ProductModal({
  isOpen,
  organizationId,
  categories,
  product,
  onClose,
  onSaved,
}) {
  const [formData, setFormData] = useState(() => ({
    categoryId: product?.categoryId || categories?.[0]?.id || "",
    code: product?.code || "",
    name: product?.name || "",
    shortName: product?.shortName || "",
    description: product?.description || "",
    unit: product?.unit || "LITRE",
    barcode: product?.barcode || "",
    color: product?.color || "",
    displayOrder: product?.displayOrder ?? "",
    active: product?.active ?? true,
  }));
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const isEditing = Boolean(product?.id);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((currentData) => ({ ...currentData, [name]: type === "checkbox" ? checked : value }));
    setErrorMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!formData.categoryId) {
      setErrorMessage("Sélectionnez une catégorie avant d’enregistrer le produit.");
      return;
    }
    if (isSaving) return;
    setIsSaving(true);
    setErrorMessage("");

    const payload = {
      categoryId: formData.categoryId,
      code: formData.code.trim(),
      name: formData.name.trim(),
      shortName: formData.shortName.trim() || null,
      description: formData.description.trim() || null,
      unit: formData.unit,
      barcode: formData.barcode.trim() || null,
      color: formData.color.trim() || null,
      displayOrder: formData.displayOrder === "" ? null : Number(formData.displayOrder),
      active: isEditing ? formData.active : true,
    };

    try {
      const savedProduct = isEditing
        ? await updateProduct(organizationId, product.id, payload)
        : await createProduct(organizationId, payload);
      onSaved?.(savedProduct, isEditing);
    } catch (error) {
      setErrorMessage(error?.message || `Impossible de ${isEditing ? "modifier" : "créer"} le produit.`);
    } finally {
      setIsSaving(false);
    }
  };

  const footer = (
    <>
      <button type="button" className="product-form-modal-cancel" onClick={onClose} disabled={isSaving}>Annuler</button>
      <button type="submit" form="product-form" className="product-form-modal-save" disabled={isSaving || !formData.categoryId}>
        {isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />Enregistrement...</> : <><Save size={18} />Enregistrer</>}
      </button>
    </>
  );

  return (
    <AppModal
      isOpen={isOpen}
      title={isEditing ? "Modifier le produit" : "Créer un produit"}
      description={isEditing ? "Mettez à jour ce produit du catalogue permanent." : "Ajoutez un produit actif réutilisable dans vos stations."}
      size="lg"
      footer={footer}
      closeOnOverlay={!isSaving}
      closeOnEscape={!isSaving}
      onClose={onClose}
    >
      <form id="product-form" className="product-form-modal-form" onSubmit={handleSubmit}>
        {errorMessage && <div className="product-form-modal-alert" role="alert">{errorMessage}</div>}
        <div className="product-form-modal-grid">
          <label className="product-form-modal-field-full"><span>Catégorie *</span><select name="categoryId" value={formData.categoryId} onChange={handleChange} required autoFocus><option value="">Sélectionner une catégorie</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name} — {category.code}</option>)}</select></label>
          <label><span>Nom *</span><input name="name" value={formData.name} onChange={handleChange} maxLength={150} required /></label>
          <label><span>Code *</span><input name="code" value={formData.code} onChange={handleChange} maxLength={30} required /></label>
          <label><span>Unité *</span><select name="unit" value={formData.unit} onChange={handleChange} required>{PRODUCT_UNITS.map((unit) => <option key={unit} value={unit}>{unit}</option>)}</select></label>
        </div>
        <details className="product-form-modal-section">
          <summary>Informations complémentaires</summary>
          <div className="product-form-modal-grid">
            <label><span>Nom court</span><input name="shortName" value={formData.shortName} onChange={handleChange} maxLength={80} /></label>
            <label><span>Code-barres</span><input name="barcode" value={formData.barcode} onChange={handleChange} maxLength={20} /></label>
            <label><span>Couleur</span><input name="color" value={formData.color} onChange={handleChange} maxLength={20} placeholder="Ex. #F59E0B" /></label>
            <label><span>Ordre d’affichage</span><input type="number" name="displayOrder" value={formData.displayOrder} onChange={handleChange} min="0" /></label>
            <label className="product-form-modal-field-full"><span>Description</span><textarea name="description" value={formData.description} onChange={handleChange} maxLength={500} rows={4} /></label>
          </div>
        </details>
        {isEditing && <details className="product-form-modal-section product-form-modal-administration">
          <summary>Administration</summary>
          <div className="product-form-modal-grid">
            <label className="product-form-modal-checkbox product-form-modal-field-full"><input type="checkbox" name="active" checked={formData.active} onChange={handleChange} /><span>Produit actif et utilisable</span></label>
          </div>
        </details>}
      </form>
    </AppModal>
  );
}

export default ProductModal;
