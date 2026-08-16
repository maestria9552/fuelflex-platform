/* eslint-disable react-hooks/set-state-in-effect */
import { Eye, Plus, RefreshCw } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import ManagerLayout from "../../components/layout/ManagerLayout";
import { getManagerOrders } from "../../services/purchaseOrder/purchaseOrderService";
import "./ManagerOrders.css";

const statusKeys = {
  DRAFT: "draft", PENDING_SUPERVISOR_APPROVAL: "pendingSupervisor", SUPERVISOR_REJECTED: "supervisorRejected",
  PENDING_SUPPLIER_APPROVAL: "pendingSupplier", SUPPLIER_REJECTED: "supplierRejected", AWAITING_RECEPTION: "awaitingReception",
};

function statusLabel(status, t) { return t(`orders:statuses.${statusKeys[status] || "draft"}`); }
function productLabel(order, t) { const count = order.items?.length || 0; return count === 1 ? order.items[0]?.productName : t("orders:list.productCount", { count }); }
function formatDate(value, locale) { return value ? new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "—"; }

function ManagerOrdersPage() {
  const { t, i18n } = useTranslation(["orders", "common"]);
  const [page, setPage] = useState({ content: [], totalPages: 0, number: 0 });
  const [loading, setLoading] = useState(true); const [error, setError] = useState(null);
  const load = useCallback(async () => { setLoading(true); setError(null); try { setPage(await getManagerOrders({ page: page.number || 0, size: 20 })); } catch (e) { setError(e); } finally { setLoading(false); } }, [page.number]);
  useEffect(() => { load(); }, [load]);
  const locale = i18n.language === "en" ? "en-US" : "fr-FR";
  return <ManagerLayout><section className="orders-page"><div className="orders-page-header"><div><p className="orders-eyebrow">{t("orders:eyebrow")}</p><h1>{t("orders:list.title")}</h1><p>{t("orders:list.subtitle")}</p></div><Link className="orders-primary-button" to="/gerant/commandes/nouvelle"><Plus size={18} />{t("orders:list.new")}</Link></div>
    {loading ? <div className="orders-state">{t("orders:loading")}</div> : error ? <div className="orders-state orders-state-error"><p>{error.status === 403 ? t("orders:errors.forbidden") : error.status === 404 ? t("orders:errors.notFound") : t("orders:errors.load")}</p><button type="button" className="orders-secondary-button" onClick={load}><RefreshCw size={16} />{t("common:actions.retry")}</button></div> : !page.content?.length ? <div className="orders-empty"><h2>{t("orders:list.emptyTitle")}</h2><p>{t("orders:list.emptyDescription")}</p><Link className="orders-primary-button" to="/gerant/commandes/nouvelle"><Plus size={18} />{t("orders:list.new")}</Link></div> : <><div className="orders-table-wrap"><table className="orders-table"><thead><tr>{["number","station","supplier","products","date","status","action"].map((key) => <th key={key}>{t(`orders:list.columns.${key}`)}</th>)}</tr></thead><tbody>{page.content.map((order) => <tr key={order.id}><td data-label={t("orders:list.columns.number")}><strong>{order.orderNumber}</strong></td><td data-label={t("orders:list.columns.station")}>{order.station?.name || "—"}</td><td data-label={t("orders:list.columns.supplier")}>{order.supplier?.displayName || t("orders:common.noSupplier")}</td><td data-label={t("orders:list.columns.products")}>{productLabel(order, t)}</td><td data-label={t("orders:list.columns.date")}>{formatDate(order.createdAt, locale)}</td><td data-label={t("orders:list.columns.status")}><span className={`orders-status orders-status-${statusKeys[order.status] || "draft"}`}>{statusLabel(order.status, t)}</span></td><td data-label={t("orders:list.columns.action")}><Link className="orders-view-link" to={`/gerant/commandes/${order.id}`}><Eye size={16} />{t("orders:list.view")}</Link></td></tr>)}</tbody></table></div>{page.totalPages > 1 && <div className="orders-pagination"><button type="button" disabled={page.number === 0} onClick={() => setPage((current) => ({ ...current, number: current.number - 1 }))}>{t("orders:list.previous")}</button><span>{t("orders:list.page", { current: (page.number || 0) + 1, total: page.totalPages })}</span><button type="button" disabled={page.number + 1 >= page.totalPages} onClick={() => setPage((current) => ({ ...current, number: current.number + 1 }))}>{t("orders:list.next")}</button></div>}</>}
  </section></ManagerLayout>;
}
export default ManagerOrdersPage;
