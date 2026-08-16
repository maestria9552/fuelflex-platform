import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertCircle, BadgeDollarSign, Building2, CheckCircle2, CircleOff, LoaderCircle, Pencil, Plus, RefreshCw, RotateCcw, Search } from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import ConfirmationModal from "../../components/modal/ConfirmationModal";
import StationProductPriceModal from "../../features/pricing/components/StationProductPriceModal";
import { formatNumber, getLocaleForLanguage } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import { getStations } from "../../services/station/stationService";
import { getActiveStationProducts } from "../../services/stationProduct/stationProductService";
import { createStationProductPrice, deactivateStationProductPrice, getStationProductPrices, updateStationProductPrice } from "../../services/stationProductPrice/stationProductPriceService";
import { getActiveTariffCategories } from "../../services/tariffCategory/tariffCategoryService";
import "./PricingPage.css";

function formatPrice(value, language) {
  if (value == null || value === "") return "";
  return formatNumber(Number(value), { language, minimumFractionDigits: 0, maximumFractionDigits: 3 });
}

function StatePanel({ icon: Icon, title, text, loading = false }) {
  return <section className="pricing-state">{loading ? <LoaderCircle className="pricing-spinner" size={31} /> : Icon && <Icon size={32} />}{title && <h2>{title}</h2>}<p>{text}</p></section>;
}

function RetryAlert({ message, onRetry, retryLabel }) {
  return <div className="pricing-alert error" role="alert"><AlertCircle size={18} /><span>{message}</span>{onRetry && <button type="button" onClick={onRetry}><RefreshCw size={15} />{retryLabel}</button>}</div>;
}

function PricingPage() {
  const { t, i18n } = useTranslation(["pricing", "common"]);
  const organizationId = getStoredUser()?.organizationId || null;
  const [stations, setStations] = useState([]);
  const [selectedStationId, setSelectedStationId] = useState("");
  const [stationProducts, setStationProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [pricesByStationProduct, setPricesByStationProduct] = useState({});
  const [searchTerm, setSearchTerm] = useState("");
  const [modalContext, setModalContext] = useState(null);
  const [deactivationContext, setDeactivationContext] = useState(null);
  const [isSaving, setIsSaving] = useState(false);
  const [isDeactivating, setIsDeactivating] = useState(false);
  const [deactivationError, setDeactivationError] = useState(null);
  const [loading, setLoading] = useState({ stations: Boolean(organizationId), categories: Boolean(organizationId), products: false, prices: false });
  const [errors, setErrors] = useState({ stations: organizationId ? null : { key: "pricing:feedback.organizationMissing" }, categories: null, products: null, prices: null, modal: null });
  const [successMessage, setSuccessMessage] = useState(null);
  const [attempts, setAttempts] = useState({ stations: 0, categories: 0, products: 0, prices: 0 });

  const setLoadingKey = (key, value) => setLoading((current) => ({ ...current, [key]: value }));
  const setError = (key, value) => setErrors((current) => ({ ...current, [key]: value }));
  const retry = (key) => setAttempts((current) => ({ ...current, [key]: current[key] + 1 }));
  const language = i18n.resolvedLanguage;
  const locale = getLocaleForLanguage(language);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";
  const retryLabel = t("common:actions.retry");

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setLoadingKey("stations", true); setError("stations", null); return getStations(organizationId, { signal: controller.signal }); })
      .then((result) => { const loaded = Array.isArray(result) ? result : []; setStations(loaded); setSelectedStationId((current) => loaded.length === 1 ? loaded[0].id : loaded.some(({ id }) => id === current) ? current : ""); })
      .catch((error) => { if (error?.name !== "AbortError") setError("stations", error?.message ? { text: error.message } : { key: "pricing:feedback.stationsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setLoadingKey("stations", false); });
    return () => controller.abort();
  }, [attempts.stations, organizationId]);

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setLoadingKey("categories", true); setError("categories", null); return getActiveTariffCategories(organizationId, { signal: controller.signal }); })
      .then((result) => setCategories(Array.isArray(result) ? result : []))
      .catch((error) => { if (error?.name !== "AbortError") setError("categories", error?.message ? { text: error.message } : { key: "pricing:feedback.categoriesLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setLoadingKey("categories", false); });
    return () => controller.abort();
  }, [attempts.categories, organizationId]);

  useEffect(() => {
    if (!organizationId || !selectedStationId) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setLoadingKey("products", true); setError("products", null); return getActiveStationProducts(organizationId, selectedStationId, { signal: controller.signal }); })
      .then((result) => { const loaded = Array.isArray(result) ? result : []; setStationProducts(loaded); if (loaded.length === 0) setPricesByStationProduct({}); })
      .catch((error) => { if (error?.name !== "AbortError") setError("products", error?.message ? { text: error.message } : { key: "pricing:feedback.productsLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setLoadingKey("products", false); });
    return () => controller.abort();
  }, [attempts.products, organizationId, selectedStationId]);

  useEffect(() => {
    if (!organizationId || !selectedStationId || stationProducts.length === 0) return undefined;
    const controller = new AbortController();
    Promise.resolve().then(() => { setLoadingKey("prices", true); setError("prices", null); return Promise.all(stationProducts.map(async (item) => [item.id, await getStationProductPrices(organizationId, selectedStationId, item.id, { signal: controller.signal })])); })
      .then((entries) => setPricesByStationProduct(Object.fromEntries(entries.map(([id, prices]) => [id, Array.isArray(prices) ? prices : []]))))
      .catch((error) => { if (error?.name !== "AbortError") setError("prices", error?.message ? { text: error.message } : { key: "pricing:feedback.pricesLoadFailed" }); })
      .finally(() => { if (!controller.signal.aborted) setLoadingKey("prices", false); });
    return () => controller.abort();
  }, [attempts.prices, organizationId, selectedStationId, stationProducts]);

  const selectedStation = stations.find(({ id }) => id === selectedStationId) || null;
  const filteredProducts = useMemo(() => { const query = searchTerm.trim().toLocaleLowerCase(locale); return query ? stationProducts.filter((item) => [item.productName, item.productCode, item.productShortName].some((value) => value?.toLocaleLowerCase(locale).includes(query))) : stationProducts; }, [locale, searchTerm, stationProducts]);
  const getPrice = (stationProductId, categoryId) => (pricesByStationProduct[stationProductId] || []).find((price) => price.tariffCategoryId === categoryId) || null;
  const openModal = (stationProduct, category, price, mode) => { setError("modal", null); setSuccessMessage(null); setModalContext({ stationProduct, category, price, mode }); };

  const handleSave = async (payload) => {
    if (!modalContext || !selectedStationId || isSaving) return;
    setIsSaving(true); setError("modal", null);
    try {
      const { stationProduct, price, mode } = modalContext;
      if (mode === "edit") await updateStationProductPrice(organizationId, selectedStationId, stationProduct.id, price.id, payload);
      else await createStationProductPrice(organizationId, selectedStationId, stationProduct.id, payload);
      setModalContext(null);
      setSuccessMessage({ key: mode === "reactivate" ? "pricing:feedback.reactivated" : mode === "edit" ? "pricing:feedback.updated" : "pricing:feedback.defined" });
      retry("prices");
    } catch (error) { setError("modal", error?.message ? { text: error.message } : { key: "pricing:feedback.saveFailed" }); }
    finally { setIsSaving(false); }
  };

  const openDeactivationModal = (stationProduct, category, price) => {
    setSuccessMessage(null);
    setDeactivationError(null);
    setDeactivationContext({ stationProduct, category, price });
  };

  const handleDeactivate = async () => {
    if (!deactivationContext || !selectedStationId || isDeactivating) return;
    const { stationProduct, price } = deactivationContext;
    setIsDeactivating(true);
    setDeactivationError(null);
    try {
      await deactivateStationProductPrice(organizationId, selectedStationId, stationProduct.id, price.id);
      setDeactivationContext(null);
      setSuccessMessage({ key: "pricing:feedback.deactivated" });
      retry("prices");
    } catch (error) {
      setDeactivationError(error?.message ? { text: error.message } : { key: "pricing:feedback.deactivateFailed" });
    } finally {
      setIsDeactivating(false);
    }
  };

  return <SupervisorLayout><main className="pricing-page">
    <header className="pricing-header"><div><span>{t("pricing:page.eyebrow")}</span><h1>{t("pricing:page.title")}</h1><p>{t("pricing:page.description")}</p></div></header>
    {successMessage && <div className="pricing-alert success" role="status"><CheckCircle2 size={18} />{renderMessage(successMessage)}</div>}
    {errors.stations && <RetryAlert message={renderMessage(errors.stations)} onRetry={organizationId ? () => retry("stations") : undefined} retryLabel={retryLabel} />}
    {loading.stations ? <StatePanel loading text={t("pricing:page.loadingStations")} /> : !errors.stations && stations.length === 0 ? <StatePanel icon={Building2} title={t("pricing:page.noStationTitle")} text={t("pricing:page.noStationDescription")} /> : !errors.stations && <>
      <section className="pricing-station-selector"><label><span>{t("pricing:page.station")}</span><select value={selectedStationId} onChange={(event) => { setSelectedStationId(event.target.value); setStationProducts([]); setPricesByStationProduct({}); setSearchTerm(""); setSuccessMessage(null); setError("products", null); setError("prices", null); }}><option value="">{t("pricing:page.selectStation")}</option>{stations.map((station) => <option key={station.id} value={station.id}>{station.name} — {station.code}</option>)}</select></label>{selectedStation && <div><small>{t("pricing:page.selectedStation")}</small><strong>{selectedStation.name}</strong><span>{selectedStation.code}</span></div>}</section>
      {!selectedStationId ? <StatePanel icon={Building2} title={t("pricing:page.noStationSelectedTitle")} text={t(stations.length > 1 ? "pricing:page.selectExplicitStation" : "pricing:page.selectStationGrid")} /> : loading.products ? <StatePanel loading text={t("pricing:page.loadingProducts")} /> : errors.products ? <RetryAlert message={renderMessage(errors.products)} onRetry={() => retry("products")} retryLabel={retryLabel} /> : stationProducts.length === 0 ? <StatePanel icon={CircleOff} title={t("pricing:page.noProductTitle")} text={t("pricing:page.noProductDescription")} /> : loading.categories ? <StatePanel loading text={t("pricing:page.loadingCategories")} /> : errors.categories ? <RetryAlert message={renderMessage(errors.categories)} onRetry={() => retry("categories")} retryLabel={retryLabel} /> : categories.length === 0 ? <StatePanel icon={BadgeDollarSign} title={t("pricing:page.noCategoryTitle")} text={t("pricing:page.noCategoryDescription")} /> : <>
        <div className="pricing-toolbar"><label><Search size={17} /><input type="search" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder={t("pricing:page.searchPlaceholder")} aria-label={t("pricing:page.searchAriaLabel")} /></label><span>{t("pricing:page.productCount", { count: filteredProducts.length })} · {t("pricing:page.categoryCount", { count: categories.length })}</span></div>
        {errors.prices && <RetryAlert message={renderMessage(errors.prices)} onRetry={() => retry("prices")} retryLabel={retryLabel} />}
        {loading.prices ? <StatePanel loading text={t("pricing:page.loadingGrid")} /> : !errors.prices && filteredProducts.length === 0 ? <StatePanel icon={Search} title={t("pricing:page.noResultTitle")} text={t("pricing:page.noResultDescription")} /> : !errors.prices && <section className="pricing-table-card"><div className="pricing-table-scroll"><table><thead><tr><th scope="col" className="pricing-product-column">{t("pricing:page.product")}</th>{categories.map((category) => <th scope="col" key={category.id}><strong>{category.name}</strong><small>{category.code}</small></th>)}</tr></thead><tbody>{filteredProducts.map((stationProduct) => <tr key={stationProduct.id}><th scope="row" className="pricing-product-cell"><strong>{stationProduct.productName}</strong><span>{stationProduct.productCode} · {stationProduct.unit}</span></th>{categories.map((category) => { const price = getPrice(stationProduct.id, category.id); if (!price) return <td key={category.id}><div className="pricing-cell empty"><span>{t("pricing:page.notConfigured")}</span><button type="button" onClick={() => openModal(stationProduct, category, null, "create")}><Plus size={14} />{t("pricing:page.definePrice")}</button></div></td>; if (!price.active) return <td key={category.id}><div className="pricing-cell inactive"><strong>{formatPrice(price.price, language)}</strong><span>{t("pricing:page.inactive")}</span><button type="button" onClick={() => openModal(stationProduct, category, price, "reactivate")}><RotateCcw size={14} />{t("pricing:page.reactivate")}</button></div></td>; return <td key={category.id}><div className="pricing-cell active"><strong>{formatPrice(price.price, language)}</strong><span>{t("pricing:page.active")}</span><div><button type="button" onClick={() => openModal(stationProduct, category, price, "edit")}><Pencil size={14} />{t("pricing:page.edit")}</button><button type="button" className="danger" onClick={() => openDeactivationModal(stationProduct, category, price)}><CircleOff size={14} />{t("pricing:page.deactivate")}</button></div></div></td>; })}</tr>)}</tbody></table></div></section>}
      </>}
    </>}
  </main>{modalContext && <StationProductPriceModal key={`${modalContext.mode}-${modalContext.price?.id || modalContext.category.id}`} context={modalContext} isSaving={isSaving} errorMessage={renderMessage(errors.modal)} onClose={() => setModalContext(null)} onSubmit={handleSave} />}<ConfirmationModal isOpen={Boolean(deactivationContext)} title={t("pricing:confirmation.title")} description={deactivationContext ? t("pricing:confirmation.description", { categoryName: deactivationContext.category.name, productName: deactivationContext.stationProduct.productName }) : ""} confirmLabel={t("pricing:confirmation.confirm")} loadingLabel={t("pricing:confirmation.loading")} cancelLabel={t("common:actions.cancel")} variant="danger" isLoading={isDeactivating} errorMessage={renderMessage(deactivationError)} onConfirm={handleDeactivate} onClose={() => { setDeactivationContext(null); setDeactivationError(null); }} /></SupervisorLayout>;
}

export default PricingPage;
