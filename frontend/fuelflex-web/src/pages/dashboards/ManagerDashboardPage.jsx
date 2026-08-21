import { BriefcaseBusiness, Fuel } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";

import ManagerLayout from "../../components/layout/ManagerLayout";
import { getManagerStockBalances } from "../../services/reception/receptionService";
import "./ManagerDashboardPage.css";

const safeNumber = (value) => { const parsed = Number(value); return Number.isFinite(parsed) ? Math.max(parsed, 0) : 0; };
const formatLiters = (value) => new Intl.NumberFormat(undefined, { maximumFractionDigits: 3 }).format(safeNumber(value));

const fillPercentage = (stock, capacity) => capacity > 0 ? Math.min((stock / capacity) * 100, 100) : 0;
const stockLevel = (value) => value < 20 ? "stock-level-low" : value < 80 ? "stock-level-normal" : "stock-level-high";
const groupBalances = (balances) => {
  const groups = new Map();
  balances.forEach((balance) => {
    const key = String(balance.stationId) + ":" + String(balance.productId);
    const currentStock = safeNumber(balance.currentStock);
    const capacity = safeNumber(balance.capacity);
    const tank = { ...balance, currentStock, capacity, fillPercentage: fillPercentage(currentStock, capacity) };
    if (!groups.has(key)) groups.set(key, { key, stationName: balance.stationName, productName: balance.productName, totalStock: 0, totalCapacity: 0, tanks: [] });
    const group = groups.get(key);
    group.totalStock += currentStock;
    group.totalCapacity += capacity;
    group.tanks.push(tank);
  });
  return [...groups.values()].map((group) => ({ ...group, totalAvailable: Math.max(group.totalCapacity - group.totalStock, 0), fillPercentage: fillPercentage(group.totalStock, group.totalCapacity) }));
};

function ManagerDashboardPage() {
  const { t } = useTranslation("managerDashboard");
  const [balances, setBalances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const stockGroups = useMemo(() => groupBalances(balances), [balances]);
  useEffect(() => { let active = true; getManagerStockBalances().then((data) => { if (active) setBalances(Array.isArray(data) ? data : []); }).catch((requestError) => { if (active) setError(requestError.message || t("stockError")); }).finally(() => { if (active) setLoading(false); }); return () => { active = false; }; }, [t]);
  return <ManagerLayout><section className="manager-dashboard"><div className="manager-dashboard-kicker"><BriefcaseBusiness size={17} />{t("eyebrow")}</div><h1>{t("title")}</h1><p className="manager-dashboard-welcome">{t("welcome")}</p><section className="manager-dashboard-stock"><header className="manager-dashboard-stock-header"><div><h2>{t("stockOverview")}</h2><p>{t("stockDescription")}</p></div><Fuel size={24}/></header>{loading?<p className="manager-dashboard-empty">{t("stockLoading")}</p>:error?<p className="manager-dashboard-error">{error}</p>:stockGroups.length?<div className="manager-stock-product-grid">{stockGroups.map(group=><article className={"manager-product-stock "+stockLevel(group.fillPercentage)} key={group.key}><header className="manager-product-header"><div><small>{group.stationName}</small><h3>{group.productName}</h3></div><Fuel size={20}/></header><div className="manager-product-total"><span>{t("totalStock")}</span><strong>{formatLiters(group.totalStock)} L</strong><small>{t("availableStock")}</small></div><div className="manager-stock-progress global" role="progressbar" aria-label={t("fillLevel")} aria-valuemin="0" aria-valuemax="100" aria-valuenow={group.fillPercentage}><span style={{width:group.fillPercentage+"%"}}/></div><div className="manager-stock-progress-label"><span>{t("fillLevel")}</span><strong>{group.fillPercentage.toLocaleString(undefined,{maximumFractionDigits:2})} %</strong></div><dl className="manager-product-metrics"><div><dt>{t("totalCapacity")}</dt><dd>{formatLiters(group.totalCapacity)} L</dd></div><div><dt>{t("availableCapacity")}</dt><dd>{formatLiters(group.totalAvailable)} L</dd></div></dl><section className="manager-tank-details"><h4>{t("tankDetails")}</h4><div className="manager-tank-grid">{group.tanks.map(tank=><article className={"manager-tank-card "+stockLevel(tank.fillPercentage)} key={tank.tankId}><header><div><small>{t("tank")}</small><strong>{tank.tankName}</strong></div><span>{tank.fillPercentage.toLocaleString(undefined,{maximumFractionDigits:2})} %</span></header><p><strong>{formatLiters(tank.currentStock)} L</strong> / {formatLiters(tank.capacity)} L</p><div className="manager-stock-progress" role="progressbar" aria-label={t("tank")+" "+tank.tankName} aria-valuemin="0" aria-valuemax="100" aria-valuenow={tank.fillPercentage}><span style={{width:tank.fillPercentage+"%"}}/></div></article>)}</div></section></article>)}</div>:<p className="manager-dashboard-empty">{t("stockEmpty")}</p>}</section></section></ManagerLayout>;
}

export default ManagerDashboardPage;
