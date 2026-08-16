import { ArrowLeft, Fuel, LoaderCircle, Pencil, Plus, RefreshCw } from "lucide-react";
import { useTranslation } from "react-i18next";

function PumpStep({ pumps, isLoading, errorMessage, successMessage, onCreate, onEdit, onBack, onContinue, onRetry }) {
  const { t } = useTranslation(["stationSetup", "pumps"]);
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><Fuel size={24} /></span><div><small>{t("steps.progress", { current: 5, total: 9 })}</small><h2>{t("pumps:page.title")}</h2><p>{t("steps.pumps.description")}</p></div></div>
      <div className="station-wizard-explanation">{t("steps.pumps.help")}</div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> {t("steps.retry")}</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />{t("steps.pumps.loading")}</div> : pumps.length === 0 ? <div className="station-wizard-resource-empty"><Fuel size={30} /><strong>{t("steps.pumps.emptyTitle")}</strong><p>{t("steps.pumps.emptyDescription")}</p><button type="button" className="station-setup-primary" onClick={onCreate}><Plus size={17} />{t("steps.pumps.createFirst")}</button></div> : <><div className="station-wizard-list-toolbar"><span>{t("steps.pumps.count", { count: pumps.length })}</span><button type="button" className="station-setup-secondary" onClick={onCreate}><Plus size={17} />{t("steps.pumps.add")}</button></div><div className="station-wizard-resource-grid">{pumps.map((pump) => <article key={pump.id} className="station-wizard-resource-card pump-card"><div><small>{pump.code} · {t("pumps:page.pumpNumber", { number: pump.pumpNumber })}</small><h3>{pump.name}</h3><p>{t(`pumps:meteringLevel.${pump.meteringLevel}.title`, { defaultValue: pump.meteringLevel })} · {t(`pumps:status.${pump.status}`, { defaultValue: pump.status })}</p><span>{[pump.manufacturer, pump.model].filter(Boolean).join(" · ") || t("steps.pumps.manufacturerMissing")}{pump.location ? ` · ${pump.location}` : ""}</span></div><button type="button" onClick={() => onEdit(pump)}><Pencil size={16} />{t("steps.edit")}</button></article>)}</div></>}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />{t("steps.pumps.back")}</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={pumps.length === 0 || isLoading}>{t("steps.pumps.continue")}</button></div>
    </section>
  );
}
export default PumpStep;
