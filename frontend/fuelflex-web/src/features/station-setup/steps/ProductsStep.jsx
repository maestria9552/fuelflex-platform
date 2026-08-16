import { Link } from "react-router-dom";
import { ArrowLeft, ArrowRight, Check, Droplets, LoaderCircle, PackagePlus, RefreshCw } from "lucide-react";
import { useTranslation } from "react-i18next";

function ProductsStep({ products, selectedProductIds, isLoading, isSaving, errorMessage, savedMessage, onToggle, onBack, onRetry, onContinue }) {
  const { t } = useTranslation(["stationSetup", "products"]);
  return <section className="station-wizard-panel">
    <div className="station-wizard-heading"><span><Droplets size={24} /></span><div><small>{t("steps.progress", { current: 2, total: 9 })}</small><h2>{t("steps.products.title")}</h2><p>{t("steps.products.description")}</p></div></div>
    <div className="station-wizard-explanation">{t("steps.products.help")}</div>

    {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> {t("steps.retry")}</button></div>}
    {savedMessage && <div className="station-wizard-alert success" role="status">{savedMessage}</div>}

    {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />{t("steps.products.loading")}</div> : products.length === 0 && !errorMessage ? <div className="station-wizard-resource-empty"><PackagePlus size={30} /><strong>{t("steps.products.emptyTitle")}</strong><p>{t("steps.products.emptyDescription")}</p><Link className="station-setup-primary" to="/superviseur/produits?returnTo=station-setup">{t("steps.products.manage")}</Link></div> : <div className="station-wizard-products-grid">{products.map((product) => {
      const isSelected = selectedProductIds.includes(product.id);
      return <button type="button" key={product.id} className={isSelected ? "selected" : ""} onClick={() => onToggle(product.id)} aria-pressed={isSelected}><span className="station-wizard-product-color" style={{ background: product.color || "#2563eb" }} /><span className="station-wizard-product-content"><strong>{product.name}</strong><small>{product.code} · {t(`products:units.${product.unit}`, { defaultValue: product.unit })}</small>{product.categoryName && <em>{product.categoryName}</em>}</span><span className="station-wizard-product-check">{isSelected && <Check size={16} />}</span></button>;
    })}</div>}

    <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack} disabled={isSaving}><ArrowLeft size={17} /> {t("steps.products.back")}</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={selectedProductIds.length === 0 || isLoading || isSaving}>{isSaving ? <LoaderCircle className="station-setup-spinner" size={17} /> : <ArrowRight size={17} />} {isSaving ? t("steps.station.saving") : t("steps.products.save")}</button></div>
  </section>;
}

export default ProductsStep;
