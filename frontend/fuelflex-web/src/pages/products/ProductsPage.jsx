import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import { AlertCircle, ArrowLeft, CheckCircle2, FolderPlus, LoaderCircle, PackageOpen, PackagePlus, Pencil, RefreshCw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import ProductCategoryModal from "../../features/product/components/ProductCategoryModal";
import ProductModal from "../../features/product/components/ProductModal";
import { getLocaleForLanguage } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import { getProductCategories } from "../../services/product/productCategoryService";
import { getProducts } from "../../services/product/productService";
import "./ProductsPage.css";

function ProductsPage() {
  const { t, i18n } = useTranslation(["products", "common"]);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const organizationId = getStoredUser()?.organizationId || null;
  const returnToStationSetup = searchParams.get("returnTo") === "station-setup";
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("ALL");
  const [isLoading, setIsLoading] = useState(Boolean(organizationId));
  const [errorMessage, setErrorMessage] = useState(organizationId ? null : { key: "products:feedback.organizationMissing" });
  const [successMessage, setSuccessMessage] = useState(null);
  const [loadAttempt, setLoadAttempt] = useState(0);
  const [categoryModal, setCategoryModal] = useState(undefined);
  const [productModal, setProductModal] = useState(undefined);
  const locale = getLocaleForLanguage(i18n.resolvedLanguage);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => {
      setIsLoading(true); setErrorMessage(null);
      return Promise.all([getProductCategories(organizationId, { signal: controller.signal }), getProducts(organizationId, { signal: controller.signal })]);
    }).then(([loadedCategories, loadedProducts]) => {
      setCategories(Array.isArray(loadedCategories) ? loadedCategories : []);
      setProducts(Array.isArray(loadedProducts) ? loadedProducts : []);
    }).catch((error) => {
      if (error?.name !== "AbortError") setErrorMessage(error?.message ? { text: error.message } : { key: "products:feedback.loadFailed" });
    }).finally(() => { if (!controller.signal.aborted) setIsLoading(false); });
    return () => controller.abort();
  }, [loadAttempt, organizationId]);

  const productCounts = useMemo(() => products.reduce((counts, product) => ({ ...counts, [product.categoryId]: (counts[product.categoryId] || 0) + 1 }), {}), [products]);
  const activeCategories = useMemo(() => categories.filter((category) => category.active), [categories]);
  const filteredProducts = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase(locale);
    return products.filter((product) => {
      const matchesCategory = categoryFilter === "ALL" || product.categoryId === categoryFilter;
      const matchesSearch = !query || [product.name, product.code, product.categoryName].some((value) => value?.toLocaleLowerCase(locale).includes(query));
      return matchesCategory && matchesSearch;
    });
  }, [categoryFilter, locale, products, searchTerm]);

  const reloadAfterSave = (messageKey) => {
    setCategoryModal(undefined); setProductModal(undefined); setSuccessMessage({ key: messageKey });
    setLoadAttempt((attempt) => attempt + 1);
  };
  const openNewProduct = () => { setSuccessMessage(null); if (activeCategories.length > 0) setProductModal(null); };

  return <SupervisorLayout>
    <main className="products-page">
      <header className="products-page-header"><div><span>{t("products:page.eyebrow")}</span><h1>{t("products:page.title")}</h1><p>{t("products:page.description")}</p></div></header>
      {returnToStationSetup && <button type="button" className="products-page-return" onClick={() => navigate("/superviseur/stations/nouvelle")}><ArrowLeft size={17} />{t("products:page.returnToStationSetup")}</button>}
      {successMessage && <div className="products-page-alert success" role="status"><CheckCircle2 size={18} />{renderMessage(successMessage)}</div>}
      {errorMessage && <div className="products-page-alert error" role="alert"><AlertCircle size={18} /><span>{renderMessage(errorMessage)}</span>{organizationId && <button type="button" onClick={() => setLoadAttempt((attempt) => attempt + 1)}><RefreshCw size={15} />{t("common:actions.retry")}</button>}</div>}

      {isLoading ? <section className="products-page-loading"><LoaderCircle className="station-setup-spinner" size={30} />{t("products:page.loading")}</section> : !errorMessage && <>
        <section className="products-page-section">
          <header><div><FolderPlus size={21} /><span><strong>{t("products:categories.title")}</strong><small>{t("products:categories.count", { count: categories.length })}</small></span></div><button type="button" onClick={() => { setSuccessMessage(null); setCategoryModal(null); }}><FolderPlus size={16} />{t("products:categories.add")}</button></header>
          {categories.length === 0 ? <div className="products-page-empty"><FolderPlus size={28} /><strong>{t("products:categories.emptyTitle")}</strong><p>{t("products:categories.emptyDescription")}</p><button type="button" onClick={() => setCategoryModal(null)}><FolderPlus size={16} />{t("products:categories.create")}</button></div> : <div className="products-page-grid">{categories.map((category) => <article key={category.id} className={!category.active ? "inactive" : ""}><div><small>{category.code}</small><h2>{category.name}</h2><p>{t("products:categories.productCount", { count: productCounts[category.id] || 0 })}</p><em>{t(category.active ? "products:availability.activeFeminine" : "products:availability.inactiveFeminine")}</em></div><button type="button" onClick={() => { setSuccessMessage(null); setCategoryModal(category); }}><Pencil size={15} />{t("products:actions.edit")}</button></article>)}</div>}
        </section>

        <section className="products-page-section">
          <header><div><PackageOpen size={21} /><span><strong>{t("products:products.title")}</strong><small>{t("products:products.count", { count: products.length })}</small></span></div><button type="button" onClick={openNewProduct} disabled={activeCategories.length === 0}><PackagePlus size={16} />{t("products:products.add")}</button></header>
          {activeCategories.length === 0 && <div className="products-page-category-required"><AlertCircle size={18} /><span>{t("products:products.categoryRequired")}</span><button type="button" onClick={() => setCategoryModal(null)}>{t("products:categories.create")}</button></div>}
          {products.length > 0 && <div className="products-page-filters"><label><Search size={16} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder={t("products:products.searchPlaceholder")} aria-label={t("products:products.searchAriaLabel")} /></label><select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)} aria-label={t("products:products.filterAriaLabel")}><option value="ALL">{t("products:products.allCategories")}</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></div>}
          {products.length === 0 ? <div className="products-page-empty"><PackageOpen size={28} /><strong>{t("products:products.emptyTitle")}</strong><p>{t("products:products.emptyDescription")}</p>{activeCategories.length > 0 && <button type="button" onClick={openNewProduct}><PackagePlus size={16} />{t("products:products.add")}</button>}</div> : filteredProducts.length === 0 ? <div className="products-page-empty compact"><Search size={27} /><strong>{t("products:products.noResultTitle")}</strong><p>{t("products:products.noResultDescription")}</p></div> : <div className="products-page-grid">{filteredProducts.map((product) => <article key={product.id} className={!product.active ? "inactive" : ""}><span className="products-page-color" style={{ backgroundColor: product.color || "#cbd5e1" }} /><div><small>{product.code}</small><h2>{product.name}</h2><p>{product.categoryName} · {t(`products:units.${product.unit}`, { defaultValue: product.unit })}</p><em>{t(product.active ? "products:availability.activeMasculine" : "products:availability.inactiveMasculine")}</em></div><button type="button" onClick={() => { setSuccessMessage(null); setProductModal(product); }}><Pencil size={15} />{t("products:actions.edit")}</button></article>)}</div>}
        </section>
      </>}
    </main>
    {categoryModal !== undefined && <ProductCategoryModal key={categoryModal?.id || "new-category"} isOpen organizationId={organizationId} category={categoryModal} onClose={() => setCategoryModal(undefined)} onSaved={(_, wasUpdate) => reloadAfterSave(wasUpdate ? "products:feedback.categoryUpdated" : "products:feedback.categoryCreated")} />}
    {productModal !== undefined && <ProductModal key={productModal?.id || "new-product"} isOpen organizationId={organizationId} categories={productModal?.id ? categories.filter((category) => category.active || category.id === productModal.categoryId) : activeCategories} product={productModal} onClose={() => setProductModal(undefined)} onSaved={(_, wasUpdate) => reloadAfterSave(wasUpdate ? "products:feedback.productUpdated" : "products:feedback.productCreated")} />}
  </SupervisorLayout>;
}

export default ProductsPage;
