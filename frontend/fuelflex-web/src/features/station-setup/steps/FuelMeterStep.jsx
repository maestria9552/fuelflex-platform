import { ArrowLeft, Gauge, LoaderCircle, Pencil, Plus, RefreshCw } from "lucide-react";
import { useTranslation } from "react-i18next";
import { formatNumber } from "../../../i18n/formatters";

function MeterSummary({ meter, onEdit, language, t }) {
  return <article className="station-wizard-meter-card"><div><small>{meter.code} · {t(`fuelMeters:technology.${meter.technology}`, { defaultValue: meter.technology })}</small><strong>{meter.name}</strong><span>{t("steps.meters.currentIndex", { value: formatNumber(meter.currentIndex, { language }) })}</span><em>{t(`fuelMeters:status.${meter.status}`, { defaultValue: meter.status })}</em></div><button type="button" onClick={onEdit}><Pencil size={16} />{t("steps.edit")}</button></article>;
}

function FuelMeterStep({ pumps, dispensingPointsByPump, pumpMetersByPump, pointMetersByPoint, isLoading, errorMessage, successMessage, onConfigurePumpMeter, onConfigurePointMeter, onBack, onContinue, onRetry }) {
  const { t, i18n } = useTranslation(["stationSetup", "fuelMeters", "pumps", "dispensingPoints"]);
  const incompleteMessages = [];
  pumps.forEach((pump) => {
    if (pump.meteringLevel === "PUMP") {
      const activeMeters = (pumpMetersByPump[pump.id] || []).filter((meter) => meter.active);
      if (activeMeters.length !== 1) incompleteMessages.push(t("steps.meters.configurePump", { name: pump.name }));
      return;
    }
    const pumpPoints = dispensingPointsByPump[pump.id] || [];
    if (pumpPoints.length === 0) incompleteMessages.push(t("steps.meters.addPoint", { name: pump.name }));
    pumpPoints.forEach((point) => {
      const activeMeters = (pointMetersByPoint[point.id] || []).filter((meter) => meter.active);
      if (activeMeters.length !== 1) incompleteMessages.push(t("steps.meters.configurePoint", { name: point.name }));
    });
  });
  const isComplete = pumps.length > 0 && incompleteMessages.length === 0;

  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><Gauge size={24} /></span><div><small>{t("steps.progress", { current: 7, total: 9 })}</small><h2>{t("fuelMeters:page.title")}</h2><p>{t("steps.meters.description")}</p></div></div>
      <div className="station-wizard-explanation">{t("steps.meters.help")}</div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> {t("steps.retry")}</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />{t("steps.meters.loading")}</div> : <div className="station-wizard-meter-groups">{pumps.map((pump) => {
        const points = dispensingPointsByPump[pump.id] || [];
        const pumpMeters = pumpMetersByPump[pump.id] || [];
        return <section key={pump.id} className="station-wizard-meter-group"><header><div><small>{pump.code}{pump.pumpNumber ? ` · ${t("pumps:page.pumpNumber", { number: pump.pumpNumber })}` : ""}</small><h3>{pump.name}</h3><span>{t(`pumps:meteringLevel.${pump.meteringLevel}.title`, { defaultValue: pump.meteringLevel })}</span></div></header>
          {pump.meteringLevel === "PUMP" ? <div className="station-wizard-meter-parent"><div className="station-wizard-meter-parent-title"><strong>{t("steps.meters.global")}</strong>{pumpMeters.length === 0 && <button type="button" className="station-setup-secondary" onClick={() => onConfigurePumpMeter(pump, null)}><Plus size={16} />{t("steps.meters.configureGlobal")}</button>}</div>{pumpMeters.length === 0 ? <p>{t("steps.meters.globalMissing")}</p> : pumpMeters.map((meter) => <MeterSummary key={meter.id} meter={meter} language={i18n.resolvedLanguage} t={t} onEdit={() => onConfigurePumpMeter(pump, meter)} />)}</div> : <div className="station-wizard-meter-points">{points.map((point) => { const meters = pointMetersByPoint[point.id] || []; return <section key={point.id}><div className="station-wizard-meter-parent-title"><div><small>{point.code}{point.nozzleNumber ? ` · ${t("dispensingPoints:page.nozzleNumber", { number: point.nozzleNumber })}` : ""}</small><strong>{point.name}</strong></div>{meters.length === 0 && <button type="button" className="station-setup-secondary" onClick={() => onConfigurePointMeter(pump, point, null)}><Plus size={16} />{t("steps.meters.configure")}</button>}</div>{meters.length === 0 ? <p>{t("steps.meters.individualMissing")}</p> : meters.map((meter) => <MeterSummary key={meter.id} meter={meter} language={i18n.resolvedLanguage} t={t} onEdit={() => onConfigurePointMeter(pump, point, meter)} />)}</section>; })}</div>}
        </section>;
      })}</div>}
      {!isLoading && !isComplete && <div className="station-wizard-requirement" role="status">{incompleteMessages.map((message) => <span key={message}>{message}</span>)}</div>}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />{t("steps.meters.back")}</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={!isComplete || isLoading}>{t("steps.meters.continue")}</button></div>
    </section>
  );
}

export default FuelMeterStep;
