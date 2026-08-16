import { AlertTriangle, ArrowLeft, CheckCircle2, LoaderCircle, RefreshCw, XCircle } from "lucide-react";
import { useTranslation } from "react-i18next";
import { formatNumber } from "../../../i18n/formatters";

const STEP_LABELS = {
  station: "stationSetup:stepper.station",
  products: "stationSetup:stepper.products",
  depots: "depots:page.title",
  tanks: "tanks:page.title",
  pumps: "pumps:page.title",
  "dispensing-points": "dispensingPoints:page.title",
  "fuel-meters": "fuelMeters:page.title",
};

function SectionStatus({ valid, warning = false }) {
  const { t } = useTranslation("stationSetup");
  if (warning) return <span className="warning"><AlertTriangle size={15} />{t("steps.review.warning")}</span>;
  return valid ? <span className="valid"><CheckCircle2 size={15} />{t("steps.review.valid")}</span> : <span className="invalid"><XCircle size={15} />{t("steps.review.correct")}</span>;
}

function ReviewStep({ station, products, depots, tanksByDepot, pumps, dispensingPointsByPump, pumpMetersByPump, pointMetersByPoint, validation, isLoading, errorMessage, onBack, onRetry, onGoToStep, onContinue }) {
  const { t, i18n } = useTranslation(["stationSetup", "stations", "products", "depots", "tanks", "pumps", "dispensingPoints", "fuelMeters"]);
  const issues = validation?.issues || [];
  const hasIssue = (step) => issues.some((issue) => issue.step === step);
  const productsValid = products.length > 0;
  const configurationValid = Boolean(validation?.valid) && productsValid;

  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><CheckCircle2 size={24} /></span><div><small>{t("steps.progress", { current: 8, total: 9 })}</small><h2>{t("stepper.review")}</h2><p>{t("steps.review.description")}</p></div></div>
      <div className="station-wizard-review-toolbar"><p>{t("steps.review.persisted")}</p><button type="button" className="station-setup-secondary" onClick={onRetry} disabled={isLoading}><RefreshCw size={16} />{t("steps.review.rerun")}</button></div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> {t("steps.retry")}</button></div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />{t("steps.review.loading")}</div> : <>
        <div className="station-wizard-review-sections">
          <section><header><div><strong>{t("stepper.station")}</strong><small>{station?.code}</small></div><SectionStatus valid={!hasIssue("station")} t={t} /></header><p>{station?.name}</p></section>
          <section><header><div><strong>{t("stepper.products")}</strong><small>{t("steps.review.selected", { count: products.length })}</small></div><SectionStatus valid={productsValid} t={t} /></header>{products.length ? <ul>{products.map((product) => <li key={product.id}><CheckCircle2 size={14} />{product.name} <small>{product.code}</small></li>)}</ul> : <p>{t("steps.review.noProduct")}</p>}</section>
          <section><header><div><strong>{t("steps.review.depotsTanks")}</strong><small>{t("steps.depots.count", { count: depots.length })}</small></div><SectionStatus valid={!hasIssue("depots") && !hasIssue("tanks")} t={t} /></header><div className="station-wizard-review-tree">{depots.map((depot) => <div key={depot.id}><strong>{depot.name}</strong><small>{depot.code}</small><ul>{(tanksByDepot[depot.id] || []).map((tank) => <li key={tank.id}><CheckCircle2 size={14} /><span>{tank.name} — {formatNumber(tank.capacityLiters, { language: i18n.resolvedLanguage })} L<small>{tank.code} · {tank.productName}</small></span></li>)}</ul></div>)}</div></section>
          <section><header><div><strong>{t("steps.review.pumpsPointsMeters")}</strong><small>{t("steps.pumps.count", { count: pumps.length })}</small></div><SectionStatus valid={!hasIssue("pumps") && !hasIssue("dispensing-points") && !hasIssue("fuel-meters")} /></header><div className="station-wizard-review-tree">{pumps.map((pump) => { const points = dispensingPointsByPump[pump.id] || []; const globalMeter = (pumpMetersByPump[pump.id] || []).find((meter) => meter.active); return <div key={pump.id}><strong>{pump.name}</strong><small>{pump.code} · {t(`pumps:meteringLevel.${pump.meteringLevel}.title`, { defaultValue: pump.meteringLevel })}</small>{pump.meteringLevel === "PUMP" && <ul><li>{globalMeter ? <CheckCircle2 size={14} /> : <XCircle size={14} />}<span>{globalMeter ? globalMeter.name : t("steps.review.globalMissing")}</span></li></ul>}<ul>{points.map((point) => { const meter = (pointMetersByPoint[point.id] || []).find((candidate) => candidate.active); return <li key={point.id}><CheckCircle2 size={14} /><span>{point.name}<small>{point.code} · {point.tankName}{pump.meteringLevel === "DISPENSING_POINT" ? ` → ${meter?.name || t("steps.review.meterMissing")}` : ""}</small></span></li>; })}</ul></div>; })}</div></section>
        </div>
        {issues.length > 0 && <section className="station-wizard-review-issues"><header><AlertTriangle size={20} /><div><strong>{t("steps.review.issuesTitle")}</strong><span>{t("steps.review.issues", { count: issues.length })}</span></div></header>{issues.map((issue, index) => <article key={`${issue.code}-${issue.objectId || index}`}><div><strong>{issue.objectName || issue.objectType}</strong><p>{issue.message}</p><small>{STEP_LABELS[issue.step] ? t(STEP_LABELS[issue.step]) : issue.step}</small></div>{STEP_LABELS[issue.step] && <button type="button" onClick={() => onGoToStep(issue.step)}>{t("steps.review.correctAction")}</button>}</article>)}</section>}
        {!productsValid && <section className="station-wizard-review-issues"><article><div><strong>{t("stepper.products")}</strong><p>{t("steps.review.selectProduct")}</p></div><button type="button" onClick={() => onGoToStep("products")}>{t("steps.review.correctAction")}</button></article></section>}
        {configurationValid && <div className="station-wizard-alert success" role="status">{t("steps.review.ready")}</div>}
      </>}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />{t("steps.review.back")}</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={!configurationValid || isLoading}>{t("steps.review.continue")}</button></div>
    </section>
  );
}

export default ReviewStep;
