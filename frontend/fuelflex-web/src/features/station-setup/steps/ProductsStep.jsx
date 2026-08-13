import { Link } from "react-router-dom";
import { ArrowLeft, ArrowRight, Check, Droplets, LoaderCircle, PackagePlus, RefreshCw } from "lucide-react";

function ProductsStep({ products, selectedProductIds, isLoading, isSaving, errorMessage, savedMessage, onToggle, onBack, onRetry, onContinue }) {
  return <section className="station-wizard-panel">
    <div className="station-wizard-heading"><span><Droplets size={24} /></span><div><small>ÉTAPE 2 SUR 9</small><h2>Produits utilisés dans cette station</h2><p>Sélectionnez les produits du catalogue organisationnel qui seront utilisés dans cette station.</p></div></div>
    <div className="station-wizard-explanation">Le catalogue est administré depuis le dashboard Produits. Cette étape sert uniquement à sélectionner les produits concernés par cette station.</div>

    {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> Réessayer</button></div>}
    {savedMessage && <div className="station-wizard-alert success" role="status">{savedMessage}</div>}

    {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />Chargement des produits actifs...</div> : products.length === 0 && !errorMessage ? <div className="station-wizard-resource-empty"><PackagePlus size={30} /><strong>Aucun produit n’est encore configuré dans votre catalogue.</strong><p>Ajoutez vos catégories et produits dans le catalogue permanent de l’organisation, puis revenez à cette étape.</p><Link className="station-setup-primary" to="/superviseur/produits?returnTo=station-setup">Gérer les produits</Link></div> : <div className="station-wizard-products-grid">{products.map((product) => {
      const isSelected = selectedProductIds.includes(product.id);
      return <button type="button" key={product.id} className={isSelected ? "selected" : ""} onClick={() => onToggle(product.id)} aria-pressed={isSelected}><span className="station-wizard-product-color" style={{ background: product.color || "#2563eb" }} /><span className="station-wizard-product-content"><strong>{product.name}</strong><small>{product.code} · {product.unit}</small>{product.categoryName && <em>{product.categoryName}</em>}</span><span className="station-wizard-product-check">{isSelected && <Check size={16} />}</span></button>;
    })}</div>}

    <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack} disabled={isSaving}><ArrowLeft size={17} /> Retour à la station</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={selectedProductIds.length === 0 || isLoading || isSaving}>{isSaving ? <LoaderCircle className="station-setup-spinner" size={17} /> : <ArrowRight size={17} />} {isSaving ? "Enregistrement..." : "Enregistrer la sélection"}</button></div>
  </section>;
}

export default ProductsStep;
