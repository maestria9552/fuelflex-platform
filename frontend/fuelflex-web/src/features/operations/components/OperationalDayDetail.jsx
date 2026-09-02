import {
  ArrowLeft,
  Droplets,
  Gauge,
  Plus,
  ReceiptText,
  RefreshCw,
  UserPlus,
} from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";

import AppModal from "../../../components/modal/AppModal";
import { formatCurrency } from "../../../i18n/formatters";
import { getOperationalPermissions } from "../../../services/auth/permissionService";
import { getActiveDepots } from "../../../services/depot/depotService";
import {
  closeOperationalDay,
  closeShiftAssignment,
  createDailyExpense,
  createShiftAssignment,
  createTankGaugeReading,
  createInternalConsumption,
  getInternalConsumptions,
  createTankReturn,
  getAvailableFuelMeters,
  getDailyExpenses,
  getEligiblePumpAttendants,
  getOperationalDay,
  getOperationalDayRjv,
  getShiftAssignments,
  getShiftReconciliations,
  getTankGaugeReadings,
  getTankReturns,
} from "../../../services/operations/operationalService";
import { getManagerStockBalances } from "../../../services/reception/receptionService";
import { getOrganizationById } from "../../../services/organization/organizationService";
import { getActiveTanks } from "../../../services/tank/tankService";
import RjvView from "./RjvView";
import ShiftClosureModal from "./ShiftClosureModal";
import TankReturnModal from "./TankReturnModal";
import InternalConsumptionModal from "./InternalConsumptionModal";
import DayClosureModal from "./DayClosureModal";

const person = (value) =>
  [value?.firstName, value?.lastName].filter(Boolean).join(" ") || "—";
const decimal = (value, locale) =>
  value === null || value === undefined
    ? "—"
    : new Intl.NumberFormat(locale, { maximumFractionDigits: 3 }).format(
        Number(value),
      );
const dateTime = (value, locale) =>
  value
    ? new Intl.DateTimeFormat(locale, {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(new Date(value))
    : "—";

export function ExpenseFormFields({
  expenseForm,
  setExpenseForm,
  currency,
  formError,
  t,
}) {
  return (
    <>
      <label>
        <span>{t("operations:fields.label")}</span>
        <input
          value={expenseForm.label}
          onChange={(event) =>
            setExpenseForm((value) => ({ ...value, label: event.target.value }))
          }
        />
      </label>
      <label>
        <span>{t("operations:fields.amount")}</span>
        <div className="operations-input-unit">
          <input
            type="number"
            min="0.001"
            step="0.001"
            value={expenseForm.amount}
            onChange={(event) =>
              setExpenseForm((value) => ({
                ...value,
                amount: event.target.value,
              }))
            }
          />
          <b>{currency}</b>
        </div>
      </label>
      <label>
        <span>{t("operations:fields.reference")}</span>
        <input
          value={expenseForm.reference}
          onChange={(event) =>
            setExpenseForm((value) => ({
              ...value,
              reference: event.target.value,
            }))
          }
        />
      </label>
      <label>
        <span>{t("operations:fields.comment")}</span>
        <textarea
          value={expenseForm.comment}
          onChange={(event) =>
            setExpenseForm((value) => ({
              ...value,
              comment: event.target.value,
            }))
          }
        />
      </label>
      {formError && (
        <p className="operations-form-error" role="alert">
          {formError}
        </p>
      )}
    </>
  );
}

export default function OperationalDayDetail({ role }) {
  const { id } = useParams();
  const { t, i18n } = useTranslation(["operations", "common"]);
  const locale = i18n.language === "en" ? "en-US" : "fr-CD";
  const language = i18n.resolvedLanguage || i18n.language;
  const permissions = getOperationalPermissions();
  const isManager = role === "manager";
  const routeBase = isManager
    ? "/gerant/operations"
    : "/superviseur/operations";
  const [day, setDay] = useState(null),
    [organization, setOrganization] = useState(null),
    [assignments, setAssignments] = useState([]),
    [expenses, setExpenses] = useState([]),
    [gauges, setGauges] = useState([]),
    [reconciliations, setReconciliations] = useState([]),
    [returns, setReturns] = useState([]),
    [rjv, setRjv] = useState(null),
    [attendants, setAttendants] = useState([]),
    [meters, setMeters] = useState([]),
    [tanks, setTanks] = useState([]),
    [stocks, setStocks] = useState([]),
    [internalByShift, setInternalByShift] = useState(new Map());
  const [loading, setLoading] = useState(true),
    [loadingTanks, setLoadingTanks] = useState(false),
    [submitting, setSubmitting] = useState(false),
    [error, setError] = useState(""),
    [success, setSuccess] = useState(""),
    [formError, setFormError] = useState(""),
    [modal, setModal] = useState(null),
    [selectedShift, setSelectedShift] = useState(null);
  const [assignmentForm, setAssignmentForm] = useState({
      pumpAttendantId: "",
      fuelMeterId: "",
    }),
    [expenseForm, setExpenseForm] = useState({
      label: "",
      amount: "",
      reference: "",
      comment: "",
    }),
    [gaugeForm, setGaugeForm] = useState({
      tankId: "",
      physicalStock: "",
      comment: "",
    });

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const loadedDay = await getOperationalDay(id);
      const [
        loadedOrganization,
        loadedAssignments,
        loadedExpenses,
        loadedGauges,
        loadedRecs,
        loadedReturns,
        loadedRjv,
        loadedStocks,
      ] = await Promise.all([
        getOrganizationById(loadedDay.organizationId),
        permissions.canViewAssignments ? getShiftAssignments(id) : [],
        permissions.canViewExpenses ? getDailyExpenses(role, id) : [],
        permissions.canViewGauges ? getTankGaugeReadings(role, id) : [],
        permissions.canViewReconciliations
          ? getShiftReconciliations(role, id)
          : [],
        permissions.canViewTankReturns ? getTankReturns(role, id) : [],
        permissions.canViewRjv ? getOperationalDayRjv(role, id) : null,
        getManagerStockBalances().catch(() => []),
      ]);
      setDay(loadedDay);
      setOrganization(loadedOrganization);
      const assignmentList = Array.isArray(loadedAssignments) ? loadedAssignments : [];
      setAssignments(assignmentList);
      const internalEntries = await Promise.all(assignmentList.map(async (assignment) => [assignment.id, await getInternalConsumptions(role, assignment.id).catch(() => [])]));
      setInternalByShift(new Map(internalEntries));
      setExpenses(Array.isArray(loadedExpenses) ? loadedExpenses : []);
      setGauges(Array.isArray(loadedGauges) ? loadedGauges : []);
      setReconciliations(Array.isArray(loadedRecs) ? loadedRecs : []);
      setReturns(Array.isArray(loadedReturns) ? loadedReturns : []);
      setRjv(loadedRjv);
      setStocks(
        (Array.isArray(loadedStocks) ? loadedStocks : []).filter(
          (stock) => stock.stationId === loadedDay.station.id,
        ),
      );
      if (
        isManager &&
        loadedDay.status === "OPEN" &&
        permissions.canCreateAssignment
      ) {
        const [a, m] = await Promise.all([
          getEligiblePumpAttendants(loadedDay.station.id),
          getAvailableFuelMeters(id),
        ]);
        setAttendants(Array.isArray(a) ? a : []);
        setMeters(Array.isArray(m) ? m : []);
      } else {
        setAttendants([]);
        setMeters([]);
      }
    } catch (requestError) {
      setError(requestError.message || t("operations:errors.loadDay"));
    } finally {
      setLoading(false);
    }
  }, [
    id,
    isManager,
    permissions.canCreateAssignment,
    permissions.canViewAssignments,
    permissions.canViewExpenses,
    permissions.canViewGauges,
    permissions.canViewReconciliations,
    permissions.canViewRjv,
    permissions.canViewTankReturns,
    role,
    t,
  ]);
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load();
  }, [load]);

  const openAssignments = useMemo(
    () => assignments.filter((item) => item.status === "OPEN"),
    [assignments],
  );
  const internalsFor = (shiftId) => internalByShift.get(shiftId) || [];
  const returnsByShift = useMemo(
    () =>
      returns.reduce((map, item) => {
        const list = map.get(item.shiftAssignmentId) || [];
        list.push(item);
        map.set(item.shiftAssignmentId, list);
        return map;
      }, new Map()),
    [returns],
  );
  const reconciliationsByShift = useMemo(
    () =>
      new Map(reconciliations.map((item) => [item.shiftAssignmentId, item])),
    [reconciliations],
  );
  const gaugedTankIds = useMemo(
    () => new Set(gauges.map((item) => item.tankId)),
    [gauges],
  );
  const availableTanks = useMemo(
    () => tanks.filter((tank) => !gaugedTankIds.has(tank.id)),
    [tanks, gaugedTankIds],
  );
  const selectedGaugeTank = tanks.find((tank) => tank.id === gaugeForm.tankId);
  const selectedGaugeStock = stocks.find(
    (stock) => stock.tankId === gaugeForm.tankId,
  );
  const gaugeVariance =
    gaugeForm.physicalStock === "" || !selectedGaugeStock
      ? null
      : Number(gaugeForm.physicalStock) -
        Number(selectedGaugeStock.currentStock || 0);
  const currency = organization?.defaultCurrency;
  const money = (value) =>
    value === null || value === undefined
      ? "—"
      : formatCurrency(Number(value), currency, { language });

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
  const loadTanks = async () => {
    setLoadingTanks(true);
    try {
      const depots = await getActiveDepots(day.organizationId, day.station.id);
      const groups = await Promise.all(
        (Array.isArray(depots) ? depots : []).map((depot) =>
          getActiveTanks(day.organizationId, day.station.id, depot.id),
        ),
      );
      const loaded = groups.flat();
      setTanks(loaded);
      return loaded;
    } finally {
      setLoadingTanks(false);
    }
  };
  const openInternal = (shift) => { setFormError(""); setSelectedShift(shift); setModal("internal"); };
  const submitInternal = (payload) => runMutation(() => createInternalConsumption(selectedShift.id, payload), "operations:feedback.internalAdded");
  const openReturn = async (shift) => {
    setFormError("");
    setSelectedShift(shift);
    try {
      await loadTanks();
      setModal("tankReturn");
    } catch (requestError) {
      setError(requestError.message || t("operations:errors.loadTanks"));
    }
  };
  const openGauge = async () => {
    setFormError("");
    try {
      const loaded = await loadTanks();
      const first = loaded.find((tank) => !gaugedTankIds.has(tank.id));
      setGaugeForm({ tankId: first?.id || "", physicalStock: "", comment: "" });
      setModal("gauge");
    } catch (requestError) {
      setError(requestError.message || t("operations:errors.loadTanks"));
    }
  };
  const submitAssignment = (event) => {
    event.preventDefault();
    if (submitting) return;
    runMutation(
      () => createShiftAssignment(id, assignmentForm),
      "operations:feedback.assignmentOpened",
    );
  };
  const submitExpense = (event) => {
    event.preventDefault();
    if (submitting) return;
    runMutation(
      () =>
        createDailyExpense(id, {
          ...expenseForm,
          reference: expenseForm.reference || null,
          comment: expenseForm.comment || null,
        }),
      "operations:feedback.expenseAdded",
    );
  };
  const submitGauge = (event) => {
    event.preventDefault();
    if (submitting) return;
    runMutation(
      () =>
        createTankGaugeReading(id, {
          tankId: gaugeForm.tankId,
          physicalStock: gaugeForm.physicalStock,
          comment: gaugeForm.comment || null,
        }),
      "operations:feedback.gaugeAdded",
    );
  };

  if (loading)
    return (
      <section className="operations-page">
        <div className="operations-state">{t("common:feedback.loading")}</div>
      </section>
    );
  if (error || !day)
    return (
      <section className="operations-page">
        <Link className="operations-back" to={routeBase}>
          <ArrowLeft size={17} />
          {t("common:actions.back")}
        </Link>
        <div className="operations-state operations-state-error">
          <p>{error || t("operations:errors.loadDay")}</p>
          <button
            className="ops-button ops-button-secondary"
            type="button"
            onClick={load}
          >
            {t("common:actions.retry")}
          </button>
        </div>
      </section>
    );

  return (
    <section className="operations-page">
      <Link className="operations-back" to={routeBase}>
        <ArrowLeft size={17} />
        {t("operations:days.back")}
      </Link>
      <header className="operations-page-header">
        <div>
          <p className="operations-eyebrow">{day.station?.name}</p>
          <h1>{t("operations:detail.title", { date: day.businessDate })}</h1>
          <p>
            {t("operations:detail.openedBy", { name: person(day.openedBy) })}
          </p>
        </div>
        <div className="operations-header-actions">
          <span className={`ops-status ops-status-${day.status}`}>
            {t(`operations:statuses.${day.status}`)}
          </span>
          <button
            className="ops-button ops-button-secondary"
            type="button"
            onClick={load}
          >
            <RefreshCw size={17} />
            {t("common:actions.refresh")}
          </button>
          {isManager && day.status === "OPEN" && permissions.canCloseDay && (
            <button
              className="ops-button ops-button-danger"
              type="button"
              onClick={() => {
                setFormError("");
                setModal("closeDay");
              }}
            >
              {t("operations:detail.closeDay")}
            </button>
          )}
        </div>
      </header>
      {day.status === "OPEN" && (
        <p className="operations-provisional">
          <span />
          {t("operations:detail.provisional")}
        </p>
      )}
      {success && (
        <div className="operations-feedback success" role="status">
          {success}
          <button type="button" onClick={() => setSuccess("")}>
            ×
          </button>
        </div>
      )}
      <div className="operations-kpis operations-kpis-small">
        <article className="operations-kpi">
          <span>{t("operations:detail.assignments")}</span>
          <strong>{assignments.length}</strong>
          <small>
            {t("operations:detail.openCount", {
              count: openAssignments.length,
            })}
          </small>
        </article>
        <article className="operations-kpi">
          <span>{t("operations:detail.reconciliations")}</span>
          <strong>{reconciliations.length}</strong>
          <small>
            {t("operations:detail.ofAssignments", {
              count: assignments.length,
            })}
          </small>
        </article>
        <article className="operations-kpi">
          <span>{t("operations:tankReturns.title")}</span>
          <strong>
            {decimal(
              returns.reduce(
                (sum, item) => sum + Number(item.quantity || 0),
                0,
              ),
              locale,
            )}{" "}
            L
          </strong>
        </article>
        <article className="operations-kpi">
          <span>{t("operations:detail.expenses")}</span>
          <strong>
            {money(
              expenses.reduce((sum, item) => sum + Number(item.amount || 0), 0),
            )}
          </strong>
        </article>
      </div>

      <section className="operations-card">
        <div className="operations-section-header">
          <div>
            <h2>{t("operations:assignments.title")}</h2>
            <p>{t("operations:assignments.description")}</p>
          </div>
          {isManager &&
            day.status === "OPEN" &&
            permissions.canCreateAssignment && (
              <button
                className="ops-button ops-button-primary operations-manager-form-action"
                type="button"
                disabled={!attendants.length || !meters.length}
                onClick={() => {
                  setAssignmentForm({
                    pumpAttendantId: attendants[0]?.id || "",
                    fuelMeterId: meters[0]?.id || "",
                  });
                  setModal("assignment");
                }}
              >
                <Plus size={17} />
                {t("operations:assignments.open")}
              </button>
            )}
        </div>
        {assignments.length ? (
          <div className="operations-shift-list">
            {assignments.map((shift) => {
              const shiftReturns = returnsByShift.get(shift.id) || [],
                rec = reconciliationsByShift.get(shift.id);
              return (
                <article
                  className={`operations-shift-card operations-shift-${shift.status}`}
                  key={shift.id}
                >
                  <header>
                    <div>
                      <span className={`ops-status ops-status-${shift.status}`}>
                        {t(`operations:statuses.${shift.status}`)}
                      </span>
                      <h3>{person(shift.pumpAttendant)}</h3>
                      <small>{shift.operationalCode}</small>
                    </div>
                    <div className="operations-shift-actions">
                      {isManager && shift.status === "OPEN" && permissions.canCreateInternalConsumption && (
                        <button className="ops-button ops-button-secondary" type="button" onClick={() => openInternal(shift)}>
                          {t("operations:internal.action")}
                        </button>
                      )}
                      {shift.status === "CLOSED" && rec && <Link className="ops-button ops-button-secondary" to={`${routeBase}/${id}/assignments/${shift.id}/print`}>Imprimer la fiche pompiste</Link>}
                      {isManager &&
                        shift.status === "OPEN" &&
                        permissions.canCreateTankReturn && (
                          <button
                            className="ops-button ops-button-secondary"
                            type="button"
                            onClick={() => openReturn(shift)}
                            disabled={loadingTanks}
                          >
                            <Droplets size={16} />
                            {t("operations:tankReturns.action")}
                          </button>
                        )}
                      {isManager &&
                        shift.status === "OPEN" &&
                        permissions.canCloseAssignment && (
                          <button
                            className="ops-button ops-button-danger"
                            type="button"
                            onClick={() => {
                              setFormError("");
                              setSelectedShift(shift);
                              setModal("closeShift");
                            }}
                          >
                            {t("operations:assignments.close")}
                          </button>
                        )}
                    </div>
                  </header>
                  <dl className="operations-shift-meta">
                    <div>
                      <dt>{t("operations:fields.pump")}</dt>
                      <dd>{shift.pump?.name || "—"}</dd>
                    </div>
                    <div>
                      <dt>{t("operations:fields.fuelMeter")}</dt>
                      <dd>{shift.fuelMeter?.name || "—"}</dd>
                    </div>
                    <div>
                      <dt>{t("operations:fields.product")}</dt>
                      <dd>{shift.productName || "—"}</dd>
                    </div>
                    <div>
                      <dt>{t("operations:fields.sourceTank")}</dt>
                      <dd>{shift.sourceTankName || "—"}</dd>
                    </div>
                    <div>
                      <dt>{t("operations:fields.openingIndex")}</dt>
                      <dd>{decimal(shift.openingIndex, locale)}</dd>
                    </div>
                    {shift.status === "CLOSED" && (
                      <>
                        <div>
                          <dt>{t("operations:fields.closingIndex")}</dt>
                          <dd>{decimal(shift.closingIndex, locale)}</dd>
                        </div>
                        <div>
                          <dt>{t("operations:fields.meteredVolume")}</dt>
                          <dd>{decimal(shift.meteredVolume, locale)} L</dd>
                        </div>
                      </>
                    )}
                  </dl>
                  <div className="operations-shift-subsection">
                    <h4>{t("operations:tankReturns.recorded")}</h4>
                    {shiftReturns.length ? (
                      <ul className="operations-return-list">
                        {shiftReturns.map((item) => (
                          <li key={item.id}>
                            <Droplets size={17} />
                            <span>
                              <strong>
                                {item.sourceTankName} →{" "}
                                {item.destinationTankName}
                              </strong>
                              <small>
                                {item.productName} ·{" "}
                                {dateTime(item.occurredAt, locale)}
                                {item.reason ? ` · ${item.reason}` : ""}
                              </small>
                            </span>
                            <b>{decimal(item.quantity, locale)} L</b>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="operations-muted">
                        {t("operations:tankReturns.empty")}
                      </p>
                    )}
                  </div>
                  {internalsFor(shift.id).length > 0 && (
                    <div className="operations-shift-subsection">
                      <h4>{t("operations:internal.recorded")}</h4>
                      <ul className="operations-data-list">
                        {internalsFor(shift.id).map((item) => <li key={item.id}><span><strong>{item.usageBeneficiary}</strong><small>{item.productName} · {dateTime(item.recordedAt, locale)}{item.observation ? ` · ${item.observation}` : ""}</small></span><b>{decimal(item.quantity, locale)} L · {money(item.totalAmount)}</b></li>)}
                      </ul>
                    </div>
                  )}
                  {rec && (
                    <div className="operations-reconciliation-card">
                      <h4>{t("operations:reconciliations.definitive")}</h4>
                      <dl>
                        {[
                          ["openingIndex", rec.openingIndex, ""],
                          ["closingIndex", rec.closingIndex, ""],
                          ["metered", rec.meteredVolume, "L"],
                          ["tankReturnVolume", rec.tankReturnVolume, "L"],
                          ["internalConsumptionVolume", rec.internalConsumptionVolume, "L"],
                          ["internalConsumptionAmount", rec.internalConsumptionAmount, "money"],
                          ["sold", rec.totalSoldVolume, "L"],
                          ["cash", rec.cashVolume, "L"],
                          ["cashUnitPrice", rec.cashUnitPrice, "moneyPerLiter"],
                          ["cashAmount", rec.cashAmount, "money"],
                          ["credit", rec.creditVolume, "L"],
                          [
                            "creditUnitPrice",
                            rec.creditUnitPrice,
                            "moneyPerLiter",
                          ],
                          ["creditAmount", rec.creditAmount, "money"],
                          ["turnover", rec.turnover, "money"],
                          ["variance", rec.volumeVariance, "L"],
                        ].map(([key, value, unit]) => (
                          <div key={key}>
                            <dt>{t(`operations:reconciliations.${key}`)}</dt>
                            <dd>
                              {unit === "money"
                                ? money(value)
                                : unit === "moneyPerLiter"
                                  ? `${money(value)}/L`
                                  : `${decimal(value, locale)} ${unit}`}
                            </dd>
                          </div>
                        ))}
                      </dl>
                    </div>
                  )}
                </article>
              );
            })}
          </div>
        ) : (
          <div className="operations-empty compact">
            <p>{t("operations:assignments.empty")}</p>
          </div>
        )}
      </section>

      <div className="operations-grid">
        <section className="operations-card">
          <div className="operations-section-header">
            <div>
              <h2>
                <ReceiptText size={20} />
                {t("operations:expenses.title")}
              </h2>
            </div>
            {isManager &&
              day.status === "OPEN" &&
              permissions.canCreateExpense && (
                <button
                  className="ops-button ops-button-secondary operations-manager-form-action"
                  type="button"
                  onClick={() => {
                    setExpenseForm({
                      label: "",
                      amount: "",
                      reference: "",
                      comment: "",
                    });
                    setModal("expense");
                  }}
                >
                  <Plus size={16} />
                  {t("operations:expenses.add")}
                </button>
              )}
          </div>
          {expenses.length ? (
            <ul className="operations-data-list">
              {expenses.map((item) => (
                <li key={item.id}>
                  <span>
                    <strong>{item.label}</strong>
                    <small>{item.reference || item.comment || "—"}</small>
                  </span>
                  <b>{money(item.amount)}</b>
                </li>
              ))}
            </ul>
          ) : (
            <p className="operations-muted">{t("operations:expenses.empty")}</p>
          )}
        </section>
        <section className="operations-card">
          <div className="operations-section-header">
            <div>
              <h2>
                <Gauge size={20} />
                {t("operations:gauges.title")}
              </h2>
            </div>
            {isManager &&
              day.status === "OPEN" &&
              permissions.canCreateGauge && (
                <button
                  className="ops-button ops-button-secondary operations-manager-form-action"
                  type="button"
                  onClick={openGauge}
                  disabled={loadingTanks}
                >
                  <Plus size={16} />
                  {t("operations:gauges.add")}
                </button>
              )}
          </div>
          {gauges.length ? (
            <ul className="operations-data-list">
              {gauges.map((item) => (
                <li key={item.id}>
                  <span>
                    <strong>{item.tankName}</strong>
                    <small>
                      {t("operations:gauges.theoretical", {
                        value: decimal(item.theoreticalStock, locale),
                      })}
                    </small>
                  </span>
                  <b>
                    {decimal(item.physicalStock, locale)} L
                    <small
                      className={
                        Number(item.stockVariance) < 0 ? "negative" : ""
                      }
                    >
                      {t("operations:gauges.variance", {
                        value: decimal(item.stockVariance, locale),
                      })}
                    </small>
                  </b>
                </li>
              ))}
            </ul>
          ) : (
            <p className="operations-muted">{t("operations:gauges.empty")}</p>
          )}
        </section>
      </div>

      <section className="operations-card">
        <div className="operations-section-header">
          <div>
            <h2>
              <Droplets size={20} />
              {t("operations:stock.title")}
            </h2>
            <p>{t("operations:stock.description")}</p>
          </div>
        </div>
        {(stocks.length ? stocks : rjv?.stocks)?.length ? (
          <div className="operations-stock-grid">
            {(stocks.length ? stocks : rjv.stocks).map((stock) => (
              <article key={stock.tankId}>
                <small>{stock.productName}</small>
                <strong>{stock.tankName}</strong>
                <b>
                  {decimal(
                    stock.currentStock ?? stock.theoreticalStock,
                    locale,
                  )}{" "}
                  L
                </b>
              </article>
            ))}
          </div>
        ) : (
          <p className="operations-muted">
            {t("operations:stock.unavailable")}
          </p>
        )}
      </section>
      {permissions.canViewRjv && (
        <section className="operations-report-section">
          <div className="operations-section-header">
            <div>
              <h2>{t("operations:rjv.title")}</h2>
              <Link className="ops-button ops-button-secondary" to={`${routeBase}/${id}/rjv/print`}>Imprimer le RJV</Link>
              <p>{t("operations:rjv.description")}</p>
            </div>
          </div>
          <RjvView
            report={rjv}
            locale={locale}
            language={language}
            currency={currency}
          />
        </section>
      )}

      <AppModal
        isOpen={modal === "assignment"}
        title={t("operations:assignmentForm.title")}
        headerIcon={UserPlus}
        size="sm"
        isLoading={submitting}
        closeOnEscape={!submitting}
        closeOnOverlay={!submitting}
        onClose={() => !submitting && setModal(null)}
        footer={
          <>
            <button
              className="app-modal-action app-modal-action-no"
              type="button"
              onClick={() => setModal(null)}
              disabled={submitting}
            >
              {t("common:actions.no")}
            </button>
            <button
              className="app-modal-action operations-manager-form-action"
              type="submit"
              form="assignment-form"
              disabled={
                submitting ||
                !assignmentForm.pumpAttendantId ||
                !assignmentForm.fuelMeterId
              }
            >
              {submitting
                ? t("common:actions.saving")
                : t("operations:assignments.open")}
            </button>
          </>
        }
      >
        <form
          className="operations-form"
          id="assignment-form"
          onSubmit={submitAssignment}
        >
          <label>
            <span>{t("operations:fields.pumpAttendant")}</span>
            <select
              value={assignmentForm.pumpAttendantId}
              onChange={(e) =>
                setAssignmentForm((v) => ({
                  ...v,
                  pumpAttendantId: e.target.value,
                }))
              }
            >
              {attendants.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.firstName} {item.lastName} · {item.operationalCode}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span>{t("operations:fields.fuelMeter")}</span>
            <select
              value={assignmentForm.fuelMeterId}
              onChange={(e) =>
                setAssignmentForm((v) => ({
                  ...v,
                  fuelMeterId: e.target.value,
                }))
              }
            >
              {meters.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name} · {item.pump?.name} ·{" "}
                  {decimal(item.currentIndex, locale)}
                </option>
              ))}
            </select>
          </label>
          {formError && <p className="operations-form-error" role="alert">{formError}</p>}
        </form>
      </AppModal>
      <AppModal
        isOpen={modal === "expense"}
        title={t("operations:expenseForm.title")}
        headerIcon={ReceiptText}
        size="sm"
        isLoading={submitting}
        closeOnEscape={!submitting}
        closeOnOverlay={!submitting}
        onClose={() => !submitting && setModal(null)}
        footer={
          <>
            <button
              className="app-modal-action app-modal-action-no"
              type="button"
              onClick={() => setModal(null)}
              disabled={submitting}
            >
              {t("common:actions.no")}
            </button>
            <button
              className="app-modal-action operations-manager-form-action"
              type="submit"
              form="expense-form"
              disabled={submitting || !expenseForm.label || !expenseForm.amount}
            >
              {submitting
                ? t("common:actions.saving")
                : t("operations:expenses.add")}
            </button>
          </>
        }
      >
        <form
          className="operations-form"
          id="expense-form"
          onSubmit={submitExpense}
        >
          <ExpenseFormFields
            expenseForm={expenseForm}
            setExpenseForm={setExpenseForm}
            currency={currency}
            formError={formError}
            t={t}
          />
        </form>
      </AppModal>
      <AppModal
        isOpen={modal === "gauge"}
        title={t("operations:gaugeForm.title")}
        description={t("operations:gaugeForm.description")}
        headerIcon={Gauge}
        size="sm"
        isLoading={submitting}
        closeOnEscape={!submitting}
        closeOnOverlay={!submitting}
        onClose={() => !submitting && setModal(null)}
        footer={
          <>
            <button
              className="app-modal-action app-modal-action-no"
              type="button"
              onClick={() => setModal(null)}
              disabled={submitting}
            >
              {t("common:actions.no")}
            </button>
            <button
              className="app-modal-action operations-manager-form-action"
              type="submit"
              form="gauge-form"
              disabled={
                submitting ||
                !gaugeForm.tankId ||
                gaugeForm.physicalStock === ""
              }
            >
              {submitting
                ? t("common:actions.saving")
                : t("operations:gauges.save")}
            </button>
          </>
        }
      >
        <form
          className="operations-form"
          id="gauge-form"
          onSubmit={submitGauge}
        >
          <label>
            <span>{t("operations:fields.tank")}</span>
            <select
              value={gaugeForm.tankId}
              onChange={(e) =>
                setGaugeForm((v) => ({
                  ...v,
                  tankId: e.target.value,
                  physicalStock: "",
                }))
              }
            >
              {availableTanks.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name} · {item.productName}
                </option>
              ))}
            </select>
          </label>
          {selectedGaugeTank && (
            <div className="operations-gauge-summary">
              <span>{t("operations:fields.product")}</span>
              <strong>{selectedGaugeTank.productName}</strong>
              <span>{t("operations:gauges.ledgerStock")}</span>
              <b>
                {selectedGaugeStock
                  ? `${decimal(selectedGaugeStock.currentStock, locale)} L`
                  : t("operations:stock.unavailable")}
              </b>
            </div>
          )}
          <label>
            <span>{t("operations:gauges.measuredQuantity")}</span>
            <div className="operations-input-unit">
              <input
                type="number"
                min="0"
                step="0.001"
                value={gaugeForm.physicalStock}
                onChange={(e) =>
                  setGaugeForm((v) => ({ ...v, physicalStock: e.target.value }))
                }
              />
              <b>L</b>
            </div>
          </label>
          <div
            className={`operations-gauge-variance ${gaugeVariance < 0 ? "negative" : ""}`}
          >
            <span>{t("operations:gauges.calculatedVariance")}</span>
            <strong>
              {gaugeVariance === null ? "—" : decimal(gaugeVariance, locale)} L
            </strong>
          </div>
          <label>
            <span>{t("operations:fields.comment")}</span>
            <textarea
              value={gaugeForm.comment}
              onChange={(e) =>
                setGaugeForm((v) => ({ ...v, comment: e.target.value }))
              }
            />
          </label>
          {formError && <p className="operations-form-error" role="alert">{formError}</p>}
        </form>
      </AppModal>
      {modal === "internal" && <InternalConsumptionModal shift={selectedShift} isLoading={submitting} errorMessage={formError} onClose={() => !submitting && setModal(null)} onSubmit={submitInternal} />}
      {modal === "tankReturn" && (
        <TankReturnModal
          shift={selectedShift}
          tanks={tanks}
          isLoading={submitting}
          errorMessage={formError}
          onClose={() => !submitting && setModal(null)}
          onSubmit={(payload) =>
            runMutation(
              () => createTankReturn(selectedShift.id, payload),
              "operations:feedback.tankReturnAdded",
            )
          }
        />
      )}
      {modal === "closeShift" && (
        <ShiftClosureModal
          shift={selectedShift}
          returns={returnsByShift.get(selectedShift?.id) || []}
          internals={internalsFor(selectedShift?.id)}
          locale={locale}
          isLoading={submitting}
          errorMessage={formError}
          onClose={() => !submitting && setModal(null)}
          onConfirm={(payload) =>
            runMutation(
              () => closeShiftAssignment(selectedShift.id, payload),
              "operations:feedback.assignmentClosed",
            )
          }
        />
      )}
      {modal === "closeDay" && <DayClosureModal
        organization={organization}
        report={rjv}
        openAssignments={openAssignments}
        reconciliationsCount={reconciliations.length}
        gaugesCount={gauges.length}
        language={language}
        isLoading={submitting}
        errorMessage={formError}
        onConfirm={(payload) =>
          runMutation(
            () => closeOperationalDay(id, payload),
            "operations:feedback.dayClosed",
          )
        }
        onClose={() => !submitting && setModal(null)}
      />}
    </section>
  );
}
