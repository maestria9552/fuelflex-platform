import { ArrowLeft, Fuel, LoaderCircle, Pencil, Plus, RefreshCw } from "lucide-react";
import { useTranslation } from "react-i18next";

function getTankLabel(point, tanks) {
  const tank = tanks.find((candidate) => candidate.id === point.tankId);
  const label = ` — `;
  return tank?.productName ? ` — ` : label;
}

function DispensingPointStep({ pumps, dispensingPointsByPump, tanks, isLoading, errorMessage, successMessage, onCreate, onEdit, onBack, onContinue, onRetry }) {
  const { t } = useTranslation(["stationSetup", "dispensingPoints", "pumps"]);
  const everyPumpHasPoint = pumps.length > 0 && pumps.every((pump) => (dispensingPointsByPump[pump.id] || []).length > 0);
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><Fuel size={24} /></span><div><small>{t("steps.progress", { current: 6, total: 9 })}</small><h2>{t("dispensingPoints:page.title")}</h2><p>{t("steps.points.description")}</p></div></div>
      <div className="station-wizard-explanation">{t("steps.points.help")}</div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> {t("steps.retry")}</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />{t("steps.points.loading")}</div> : <div className="station-wizard-pump-groups">{pumps.map((pump) => { const points = dispensingPointsByPump[pump.id] || []; return <section key={pump.id} className="station-wizard-pump-group"><header><div><small>{pump.code}{pump.pumpNumber ? ` · ${t("pumps:page.pumpNumber", { number: pump.pumpNumber })}` : ""}</small><h3>{pump.name}</h3><span>{t(`pumps:meteringLevel.${pump.meteringLevel}.title`, { defaultValue: pump.meteringLevel })}</span></div><button type="button" onClick={() => onCreate(pump)} disabled={tanks.length === 0}><Plus size={16} />{t("steps.points.add")}</button></header><p className="station-wizard-metering-note">{t(pump.meteringLevel === "PUMP" ? "steps.points.pumpMeterMessage" : "steps.points.pointMeterMessage")}</p>{points.length === 0 ? <div className="station-wizard-pump-empty"><p>{t("steps.points.empty")}</p><button type="button" className="station-setup-secondary" onClick={() => onCreate(pump)} disabled={tanks.length === 0}><Plus size={16} />{t("steps.points.add")}</button></div> : <div className="station-wizard-dispensing-point-list">{points.map((point) => <article key={point.id}><div><small>{point.code}{point.nozzleNumber ? ` · ${t("dispensingPoints:page.nozzleNumber", { number: point.nozzleNumber })}` : ""}</small><strong>{point.name}</strong><span>{getTankLabel(point, tanks)}</span><em>{t(`dispensingPoints:status.${point.status}`, { defaultValue: point.status })}</em></div><button type="button" onClick={() => onEdit(pump, point)}><Pencil size={16} />{t("steps.edit")}</button></article>)}</div>}</section>; })}</div>}
      {!isLoading && !everyPumpHasPoint && <p className="station-wizard-requirement" role="status">{t("steps.points.requirement")}</p>}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />{t("steps.points.back")}</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={!everyPumpHasPoint || isLoading}>{t("steps.points.continue")}</button></div>
    </section>
  );
}

export default DispensingPointStep;
