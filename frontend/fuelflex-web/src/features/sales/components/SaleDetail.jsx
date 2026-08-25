import { ArrowLeft, RefreshCw } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import { getSalePermissions } from "../../../services/auth/permissionService";
import { getPosSale } from "../../../services/sale/saleService";
import SaleReversalModal from "./SaleReversalModal";

function decimal(value, locale) {
  if (value === null || value === undefined) return "—";
  return new Intl.NumberFormat(locale, { maximumFractionDigits: 3 }).format(Number(value));
}

export default function SaleDetail({ role }) {
  const { id } = useParams();
  const { t, i18n } = useTranslation(["sales", "common"]);
  const locale = i18n.language === "en" ? "en-US" : "fr-CD";
  const isManager = role === "manager";
  const routeBase = isManager ? "/gerant/ventes" : "/superviseur/ventes";
  const permissions = getSalePermissions();
  const [sale, setSale] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [reversalOpen, setReversalOpen] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setSale(await getPosSale(role, id));
    } catch (requestError) {
      setError(requestError.message || t("sales:errors.loadDetail"));
    } finally {
      setLoading(false);
    }
  }, [id, role, t]);

  // Synchronise le détail avec la ressource sécurisée.
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load();
  }, [load]);

  if (loading) return <section className="sales-page"><div className="sales-state">{t("common:feedback.loading")}</div></section>;
  if (error || !sale) return <section className="sales-page"><Link className="sales-back" to={routeBase}><ArrowLeft size={17}/>{t("sales:detail.back")}</Link><div className="sales-state sales-state-error"><p>{error || t("sales:errors.loadDetail")}</p><button className="sales-button sales-button-secondary" type="button" onClick={load}>{t("common:actions.retry")}</button></div></section>;

  const fields = [
    ["station", sale.station?.name],
    ["operationalDay", sale.operationalDayId],
    ["pumpAttendant", sale.pumpAttendant ? `${sale.pumpAttendant.firstName} ${sale.pumpAttendant.lastName} · ${sale.pumpAttendant.operationalCode}` : null],
    ["pump", sale.pump?.name],
    ["dispensingPoint", sale.dispensingPoint?.name],
    ["fuelMeter", sale.fuelMeter?.name],
    ["tank", sale.tank?.name],
    ["product", sale.product?.name],
    ["tariff", sale.tariffCategory?.name],
    ["quantity", `${decimal(sale.quantity, locale)} L`],
    ["unitPrice", decimal(sale.unitPrice, locale)],
    ["totalAmount", decimal(sale.totalAmount, locale)],
    ["vehicleType", t(`sales:vehicleTypes.${sale.vehicleType}`)],
    ["licensePlate", sale.licensePlate],
    ["creditCustomer", sale.creditCustomer?.name],
  ];

  return (
    <section className="sales-page">
      <Link className="sales-back" to={routeBase}><ArrowLeft size={17}/>{t("sales:detail.back")}</Link>
      <header className="sales-page-header">
        <div><p className="sales-eyebrow">{t("sales:detail.eyebrow")}</p><h1>{sale.saleNumber}</h1><p>{sale.soldAt ? new Intl.DateTimeFormat(locale, { dateStyle: "long", timeStyle: "short" }).format(new Date(sale.soldAt)) : "—"}</p></div>
        <div className="sales-header-actions"><span className={`sale-status sale-status-${sale.status}`}>{t(`sales:statuses.${sale.status}`)}</span><button className="sales-button sales-button-secondary" type="button" onClick={load}><RefreshCw size={17}/>{t("common:actions.refresh")}</button>{isManager && permissions.canReverse && sale.status === "EFFECTIVE" && <button className="sales-button sales-button-danger" type="button" onClick={() => setReversalOpen(true)}>{t("sales:reversal.action")}</button>}</div>
      </header>
      {success && <div className="sales-feedback success">{success}</div>}
      <div className="sales-detail-grid">
        <section className="sales-card sales-detail-main">
          <div className="sales-section-heading"><h2>{t("sales:detail.information")}</h2><span className={`sale-type sale-type-${sale.saleType}`}>{t(`sales:types.${sale.saleType}`)}</span></div>
          <dl>{fields.filter(([, value]) => value !== null && value !== undefined && value !== "").map(([key, value]) => <div key={key}><dt>{t(`sales:fields.${key}`)}</dt><dd>{value}</dd></div>)}</dl>
        </section>
        <aside className="sales-card sales-amount-card"><span>{t("sales:fields.totalAmount")}</span><strong>{decimal(sale.totalAmount, locale)}</strong><small>{decimal(sale.quantity, locale)} L × {decimal(sale.unitPrice, locale)}</small></aside>
      </div>
      {sale.status === "REVERSED" && <section className="sales-card sales-reversal-record"><h2>{t("sales:detail.reversal")}</h2><dl><div><dt>{t("sales:fields.reversedAt")}</dt><dd>{sale.reversedAt ? new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(sale.reversedAt)) : "—"}</dd></div><div><dt>{t("sales:fields.reversalReason")}</dt><dd>{sale.reversalReason || "—"}</dd></div></dl></section>}
      <SaleReversalModal sale={sale} isOpen={reversalOpen} onClose={() => setReversalOpen(false)} onSuccess={(updated) => { setSale(updated); setReversalOpen(false); setSuccess(t("sales:feedback.reversed")); }} />
    </section>
  );
}
