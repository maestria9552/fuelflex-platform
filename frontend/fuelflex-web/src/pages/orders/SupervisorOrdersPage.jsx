import { Eye, RefreshCw } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import SupervisorLayout from "../../components/layout/SupervisorLayout";
import { getSupervisorOrders } from "../../services/purchaseOrder/supervisorPurchaseOrderService";
import "./SupervisorOrders.css";

const statusKey = { DRAFT: "draft", PENDING_SUPERVISOR_APPROVAL: "pendingSupervisor", SUPERVISOR_REJECTED: "supervisorRejected", PENDING_SUPPLIER_APPROVAL: "pendingSupplier", SUPPLIER_REJECTED: "supplierRejected", AWAITING_RECEPTION: "awaitingReception" };
const filterStatus = { pending: "PENDING_SUPERVISOR_APPROVAL", approved: ["PENDING_SUPPLIER_APPROVAL", "AWAITING_RECEPTION"], rejected: ["SUPERVISOR_REJECTED", "SUPPLIER_REJECTED"] };
function date(value, locale) { return value ? new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "—"; }
function badgeClass(status) { if (status === "PENDING_SUPERVISOR_APPROVAL") return "pending"; if (status?.includes("REJECTED")) return "rejected"; if (["AWAITING_RECEPTION", "PENDING_SUPPLIER_APPROVAL"].includes(status)) return "approved"; return "neutral"; }

function SupervisorOrdersPage() {
  const { t, i18n } = useTranslation(["orders", "common"]);
  const [page, setPage] = useState({ content: [], totalPages: 0, number: 0 });
  const [filter, setFilter] = useState("pending");
  const [loading, setLoading] = useState(true); const [error, setError] = useState(null);
  const locale = i18n.language === "en" ? "en-US" : "fr-FR";
  const load = useCallback(async () => { setLoading(true); setError(null); try { setPage(await getSupervisorOrders({ page: 0, size: 50 })); } catch (e) { setError(e); } finally { setLoading(false); } }, []);
  // This effect synchronizes the page with the protected API; the request itself owns the state updates.
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => { load(); }, [load]);
  const orders = useMemo(() => { const all = page.content || []; if (filter === "all") return all; const wanted = filterStatus[filter]; return all.filter((order) => Array.isArray(wanted) ? wanted.includes(order.status) : order.status === wanted); }, [filter, page.content]);
  const filterLabels = { all: t("orders:supervisor.filters.all"), pending: t("orders:supervisor.filters.pending"), approved: t("orders:supervisor.filters.approved"), rejected: t("orders:supervisor.filters.rejected") };
  return <SupervisorLayout><main className="supervisor-orders-page"><header className="supervisor-orders-header"><div><p className="supervisor-orders-eyebrow">{t("orders:eyebrow")}</p><h1>{t("orders:supervisor.list.title")}</h1><p>{t("orders:supervisor.list.subtitle")}</p></div></header><div className="supervisor-orders-filters">{Object.keys(filterLabels).map((key) => <button type="button" key={key} className={`supervisor-orders-filter ${filter === key ? "supervisor-orders-filter-active" : ""}`} onClick={() => setFilter(key)}>{filterLabels[key]}</button>)}</div>{loading ? <div className="supervisor-orders-state">{t("orders:loading")}</div> : error ? <div className="supervisor-orders-state supervisor-orders-state-error"><p>{error.status === 403 ? t("orders:errors.forbidden") : t("orders:errors.load")}</p><button type="button" className="supervisor-orders-secondary" onClick={load}><RefreshCw size={16} />{t("common:actions.retry")}</button></div> : !orders.length ? <div className="supervisor-orders-state"><p>{t("orders:supervisor.list.empty")}</p></div> : <div className="supervisor-orders-table-wrap"><table className="supervisor-orders-table"><thead><tr>{["number", "station", "manager", "supplier", "products", "date", "status", "action"].map((key) => <th key={key}>{t(`orders:supervisor.list.columns.${key}`)}</th>)}</tr></thead><tbody>{orders.map((order) => <tr key={order.id}><td data-label={t("orders:supervisor.list.columns.number")}><strong>{order.orderNumber}</strong></td><td data-label={t("orders:supervisor.list.columns.station")}>{order.station?.name || "—"}</td><td data-label={t("orders:supervisor.list.columns.manager")}>{order.createdBy ? `${order.createdBy.firstName} ${order.createdBy.lastName}` : "—"}</td><td data-label={t("orders:supervisor.list.columns.supplier")}>{order.supplier?.displayName || t("orders:common.noSupplier")}</td><td data-label={t("orders:supervisor.list.columns.products")}>{order.items?.length === 1 ? order.items[0].productName : t("orders:list.productCount", { count: order.items?.length || 0 })}</td><td data-label={t("orders:supervisor.list.columns.date")}>{date(order.submittedAt || order.createdAt, locale)}</td><td data-label={t("orders:supervisor.list.columns.status")}><span className={`supervisor-orders-status supervisor-orders-status-${badgeClass(order.status)}`}>{t(`orders:statuses.${statusKey[order.status] || "draft"}`)}</span></td><td data-label={t("orders:supervisor.list.columns.action")}><Link className="supervisor-orders-action" to={`/superviseur/commandes/${order.id}`}><Eye size={16} />{t("orders:supervisor.list.view")}</Link></td></tr>)}</tbody></table></div>}{page.totalPages > 1 && <div className="supervisor-orders-filters"><span className="supervisor-orders-state">{t("orders:list.page", { current: page.number + 1, total: page.totalPages })}</span></div>}</main></SupervisorLayout>;
}
export default SupervisorOrdersPage;
