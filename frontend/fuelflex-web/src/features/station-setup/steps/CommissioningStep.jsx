import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  AlertTriangle,
  ArrowLeft,
  CheckCircle2,
  CircleDollarSign,
  Gauge,
  LayoutDashboard,
  LoaderCircle,
  RefreshCw,
} from "lucide-react";

const SUMMARY_ITEMS = [["depots", "depot"], ["tanks", "tank"], ["pumps", "pump"], ["dispensingPoints", "point"], ["fuelMeters", "meter"]];

function CommissioningStep({
  station,
  selectedProductIds,
  validation,
  isLoading,
  isCompleting,
  isCompleted,
  errorMessage,
  onBack,
  onRetry,
  onFinish,
}) {
  const navigate = useNavigate();
  const { t } = useTranslation("stationSetup");
  const [pricingMessageVisible, setPricingMessageVisible] = useState(false);
  const issues = validation?.issues || [];
  const isValid = Boolean(validation?.valid) && selectedProductIds.length > 0;

  if (isCompleted) {
    return (
      <section className="station-wizard-panel station-wizard-commissioning">
        <div className="station-wizard-completion-hero">
          <span><CheckCircle2 size={34} /></span>
          <small>{t("steps.commissioning.completeKicker")}</small>
          <h2>{t("steps.commissioning.completeTitle")}</h2>
          <p>{t("steps.commissioning.completeDescription")}</p>
        </div>
        {pricingMessageVisible && (
          <div className="station-wizard-alert success" role="status">
            {t("steps.commissioning.pricingLater")}
          </div>
        )}
        <div className="station-wizard-actions station-wizard-completion-actions">
          <button type="button" className="station-setup-secondary" onClick={() => navigate("/superviseur/dashboard")}>
            <LayoutDashboard size={17} />{t("steps.commissioning.dashboard")}
          </button>
          <button type="button" className="station-setup-primary" onClick={() => setPricingMessageVisible(true)}>
            <CircleDollarSign size={17} />{t("steps.commissioning.pricing")}
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="station-wizard-panel station-wizard-commissioning">
      <div className="station-wizard-heading">
        <span><Gauge size={24} /></span>
        <div>
          <small>{t("steps.progress", { current: 9, total: 9 })}</small>
          <h2>{t("steps.commissioning.title")}</h2>
          <p>{t("steps.commissioning.description")}</p>
        </div>
      </div>

      <div className="station-wizard-explanation">{t("steps.commissioning.help")}</div>

      {errorMessage && (
        <div className="station-wizard-alert error" role="alert">
          <span>{errorMessage}</span>
          <button type="button" onClick={onRetry} disabled={isLoading || isCompleting}>
            <RefreshCw size={15} /> {t("steps.retry")}
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="station-wizard-products-loading">
          <LoaderCircle className="station-setup-spinner" size={28} />
          {t("steps.commissioning.loading")}
        </div>
      ) : (
        <>
          {validation?.valid && (
            <div className="station-wizard-commissioning-summary">
              <header>
                <CheckCircle2 size={22} />
                <div><strong>{t("steps.commissioning.validated")}</strong><span>{station?.name}</span></div>
              </header>
              <div className="station-wizard-commissioning-counts">
                {SUMMARY_ITEMS.map(([key, label]) => {
                  const count = validation?.summary?.[key] || 0;
                  return <div key={key}><CheckCircle2 size={16} /><span>{t(`steps.commissioning.${label}`, { count })}</span></div>;
                })}
              </div>
            </div>
          )}

          {(!validation?.valid || selectedProductIds.length === 0) && (
            <section className="station-wizard-review-issues">
              <header><AlertTriangle size={20} /><div><strong>{t("steps.review.issuesTitle")}</strong><span>{t("steps.commissioning.cannotComplete")}</span></div></header>
              {selectedProductIds.length === 0 && <article><div><strong>{t("stepper.products")}</strong><p>{t("steps.review.selectProduct")}</p></div></article>}
              {issues.map((issue, index) => <article key={`${issue.code}-${issue.objectId || index}`}><div><strong>{issue.objectName || issue.objectType}</strong><p>{issue.message}</p></div></article>)}
            </section>
          )}
        </>
      )}

      <div className="station-wizard-actions">
        <button type="button" className="station-setup-secondary" onClick={onBack} disabled={isCompleting}>
          <ArrowLeft size={17} />{t("steps.commissioning.back")}
        </button>
        <button type="button" className="station-setup-primary" onClick={onFinish} disabled={!isValid || isLoading || isCompleting}>
          {isCompleting ? <LoaderCircle className="station-setup-spinner" size={17} /> : <CheckCircle2 size={17} />}
          {isCompleting ? t("steps.commissioning.validating") : t("steps.commissioning.finish")}
        </button>
      </div>
    </section>
  );
}

export default CommissioningStep;
