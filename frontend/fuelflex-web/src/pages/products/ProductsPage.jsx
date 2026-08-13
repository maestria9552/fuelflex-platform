import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { AlertCircle, ArrowLeft, CheckCircle2, FolderPlus, LoaderCircle, PackageOpen, PackagePlus, Pencil, RefreshCw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import ProductCategoryModal from "../../features/product/components/ProductCategoryModal";
import ProductModal from "../../features/product/components/ProductModal";
import { getStoredUser } from "../../services/auth/authStorage";
import { getProductCategories } from "../../services/product/productCategoryService";
import { getProducts } from "../../services/product/productService";
import "./ProductsPage.css";

function ProductsPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const organizationId = getStoredUser()?.organizationId || null;
  const returnToStationSetup = searchParams.get("returnTo") === "station-setup";
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("ALL");
  const [isLoading, setIsLoading] = useState(Boolean(organizationId));
  const [errorMessage, setErrorMessage] = useState(organizationId ? "" : "Aucune société n’est associée à ce compte.");
  const [successMessage, setSuccessMessage] = useState("");
  const [loadAttempt, setLoadAttempt] = useState(0);
  const [categoryModal, setCategoryModal] = useState(undefined);
  const [productModal, setProductModal] = useState(undefined);

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => {
      setIsLoading(true); setErrorMessage("");
      return Promise.all([getProductCategories(organizationId, { signal: controller.signal }), getProducts(organizationId, { signal: controller.signal })]);
    }).then(([loadedCategories, loadedProducts]) => {
      setCategories(Array.isArray(loadedCategories) ? loadedCategories : []);
      setProducts(Array.isArray(loadedProducts) ? loadedProducts : []);
    }).catch((error) => {
      if (error?.name !== "AbortError") setErrorMessage(error?.message || "Impossible de charger le catalogue de produits.");
    }).finally(() => { if (!controller.signal.aborted) setIsLoading(false); });
    return () => controller.abort();
  }, [loadAttempt, organizationId]);

  const productCounts = useMemo(() => products.reduce((counts, product) => ({ ...counts, [product.categoryId]: (counts[product.categoryId] || 0) + 1 }), {}), [products]);
  const activeCategories = useMemo(() => categories.filter((category) => category.active), [categories]);
  const filteredProducts = useMemo(() => {
    const query = searchTerm.trim().toLocaleLowerCase("fr");
    return products.filter((product) => {
      const matchesCategory = categoryFilter === "ALL" || product.categoryId === categoryFilter;
      const matchesSearch = !query || [product.name, product.code, product.categoryName].some((value) => value?.toLocaleLowerCase("fr").includes(query));
      return matchesCategory && matchesSearch;
    });
  }, [categoryFilter, products, searchTerm]);

  const reloadAfterSave = (message) => {
    setCategoryModal(undefined); setProductModal(undefined); setSuccessMessage(message);
    setLoadAttempt((attempt) => attempt + 1);
  };

  const openNewProduct = () => {
    setSuccessMessage("");
    if (activeCategories.length === 0) return;
    setProductModal(null);
  };

  return <SupervisorLayout>
    <main className="products-page">
      <header className="products-page-header">
        <div><span>CATALOGUE ORGANISATION</span><h1>Catalogue produits</h1><p>Configurez ici les produits disponibles dans votre organisation. Ils pourront ensuite être utilisés dans vos différentes stations.</p></div>
      </header>

      {returnToStationSetup && <button type="button" className="products-page-return" onClick={() => navigate("/superviseur/stations/nouvelle")}><ArrowLeft size={17} />Retour à la création de station</button>}
      {successMessage && <div className="products-page-alert success" role="status"><CheckCircle2 size={18} />{successMessage}</div>}
      {errorMessage && <div className="products-page-alert error" role="alert"><AlertCircle size={18} /><span>{errorMessage}</span>{organizationId && <button type="button" onClick={() => setLoadAttempt((attempt) => attempt + 1)}><RefreshCw size={15} />Réessayer</button>}</div>}

      {isLoading ? <section className="products-page-loading"><LoaderCircle className="station-setup-spinner" size={30} />Chargement du catalogue...</section> : !errorMessage && <>
        <section className="products-page-section">
          <header><div><FolderPlus size={21} /><span><strong>Catégories</strong><small>{categories.length} catégorie{categories.length > 1 ? "s" : ""}</small></span></div><button type="button" onClick={() => { setSuccessMessage(""); setCategoryModal(null); }}><FolderPlus size={16} />Ajouter une catégorie</button></header>
          {categories.length === 0 ? <div className="products-page-empty"><FolderPlus size={28} /><strong>Aucune catégorie configurée</strong><p>Créez une catégorie avant d’ajouter les produits du catalogue.</p><button type="button" onClick={() => setCategoryModal(null)}><FolderPlus size={16} />Créer une catégorie</button></div> : <div className="products-page-grid">{categories.map((category) => <article key={category.id} className={!category.active ? "inactive" : ""}><div><small>{category.code}</small><h2>{category.name}</h2><p>{productCounts[category.id] || 0} produit{(productCounts[category.id] || 0) > 1 ? "s" : ""}</p><em>{category.active ? "Active" : "Inactive"}</em></div><button type="button" onClick={() => { setSuccessMessage(""); setCategoryModal(category); }}><Pencil size={15} />Modifier</button></article>)}</div>}
        </section>

        <section className="products-page-section">
          <header><div><PackageOpen size={21} /><span><strong>Produits</strong><small>{products.length} produit{products.length > 1 ? "s" : ""}</small></span></div><button type="button" onClick={openNewProduct} disabled={activeCategories.length === 0}><PackagePlus size={16} />Ajouter un produit</button></header>
          {activeCategories.length === 0 && <div className="products-page-category-required"><AlertCircle size={18} /><span>Une catégorie active doit être créée avant d’ajouter un produit.</span><button type="button" onClick={() => setCategoryModal(null)}>Créer une catégorie</button></div>}
          {products.length > 0 && <div className="products-page-filters"><label><Search size={16} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder="Rechercher par nom, code ou catégorie" aria-label="Rechercher un produit" /></label><select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)} aria-label="Filtrer par catégorie"><option value="ALL">Toutes les catégories</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></div>}
          {products.length === 0 ? <div className="products-page-empty"><PackageOpen size={28} /><strong>Aucun produit configuré</strong><p>Ajoutez autant de produits que nécessaire à votre catalogue permanent.</p>{activeCategories.length > 0 && <button type="button" onClick={openNewProduct}><PackagePlus size={16} />Ajouter un produit</button>}</div> : filteredProducts.length === 0 ? <div className="products-page-empty compact"><Search size={27} /><strong>Aucun produit trouvé</strong><p>Modifiez la recherche ou le filtre de catégorie.</p></div> : <div className="products-page-grid">{filteredProducts.map((product) => <article key={product.id} className={!product.active ? "inactive" : ""}><span className="products-page-color" style={{ backgroundColor: product.color || "#cbd5e1" }} /><div><small>{product.code}</small><h2>{product.name}</h2><p>{product.categoryName} · {product.unit}</p><em>{product.active ? "Actif" : "Inactif"}</em></div><button type="button" onClick={() => { setSuccessMessage(""); setProductModal(product); }}><Pencil size={15} />Modifier</button></article>)}</div>}
        </section>
      </>}
    </main>

    {categoryModal !== undefined && <ProductCategoryModal key={categoryModal?.id || "new-category"} isOpen organizationId={organizationId} category={categoryModal} onClose={() => setCategoryModal(undefined)} onSaved={(_, wasUpdate) => reloadAfterSave(wasUpdate ? "La catégorie a été modifiée avec succès." : "La catégorie a été créée avec succès.")} />}
    {productModal !== undefined && <ProductModal key={productModal?.id || "new-product"} isOpen organizationId={organizationId} categories={productModal?.id ? categories.filter((category) => category.active || category.id === productModal.categoryId) : activeCategories} product={productModal} onClose={() => setProductModal(undefined)} onSaved={(_, wasUpdate) => reloadAfterSave(wasUpdate ? "Le produit a été modifié avec succès." : "Le produit a été créé avec succès.")} />}
  </SupervisorLayout>;
}

export default ProductsPage;
