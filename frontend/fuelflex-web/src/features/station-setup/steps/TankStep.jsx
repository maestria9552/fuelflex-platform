import { ArrowLeft, CircleGauge, LoaderCircle, Pencil, Plus, RefreshCw } from "lucide-react";
import { useTranslation } from "react-i18next";
import { formatNumber } from "../../../i18n/formatters";

function TankStep({ depots, tanksByDepot, isLoading, errorMessage, successMessage, onCreate, onEdit, onBack, onContinue, onRetry }) {
  const { t, i18n } = useTranslation(["stationSetup", "tanks"]);
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><CircleGauge size={24} /></span><div><small>{t("steps.progress", { current: 4, total: 9 })}</small><h2>{t("tanks:page.title")}</h2><p>{t("steps.tanks.description")}</p></div></div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> {t("steps.retry")}</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />{t("steps.tanks.loading")}</div> : (
        <><div className="station-wizard-list-toolbar"><span>{t("steps.tanks.grouped")}</span><button type="button" className="station-setup-secondary" onClick={() => onCreate()}><Plus size={17} />{t("steps.tanks.add")}</button></div>
        <div className="station-wizard-depot-groups">{depots.map((depot) => { const tanks = tanksByDepot[depot.id] || []; return <section key={depot.id} className="station-wizard-depot-group"><header><div><small>{depot.code}</small><h3>{depot.name}</h3></div><button type="button" onClick={() => onCreate(depot.id)}><Plus size={16} />{t("steps.add")}</button></header>{tanks.length === 0 ? <p className="station-wizard-group-empty">{t("steps.tanks.empty")}</p> : <div className="station-wizard-tank-list">{tanks.map((tank) => <article key={tank.id}><div><small>{tank.code}</small><strong>{tank.name}</strong><span>{tank.productName} · {formatNumber(tank.capacityLiters, { language: i18n.resolvedLanguage })} L</span></div><button type="button" onClick={() => onEdit(tank)}><Pencil size={16} />{t("steps.edit")}</button></article>)}</div>}</section>; })}</div></>
      )}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />{t("steps.tanks.back")}</button><button type="button" className="station-setup-primary" onClick={onContinue}>{t("steps.tanks.continue")}</button></div>
    </section>
  );
}
export default TankStep;
