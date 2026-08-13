import { ArrowLeft, Fuel, LoaderCircle, Pencil, Plus, RefreshCw } from "lucide-react";

const METERING_LABELS = {
  PUMP: "Comptage global",
  DISPENSING_POINT: "Comptage par pistolet",
};

function PumpStep({ pumps, isLoading, errorMessage, successMessage, onCreate, onEdit, onBack, onContinue, onRetry }) {
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><Fuel size={24} /></span><div><small>ÉTAPE 5 SUR 9</small><h2>Pompes</h2><p>Configurez les distributeurs physiques de la station et leur mode de comptage.</p></div></div>
      <div className="station-wizard-explanation">Le comptage global convient aux pistolets alimentés par la même citerne. Choisissez le comptage par pistolet lorsque chaque point doit être suivi indépendamment.</div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> Réessayer</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />Chargement des pompes...</div> : pumps.length === 0 ? <div className="station-wizard-resource-empty"><Fuel size={30} /><strong>Aucune pompe configurée</strong><p>Créez le premier distributeur physique de cette station.</p><button type="button" className="station-setup-primary" onClick={onCreate}><Plus size={17} />Créer la première pompe</button></div> : <><div className="station-wizard-list-toolbar"><span>{pumps.length} pompe{pumps.length > 1 ? "s" : ""}</span><button type="button" className="station-setup-secondary" onClick={onCreate}><Plus size={17} />Ajouter une pompe</button></div><div className="station-wizard-resource-grid">{pumps.map((pump) => <article key={pump.id} className="station-wizard-resource-card pump-card"><div><small>{pump.code} · Pompe n° {pump.pumpNumber}</small><h3>{pump.name}</h3><p>{METERING_LABELS[pump.meteringLevel] || pump.meteringLevel} · {pump.status}</p><span>{[pump.manufacturer, pump.model].filter(Boolean).join(" · ") || "Fabricant non renseigné"}{pump.location ? ` · ${pump.location}` : ""}</span></div><button type="button" onClick={() => onEdit(pump)}><Pencil size={16} />Modifier</button></article>)}</div></>}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />Retour aux citernes</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={pumps.length === 0 || isLoading}>Continuer vers les pistolets</button></div>
    </section>
  );
}
export default PumpStep;
