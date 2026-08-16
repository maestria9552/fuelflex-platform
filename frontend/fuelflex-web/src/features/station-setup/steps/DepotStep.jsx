import { ArrowLeft, ArrowRight, MapPinned, Pencil, Plus, RefreshCw, LoaderCircle } from "lucide-react";
import { useTranslation } from "react-i18next";

function DepotStep({ depots, isLoading, errorMessage, successMessage, onCreate, onEdit, onBack, onContinue, onRetry }) {
  const { t } = useTranslation(["stationSetup", "depots"]);
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading">
        <span><MapPinned size={24} /></span>
        <div><small>{t("steps.progress", { current: 3, total: 9 })}</small><h2>{t("depots:page.title")}</h2><p>{t("steps.depots.description")}</p></div>
      </div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> {t("steps.retry")}</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? (
        <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />{t("steps.depots.loading")}</div>
      ) : depots.length === 0 ? (
        <div className="station-wizard-resource-empty"><MapPinned size={30} /><strong>{t("steps.depots.emptyTitle")}</strong><p>{t("steps.depots.emptyDescription")}</p><button type="button" className="station-setup-primary" onClick={onCreate}><Plus size={17} />{t("steps.depots.createFirst")}</button></div>
      ) : (
        <><div className="station-wizard-list-toolbar"><span>{t("steps.depots.count", { count: depots.length })}</span><button type="button" className="station-setup-secondary" onClick={onCreate}><Plus size={17} />{t("steps.depots.add")}</button></div>
        <div className="station-wizard-resource-grid">{depots.map((depot) => <article key={depot.id} className="station-wizard-resource-card"><div><small>{depot.code}</small><h3>{depot.name}</h3>{depot.description && <p>{depot.description}</p>}<span>{depot.location || t("steps.depots.locationMissing")}</span></div><button type="button" onClick={() => onEdit(depot)}><Pencil size={16} />{t("steps.edit")}</button></article>)}</div></>
      )}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />{t("steps.depots.back")}</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={isLoading || depots.length === 0}>{t("steps.depots.continue")}<ArrowRight size={17} /></button></div>
    </section>
  );
}
export default DepotStep;
