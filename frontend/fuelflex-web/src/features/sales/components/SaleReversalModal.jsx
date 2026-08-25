import { useState } from "react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";
import { reversePosSale } from "../../../services/sale/saleService";

export default function SaleReversalModal({ sale, isOpen, onClose, onSuccess }) {
  const { t } = useTranslation(["sales", "common"]);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const close = () => {
    if (!submitting) {
      setReason("");
      setError("");
      onClose?.();
    }
  };

  const submit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const reversedSale = await reversePosSale(sale.id, reason.trim());
      setReason("");
      onSuccess?.(reversedSale);
    } catch (requestError) {
      setError(requestError.message || t("sales:errors.reverse"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppModal
      isOpen={isOpen}
      title={t("sales:reversal.title")}
      description={t("sales:reversal.description", { number: sale?.saleNumber })}
      size="sm"
      closeOnEscape={!submitting}
      closeOnOverlay={!submitting}
      onClose={close}
      footer={
        <>
          <button className="sales-button sales-button-secondary" type="button" onClick={close} disabled={submitting}>
            {t("common:actions.cancel")}
          </button>
          <button className="sales-button sales-button-danger" type="submit" form="sale-reversal-form" disabled={submitting || !reason.trim()}>
            {submitting ? t("sales:reversal.loading") : t("sales:reversal.confirm")}
          </button>
        </>
      }
    >
      <form className="sales-form" id="sale-reversal-form" onSubmit={submit}>
        <label>
          <span>{t("sales:fields.reversalReason")}</span>
          <textarea rows="4" maxLength="1000" value={reason} onChange={(event) => setReason(event.target.value)} required autoFocus />
        </label>
        <p className="sales-warning">{t("sales:reversal.warning")}</p>
        {error && <p className="sales-form-error" role="alert">{error}</p>}
      </form>
    </AppModal>
  );
}
