import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { formatCurrency, formatDateTime } from "../../../i18n/formatters";
import { getOrganizationById } from "../../../services/organization/organizationService";
import { getInternalConsumptions, getOperationalDayForRole, getShiftAssignmentsForRole, getShiftReconciliations, getTankReturns } from "../../../services/operations/operationalService";
import PrintableReportHeader from "./PrintableReportHeader";
import "./OperationsPrint.css";

const n = (value) => Number(value || 0).toLocaleString(undefined, { maximumFractionDigits: 3 });

export default function PumpAttendantPrintPage({ role }) {
  const { dayId, assignmentId } = useParams();
  const [searchParams] = useSearchParams();
  const { t, i18n } = useTranslation("reports");
  const [data, setData] = useState(null);

  useEffect(() => {
    (async () => {
      const day = await getOperationalDayForRole(role, dayId);
      const [assignments, reconciliations, returns, internals, organization] = await Promise.all([
        getShiftAssignmentsForRole(role, dayId),
        getShiftReconciliations(role, dayId),
        getTankReturns(role, dayId),
        getInternalConsumptions(role, assignmentId),
        getOrganizationById(day.organizationId),
      ]);
      setData({ day, shift: assignments.find((item) => item.id === assignmentId), rec: reconciliations.find((item) => item.shiftAssignmentId === assignmentId), returns: returns.filter((item) => item.shiftAssignmentId === assignmentId), internals, organization });
    })();
  }, [assignmentId, dayId, role]);

  useEffect(() => {
    if (!data || searchParams.get("print") !== "1") return undefined;
    const timer = window.setTimeout(() => window.print(), 250);
    return () => window.clearTimeout(timer);
  }, [data, searchParams]);

  if (!data || !data.shift || !data.rec) return <main className="print-shell">{t("print.loading")}</main>;
  const { day, shift, rec, returns, internals, organization } = data;
  const money = (value) => formatCurrency(Number(value || 0), organization?.defaultCurrency, { language: i18n.resolvedLanguage });
  const dash = t("print.dash");
  const labels = t("print.sheet", { returnObjects: true });
  const activityRows = [[labels.meterVolume, `${n(rec.meteredVolume)} L`], [labels.tankReturn, `${n(rec.tankReturnVolume)} L`], [labels.internal, `${n(rec.internalConsumptionVolume)} L`], [labels.sold, `${n(rec.totalSoldVolume)} L`], ["CASH", `${n(rec.cashVolume)} L`], ["CREDIT", `${n(rec.creditVolume)} L`]];
  const reconciliationRows = [[labels.meterVolume, `${n(rec.meteredVolume)} L`], [labels.returned, `${n(rec.tankReturnVolume)} L`], [labels.internal, `${n(rec.internalConsumptionVolume)} L`], ["CASH", `${n(rec.cashVolume)} L`], ["CREDIT", `${n(rec.creditVolume)} L`], [labels.accounted, `${n(rec.accountedVolume)} L`], [labels.variance, `${n(rec.volumeVariance)} L`]];

  return <main className="print-shell">
    <div className="print-toolbar"><Link to={`/${role === "manager" ? "gerant" : "superviseur"}/operations/${dayId}`}>{t("actions.back")}</Link><button onClick={() => window.print()}>{t("actions.print")}</button></div>
    <PrintableReportHeader organization={organization} day={day} title={labels.title} reference={shift.operationalCode || shift.id} status={t("print.closed")} language={i18n.resolvedLanguage} />
    <section className="print-section"><h2>{labels.general}</h2><div className="print-meta"><span>{t("print.date")} : {day.businessDate}</span><span>{labels.attendant} : {rec.pumpAttendantName}</span><span>{labels.code} : {shift.operationalCode || dash}</span><span>{labels.pump} : {rec.pumpName}</span><span>{labels.meter} : {rec.fuelMeterName}</span><span>{labels.product} : {rec.productName}</span><span>{labels.sourceTank} : {rec.sourceTankName}</span><span>{labels.openedAt} : {shift.openedAt ? formatDateTime(shift.openedAt, { language: i18n.resolvedLanguage }) : dash}</span><span>{labels.closedAt} : {rec.calculatedAt ? formatDateTime(rec.calculatedAt, { language: i18n.resolvedLanguage }) : dash}</span></div></section>
    <section className="print-section"><h2>{labels.meterIndexes}</h2><table className="print-table"><tbody><tr><th>{labels.before}</th><td>{n(rec.openingIndex)}</td><th>{labels.after}</th><td>{n(rec.closingIndex)}</td><th>{labels.output}</th><td>{n(rec.meteredVolume)} L</td></tr></tbody></table></section>
    <section className="print-section"><h2>{labels.activity}</h2><table className="print-table"><tbody>{activityRows.map(([label, value]) => <tr key={label}><th>{label}</th><td>{value}</td></tr>)}</tbody></table></section>
    <section className="print-section"><h2>{labels.cashCredit}</h2><table className="print-table"><tbody><tr><th>{labels.cashQuantity}</th><td>{n(rec.cashVolume)} L</td><th>{labels.cashUnitPrice}</th><td>{money(rec.cashUnitPrice)}</td><th>{labels.cashAmount}</th><td>{money(rec.cashAmount)}</td></tr><tr><th>{labels.creditQuantity}</th><td>{n(rec.creditVolume)} L</td><th>{labels.creditUnitPrice}</th><td>{money(rec.creditUnitPrice)}</td><th>{labels.creditAmount}</th><td>{money(rec.creditAmount)}</td></tr></tbody></table><p className="print-total">{labels.turnover} : {money(rec.turnover)}</p></section>
    <section className="print-section"><h2>{labels.internalTitle}</h2><table className="print-table"><thead><tr>{[labels.usage, labels.product, labels.quantity, labels.internalPrice, labels.valuation, labels.observation].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{internals.map((item) => <tr key={item.id}><td>{item.usageBeneficiary}</td><td>{item.productName}</td><td>{n(item.quantity)} L</td><td>{money(item.unitPrice)}</td><td>{money(item.totalAmount)}</td><td>{item.observation || dash}</td></tr>)}</tbody></table><p className="print-total">{labels.totalInternal} : {n(rec.internalConsumptionVolume)} L · {money(rec.internalConsumptionAmount)}</p></section>
    <section className="print-section"><h2>{labels.returnsTitle}</h2><table className="print-table"><thead><tr>{[labels.source, labels.destination, labels.product, labels.quantity, labels.reason, labels.date].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{returns.map((item) => <tr key={item.id}><td>{item.sourceTankName}</td><td>{item.destinationTankName}</td><td>{item.productName}</td><td>{n(item.quantity)} L</td><td>{item.reason || dash}</td><td>{formatDateTime(item.occurredAt, { language: i18n.resolvedLanguage })}</td></tr>)}</tbody></table><p className="print-total">{labels.totalReturned} : {n(rec.tankReturnVolume)} L</p></section>
    <section className="print-section"><h2>{labels.reconciliation}</h2><table className="print-table"><tbody>{reconciliationRows.map(([label, value]) => <tr key={label}><th>{label}</th><td>{value}</td></tr>)}</tbody></table></section>
    <section className="print-section"><h2>{labels.observationTitle}</h2><div className="print-note" /><div className="print-note" /></section>
    <footer className="print-signatures"><div>{labels.attendant} :</div><div>{labels.manager} :</div></footer>
  </main>;
}
