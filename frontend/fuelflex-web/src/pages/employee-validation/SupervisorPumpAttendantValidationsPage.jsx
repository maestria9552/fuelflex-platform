import {
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  Eye,
  FileCheck2,
  LoaderCircle,
  RefreshCw,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import { formatDateTime } from "../../i18n/formatters";
import { getPumpAttendantValidationRequests } from "../../services/employee/pumpAttendantValidationService";
import "./PumpAttendantValidation.css";

const PAGE_SIZE = 10;
const STATUSES = [
  "DRAFT",
  "PENDING_SUPERVISOR_APPROVAL",
  "RETURNED_FOR_CORRECTION",
  "VALIDATED",
  "REJECTED",
  "CANCELLED",
];
const EMPTY_PAGE = {
  content: [],
  page: 0,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

function SupervisorPumpAttendantValidationsPage() {
  const { t, i18n } = useTranslation([
    "pumpAttendantValidation",
    "common",
  ]);
  const navigate = useNavigate();
  const [requests, setRequests] = useState(EMPTY_PAGE);
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("PENDING_SUPERVISOR_APPROVAL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);

  const load = useCallback(async (signal) => {
    void reload;
    setLoading(true);
    setError("");
    try {
      const response = await getPumpAttendantValidationRequests("supervisor", {
        page,
        size: PAGE_SIZE,
        status,
      }, { signal });
      setRequests(response || EMPTY_PAGE);
    } catch (requestError) {
      if (requestError?.name !== "AbortError") {
        setError(requestError?.message
          || t("pumpAttendantValidation:errors.loadRequests"));
      }
    } finally {
      if (!signal.aborted) setLoading(false);
    }
  }, [page, reload, status, t]);

  useEffect(() => {
    const controller = new AbortController();
    Promise.resolve().then(() => load(controller.signal));
    return () => controller.abort();
  }, [load]);

  const date = (value) => value
    ? formatDateTime(value, { language: i18n.resolvedLanguage })
    : "—";

  return (
    <SupervisorLayout>
      <main className="validation-page">
        <header className="validation-page-header">
          <div>
            <span>{t("pumpAttendantValidation:supervisor.eyebrow")}</span>
            <h1>{t("pumpAttendantValidation:supervisor.title")}</h1>
            <p>{t("pumpAttendantValidation:supervisor.description")}</p>
          </div>
        </header>

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

        <section className="validation-panel">
          <header>
            <div>
              <h2>{t("pumpAttendantValidation:supervisor.requestsTitle")}</h2>
              <p>{t("pumpAttendantValidation:supervisor.requestsDescription")}</p>
            </div>
            <label className="validation-inline-filter">
              <span>{t("pumpAttendantValidation:fields.status")}</span>
              <select
                value={status}
                onChange={(event) => {
                  setPage(0);
                  setStatus(event.target.value);
                }}
              >
                <option value="">{t("pumpAttendantValidation:filters.all")}</option>
                {STATUSES.map((option) => (
                  <option key={option} value={option}>
                    {t(`pumpAttendantValidation:requestStatus.${option}`)}
                  </option>
                ))}
              </select>
            </label>
          </header>

          {loading ? (
            <div className="validation-state">
              <LoaderCircle className="validation-spin" size={28} />
              {t("pumpAttendantValidation:feedback.loading")}
            </div>
          ) : requests.content.length === 0 ? (
            <div className="validation-state empty">
              <FileCheck2 size={34} />
              <strong>{t("pumpAttendantValidation:supervisor.noRequests")}</strong>
              <p>{t("pumpAttendantValidation:supervisor.noRequestsDescription")}</p>
            </div>
          ) : (
            <div className="validation-table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>{t("pumpAttendantValidation:fields.document")}</th>
                    <th>{t("pumpAttendantValidation:fields.station")}</th>
                    <th>{t("pumpAttendantValidation:fields.createdBy")}</th>
                    <th>{t("pumpAttendantValidation:fields.pumpAttendants")}</th>
                    <th>{t("pumpAttendantValidation:fields.status")}</th>
                    <th>{t("pumpAttendantValidation:fields.submittedAt")}</th>
                    <th><span className="sr-only">Actions</span></th>
                  </tr>
                </thead>
                <tbody>
                  {requests.content.map((request) => (
                    <tr key={request.id}>
                      <td><strong>{request.requestNumber}</strong></td>
                      <td>{request.station?.name || "—"}</td>
                      <td>{request.createdBy
                        ? `${request.createdBy.firstName} ${request.createdBy.lastName}`
                        : "—"}</td>
                      <td>{request.pumpAttendants?.length || 0}</td>
                      <td>
                        <span className={`validation-status ${request.status}`}>
                          {t(`pumpAttendantValidation:requestStatus.${request.status}`)}
                        </span>
                      </td>
                      <td>{date(request.submittedAt || request.createdAt)}</td>
                      <td>
                        <button
                          type="button"
                          className="validation-icon-button"
                          title={t("common:actions.view")}
                          onClick={() => navigate(`/superviseur/validations-pompistes/${request.id}`)}
                        >
                          <Eye size={16} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {requests.totalPages > 1 && (
            <nav className="validation-pagination">
              <span>{t("pumpAttendantValidation:pagination.total", {
                count: requests.totalElements,
              })}</span>
              <div>
                <button
                  type="button"
                  disabled={requests.first}
                  onClick={() => setPage((value) => value - 1)}
                >
                  <ChevronLeft size={17} />
                </button>
                <span>{t("pumpAttendantValidation:pagination.page", {
                  current: requests.page + 1,
                  total: requests.totalPages,
                })}</span>
                <button
                  type="button"
                  disabled={requests.last}
                  onClick={() => setPage((value) => value + 1)}
                >
                  <ChevronRight size={17} />
                </button>
              </div>
            </nav>
          )}
        </section>
      </main>
    </SupervisorLayout>
  );
}

export default SupervisorPumpAttendantValidationsPage;
