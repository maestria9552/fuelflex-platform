import { CalendarCheck } from "lucide-react";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";
import { formatCurrency } from "../../../i18n/formatters";

const numeric = (value) => value === "" ? 0 : Number(value);

export default function DayClosureModal({ organization, report, openAssignments, reconciliationsCount, gaugesCount, language, isLoading, errorMessage, onClose, onConfirm }) {
  const { t } = useTranslation(["operations", "common"]);
  const currency = organization?.defaultCurrency || report?.referenceCurrency || "USD";
  const isReferenceUsd = currency.toUpperCase() === "USD";
  const [physicalReferenceAmount, setPhysicalReferenceAmount] = useState("");
  const [physicalUsdAmount, setPhysicalUsdAmount] = useState(isReferenceUsd ? "0" : "");
  const [usdExchangeRate, setUsdExchangeRate] = useState(isReferenceUsd ? "1" : "");
  const cashGross = Number(report?.cashGrossExpected ?? report?.expectedCash ?? 0);
  const expenses = Number(report?.disbursedExpenseAmount ?? report?.expensesAmount ?? report?.expenseAmount ?? 0);
  const cashNet = Number(report?.cashNetExpected ?? report?.expectedNetCash ?? cashGross - expenses);

  const preview = useMemo(() => {
    const convertedUsd = isReferenceUsd ? 0 : numeric(physicalUsdAmount) * numeric(usdExchangeRate);
    const observed = numeric(physicalReferenceAmount) + convertedUsd;
    const variance = observed - cashNet;
    const status = Math.abs(variance) < 0.0005 ? "CONFORME" : variance > 0 ? "EXCEDENT" : "DEFICIT";
    return { convertedUsd, observed, variance, status };
  }, [cashNet, isReferenceUsd, physicalReferenceAmount, physicalUsdAmount, usdExchangeRate]);

  const money = (value) => formatCurrency(value, currency, { language });
  const blocked = openAssignments.length > 0;
  const invalid = physicalReferenceAmount === "" || Number(physicalReferenceAmount) < 0
    || physicalUsdAmount === "" || Number(physicalUsdAmount) < 0
    || usdExchangeRate === "" || Number(usdExchangeRate) <= 0;

  const submit = (event) => {
    event.preventDefault();
    if (blocked || invalid || isLoading) return;
    onConfirm({
      physicalReferenceAmount: numeric(physicalReferenceAmount).toFixed(3),
      physicalUsdAmount: numeric(physicalUsdAmount).toFixed(3),
      usdExchangeRate: numeric(usdExchangeRate).toFixed(6),
    });
  };

  return <AppModal
    isOpen
    title={t("operations:closeDay.title")}
    description={blocked ? t("operations:closeDay.blocked", { count: openAssignments.length }) : t("operations:closeDay.description", { reconciliations: reconciliationsCount, gauges: gaugesCount })}
    headerIcon={CalendarCheck}
    size="lg"
    isLoading={isLoading}
    closeOnEscape={!isLoading}
    closeOnOverlay={!isLoading}
    onClose={onClose}
    footer={<><button className="app-modal-action app-modal-action-no" type="button" onClick={onClose} disabled={isLoading}>{t("operations:modal.no")}</button><button className="app-modal-action operations-manager-form-action" type="submit" form="close-operational-day-form" disabled={blocked || invalid || isLoading}>{isLoading ? t("operations:closeDay.loading") : t("operations:modal.yes")}</button></>}
  >
    {blocked ? <ul className="operations-open-shifts">{openAssignments.map((item) => <li key={item.id}>{[item.pumpAttendant?.firstName, item.pumpAttendant?.lastName].filter(Boolean).join(" ")} · {item.pump?.name}</li>)}</ul> : <form id="close-operational-day-form" className="cash-closure" onSubmit={submit}>
      <h3>{t("operations:closeDay.cash.title")}</h3>
      <div className="cash-closure-summary">
        <div><span>{t("operations:closeDay.cash.referenceCurrency")}</span><strong>{currency}</strong></div>
        <div><span>{t("operations:closeDay.cash.grossExpected")}</span><strong>{money(cashGross)}</strong></div>
        <div><span>{t("operations:closeDay.cash.disbursedExpenses")}</span><strong>{money(expenses)}</strong></div>
        <div><span>{t("operations:closeDay.cash.netExpected")}</span><strong>{money(cashNet)}</strong></div>
      </div>
      <div className="cash-closure-inputs">
        <label><span>{t("operations:closeDay.cash.referenceCash", { currency })}</span><input type="number" min="0" step="0.001" value={physicalReferenceAmount} onChange={(event) => setPhysicalReferenceAmount(event.target.value)} required /></label>
        <label><span>{t("operations:closeDay.cash.usdCash")}</span><input type="number" min="0" step="0.001" value={physicalUsdAmount} onChange={(event) => setPhysicalUsdAmount(event.target.value)} disabled={isReferenceUsd} required /></label>
        <label><span>{t("operations:closeDay.cash.rate", { currency })}</span><div className="cash-rate"><small>1 USD =</small><input type="number" min="0.000001" step="0.000001" value={usdExchangeRate} onChange={(event) => setUsdExchangeRate(event.target.value)} disabled={isReferenceUsd} required /><small>{currency}</small></div></label>
      </div>
      <div className="cash-closure-preview">
        <div><span>{t("operations:closeDay.cash.convertedUsd")}</span><strong>{money(preview.convertedUsd)}</strong></div>
        <div><span>{t("operations:closeDay.cash.observed")}</span><strong>{money(preview.observed)}</strong></div>
        <div><span>{t("operations:closeDay.cash.variance")}</span><strong>{money(preview.variance)}</strong></div>
        <b className={`cash-status cash-status-${preview.status}`}>{t(`operations:closeDay.cash.statuses.${preview.status}`)}</b>
      </div>
      <p className="cash-closure-authority">{t("operations:closeDay.cash.backendAuthority")}</p>
      {errorMessage && <p className="operations-form-error" role="alert">{errorMessage}</p>}
    </form>}
  </AppModal>;
}
