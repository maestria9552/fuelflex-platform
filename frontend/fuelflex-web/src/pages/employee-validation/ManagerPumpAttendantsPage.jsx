import {
  AlertCircle,
  BadgeCheck,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Eye,
  FilePlus2,
  LoaderCircle,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  UsersRound,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

import ManagerLayout from "../../components/layout/ManagerLayout";
import PumpAttendantDraftModal from "../../features/employee-validation/components/PumpAttendantDraftModal";
import { formatDateTime } from "../../i18n/formatters";
import { getPumpAttendantValidationPermissions } from "../../services/auth/permissionService";
import {
  createPumpAttendantValidationRequest,
  getManagerPumpAttendants,
  getPumpAttendantValidationRequests,
} from "../../services/employee/pumpAttendantValidationService";
import { getManagerStations } from "../../services/operations/operationalService";
import {
  isPreparablePumpAttendant,
  onlyPreparablePumpAttendants,
  PREPARABLE_PUMP_ATTENDANT_STATUS,
} from "./pumpAttendantPreparation";
import "./PumpAttendantValidation.css";

const PAGE_SIZE = 10;
const REQUEST_STATUSES = [
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

function ManagerPumpAttendantsPage() {
  const { t, i18n } = useTranslation([
    "pumpAttendantValidation",
    "common",
  ]);
  const navigate = useNavigate();
  const permissions = getPumpAttendantValidationPermissions();
  const [candidates, setCandidates] = useState(EMPTY_PAGE);
  const [requests, setRequests] = useState(EMPTY_PAGE);
  const [stations, setStations] = useState([]);
  const [candidatePage, setCandidatePage] = useState(0);
  const [requestPage, setRequestPage] = useState(0);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [requestStatus, setRequestStatus] = useState("");
  const [selectedIds, setSelectedIds] = useState([]);
  const [stationId, setStationId] = useState("");
  const [formEmployee, setFormEmployee] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreatingRequest, setIsCreatingRequest] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      setCandidatePage(0);
      setSearch(searchInput.trim());
    }, 350);
    return () => window.clearTimeout(timer);
  }, [searchInput]);

  const load = useCallback(async (signal) => {
    void reload;
    setIsLoading(true);
    setError("");
    try {
      const [candidatePageData, requestPageData, accessibleStations] =
        await Promise.all([
          getManagerPumpAttendants({
            page: candidatePage,
            size: PAGE_SIZE,
            search,
            status: PREPARABLE_PUMP_ATTENDANT_STATUS,
          }, { signal }),
          getPumpAttendantValidationRequests("manager", {
            page: requestPage,
            size: PAGE_SIZE,
            status: requestStatus,
          }, { signal }),
          getManagerStations({ signal }),
        ]);
      setCandidates(onlyPreparablePumpAttendants(candidatePageData || EMPTY_PAGE));
      setRequests(requestPageData || EMPTY_PAGE);
      setStations(Array.isArray(accessibleStations) ? accessibleStations : []);
      setStationId((current) => current
        || accessibleStations?.[0]?.id
        || "");
    } catch (requestError) {
      if (requestError?.name !== "AbortError") {
        setError(requestError?.message
          || t("pumpAttendantValidation:errors.load"));
      }
    } finally {
      if (!signal.aborted) setIsLoading(false);
    }
  }, [
    candidatePage,
    reload,
    requestPage,
    requestStatus,
    search,
    t,
  ]);

  useEffect(() => {
    const controller = new AbortController();
    Promise.resolve().then(() => load(controller.signal));
    return () => controller.abort();
  }, [load]);

  const toggleCandidate = (candidate) => {
    if (candidate.validationRequestId
      || !isPreparablePumpAttendant(candidate)
      || !candidate.station?.id) return;
    setSelectedIds((current) => {
      if (current.includes(candidate.id)) {
        return current.filter((id) => id !== candidate.id);
      }
      if (current.length > 0 && stationId !== candidate.station.id) {
        setError(t("pumpAttendantValidation:errors.mixedStations"));
        return current;
      }
      setStationId(candidate.station.id);
      return [...current, candidate.id];
    });
  };

  const createRequest = async () => {
    setIsCreatingRequest(true);
    setError("");
    try {
      const created = await createPumpAttendantValidationRequest({
        stationId,
        pumpAttendantIds: selectedIds,
      });
      setSelectedIds([]);
      setSuccess(t("pumpAttendantValidation:feedback.requestCreated"));
      navigate(`/gerant/validations-pompistes/${created.id}`);
    } catch (requestError) {
      setError(requestError?.message
        || t("pumpAttendantValidation:errors.createRequest"));
    } finally {
      setIsCreatingRequest(false);
    }
  };

  const candidateCanEdit = (candidate) => [
    "PREPARATION",
    "RETURNED_FOR_CORRECTION",
  ].includes(candidate.validationStatus);
  const date = (value) => value
    ? formatDateTime(value, { language: i18n.resolvedLanguage })
    : "—";

  return (
    <ManagerLayout>
      <main className="validation-page validation-manager-page">
        <header className="validation-page-header">
          <div>
            <h1>{t("pumpAttendantValidation:manager.title")}</h1>
            <p>{t("pumpAttendantValidation:manager.description")}</p>
          </div>
          {permissions.canPrepare && (
            <button
              type="button"
              className="validation-primary"
              onClick={() => {
                setFormEmployee(null);
                setFormOpen(true);
              }}
            >
              <Plus size={17} />
              {t("pumpAttendantValidation:manager.newCandidate")}
            </button>
          )}
        </header>

        {success && (
          <div className="validation-alert success" role="status">
            <CheckCircle2 size={18} />
            <span>{success}</span>
            <button type="button" onClick={() => setSuccess("")}>×</button>
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

        <section className="validation-panel validation-candidates-panel">
          <header>
            <div>
              <h2>{t("pumpAttendantValidation:manager.candidatesTitle")}</h2>
              <p>{t("pumpAttendantValidation:manager.candidatesDescription")}</p>
            </div>
            <span className="validation-count" aria-label={t("pumpAttendantValidation:pagination.total", {
              count: candidates.totalElements,
            })}>
              {candidates.totalElements}
            </span>
          </header>
          <div className="validation-toolbar">
            <label className="validation-search">
              <Search size={17} />
              <input
                type="search"
                value={searchInput}
                onChange={(event) => setSearchInput(event.target.value)}
                placeholder={t("pumpAttendantValidation:filters.search")}
              />
            </label>
          </div>

          {isLoading ? (
            <div className="validation-state">
              <LoaderCircle className="validation-spin" size={27} />
              {t("pumpAttendantValidation:feedback.loading")}
            </div>
          ) : candidates.content.length === 0 ? (
            <div className="validation-state empty">
              <span className="validation-empty-icon">
                <UsersRound size={29} />
                <BadgeCheck size={16} />
              </span>
              <strong>{t("pumpAttendantValidation:manager.noCandidates")}</strong>
              <p>{t("pumpAttendantValidation:manager.noCandidatesDescription")}</p>
            </div>
          ) : (
            <div className="validation-table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>{t("pumpAttendantValidation:fields.select")}</th>
                    <th>{t("pumpAttendantValidation:fields.pumpAttendant")}</th>
                    <th>{t("pumpAttendantValidation:fields.operationalCode")}</th>
                    <th>{t("pumpAttendantValidation:fields.station")}</th>
                    <th>{t("pumpAttendantValidation:fields.status")}</th>
                    <th>{t("pumpAttendantValidation:fields.document")}</th>
                    <th><span className="sr-only">Actions</span></th>
                  </tr>
                </thead>
                <tbody>
                  {candidates.content.map((candidate) => {
                    const selectable = isPreparablePumpAttendant(candidate)
                      && !candidate.validationRequestId
                      && Boolean(candidate.station?.id)
                      && (selectedIds.length === 0
                        || candidate.station.id === stationId
                        || selectedIds.includes(candidate.id));
                    return (
                      <tr
                        key={candidate.id}
                        className={selectedIds.includes(candidate.id) ? "is-selected" : undefined}
                      >
                        <td>
                          <input
                            type="checkbox"
                            checked={selectedIds.includes(candidate.id)}
                            disabled={!selectable}
                            onChange={() => toggleCandidate(candidate)}
                            aria-label={t("pumpAttendantValidation:fields.selectCandidate", {
                              name: `${candidate.firstName} ${candidate.lastName}`,
                            })}
                          />
                        </td>
                        <td>
                          <strong>{candidate.lastName} {candidate.postName} {candidate.firstName}</strong>
                          <small>{candidate.email} · {candidate.phoneNumber}</small>
                        </td>
                        <td>{candidate.operationalCode}</td>
                        <td>{candidate.station?.name || "—"}</td>
                        <td>
                          <span className={`validation-status ${candidate.validationStatus}`}>
                            {t(`pumpAttendantValidation:candidateStatus.${candidate.validationStatus}`)}
                          </span>
                        </td>
                        <td>
                          {candidate.validationRequestId ? (
                            <button
                              type="button"
                              className="validation-link"
                              onClick={() => navigate(`/gerant/validations-pompistes/${candidate.validationRequestId}`)}
                            >
                              {candidate.validationRequestNumber}
                            </button>
                          ) : "—"}
                        </td>
                        <td>
                          {permissions.canPrepare && candidateCanEdit(candidate) && (
                            <button
                              type="button"
                              className="validation-icon-button"
                              title={t("common:actions.edit")}
                              onClick={() => {
                                setFormEmployee(candidate);
                                setFormOpen(true);
                              }}
                            >
                              <Pencil size={16} />
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          <Pagination
            data={candidates}
            onPrevious={() => setCandidatePage((value) => value - 1)}
            onNext={() => setCandidatePage((value) => value + 1)}
            t={t}
          />

          {permissions.canCreateRequest && (
            <div className="validation-document-builder">
              <div>
                <FilePlus2 size={22} />
                <div>
                  <strong>{t("pumpAttendantValidation:manager.buildDocument")}</strong>
                  <span>{t("pumpAttendantValidation:manager.selectedCount", {
                    count: selectedIds.length,
                  })}</span>
                </div>
              </div>
              <label>
                <span>{t("pumpAttendantValidation:fields.station")}</span>
                <select
                  value={stationId}
                  onChange={(event) => setStationId(event.target.value)}
                  disabled={selectedIds.length > 0}
                >
                  {stations.map((stationOption) => (
                    <option key={stationOption.id} value={stationOption.id}>
                      {stationOption.name}
                    </option>
                  ))}
                </select>
              </label>
              <button
                type="button"
                className="validation-primary"
                disabled={isCreatingRequest || !stationId || selectedIds.length === 0}
                onClick={createRequest}
              >
                {isCreatingRequest
                  ? <LoaderCircle className="validation-spin" size={17} />
                  : <FilePlus2 size={17} />}
                {t("pumpAttendantValidation:actions.createDocument")}
              </button>
            </div>
          )}
        </section>

        <section className="validation-panel validation-requests-panel">
          <header>
            <div>
              <h2>{t("pumpAttendantValidation:manager.requestsTitle")}</h2>
              <p>{t("pumpAttendantValidation:manager.requestsDescription")}</p>
            </div>
            <label className="validation-inline-filter">
              <span>{t("pumpAttendantValidation:fields.status")}</span>
              <select
                value={requestStatus}
                onChange={(event) => {
                  setRequestPage(0);
                  setRequestStatus(event.target.value);
                }}
              >
                <option value="">{t("pumpAttendantValidation:filters.all")}</option>
                {REQUEST_STATUSES.map((status) => (
                  <option key={status} value={status}>
                    {t(`pumpAttendantValidation:requestStatus.${status}`)}
                  </option>
                ))}
              </select>
            </label>
          </header>
          {!isLoading && requests.content.length === 0 ? (
            <div className="validation-state empty">
              <FilePlus2 size={30} />
              <strong>{t("pumpAttendantValidation:manager.noRequests")}</strong>
            </div>
          ) : !isLoading && (
            <div className="validation-table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>{t("pumpAttendantValidation:fields.document")}</th>
                    <th>{t("pumpAttendantValidation:fields.station")}</th>
                    <th>{t("pumpAttendantValidation:fields.pumpAttendants")}</th>
                    <th>{t("pumpAttendantValidation:fields.status")}</th>
                    <th>{t("pumpAttendantValidation:fields.createdAt")}</th>
                    <th><span className="sr-only">Actions</span></th>
                  </tr>
                </thead>
                <tbody>
                  {requests.content.map((request) => (
                    <tr key={request.id}>
                      <td><strong>{request.requestNumber}</strong></td>
                      <td>{request.station?.name || "—"}</td>
                      <td>{request.pumpAttendants?.length || 0}</td>
                      <td>
                        <span className={`validation-status ${request.status}`}>
                          {t(`pumpAttendantValidation:requestStatus.${request.status}`)}
                        </span>
                      </td>
                      <td>{date(request.createdAt)}</td>
                      <td>
                        <button
                          type="button"
                          className="validation-icon-button"
                          onClick={() => navigate(`/gerant/validations-pompistes/${request.id}`)}
                          title={t("common:actions.view")}
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
          <Pagination
            data={requests}
            onPrevious={() => setRequestPage((value) => value - 1)}
            onNext={() => setRequestPage((value) => value + 1)}
            t={t}
          />
        </section>
      </main>

      {formOpen && (
        <PumpAttendantDraftModal
          employee={formEmployee}
          stations={stations}
          onClose={() => setFormOpen(false)}
          onSaved={(_, editing) => {
            setFormOpen(false);
            setSuccess(t(editing
              ? "pumpAttendantValidation:feedback.candidateUpdated"
              : "pumpAttendantValidation:feedback.candidateCreated"));
            setReload((value) => value + 1);
          }}
        />
      )}
    </ManagerLayout>
  );
}

function Pagination({ data, onPrevious, onNext, t }) {
  if (!data || data.totalPages <= 1) return null;
  return (
    <nav className="validation-pagination">
      <span>{t("pumpAttendantValidation:pagination.total", {
        count: data.totalElements,
      })}</span>
      <div>
        <button type="button" disabled={data.first} onClick={onPrevious}>
          <ChevronLeft size={17} />
        </button>
        <span>{t("pumpAttendantValidation:pagination.page", {
          current: data.page + 1,
          total: data.totalPages,
        })}</span>
        <button type="button" disabled={data.last} onClick={onNext}>
          <ChevronRight size={17} />
        </button>
      </div>
    </nav>
  );
}

export default ManagerPumpAttendantsPage;
