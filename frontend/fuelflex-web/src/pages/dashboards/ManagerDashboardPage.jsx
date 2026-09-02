import { ArrowDownRight, ArrowRight, ArrowUpRight, Banknote, BriefcaseBusiness, CreditCard, Fuel, Landmark, ReceiptText, WalletCards } from "lucide-react";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import ManagerLayout from "../../components/layout/ManagerLayout";
import { formatCurrency, formatDate, formatNumber, formatVolume } from "../../i18n/formatters";
import { getManagerDashboard } from "../../services/dashboard/managerDashboardService";
import { getManagerStations } from "../../services/operations/operationalService";
import "./ManagerDashboardPage.css";

const number = (value) => { const parsed = Number(value); return Number.isFinite(parsed) ? parsed : 0; };
const PRODUCT_ACCENTS = ["#087443", "#6941c6", "#175cd3", "#b54708", "#0e7490", "#c2410c"];
const variation = (current, previous) => {
  const now = number(current), before = number(previous);
  if (before === 0) return now === 0 ? { value: 0, direction: "flat" } : { value: null, direction: "up" };
  const value = ((now - before) / Math.abs(before)) * 100;
  return { value, direction: value > 0 ? "up" : value < 0 ? "down" : "flat" };
};
function Trend({ current, previous, t, language, money }) {
  const trend = variation(current, previous);
  if (number(previous) === 0) return <span className="manager-previous-zero">{t("previousPeriodValue", { value: money(0) })}</span>;
  const Icon = trend.direction === "up" ? ArrowUpRight : trend.direction === "down" ? ArrowDownRight : ArrowRight;
  return <span className="manager-trend-row"><span className={"manager-trend " + trend.direction}><Icon size={15}/>{formatNumber(Math.abs(trend.value), { language, maximumFractionDigits: 1 }) + " %"}</span><span>{t("versusPreviousAmount", { value: money(previous) })}</span></span>;
}
function SalesChart({ data, currency, language, t }) {
  const width = 940, height = 330, left = 68, right = 20, top = 24, bottom = 44;
  const max = Math.max(...data.flatMap((row) => [number(row.cash), number(row.credit)]), 1);
  const x = (index) => left + (index * (width - left - right)) / Math.max(data.length - 1, 1);
  const y = (value) => top + (1 - number(value) / max) * (height - top - bottom);
  const points = (key) => data.map((row, index) => x(index) + "," + y(row[key])).join(" ");
  const money = (value) => formatCurrency(number(value), currency, { language, minimumFractionDigits: 2, maximumFractionDigits: 2, useGrouping: "always" });
  return <div className="manager-chart-wrap"><svg className="manager-sales-chart" viewBox={"0 0 " + width + " " + height} role="img" aria-label={t("salesChartAria")}>
    {[0, .25, .5, .75, 1].map((tick) => { const value = max * (1 - tick), py = top + tick * (height - top - bottom); return <g key={tick}><line x1={left} x2={width-right} y1={py} y2={py} className="chart-grid"/><text x={left-12} y={py+4} textAnchor="end" className="chart-axis">{formatNumber(value, { language, notation: "compact", maximumFractionDigits: 1 })}</text></g>; })}
    <polyline points={points("cash")} className="chart-line cash"/><polyline points={points("credit")} className="chart-line credit"/>
    {data.map((row, index) => <g key={row.date}><circle cx={x(index)} cy={y(row.cash)} r="3.5" className="chart-dot cash"><title>{formatDate(row.date + "T00:00:00", { language, dateStyle: "medium" }) + " — CASH: " + money(row.cash)}</title></circle><circle cx={x(index)} cy={y(row.credit)} r="3.5" className="chart-dot credit"><title>{formatDate(row.date + "T00:00:00", { language, dateStyle: "medium" }) + " — CREDIT: " + money(row.credit)}</title></circle>{(index % 5 === 0 || index === data.length - 1) && <text x={x(index)} y={height-14} textAnchor="middle" className="chart-axis">{formatDate(row.date + "T00:00:00", { language, day: "2-digit", month: "short" })}</text>}</g>)}
  </svg></div>;
}
function Progress({ value, compact = false, label }) {
  const percentage = Math.min(Math.max(number(value), 0), 100);
  return <div className={"manager-progress " + (compact ? "compact" : "product")} role="progressbar" aria-label={label} aria-valuemin="0" aria-valuemax="100" aria-valuenow={percentage}><span style={{ width: percentage + "%" }}/></div>;
}
function ManagerDashboardPage() {
  const { t, i18n } = useTranslation("managerDashboard"), language = i18n.resolvedLanguage;
  const [stations, setStations] = useState([]), [stationId, setStationId] = useState("");
  const [dashboard, setDashboard] = useState(null);
  const [initialLoading, setInitialLoading] = useState(true), [error, setError] = useState("");
  useEffect(() => { let active = true; getManagerStations().then((stationRows) => { if (!active) return; const available = Array.isArray(stationRows) ? stationRows : []; setStations(available); setStationId((value) => value || available[0]?.id || ""); }).catch((requestError) => active && setError(requestError.message || t("loadError"))).finally(() => active && setInitialLoading(false)); return () => { active = false; }; }, [t]);
  useEffect(() => { if (!stationId) return; let active = true; getManagerDashboard(stationId).then((data) => { if (active) { setDashboard(data); setError(""); } }).catch((requestError) => active && setError(requestError.message || t("loadError"))); return () => { active = false; }; }, [stationId, t]);
  const money = (value) => formatCurrency(number(value), dashboard.currency, { language, minimumFractionDigits: 2, maximumFractionDigits: 2, useGrouping: "always" });
  const volume = (value) => formatVolume(number(value), { language, minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const percent = (value) => formatNumber(number(value), { language, minimumFractionDigits: 1, maximumFractionDigits: 1 }) + " %";
  const kpis = dashboard ? [
    { key: "revenue", label: t("revenue"), icon: Landmark, value: dashboard.current.revenue, previous: dashboard.previous.revenue },
    { key: "cash", label: t("cashSales"), icon: Banknote, value: dashboard.current.cash, previous: dashboard.previous.cash },
    { key: "credit", label: t("creditSales"), icon: CreditCard, value: dashboard.current.credit, previous: dashboard.previous.credit },
    { key: "expenses", label: t("expenses"), icon: ReceiptText, value: dashboard.current.disbursedExpenses, previous: dashboard.previous.disbursedExpenses },
  ] : [];
  return <ManagerLayout><main className="manager-dashboard"><header className="manager-dashboard-heading"><div><span className="manager-dashboard-kicker"><BriefcaseBusiness size={17}/>{t("eyebrow")}</span><h1>{t("title")}</h1><p>{t("periodDescription")}</p></div>{stations.length > 0 && <label className="manager-station-select"><span>{t("station")}</span><select value={stationId} onChange={(event) => { setError(""); setStationId(event.target.value); }}>{stations.map((station) => <option key={station.id} value={station.id}>{station.name}</option>)}</select></label>}</header>
  {initialLoading ? <div className="manager-dashboard-state">{t("loading")}</div> : !stations.length ? <div className="manager-dashboard-state">{t("noStation")}</div> : error ? <div className="manager-dashboard-state error">{error}</div> : !dashboard || String(dashboard.stationId) !== String(stationId) ? <div className="manager-dashboard-state">{t("loading")}</div> : <>
    <section className="manager-kpi-grid" aria-label={t("financialKpis")}>{kpis.map(({ key, label, icon: Icon, value, previous }) => <article className={"manager-kpi " + key} key={key}><header><span>{label}</span><i><Icon size={19}/></i></header><strong>{money(value)}</strong><footer><Trend current={value} previous={previous} t={t} language={language} money={money}/></footer></article>)}</section>
    <section className="manager-analysis-grid"><article className="manager-panel manager-sales-panel"><header><div><h2>{t("salesChart")}</h2><p>{t("salesChartDescription")}</p></div><div className="manager-chart-legend"><span className="cash">{t("cash")}</span><span className="credit">{t("credit")}</span></div></header><SalesChart data={dashboard.dailySales || []} currency={dashboard.currency} language={language} t={t}/></article>
      <aside className="manager-summary-column"><article className="manager-summary-card internal"><header><Fuel size={19}/><span>{t("internalPeriod")}</span></header><dl><div><dt>{t("volume")}</dt><dd>{volume(dashboard.current.internalVolume)}</dd></div><div><dt>{t("valuation")}</dt><dd>{money(dashboard.current.internalAmount)}</dd></div></dl><p>{t("nonCashCharge")}</p></article><article className="manager-summary-card cash-result"><header><WalletCards size={19}/><span>{t("cashResultPeriod")}</span></header><small>{t("cashMinusExpenses")}</small><strong>{money(dashboard.current.cashAfterExpenses)}</strong><p>{t("cashAfterExpensesHelp")}</p></article></aside>
    </section>
    <section className="manager-panel manager-stock-panel"><header><div><h2>{t("stockOverview")}</h2><p>{t("stockDescriptionDetailed")}</p></div><Fuel size={23}/></header>{dashboard.products?.length ? <div className="manager-product-grid">{dashboard.products.map((product, index) => <article className="manager-product-card" key={product.productId} style={{ "--product-accent": PRODUCT_ACCENTS[index % PRODUCT_ACCENTS.length] }}><header><div><span>{t("product")}</span><h3>{product.productName}</h3></div><b>{percent(product.fillPercentage)}</b></header><div className="manager-product-overview"><div className="manager-product-stock"><span>{t("globalStock")}</span><strong>{volume(product.stockQuantity)}</strong></div><div className="manager-product-gauge" style={{ "--fill": number(product.fillPercentage) * 3.6 + "deg" }} role="progressbar" aria-label={t("productFillAria", { product: product.productName })} aria-valuemin="0" aria-valuemax="100" aria-valuenow={number(product.fillPercentage)}><span>{percent(product.fillPercentage)}</span></div></div><dl className="manager-product-facts"><div><dt>{t("totalCapacity")}</dt><dd>{volume(product.totalCapacity)}</dd></div><div><dt>{t("fillLevel")}</dt><dd>{percent(product.fillPercentage)}</dd></div><div><dt>{t("tanks")}</dt><dd>{t("tankCount", { count: product.tanks.length })}</dd></div></dl><section className="manager-tanks"><h4>{t("tankDetails")}</h4>{product.tanks.map((tank) => <article className="manager-tank-row" key={tank.tankId}><header><strong>{tank.tankName}</strong><span>{percent(tank.fillPercentage)}</span></header><p>{volume(tank.stockQuantity)} <span>/ {volume(tank.capacity)}</span></p><Progress compact value={tank.fillPercentage} label={t("tankFillAria", { tank: tank.tankName })}/></article>)}</section></article>)}</div> : <div className="manager-dashboard-state compact">{t("stockEmpty")}</div>}</section>
  </>}</main></ManagerLayout>;
}
export default ManagerDashboardPage;
