import { useState } from "react";
import { LoaderCircle } from "lucide-react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";

const PRICE_PATTERN = /^\d{1,16}(?:[.,]\d{1,3})?$/;

function validatePrice(value, t) {
  const normalized = value.trim();
  if (!normalized) return t("pricing:validation.required");
  if (!PRICE_PATTERN.test(normalized)) {
    return t("pricing:validation.format");
  }
  if (!/[1-9]/.test(normalized)) {
    return t("pricing:validation.positive");
  }
  return "";
}

function StationProductPriceModal({
  context,
  isSaving,
  errorMessage,
  onClose,
  onSubmit,
}) {
  const { t } = useTranslation(["pricing", "common"]);
  const { stationProduct, category, price, mode } = context;
  const [priceValue, setPriceValue] = useState(
    price?.price == null ? "" : String(price.price)
  );
  const [active, setActive] = useState(price?.active ?? true);
  const [validationError, setValidationError] = useState("");

  const handleSubmit = (event) => {
    event.preventDefault();
    const nextError = validatePrice(priceValue, t);
    setValidationError(nextError);
    if (nextError) return;
    const normalizedPrice = priceValue.trim().replace(",", ".");
    onSubmit(
      mode === "edit"
        ? { price: normalizedPrice, active }
        : { tariffCategoryId: category.id, price: normalizedPrice }
    );
  };

  const title = mode === "edit"
    ? t("pricing:modal.editTitle")
    : mode === "reactivate"
      ? t("pricing:modal.reactivateTitle")
      : t("pricing:modal.defineTitle");

  return (
    <AppModal
      isOpen
      size="sm"
      title={title}
      description={t("pricing:modal.description")}
      closeOnEscape={!isSaving}
      closeOnOverlay={!isSaving}
      onClose={isSaving ? undefined : onClose}
      footer={(
        <>
          <button type="button" className="pricing-modal-secondary" onClick={onClose} disabled={isSaving}>{t("common:actions.cancel")}</button>
          <button type="submit" form="station-product-price-form" className="pricing-modal-primary" disabled={isSaving}>
            {isSaving && <LoaderCircle className="pricing-spinner" size={16} />}
            {isSaving ? t("pricing:modal.saving") : mode === "reactivate" ? t("pricing:modal.reactivate") : t("common:actions.save")}
          </button>
        </>
      )}
    >
      <form id="station-product-price-form" className="pricing-modal-form" onSubmit={handleSubmit}>
        <div className="pricing-modal-context">
          <div><span>{t("pricing:modal.product")}</span><strong>{stationProduct.productName}</strong><small>{stationProduct.productCode} · {stationProduct.unit}</small></div>
          <div><span>{t("pricing:modal.category")}</span><strong>{category.name}</strong><small>{category.code}</small></div>
        </div>
        <label>
          <span>{t("pricing:modal.price")} *</span>
          <input
            autoFocus
            type="text"
            inputMode="decimal"
            value={priceValue}
            onChange={(event) => { setPriceValue(event.target.value); setValidationError(""); }}
            placeholder={t("pricing:modal.placeholder")}
            aria-invalid={Boolean(validationError)}
            disabled={isSaving}
          />
          <small>{t("pricing:modal.help")}</small>
        </label>
        {mode === "edit" && (
          <details className="pricing-modal-administration">
            <summary>{t("pricing:modal.administration")}</summary>
            <label className="pricing-modal-switch">
              <input type="checkbox" checked={active} onChange={(event) => setActive(event.target.checked)} disabled={isSaving} />
              <span><strong>{t("pricing:modal.activePrice")}</strong><small>{t("pricing:modal.activeHelp")}</small></span>
            </label>
          </details>
        )}
        {(validationError || errorMessage) && <p className="pricing-modal-error" role="alert">{validationError || errorMessage}</p>}
      </form>
    </AppModal>
  );
}

export default StationProductPriceModal;
