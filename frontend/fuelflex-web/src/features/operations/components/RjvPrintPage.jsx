import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getOperationalDayForRole, getOperationalDayRjv } from "../../../services/operations/operationalService";
import { getOrganizationById } from "../../../services/organization/organizationService";
import { formatCurrency, formatDateTime } from "../../../i18n/formatters";
import PrintableReportHeader from "./PrintableReportHeader";
import "./OperationsPrint.css";

const n = (value) => Number(value || 0).toLocaleString(undefined, { maximumFractionDigits: 3 });

export default function RjvPrintPage({ role }) {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const { t, i18n } = useTranslation("reports");
  const [data, setData] = useState(null);

  useEffect(() => {
    Promise.all([getOperationalDayForRole(role, id), getOperationalDayRjv(role, id)])
      .then(async ([day, rjv]) => setData({ day, rjv, organization: await getOrganizationById(day.organizationId) }));
  }, [id, role]);

  useEffect(() => {
    if (!data || searchParams.get("print") !== "1") return undefined;
    const timer = window.setTimeout(() => window.print(), 250);
    return () => window.clearTimeout(timer);
  }, [data, searchParams]);

  if (!data) return <main className="print-shell">{t("print.loading")}</main>;
  const { day, rjv, organization } = data;
  const money = (value) => formatCurrency(Number(value || 0), organization?.defaultCurrency, { language: i18n.resolvedLanguage });
  const dash = t("print.dash");
  const labels = t("print.rjv", { returnObjects: true });
  const productGroups = Object.values((rjv.reconciliations || []).reduce((groups, item) => {
    const key = item.productId || item.productName;
    groups[key] ||= { id: key, name: item.productName, rows: [] };
    groups[key].rows.push(item);
    return groups;
  }, {}));

  return <main className="print-shell print-rjv">
    <div className="print-toolbar"><Link to={`/${role === "manager" ? "gerant" : "superviseur"}/operations/${id}`}>{t("actions.back")}</Link><button onClick={() => window.print()}>{t("actions.print")}</button></div>
    <PrintableReportHeader organization={organization} day={day} title={labels.title} reference={day.id} status={t(day.status === "OPEN" ? "print.provisional" : "print.definitive")} language={i18n.resolvedLanguage} extraMeta={[[t("print.currency"), organization?.defaultCurrency]]} />
    <section className="print-section print-rjv-primary">
      <h2>{labels.pumpSales}</h2>
      <div className="print-product-grid">{productGroups.map((group) => <article className="print-product-block" key={group.id}>
        <h3 className="print-product-title">{group.name}</h3>
        <div className="print-table-block"><h3>A · {labels.indexActivity}</h3><table className="print-table print-table-rjv"><thead><tr>{[labels.attendant, labels.pumpMeter, labels.openingIndex, labels.closingIndex, labels.meter].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{group.rows.map((item) => <tr key={item.id}><td>{item.pumpAttendantName}</td><td>{item.pumpName}<small>{item.fuelMeterName}</small></td><td className="print-number">{n(item.openingIndex)}</td><td className="print-number">{n(item.closingIndex)}</td><td className="print-number">{n(item.meteredVolume)}</td></tr>)}</tbody></table></div>
        <div className="print-table-block"><h3>B · {labels.volumeDistribution}</h3><table className="print-table print-table-rjv"><thead><tr>{[labels.attendantPump, labels.return, labels.internal, labels.cash, labels.credit, labels.sold].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{group.rows.map((item) => <tr key={item.id}><td>{item.pumpAttendantName}<small>{item.pumpName} · {item.fuelMeterName}</small></td><td className="print-number">{n(item.tankReturnVolume)}</td><td className="print-number">{n(item.internalConsumptionVolume)}</td><td className="print-number">{n(item.cashVolume)}</td><td className="print-number">{n(item.creditVolume)}</td><td className="print-number"><b>{n(item.totalSoldVolume)}</b></td></tr>)}</tbody></table></div>
        <div className="print-table-block"><h3>C · {labels.productValuation}</h3><table className="print-table print-table-rjv"><thead><tr>{[labels.attendantPump, labels.cashPrice, labels.cashAmount, labels.creditPrice, labels.creditAmount, labels.internalPrice, labels.internalValuation].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{group.rows.map((item) => <tr key={item.id}><td>{item.pumpAttendantName}<small>{item.pumpName}</small></td><td className="print-number">{money(item.cashUnitPrice)}</td><td className="print-number">{money(item.cashAmount)}</td><td className="print-number">{money(item.creditUnitPrice)}</td><td className="print-number">{money(item.creditAmount)}</td><td className="print-number">{money(item.internalUnitPrice)}</td><td className="print-number">{money(item.internalConsumptionAmount)}</td></tr>)}</tbody></table></div>
      </article>)}</div>
    </section>
    <section className="print-section"><h2>{labels.productSummary}</h2><table className="print-table"><thead><tr>{[labels.product, labels.litersSold, labels.cash, labels.credit, labels.cashRevenue, labels.creditRevenue].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{(rjv.byProduct || []).map((item) => <tr key={item.id}><td>{item.name}</td><td>{n(item.totalVolume)}</td><td>{n(item.cashVolume)}</td><td>{n(item.creditVolume)}</td><td>{money(item.cashAmount)}</td><td>{money(item.creditAmount)}</td></tr>)}</tbody></table><p className="print-total">{labels.commercialTurnover} : {money(rjv.totalSalesAmount)} · {labels.expectedCash} : {money(rjv.expectedCash)}</p></section>
    <section className="print-section"><h2>{labels.creditDetail}</h2><p className="print-muted">{labels.creditAggregate}</p><p className="print-total">{labels.totalCredit} : {n(rjv.creditVolume)} L · {money(rjv.creditAmount)}</p></section>
    <section className="print-section"><h2>{labels.internalTitle}</h2><table className="print-table"><thead><tr>{[labels.usage, labels.attendant, labels.product, labels.quantity, labels.internalPrice, labels.valuation, labels.observation].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{(rjv.internalConsumptions || []).map((item) => <tr key={item.id}><td>{item.usageBeneficiary}</td><td>{item.pumpAttendantName}</td><td>{item.productName}</td><td>{n(item.quantity)}</td><td>{money(item.unitPrice)}</td><td>{money(item.totalAmount)}</td><td>{item.observation || dash}</td></tr>)}</tbody></table><p className="print-total">{labels.totalInternal} : {n(rjv.internalConsumptionVolume)} L · {money(rjv.internalConsumptionAmount)}</p><p>{labels.internalNote}</p></section>
    <section className="print-section"><h2>{labels.returnsTitle}</h2><table className="print-table"><thead><tr>{[labels.attendant, labels.product, labels.sourceDestination, labels.quantity, labels.reason].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{(rjv.reconciliations || []).filter((item) => Number(item.tankReturnVolume) > 0).map((item) => <tr key={item.id}><td>{item.pumpAttendantName}</td><td>{item.productName}</td><td>{item.sourceTankName}</td><td>{n(item.tankReturnVolume)}</td><td>{dash}</td></tr>)}</tbody></table></section>
    <section className="print-section"><h2>{labels.stockTitle}</h2><table className="print-table"><thead><tr>{[labels.product, labels.tank, labels.theoreticalStock, labels.physicalStock, labels.variance].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{(rjv.stocks || []).map((item) => <tr key={item.tankId}><td>{item.productName}</td><td>{item.tankName}</td><td>{n(item.theoreticalStock)} L</td><td>{n(item.physicalStock)} L</td><td>{n(item.stockVariance)} L</td></tr>)}</tbody></table></section>
    <section className="print-section"><h2>{labels.expensesTitle}</h2><table className="print-table"><thead><tr>{[labels.label, labels.amount, labels.expenseDate, labels.referenceObservation].map((label) => <th key={label}>{label}</th>)}</tr></thead><tbody>{(rjv.expenses || []).map((item) => <tr key={item.id}><td>{item.label}</td><td>{money(item.amount)}</td><td>{formatDateTime(item.createdAt, { language: i18n.resolvedLanguage })}</td><td>{item.reference || item.comment || dash}</td></tr>)}</tbody></table><p className="print-total">{labels.totalExpenses} : {money(rjv.expensesAmount ?? rjv.expenseAmount)}</p></section>
    <section className="print-section print-financial"><h2>{labels.financialTitle}</h2>
      <div className="print-financial-block"><h3>A. {labels.sales}</h3><table className="print-table"><thead><tr><th>{labels.product}</th><th>{labels.cashAmount}</th><th>{labels.creditAmount}</th><th>{labels.commercialTurnover}</th></tr></thead><tbody>{(rjv.byProduct || []).map((item) => <tr key={item.id}><td>{item.name}</td><td className="print-number">{money(item.cashAmount)}</td><td className="print-number">{money(item.creditAmount)}</td><td className="print-number">{money(item.totalAmount)}</td></tr>)}</tbody><tfoot><tr><th>{labels.stationTotal}</th><th className="print-number">{money(rjv.cashGrossExpected ?? rjv.cashAmount)}</th><th className="print-number">{money(rjv.creditAmount)}</th><th className="print-number">{money(rjv.totalSalesAmount)}</th></tr></tfoot></table></div>
      <div className="print-financial-block"><h3>B. {labels.charges}</h3><p>{labels.disbursedExpenses} : <b>{money(rjv.disbursedExpenseAmount ?? rjv.expensesAmount)}</b></p><p>{labels.internalNonDisbursed} : <b>{money(rjv.internalConsumptionAmount)}</b></p></div>
      <div className="print-financial-block"><h3>C. {labels.cashPosition}</h3>{rjv.cashReconciliationAvailable ? <><dl className="print-financial-grid"><div><dt>{labels.grossCash}</dt><dd>{money(rjv.cashGrossExpected ?? rjv.cashAmount)}</dd></div><div><dt>{labels.disbursedExpenses}</dt><dd>{money(rjv.disbursedExpenseAmount ?? rjv.expensesAmount)}</dd></div><div><dt>{labels.netCash}</dt><dd>{money(rjv.cashNetExpected ?? rjv.expectedNetCash)}</dd></div><div><dt>{labels.referenceCash}</dt><dd>{money(rjv.physicalReferenceAmount)}</dd></div><div><dt>{labels.usdCash}</dt><dd>{n(rjv.physicalUsdAmount)} USD</dd></div><div><dt>{labels.usdRate}</dt><dd>1 USD = {n(rjv.usdExchangeRate)} {rjv.referenceCurrency}</dd></div><div><dt>{labels.usdEquivalent}</dt><dd>{money(rjv.convertedUsdAmount)}</dd></div><div><dt>{labels.observedCash}</dt><dd>{money(rjv.observedCashAmount)}</dd></div><div><dt>{labels.cashVariance}</dt><dd>{money(rjv.cashVariance)}</dd></div></dl><p className={`print-cash-status print-cash-status-${rjv.cashStatus}`}>{labels.cashStatus} : {labels.statuses?.[rjv.cashStatus]}</p></> : <div className="print-historical-cash"><b>{labels.historicalCashUnavailable}</b><p>{labels.historicalCashExplanation}</p></div>}</div>
    </section>
    <section className="print-section"><h2>{labels.reconciliationTitle}</h2><p>{labels.meterVolume} : {n(rjv.meteredVolume)} L · {labels.accountedVolume} : {n(rjv.accountedVolume)} L · {labels.variance} : {n(rjv.volumeVariance)} L</p></section>
    <footer className="print-signatures"><div>{labels.manager} :</div><div>{labels.supervisor} :</div><div>{labels.observations} :</div><div>{labels.generated}</div></footer>
  </main>;
}
