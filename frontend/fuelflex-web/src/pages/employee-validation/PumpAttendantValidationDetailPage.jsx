import {
  AlertCircle,
  ArrowLeft,
  Building2,
  CalendarClock,
  CheckCircle2,
  FileCheck2,
  LoaderCircle,
  Pencil,
  RefreshCw,
  Send,
  UserRoundCheck,
  UsersRound,
  XCircle,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";

import ManagerLayout from "../../components/layout/ManagerLayout";
import SupervisorLayout from "../../components/layout/SupervisorLayout";
import ConfirmationModal from "../../components/modal/ConfirmationModal";
import PumpAttendantDraftModal from "../../features/employee-validation/components/PumpAttendantDraftModal";
import ValidationCommentModal from "../../features/employee-validation/components/ValidationCommentModal";
import { formatDate, formatDateTime } from "../../i18n/formatters";
import { getPumpAttendantValidationPermissions } from "../../services/auth/permissionService";
import { getManagerStations } from "../../services/operations/operationalService";
import {
  approvePumpAttendantValidationRequest,
  cancelPumpAttendantValidationRequest,
  getPumpAttendantValidationRequest,
  rejectPumpAttendantValidationRequest,
  returnPumpAttendantValidationRequest,
  submitPumpAttendantValidationRequest,
} from "../../services/employee/pumpAttendantValidationService";
import "./PumpAttendantValidation.css";

function PumpAttendantValidationDetailPage({ role }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation([
    "pumpAttendantValidation",
    "common",
  ]);
  const permissions = getPumpAttendantValidationPermissions();
  const [request, setRequest] = useState(null);
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [reload, setReload] = useState(0);
  const [confirmation, setConfirmation] = useState(null);
  const [commentAction, setCommentAction] = useState(null);
  const [editingCandidate, setEditingCandidate] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState("");

  const load = useCallback(async (signal) => {
    void reload;
    setLoading(true);
    setError("");
    try {
      const [loadedRequest, accessibleStations] = await Promise.all([
        getPumpAttendantValidationRequest(role, id, { signal }),
        role === "manager" ? getManagerStations({ signal }) : [],
      ]);
      setRequest(loadedRequest);
      setStations(Array.isArray(accessibleStations) ? accessibleStations : []);
    } catch (requestError) {
      if (requestError?.name !== "AbortError") {
        setError(requestError?.message
          || t("pumpAttendantValidation:errors.loadDetail"));
      }
    } finally {
      if (!signal.aborted) setLoading(false);
    }
  }, [id, reload, role, t]);

  useEffect(() => {
    const controller = new AbortController();
    Promise.resolve().then(() => load(controller.signal));
    return () => controller.abort();
  }, [load]);

  const notifyRefresh = () => {
    window.dispatchEvent(new CustomEvent("fuelflex:notifications-refresh"));
  };

  const runAction = async (action, comment) => {
    setActionLoading(true);
    setActionError("");
    try {
      let updated;
      if (action === "submit") {
        updated = await submitPumpAttendantValidationRequest(id);
      } else if (action === "cancel") {
        updated = await cancelPumpAttendantValidationRequest(id, comment);
      } else if (action === "approve") {
        updated = await approvePumpAttendantValidationRequest(id);
      } else if (action === "return") {
        updated = await returnPumpAttendantValidationRequest(id, comment);
      } else if (action === "reject") {
        updated = await rejectPumpAttendantValidationRequest(id, comment);
      }
      setRequest(updated);
      setConfirmation(null);
      setCommentAction(null);
      setSuccess(t(`pumpAttendantValidation:feedback.${action}`));
      notifyRefresh();
    } catch (requestError) {
      setActionError(requestError?.message
        || t("pumpAttendantValidation:errors.action"));
    } finally {
      setActionLoading(false);
    }
  };

  const backPath = role === "manager"
    ? "/gerant/pompistes"
    : "/superviseur/validations-pompistes";
  const Layout = role === "manager" ? ManagerLayout : SupervisorLayout;
  const date = (value) => value
    ? formatDateTime(value, { language: i18n.resolvedLanguage })
    : "—";
  const dateOnly = (value) => value
    ? formatDate([value, "T00:00:00"].join(""), { language: i18n.resolvedLanguage, dateStyle: "medium" })
    : "—";
  const editable = role === "manager"
    && ["DRAFT", "RETURNED_FOR_CORRECTION"].includes(request?.status);
  const pending = request?.status === "PENDING_SUPERVISOR_APPROVAL";

  return (
    <Layout>
      <main className="validation-page validation-detail-page">
        <button
          type="button"
          className="validation-back"
          onClick={() => navigate(backPath)}
        >
          <ArrowLeft size={17} />
          {t("pumpAttendantValidation:detail.back")}
        </button>

        {success && (
          <div className="validation-alert success" role="status">
            <CheckCircle2 size={18} />
            {success}
          </div>
        )}
        {error && (
          <div className="validation-alert error" role="alert">
            <AlertCircle size={18} />
            <span>{error}</span>
            <button type="button" onClick={() => setReload((value) => value + 1)}>
              <RefreshCw size={15} />
              {t("common:actions.retry")}
            </button>
          </div>
        )}

        {loading ? (
          <div className="validation-state">
            <LoaderCircle className="validation-spin" size={28} />
            {t("pumpAttendantValidation:feedback.loading")}
          </div>
        ) : request && (
          <>
            <header className="validation-detail-header">
              <div className="validation-detail-icon"><FileCheck2 size={28} /></div>
              <div>
                <span className={`validation-status ${request.status}`}>
                  {t(`pumpAttendantValidation:requestStatus.${request.status}`)}
                </span>
                <h1>{request.requestNumber}</h1>
                <p>{t("pumpAttendantValidation:detail.subtitle", {
                  station: request.station?.name,
                  count: request.pumpAttendants?.length || 0,
                })}</p>
              </div>
              <div className="validation-detail-actions">
                {role === "manager" && editable && permissions.canSubmit && (
                  <>
                    <button
                      type="button"
                      className="validation-secondary danger"
                      onClick={() => {
                        setActionError("");
                        setCommentAction("cancel");
                      }}
                    >
                      <XCircle size={17} />
                      {t("pumpAttendantValidation:actions.cancelDocument")}
                    </button>
                    <button
                      type="button"
                      className="validation-primary"
                      onClick={() => {
                        setActionError("");
                        setConfirmation("submit");
                      }}
                    >
                      <Send size={17} />
                      {t(request.status === "RETURNED_FOR_CORRECTION"
                        ? "pumpAttendantValidation:actions.resubmit"
                        : "pumpAttendantValidation:actions.submit")}
                    </button>
                  </>
                )}
                {role === "supervisor" && pending && permissions.canReview && (
                  <>
                    <button
                      type="button"
                      className="validation-secondary danger"
                      onClick={() => {
                        setActionError("");
                        setCommentAction("reject");
                      }}
                    >
                      <XCircle size={17} />
                      {t("pumpAttendantValidation:actions.reject")}
                    </button>
                    <button
                      type="button"
                      className="validation-secondary warning"
                      onClick={() => {
                        setActionError("");
                        setCommentAction("return");
                      }}
                    >
                      <RefreshCw size={17} />
                      {t("pumpAttendantValidation:actions.return")}
                    </button>
                    <button
                      type="button"
                      className="validation-primary"
                      onClick={() => {
                        setActionError("");
                        setConfirmation("approve");
                      }}
                    >
                      <UserRoundCheck size={17} />
                      {t("pumpAttendantValidation:actions.approve")}
                    </button>
                  </>
                )}
              </div>
            </header>

            <section className="validation-summary-grid">
              <article>
                <Building2 size={20} />
                <span>{t("pumpAttendantValidation:fields.station")}</span>
                <strong>{request.station?.name || "—"}</strong>
                <small>{request.station?.code || ""}</small>
              </article>
              <article>
                <UsersRound size={20} />
                <span>{t("pumpAttendantValidation:fields.createdBy")}</span>
                <strong>{request.createdBy
                  ? `${request.createdBy.firstName} ${request.createdBy.lastName}`
                  : "—"}</strong>
              </article>
              <article>
                <CalendarClock size={20} />
                <span>{t("pumpAttendantValidation:fields.submittedAt")}</span>
                <strong>{date(request.submittedAt)}</strong>
              </article>
            </section>

            {request.reviewComment && (
              <section className="validation-review-note">
                <AlertCircle size={20} />
                <div>
                  <strong>{t("pumpAttendantValidation:fields.reviewComment")}</strong>
                  <p>{request.reviewComment}</p>
                  <small>{request.reviewedBy
                    ? `${request.reviewedBy.firstName} ${request.reviewedBy.lastName} · ${date(request.reviewedAt)}`
                    : date(request.reviewedAt)}</small>
                </div>
              </section>
            )}

            <section className="validation-panel">
              <header>
                <div>
                  <h2>{t("pumpAttendantValidation:detail.pumpAttendants")}</h2>
                  <p>{t("pumpAttendantValidation:detail.snapshotNotice")}</p>
                </div>
              </header>
              <div className="validation-table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>{t("pumpAttendantValidation:fields.pumpAttendant")}</th>
                      <th>{t("pumpAttendantValidation:fields.gender")}</th>
                      <th>{t("pumpAttendantValidation:fields.birth")}</th>
                      <th>{t("pumpAttendantValidation:fields.address")}</th>
                      <th>{t("pumpAttendantValidation:fields.operationalCode")}</th>
                      <th>{t("pumpAttendantValidation:fields.station")}</th>
                      <th>{t("pumpAttendantValidation:fields.phone")}</th>
                      <th>{t("pumpAttendantValidation:fields.status")}</th>
                      {editable && <th><span className="sr-only">Actions</span></th>}
                    </tr>
                  </thead>
                  <tbody>
                    {request.pumpAttendants.map((candidate) => (
                      <tr key={candidate.id}>
                        <td>
                          <strong>{candidate.lastName} {candidate.postName} {candidate.firstName}</strong>
                          <small>{candidate.email}</small>
                        </td>
                        <td>{candidate.gender ? t("pumpAttendantValidation:gender." + candidate.gender) : "—"}</td>
                        <td>{candidate.birthPlace || "—"}<small>{dateOnly(candidate.birthDate)}</small></td>
                        <td>{candidate.address || "—"}</td>
                        <td>{candidate.operationalCode}</td>
                        <td>{candidate.station?.name || request.station?.name || "—"}</td>
                        <td>{candidate.phoneNumber}</td>
                        <td>
                          <span className={`validation-status ${candidate.validationStatus}`}>
                            {t(`pumpAttendantValidation:candidateStatus.${candidate.validationStatus}`)}
                          </span>
                        </td>
                        {editable && (
                          <td>
                            <button
                              type="button"
                              className="validation-icon-button"
                              title={t("common:actions.edit")}
                              onClick={() => setEditingCandidate({
                                ...candidate,
                                id: candidate.pumpAttendantId,
                                station: candidate.station || request.station,
                              })}
                            >
                              <Pencil size={16} />
                            </button>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>

            <section className="validation-panel validation-history">
              <header>
                <div>
                  <h2>{t("pumpAttendantValidation:detail.history")}</h2>
                  <p>{t("pumpAttendantValidation:detail.historyDescription")}</p>
                </div>
              </header>
              {request.history.length === 0 ? (
                <div className="validation-state empty">
                  {t("pumpAttendantValidation:detail.noHistory")}
                </div>
              ) : (
                <ol>
                  {request.history.map((event) => (
                    <li key={event.id}>
                      <span />
                      <div>
                        <strong>{t(`pumpAttendantValidation:history.${event.action}`)}</strong>
                        <p>{event.performedBy
                          ? `${event.performedBy.firstName} ${event.performedBy.lastName}`
                          : "—"} · {date(event.performedAt)}</p>
                        {event.comment && <small>{event.comment}</small>}
                      </div>
                    </li>
                  ))}
                </ol>
              )}
            </section>
          </>
        )}
      </main>

      {editingCandidate && (
        <PumpAttendantDraftModal
          employee={editingCandidate}
          stations={stations}
          onClose={() => setEditingCandidate(null)}
          onSaved={() => {
            setEditingCandidate(null);
            setSuccess(t("pumpAttendantValidation:feedback.candidateUpdated"));
            setReload((value) => value + 1);
          }}
        />
      )}

      <ConfirmationModal
        isOpen={Boolean(confirmation)}
        variant={confirmation === "approve" ? "default" : "warning"}
        title={t(`pumpAttendantValidation:confirm.${confirmation || "submit"}Title`)}
        description={t(`pumpAttendantValidation:confirm.${confirmation || "submit"}Description`)}
        confirmLabel={t(`pumpAttendantValidation:actions.${confirmation || "submit"}`)}
        isLoading={actionLoading}
        errorMessage={actionError}
        onClose={() => setConfirmation(null)}
        onConfirm={() => runAction(confirmation)}
      />

      <ValidationCommentModal
        key={commentAction || "closed"}
        isOpen={Boolean(commentAction)}
        variant={commentAction === "reject" || commentAction === "cancel"
          ? "danger" : "warning"}
        title={t(`pumpAttendantValidation:confirm.${commentAction || "return"}Title`)}
        description={t(`pumpAttendantValidation:confirm.${commentAction || "return"}Description`)}
        confirmLabel={t(`pumpAttendantValidation:actions.${commentAction || "return"}`)}
        isLoading={actionLoading}
        errorMessage={actionError}
        onClose={() => setCommentAction(null)}
        onConfirm={(comment) => runAction(commentAction, comment)}
      />
    </Layout>
  );
}

export default PumpAttendantValidationDetailPage;
