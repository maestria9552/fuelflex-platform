import { ArrowLeft, Calculator, Gauge, Plus, ReceiptText, RefreshCw } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import AppModal from "../../../components/modal/AppModal";
import ConfirmationModal from "../../../components/modal/ConfirmationModal";
import { getOperationalPermissions } from "../../../services/auth/permissionService";
import { getActiveDepots } from "../../../services/depot/depotService";
import {
  closeOperationalDay,
  closeShiftAssignment,
  createDailyExpense,
  createShiftAssignment,
  createTankGaugeReading,
  getAvailableFuelMeters,
  getDailyExpenses,
  getEligiblePumpAttendants,
  getOperationalDay,
  getOperationalDayRjv,
  getShiftAssignments,
  getShiftReconciliations,
  getTankGaugeReadings,
} from "../../../services/operations/operationalService";
import { getActiveTanks } from "../../../services/tank/tankService";
import RjvView from "./RjvView";

function person(value) {
  return [value?.firstName, value?.lastName].filter(Boolean).join(" ") || "—";
}

function decimal(value, locale) {
  if (value === null || value === undefined) return "—";
  return new Intl.NumberFormat(locale, { maximumFractionDigits: 3 }).format(Number(value));
}

export default function OperationalDayDetail({ role }) {
  const { id } = useParams();
  const { t, i18n } = useTranslation(["operations", "common"]);
  const locale = i18n.language === "en" ? "en-US" : "fr-CD";
  const permissions = getOperationalPermissions();
  const isManager = role === "manager";
  const routeBase = isManager ? "/gerant/operations" : "/superviseur/operations";
  const [day, setDay] = useState(null);
  const [assignments, setAssignments] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [gauges, setGauges] = useState([]);
  const [reconciliations, setReconciliations] = useState([]);
  const [rjv, setRjv] = useState(null);
  const [attendants, setAttendants] = useState([]);
  const [meters, setMeters] = useState([]);
  const [tanks, setTanks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingTanks, setLoadingTanks] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [formError, setFormError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [modal, setModal] = useState(null);
  const [selectedShift, setSelectedShift] = useState(null);
  const [assignmentForm, setAssignmentForm] = useState({ pumpAttendantId: "", fuelMeterId: "" });
  const [closingIndex, setClosingIndex] = useState("");
  const [expenseForm, setExpenseForm] = useState({ label: "", amount: "", reference: "", comment: "" });
  const [gaugeForm, setGaugeForm] = useState({ tankId: "", physicalStock: "", comment: "" });

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const loadedDay = await getOperationalDay(id);
      const [loadedAssignments, loadedExpenses, loadedGauges, loadedReconciliations, loadedRjv] = await Promise.all([
        permissions.canViewAssignments ? getShiftAssignments(id) : Promise.resolve([]),
        permissions.canViewExpenses ? getDailyExpenses(role, id) : Promise.resolve([]),
        permissions.canViewGauges ? getTankGaugeReadings(role, id) : Promise.resolve([]),
        permissions.canViewReconciliations ? getShiftReconciliations(role, id) : Promise.resolve([]),
        permissions.canViewRjv ? getOperationalDayRjv(role, id) : Promise.resolve(null),
      ]);
      setDay(loadedDay);
      setAssignments(Array.isArray(loadedAssignments) ? loadedAssignments : []);
      setExpenses(Array.isArray(loadedExpenses) ? loadedExpenses : []);
      setGauges(Array.isArray(loadedGauges) ? loadedGauges : []);
      setReconciliations(Array.isArray(loadedReconciliations) ? loadedReconciliations : []);
      setRjv(loadedRjv);
      if (isManager && loadedDay.status === "OPEN" && permissions.canCreateAssignment) {
        const [loadedAttendants, loadedMeters] = await Promise.all([
          getEligiblePumpAttendants(loadedDay.station.id),
          getAvailableFuelMeters(id),
        ]);
        setAttendants(Array.isArray(loadedAttendants) ? loadedAttendants : []);
        setMeters(Array.isArray(loadedMeters) ? loadedMeters : []);
      } else {
        setAttendants([]);
        setMeters([]);
      }
    } catch (requestError) {
      setError(requestError.message || t("operations:errors.loadDay"));
    } finally {
      setLoading(false);
    }
  }, [id, isManager, permissions.canCreateAssignment, permissions.canViewAssignments, permissions.canViewExpenses, permissions.canViewGauges, permissions.canViewReconciliations, permissions.canViewRjv, role, t]);

  // Synchronise la page avec les read-models protégés.
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load();
  }, [load]);

  const openAssignments = useMemo(() => assignments.filter((item) => item.status === "OPEN"), [assignments]);
  const gaugedTankIds = useMemo(() => new Set(gauges.map((item) => item.tankId)), [gauges]);
  const availableTanks = useMemo(() => tanks.filter((tank) => !gaugedTankIds.has(tank.id)), [tanks, gaugedTankIds]);

  const runMutation = async (action, successKey) => {
    setSubmitting(true);
    setFormError("");
    try {
      await action();
      setModal(null);
      setSelectedShift(null);
      setSuccess(t(successKey));
      await load();
      return true;
    } catch (requestError) {
      setFormError(requestError.message || t("operations:errors.action"));
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  const openAssignmentModal = () => {
    setFormError("");
    setAssignmentForm({
      pumpAttendantId: attendants[0]?.id || "",
      fuelMeterId: meters[0]?.id || "",
    });
    setModal("assignment");
  };

  const submitAssignment = (event) => {
    event.preventDefault();
    runMutation(
      () => createShiftAssignment(id, assignmentForm),
      "operations:feedback.assignmentOpened"
    );
  };

  const openCloseShift = (shift) => {
    setFormError("");
    setSelectedShift(shift);
    setClosingIndex(shift.openingIndex ?? "");
    setModal("closeShift");
  };

  const submitCloseShift = (event) => {
    event.preventDefault();
    runMutation(
      () => closeShiftAssignment(selectedShift.id, closingIndex),
      "operations:feedback.assignmentClosed"
    );
  };

  const submitExpense = (event) => {
    event.preventDefault();
    runMutation(
      () => createDailyExpense(id, {
        ...expenseForm,
        reference: expenseForm.reference || null,
        comment: expenseForm.comment || null,
      }),
      "operations:feedback.expenseAdded"
    );
  };

  const openGaugeModal = async () => {
    setFormError("");
    setLoadingTanks(true);
    try {
      const depots = await getActiveDepots(day.organizationId, day.station.id);
      const tankGroups = await Promise.all(
        (Array.isArray(depots) ? depots : []).map((depot) =>
          getActiveTanks(day.organizationId, day.station.id, depot.id)
        )
      );
      const loadedTanks = tankGroups.flat();
      setTanks(loadedTanks);
      const firstAvailable = loadedTanks.find((tank) => !gaugedTankIds.has(tank.id));
      setGaugeForm({ tankId: firstAvailable?.id || "", physicalStock: "", comment: "" });
      setModal("gauge");
    } catch (requestError) {
      setError(requestError.message || t("operations:errors.loadTanks"));
    } finally {
      setLoadingTanks(false);
    }
  };

  const submitGauge = (event) => {
    event.preventDefault();
    runMutation(
      () => createTankGaugeReading(id, {
        tankId: gaugeForm.tankId,
        physicalStock: gaugeForm.physicalStock,
        comment: gaugeForm.comment || null,
      }),
      "operations:feedback.gaugeAdded"
    );
  };

  const submitCloseDay = () => {
    runMutation(() => closeOperationalDay(id), "operations:feedback.dayClosed");
  };

  if (loading) {
    return <section className="operations-page"><div className="operations-state">{t("common:feedback.loading")}</div></section>;
  }

  if (error || !day) {
    return (
      <section className="operations-page">
        <Link className="operations-back" to={routeBase}><ArrowLeft size={17} />{t("common:actions.back")}</Link>
        <div className="operations-state operations-state-error"><p>{error || t("operations:errors.loadDay")}</p><button className="ops-button ops-button-secondary" type="button" onClick={load}>{t("common:actions.retry")}</button></div>
      </section>
    );
  }

  return (
    <section className="operations-page">
      <Link className="operations-back" to={routeBase}><ArrowLeft size={17} />{t("operations:days.back")}</Link>
      <header className="operations-page-header">
        <div>
          <p className="operations-eyebrow">{day.station?.name}</p>
          <h1>{t("operations:detail.title", { date: day.businessDate })}</h1>
          <p>{t("operations:detail.openedBy", { name: person(day.openedBy) })}</p>
        </div>
        <div className="operations-header-actions">
          <span className={`ops-status ops-status-${day.status}`}>{t(`operations:statuses.${day.status}`)}</span>
          <button className="ops-button ops-button-secondary" type="button" onClick={load}><RefreshCw size={17} />{t("common:actions.refresh")}</button>
          {isManager && day.status === "OPEN" && permissions.canCloseDay && (
            <button className="ops-button ops-button-danger" type="button" onClick={() => { setFormError(""); setModal("closeDay"); }}>
              {t("operations:detail.closeDay")}
            </button>
          )}
        </div>
      </header>
      {success && <div className="operations-feedback success" role="status">{success}<button type="button" onClick={() => setSuccess("")}>×</button></div>}

      <div className="operations-kpis operations-kpis-small">
        <article className="operations-kpi"><span>{t("operations:detail.assignments")}</span><strong>{assignments.length}</strong><small>{t("operations:detail.openCount", { count: openAssignments.length })}</small></article>
        <article className="operations-kpi"><span>{t("operations:detail.reconciliations")}</span><strong>{reconciliations.length}</strong><small>{t("operations:detail.ofAssignments", { count: assignments.length })}</small></article>
        <article className="operations-kpi"><span>{t("operations:detail.gauges")}</span><strong>{gauges.length}</strong></article>
        <article className="operations-kpi"><span>{t("operations:detail.expenses")}</span><strong>{decimal(expenses.reduce((sum, item) => sum + Number(item.amount || 0), 0), locale)}</strong></article>
      </div>

      <section className="operations-card">
        <div className="operations-section-header">
          <div><h2>{t("operations:assignments.title")}</h2><p>{t("operations:assignments.description")}</p></div>
          {isManager && day.status === "OPEN" && permissions.canCreateAssignment && (
            <button className="ops-button ops-button-primary" type="button" onClick={openAssignmentModal} disabled={!attendants.length || !meters.length}>
              <Plus size={17} />{t("operations:assignments.open")}
            </button>
          )}
        </div>
        {isManager && day.status === "OPEN" && permissions.canCreateAssignment && (!attendants.length || !meters.length) && (
          <p className="operations-info">{t(!attendants.length ? "operations:assignments.noAttendant" : "operations:assignments.noMeter")}</p>
        )}
        {assignments.length ? (
          <div className="operations-table-wrap"><table className="operations-table"><thead><tr><th>{t("operations:fields.pumpAttendant")}</th><th>{t("operations:fields.fuelMeter")}</th><th>{t("operations:fields.pump")}</th><th>{t("operations:fields.openingIndex")}</th><th>{t("operations:fields.closingIndex")}</th><th>{t("operations:fields.meteredVolume")}</th><th>{t("operations:fields.status")}</th><th /></tr></thead>
          <tbody>{assignments.map((shift) => <tr key={shift.id}><td><strong>{person(shift.pumpAttendant)}</strong><br/><small>{shift.operationalCode}</small></td><td>{shift.fuelMeter?.name || "—"}</td><td>{shift.pump?.name || "—"}{shift.dispensingPoint?.name ? <small className="operations-block">{shift.dispensingPoint.name}</small> : null}</td><td>{decimal(shift.openingIndex, locale)}</td><td>{decimal(shift.closingIndex, locale)}</td><td>{decimal(shift.meteredVolume, locale)} L</td><td><span className={`ops-status ops-status-${shift.status}`}>{t(`operations:statuses.${shift.status}`)}</span></td><td>{isManager && shift.status === "OPEN" && permissions.canCloseAssignment && <button className="operations-row-action" type="button" onClick={() => openCloseShift(shift)}>{t("operations:assignments.close")}</button>}</td></tr>)}</tbody></table></div>
        ) : <div className="operations-empty compact"><p>{t("operations:assignments.empty")}</p></div>}
      </section>

      <div className="operations-grid">
        <section className="operations-card">
          <div className="operations-section-header"><div><h2><ReceiptText size={20}/>{t("operations:expenses.title")}</h2></div>{isManager && day.status === "OPEN" && permissions.canCreateExpense && <button className="ops-button ops-button-secondary" type="button" onClick={() => { setFormError(""); setExpenseForm({ label: "", amount: "", reference: "", comment: "" }); setModal("expense"); }}><Plus size={16}/>{t("operations:expenses.add")}</button>}</div>
          {expenses.length ? <ul className="operations-data-list">{expenses.map((expense) => <li key={expense.id}><span><strong>{expense.label}</strong><small>{expense.reference || expense.comment || "—"}</small></span><b>{decimal(expense.amount, locale)}</b></li>)}</ul> : <p className="operations-muted">{t("operations:expenses.empty")}</p>}
        </section>
        <section className="operations-card">
          <div className="operations-section-header"><div><h2><Gauge size={20}/>{t("operations:gauges.title")}</h2></div>{isManager && day.status === "OPEN" && permissions.canCreateGauge && <button className="ops-button ops-button-secondary" type="button" onClick={openGaugeModal} disabled={loadingTanks}><Plus size={16}/>{loadingTanks ? t("common:feedback.loading") : t("operations:gauges.add")}</button>}</div>
          {gauges.length ? <ul className="operations-data-list">{gauges.map((reading) => <li key={reading.id}><span><strong>{reading.tankName}</strong><small>{t("operations:gauges.theoretical", { value: decimal(reading.theoreticalStock, locale) })}</small></span><b>{decimal(reading.physicalStock, locale)} L <small className={Number(reading.stockVariance) < 0 ? "negative" : ""}>{t("operations:gauges.variance", { value: decimal(reading.stockVariance, locale) })}</small></b></li>)}</ul> : <p className="operations-muted">{t("operations:gauges.empty")}</p>}
        </section>
      </div>

      <section className="operations-card">
        <div className="operations-section-header"><div><h2><Calculator size={20}/>{t("operations:reconciliations.title")}</h2><p>{t("operations:reconciliations.description")}</p></div></div>
        {reconciliations.length ? <div className="operations-table-wrap"><table className="operations-table operations-table-compact"><thead><tr><th>{t("operations:fields.pumpAttendant")}</th><th>{t("operations:fields.fuelMeter")}</th><th>{t("operations:fields.openingIndex")}</th><th>{t("operations:fields.closingIndex")}</th><th>{t("operations:reconciliations.metered")}</th><th>{t("operations:reconciliations.cash")}</th><th>{t("operations:reconciliations.credit")}</th><th>{t("operations:reconciliations.variance")}</th></tr></thead><tbody>{reconciliations.map((item) => <tr key={item.id}><td>{item.pumpAttendantName}</td><td>{item.fuelMeterName}</td><td>{decimal(item.openingIndex, locale)}</td><td>{decimal(item.closingIndex, locale)}</td><td>{decimal(item.meteredVolume, locale)} L</td><td>{decimal(item.cashVolume, locale)} L</td><td>{decimal(item.creditVolume, locale)} L</td><td>{decimal(item.volumeVariance, locale)} L</td></tr>)}</tbody></table></div> : <p className="operations-muted">{t("operations:reconciliations.empty")}</p>}
      </section>

      {permissions.canViewRjv && <section className="operations-report-section"><div className="operations-section-header"><div><h2>{t("operations:rjv.title")}</h2><p>{t("operations:rjv.description")}</p></div></div><RjvView report={rjv} locale={locale} /></section>}

      <AppModal isOpen={modal === "assignment"} title={t("operations:assignmentForm.title")} size="sm" onClose={() => !submitting && setModal(null)} footer={<><button className="ops-button ops-button-secondary" type="button" onClick={() => setModal(null)} disabled={submitting}>{t("common:actions.cancel")}</button><button className="ops-button ops-button-primary" type="submit" form="assignment-form" disabled={submitting || !assignmentForm.pumpAttendantId || !assignmentForm.fuelMeterId}>{submitting ? t("common:actions.saving") : t("operations:assignments.open")}</button></>}>
        <form className="operations-form" id="assignment-form" onSubmit={submitAssignment}><label><span>{t("operations:fields.pumpAttendant")}</span><select value={assignmentForm.pumpAttendantId} onChange={(event) => setAssignmentForm((value) => ({ ...value, pumpAttendantId: event.target.value }))} required>{attendants.map((item) => <option value={item.id} key={item.id}>{item.firstName} {item.lastName} · {item.operationalCode}</option>)}</select></label><label><span>{t("operations:fields.fuelMeter")}</span><select value={assignmentForm.fuelMeterId} onChange={(event) => setAssignmentForm((value) => ({ ...value, fuelMeterId: event.target.value }))} required>{meters.map((meter) => <option value={meter.id} key={meter.id}>{meter.name} · {meter.pump?.name}{meter.dispensingPoint?.name ? ` / ${meter.dispensingPoint.name}` : ""} · {decimal(meter.currentIndex, locale)}</option>)}</select></label>{formError && <p className="operations-form-error">{formError}</p>}</form>
      </AppModal>

      <AppModal isOpen={modal === "closeShift"} title={t("operations:closeShift.title")} description={t("operations:closeShift.description", { name: person(selectedShift?.pumpAttendant), index: decimal(selectedShift?.openingIndex, locale) })} size="sm" onClose={() => !submitting && setModal(null)} footer={<><button className="ops-button ops-button-secondary" type="button" onClick={() => setModal(null)} disabled={submitting}>{t("common:actions.cancel")}</button><button className="ops-button ops-button-primary" type="submit" form="close-shift-form" disabled={submitting || closingIndex === ""}>{submitting ? t("common:actions.saving") : t("operations:assignments.close")}</button></>}>
        <form className="operations-form" id="close-shift-form" onSubmit={submitCloseShift}><label><span>{t("operations:fields.closingIndex")}</span><input type="number" min={selectedShift?.openingIndex ?? 0} step="0.001" value={closingIndex} onChange={(event) => setClosingIndex(event.target.value)} required /></label>{formError && <p className="operations-form-error">{formError}</p>}</form>
      </AppModal>

      <AppModal isOpen={modal === "expense"} title={t("operations:expenseForm.title")} size="sm" onClose={() => !submitting && setModal(null)} footer={<><button className="ops-button ops-button-secondary" type="button" onClick={() => setModal(null)} disabled={submitting}>{t("common:actions.cancel")}</button><button className="ops-button ops-button-primary" type="submit" form="expense-form" disabled={submitting || !expenseForm.label.trim() || !expenseForm.amount}>{submitting ? t("common:actions.saving") : t("operations:expenses.add")}</button></>}>
        <form className="operations-form" id="expense-form" onSubmit={submitExpense}><label><span>{t("operations:fields.label")}</span><input maxLength="180" value={expenseForm.label} onChange={(event) => setExpenseForm((value) => ({ ...value, label: event.target.value }))} required /></label><label><span>{t("operations:fields.amount")}</span><input type="number" min="0.001" step="0.001" value={expenseForm.amount} onChange={(event) => setExpenseForm((value) => ({ ...value, amount: event.target.value }))} required /></label><label><span>{t("operations:fields.reference")}</span><input maxLength="100" value={expenseForm.reference} onChange={(event) => setExpenseForm((value) => ({ ...value, reference: event.target.value }))} /></label><label><span>{t("operations:fields.comment")}</span><textarea maxLength="1000" rows="3" value={expenseForm.comment} onChange={(event) => setExpenseForm((value) => ({ ...value, comment: event.target.value }))} /></label>{formError && <p className="operations-form-error">{formError}</p>}</form>
      </AppModal>

      <AppModal isOpen={modal === "gauge"} title={t("operations:gaugeForm.title")} size="sm" onClose={() => !submitting && setModal(null)} footer={<><button className="ops-button ops-button-secondary" type="button" onClick={() => setModal(null)} disabled={submitting}>{t("common:actions.cancel")}</button><button className="ops-button ops-button-primary" type="submit" form="gauge-form" disabled={submitting || !gaugeForm.tankId || gaugeForm.physicalStock === ""}>{submitting ? t("common:actions.saving") : t("operations:gauges.add")}</button></>}>
        <form className="operations-form" id="gauge-form" onSubmit={submitGauge}><label><span>{t("operations:fields.tank")}</span><select value={gaugeForm.tankId} onChange={(event) => setGaugeForm((value) => ({ ...value, tankId: event.target.value }))} required>{availableTanks.length === 0 && <option value="">{t("operations:gauges.allRecorded")}</option>}{availableTanks.map((tank) => <option value={tank.id} key={tank.id}>{tank.name} · {tank.productName}</option>)}</select></label><label><span>{t("operations:fields.physicalStock")}</span><input type="number" min="0" step="0.001" value={gaugeForm.physicalStock} onChange={(event) => setGaugeForm((value) => ({ ...value, physicalStock: event.target.value }))} required /></label><label><span>{t("operations:fields.comment")}</span><textarea maxLength="1000" rows="3" value={gaugeForm.comment} onChange={(event) => setGaugeForm((value) => ({ ...value, comment: event.target.value }))} /></label>{formError && <p className="operations-form-error">{formError}</p>}</form>
      </AppModal>

      <ConfirmationModal isOpen={modal === "closeDay"} title={t("operations:closeDay.title")} description={t("operations:closeDay.description", { open: openAssignments.length, reconciliations: reconciliations.length, gauges: gauges.length })} confirmLabel={t("operations:detail.closeDay")} loadingLabel={t("operations:closeDay.loading")} variant="warning" isLoading={submitting} errorMessage={formError} onConfirm={submitCloseDay} onClose={() => setModal(null)} />
    </section>
  );
}
