import { ChevronLeft, ChevronRight, Filter, RefreshCw, RotateCcw, Search, ShoppingCart } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";

import { getSalePermissions } from "../../../services/auth/permissionService";
import {
  getManagerStations,
  getOperationalDays,
  getShiftAssignments,
} from "../../../services/operations/operationalService";
import { getPosSales } from "../../../services/sale/saleService";
import SaleReversalModal from "./SaleReversalModal";

const emptyFilters = {
  stationId: "",
  operationalDayId: "",
  pumpAttendantId: "",
  saleType: "",
  status: "",
  from: "",
  to: "",
  sort: "soldAt,desc",
};

function dateBoundary(date, endOfDay = false) {
  if (!date) return "";
  return new Date(`${date}T${endOfDay ? "23:59:59.999" : "00:00:00.000"}`).toISOString();
}

function decimal(value, locale) {
  return new Intl.NumberFormat(locale, { maximumFractionDigits: 3 }).format(Number(value || 0));
}

export default function SalesList({ role }) {
  const { t, i18n } = useTranslation(["sales", "common"]);
  const locale = i18n.language === "en" ? "en-US" : "fr-CD";
  const isManager = role === "manager";
  const routeBase = isManager ? "/gerant/ventes" : "/superviseur/ventes";
  const permissions = getSalePermissions();
  const [pageData, setPageData] = useState({ content: [], number: 0, totalPages: 0, totalElements: 0, first: true, last: true });
  const [days, setDays] = useState([]);
  const [stations, setStations] = useState([]);
  const [attendants, setAttendants] = useState([]);
  const [draft, setDraft] = useState(emptyFilters);
  const [applied, setApplied] = useState(emptyFilters);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [reversalSale, setReversalSale] = useState(null);

  const loadMetadata = useCallback(async () => {
    try {
      const [loadedDays, managerStations] = await Promise.all([
        getOperationalDays(),
        isManager ? getManagerStations() : Promise.resolve([]),
      ]);
      const safeDays = Array.isArray(loadedDays) ? loadedDays : [];
      setDays(safeDays);
      setStations(
        isManager
          ? (Array.isArray(managerStations) ? managerStations : [])
          : Array.from(new Map(safeDays.filter((day) => day.station?.id).map((day) => [day.station.id, day.station])).values())
      );
    } catch {
      // La liste des ventes reste utilisable même si les options secondaires échouent.
    }
  }, [isManager]);

  const loadSales = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const response = await getPosSales(role, {
        stationId: applied.stationId,
        operationalDayId: applied.operationalDayId,
        pumpAttendantId: applied.pumpAttendantId,
        saleType: applied.saleType,
        status: applied.status,
        from: dateBoundary(applied.from),
        to: dateBoundary(applied.to, true),
        page,
        size: 20,
        sort: applied.sort,
      });
      setPageData(response || { content: [], number: 0, totalPages: 0, totalElements: 0, first: true, last: true });
    } catch (requestError) {
      setError(requestError.message || t("sales:errors.load"));
    } finally {
      setLoading(false);
    }
  }, [applied, page, role, t]);

  // Charge les options de filtre exposées par les read-models.
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadMetadata();
  }, [loadMetadata]);

  // Recharge la page quand les filtres serveur ou la pagination changent.
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadSales();
  }, [loadSales]);

  useEffect(() => {
    if (!draft.operationalDayId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setAttendants([]);
      return undefined;
    }
    let active = true;
    getShiftAssignments(draft.operationalDayId)
      .then((items) => {
        if (!active) return;
        const unique = Array.from(
          new Map((Array.isArray(items) ? items : []).map((shift) => [shift.pumpAttendant?.id, {
            ...shift.pumpAttendant,
            operationalCode: shift.operationalCode,
          }])).values()
        ).filter((item) => item.id);
        setAttendants(unique);
      })
      .catch(() => active && setAttendants([]));
    return () => { active = false; };
  }, [draft.operationalDayId]);

  const filteredDays = useMemo(
    () => days.filter((day) => !draft.stationId || day.station?.id === draft.stationId),
    [days, draft.stationId]
  );

  const submitFilters = (event) => {
    event.preventDefault();
    setPage(0);
    setApplied(draft);
  };

  const resetFilters = () => {
    setDraft(emptyFilters);
    setApplied(emptyFilters);
    setAttendants([]);
    setPage(0);
  };

  const onReversalSuccess = () => {
    setReversalSale(null);
    setSuccess(t("sales:feedback.reversed"));
    loadSales();
  };

  return (
    <section className="sales-page">
      <header className="sales-page-header">
        <div>
          <p className="sales-eyebrow">{t("sales:eyebrow")}</p>
          <h1>{t("sales:list.title")}</h1>
          <p>{t(isManager ? "sales:list.managerSubtitle" : "sales:list.supervisorSubtitle")}</p>
        </div>
        <button className="sales-button sales-button-secondary" type="button" onClick={loadSales} disabled={loading}>
          <RefreshCw size={17} className={loading ? "sales-spin" : ""} />{t("common:actions.refresh")}
        </button>
      </header>

      {success && <div className="sales-feedback success" role="status">{success}<button type="button" onClick={() => setSuccess("")}>×</button></div>}

      <form className="sales-filters" onSubmit={submitFilters}>
        <div className="sales-filter-title"><Filter size={18}/><strong>{t("sales:filters.title")}</strong></div>
        <label><span>{t("sales:fields.station")}</span><select value={draft.stationId} onChange={(event) => setDraft((value) => ({ ...value, stationId: event.target.value, operationalDayId: "", pumpAttendantId: "" }))}><option value="">{t("sales:filters.allStations")}</option>{stations.map((station) => <option value={station.id} key={station.id}>{station.name}</option>)}</select></label>
        <label><span>{t("sales:fields.operationalDay")}</span><select value={draft.operationalDayId} onChange={(event) => setDraft((value) => ({ ...value, operationalDayId: event.target.value, pumpAttendantId: "" }))}><option value="">{t("sales:filters.allDays")}</option>{filteredDays.map((day) => <option value={day.id} key={day.id}>{day.businessDate} · {day.station?.name}</option>)}</select></label>
        <label><span>{t("sales:fields.pumpAttendant")}</span><select value={draft.pumpAttendantId} onChange={(event) => setDraft((value) => ({ ...value, pumpAttendantId: event.target.value }))} disabled={!draft.operationalDayId}><option value="">{t("sales:filters.allAttendants")}</option>{attendants.map((item) => <option value={item.id} key={item.id}>{item.firstName} {item.lastName} · {item.operationalCode}</option>)}</select></label>
        <label><span>{t("sales:fields.saleType")}</span><select value={draft.saleType} onChange={(event) => setDraft((value) => ({ ...value, saleType: event.target.value }))}><option value="">{t("sales:filters.allTypes")}</option><option value="CASH">{t("sales:types.CASH")}</option><option value="CREDIT">{t("sales:types.CREDIT")}</option></select></label>
        <label><span>{t("sales:fields.status")}</span><select value={draft.status} onChange={(event) => setDraft((value) => ({ ...value, status: event.target.value }))}><option value="">{t("sales:filters.allStatuses")}</option><option value="EFFECTIVE">{t("sales:statuses.EFFECTIVE")}</option><option value="REVERSED">{t("sales:statuses.REVERSED")}</option></select></label>
        <label><span>{t("sales:fields.from")}</span><input type="date" value={draft.from} onChange={(event) => setDraft((value) => ({ ...value, from: event.target.value }))}/></label>
        <label><span>{t("sales:fields.to")}</span><input type="date" min={draft.from || undefined} value={draft.to} onChange={(event) => setDraft((value) => ({ ...value, to: event.target.value }))}/></label>
        <label><span>{t("sales:fields.sort")}</span><select value={draft.sort} onChange={(event) => setDraft((value) => ({ ...value, sort: event.target.value }))}><option value="soldAt,desc">{t("sales:filters.newest")}</option><option value="soldAt,asc">{t("sales:filters.oldest")}</option><option value="totalAmount,desc">{t("sales:filters.highestAmount")}</option></select></label>
        <div className="sales-filter-actions"><button className="sales-button sales-button-secondary" type="button" onClick={resetFilters}><RotateCcw size={16}/>{t("common:actions.reset")}</button><button className="sales-button sales-button-primary" type="submit"><Search size={16}/>{t("sales:filters.apply")}</button></div>
      </form>

      <div className="sales-list-summary"><span>{t("sales:list.resultCount", { count: pageData.totalElements || 0 })}</span></div>
      {loading ? (
        <div className="sales-state">{t("common:feedback.loading")}</div>
      ) : error ? (
        <div className="sales-state sales-state-error"><p>{error}</p><button className="sales-button sales-button-secondary" type="button" onClick={loadSales}>{t("common:actions.retry")}</button></div>
      ) : !pageData.content?.length ? (
        <div className="sales-empty"><ShoppingCart size={36}/><h2>{t("sales:list.emptyTitle")}</h2><p>{t("sales:list.emptyDescription")}</p></div>
      ) : (
        <div className="sales-table-wrap"><table className="sales-table"><thead><tr><th>{t("sales:fields.saleNumber")}</th><th>{t("sales:fields.soldAt")}</th><th>{t("sales:fields.station")}</th><th>{t("sales:fields.pumpAttendant")}</th><th>{t("sales:fields.product")}</th><th>{t("sales:fields.saleType")}</th><th>{t("sales:fields.quantity")}</th><th>{t("sales:fields.totalAmount")}</th><th>{t("sales:fields.status")}</th><th /></tr></thead><tbody>{pageData.content.map((sale) => <tr key={sale.id}><td><Link to={`${routeBase}/${sale.id}`}><strong>{sale.saleNumber}</strong></Link></td><td>{sale.soldAt ? new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(sale.soldAt)) : "—"}</td><td>{sale.station?.name || "—"}</td><td>{sale.pumpAttendant ? `${sale.pumpAttendant.firstName} ${sale.pumpAttendant.lastName}` : "—"}<small>{sale.pumpAttendant?.operationalCode}</small></td><td>{sale.product?.name || "—"}</td><td><span className={`sale-type sale-type-${sale.saleType}`}>{t(`sales:types.${sale.saleType}`)}</span></td><td>{decimal(sale.quantity, locale)} L</td><td><strong>{decimal(sale.totalAmount, locale)}</strong></td><td><span className={`sale-status sale-status-${sale.status}`}>{t(`sales:statuses.${sale.status}`)}</span></td><td><div className="sales-row-actions"><Link to={`${routeBase}/${sale.id}`}>{t("common:actions.view")}</Link>{isManager && permissions.canReverse && sale.status === "EFFECTIVE" && <button type="button" onClick={() => setReversalSale(sale)}>{t("sales:reversal.action")}</button>}</div></td></tr>)}</tbody></table></div>
      )}

      <nav className="sales-pagination" aria-label={t("sales:list.pagination")}>
        <button className="sales-button sales-button-secondary" type="button" onClick={() => setPage((value) => Math.max(0, value - 1))} disabled={loading || pageData.first}><ChevronLeft size={17}/>{t("common:actions.previous")}</button>
        <span>{t("sales:list.page", { current: (pageData.number ?? page) + 1, total: Math.max(pageData.totalPages || 1, 1) })}</span>
        <button className="sales-button sales-button-secondary" type="button" onClick={() => setPage((value) => value + 1)} disabled={loading || pageData.last}><ChevronRight size={17}/>{t("common:actions.next")}</button>
      </nav>
      <SaleReversalModal sale={reversalSale} isOpen={Boolean(reversalSale)} onClose={() => setReversalSale(null)} onSuccess={onReversalSuccess} />
    </section>
  );
}
