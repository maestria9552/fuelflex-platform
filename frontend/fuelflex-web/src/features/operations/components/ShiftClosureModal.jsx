import { Gauge } from "lucide-react";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";
import ConfirmationModal from "../../../components/modal/ConfirmationModal";

const number = (value) => value === "" || value === null || value === undefined ? null : Number(value);
const volume = (value, locale) => new Intl.NumberFormat(locale, { maximumFractionDigits: 3 }).format(Number(value || 0));

export default function ShiftClosureModal({ shift, returns, internals = [], locale, isLoading, errorMessage, onClose, onConfirm }) {
  const { t } = useTranslation(["operations", "common"]);
  const [closingIndex, setClosingIndex] = useState(shift?.openingIndex ?? "");
  const [creditQuantity, setCreditQuantity] = useState("0");
  const [confirming, setConfirming] = useState(false);
  const returned = useMemo(() => returns.reduce((sum, item) => sum + Number(item.quantity || 0), 0), [returns]);
  const internal = useMemo(() => internals.reduce((sum, item) => sum + Number(item.quantity || 0), 0), [internals]);
  const opening = Number(shift?.openingIndex || 0);
  const closing = number(closingIndex);
  const credit = number(creditQuantity);
  const metered = closing === null ? null : closing - opening;
  const sold = metered === null ? null : metered - returned - internal;
  const cash = sold === null || credit === null ? null : sold - credit;
  let validation = "";
  if (closing === null) validation = t("operations:closeShift.errors.finalRequired");
  else if (closing < opening) validation = t("operations:closeShift.errors.finalBelowOpening");
  else if (credit === null || credit < 0) validation = t("operations:closeShift.errors.creditNegative");
  else if (returned + internal > metered) validation = t("operations:closeShift.errors.returnsAboveOutput");
  else if (credit > sold) validation = t("operations:closeShift.errors.creditAboveSaleable", { value: volume(sold, locale) });
  else if (cash < 0) validation = t("operations:closeShift.errors.cashNegative");
  const rows = [["openingIndex", opening],["closingIndex", closing],["meteredVolume", metered],["tankReturnVolume", returned],["internalConsumptionVolume", internal],["soldVolume", sold],["cashVolume", cash],["creditVolume", credit]];
  const requestConfirmation = (event) => { event.preventDefault(); if (!validation && !isLoading) setConfirming(true); };
  const close = () => { if (!isLoading) { setConfirming(false); onClose(); } };

  return <>
    <AppModal isOpen={Boolean(shift) && !confirming} title={t("operations:closeShift.title")} description={t("operations:closeShift.description")} headerIcon={Gauge} size="md" isLoading={isLoading} closeOnEscape={!isLoading} closeOnOverlay={!isLoading} onClose={close} footer={<><button className="app-modal-action app-modal-action-no" type="button" onClick={close} disabled={isLoading}>{t("common:actions.no")}</button><button className="app-modal-action operations-manager-form-action" type="submit" form="close-shift-form" disabled={isLoading || Boolean(validation)}>{t("operations:closeShift.review")}</button></>}>
      <div className="operations-readonly-grid"><div><span>{t("operations:fields.pumpAttendant")}</span><strong>{shift?.pumpAttendant?.firstName} {shift?.pumpAttendant?.lastName}</strong></div><div><span>{t("operations:fields.pump")}</span><strong>{shift?.pump?.name || "—"}</strong></div><div><span>{t("operations:fields.fuelMeter")}</span><strong>{shift?.fuelMeter?.name || "—"}</strong></div><div><span>{t("operations:fields.product")}</span><strong>{shift?.productName || "—"}</strong></div><div><span>{t("operations:fields.sourceTank")}</span><strong>{shift?.sourceTankName || "—"}</strong></div><div><span>{t("operations:fields.returnedQuantity")}</span><strong>{volume(returned, locale)} L</strong></div><div><span>{t("operations:reconciliations.internalConsumptionVolume")}</span><strong>{volume(internal, locale)} L</strong></div></div>
      <form className="operations-form" id="close-shift-form" onSubmit={requestConfirmation}><label><span>{t("operations:fields.closingIndex")}</span><input type="number" min={opening} step="0.001" value={closingIndex} onChange={(event) => setClosingIndex(event.target.value)} required/></label><label><span>{t("operations:fields.creditQuantity")}</span><div className="operations-input-unit"><input type="number" min="0" step="0.001" value={creditQuantity} onChange={(event) => setCreditQuantity(event.target.value)} required/><b>L</b></div></label>{validation && <p className="operations-form-error" role="alert">{validation}</p>}{errorMessage && <p className="operations-form-error" role="alert">{errorMessage}</p>}</form>
      <div className="operations-calculation"><h3>{t("operations:closeShift.preview")}</h3><dl>{rows.map(([key,value]) => <div key={key}><dt>{t(`operations:closeShift.metrics.${key}`)}</dt><dd>{value === null ? "—" : volume(value, locale)} {key.includes("Index") ? "" : "L"}</dd></div>)}</dl><p>{t("operations:closeShift.financialAfter")}</p></div>
    </AppModal>
    <ConfirmationModal isOpen={Boolean(shift) && confirming} title={t("operations:closeShift.confirmTitle")} description={t("operations:closeShift.confirmDescription")} confirmLabel={t("common:actions.yes")} cancelLabel={t("common:actions.no")} loadingLabel={t("operations:closeShift.loading")} variant="warning" isLoading={isLoading} errorMessage={errorMessage} onClose={() => setConfirming(false)} onConfirm={() => onConfirm({ closingIndex, creditQuantity })}>
      <dl className="operations-confirmation-summary"><div><dt>{t("operations:fields.pumpAttendant")}</dt><dd>{shift?.pumpAttendant?.firstName} {shift?.pumpAttendant?.lastName}</dd></div><div><dt>{t("operations:fields.pump")}</dt><dd>{shift?.pump?.name || "—"}</dd></div>{rows.map(([key,value]) => <div key={key}><dt>{t(`operations:closeShift.metrics.${key}`)}</dt><dd>{volume(value, locale)} {key.includes("Index") ? "" : "L"}</dd></div>)}</dl>
    </ConfirmationModal>
  </>;
}
