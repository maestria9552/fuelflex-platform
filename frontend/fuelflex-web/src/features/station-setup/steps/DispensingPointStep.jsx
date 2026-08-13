import { ArrowLeft, Fuel, LoaderCircle, Pencil, Plus, RefreshCw } from "lucide-react";

const METERING_LABELS = { PUMP: "Comptage global à la pompe", DISPENSING_POINT: "Comptage par pistolet" };
const METERING_MESSAGES = { PUMP: "Le comptage de cette pompe sera configuré globalement à l’étape Compteurs.", DISPENSING_POINT: "Les compteurs de cette pompe seront configurés par pistolet à l’étape Compteurs." };

function getTankLabel(point, tanks) {
  const tank = tanks.find((candidate) => candidate.id === point.tankId);
  const label = ` — `;
  return tank?.productName ? ` — ` : label;
}

function DispensingPointStep({ pumps, dispensingPointsByPump, tanks, isLoading, errorMessage, successMessage, onCreate, onEdit, onBack, onContinue, onRetry }) {
  const everyPumpHasPoint = pumps.length > 0 && pumps.every((pump) => (dispensingPointsByPump[pump.id] || []).length > 0);
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><Fuel size={24} /></span><div><small>ÉTAPE 6 SUR 9</small><h2>Pistolets</h2><p>Configurez les points de distribution physiques associés aux pompes.</p></div></div>
      <div className="station-wizard-explanation">Chaque pistolet appartient à une pompe. Le mode de comptage choisi sur la pompe déterminera ensuite où son compteur sera configuré.</div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> Réessayer</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />Chargement des pistolets...</div> : <div className="station-wizard-pump-groups">{pumps.map((pump) => { const points = dispensingPointsByPump[pump.id] || []; return <section key={pump.id} className="station-wizard-pump-group"><header><div><small>{pump.code}{pump.pumpNumber ? ` · Pompe n° ${pump.pumpNumber}` : ""}</small><h3>{pump.name}</h3><span>{METERING_LABELS[pump.meteringLevel] || pump.meteringLevel}</span></div><button type="button" onClick={() => onCreate(pump)} disabled={tanks.length === 0}><Plus size={16} />Ajouter un pistolet</button></header><p className="station-wizard-metering-note">{METERING_MESSAGES[pump.meteringLevel]}</p>{points.length === 0 ? <div className="station-wizard-pump-empty"><p>Aucun pistolet configuré pour cette pompe.</p><button type="button" className="station-setup-secondary" onClick={() => onCreate(pump)} disabled={tanks.length === 0}><Plus size={16} />Ajouter un pistolet</button></div> : <div className="station-wizard-dispensing-point-list">{points.map((point) => <article key={point.id}><div><small>{point.code}{point.nozzleNumber ? ` · Pistolet n° ${point.nozzleNumber}` : ""}</small><strong>{point.name}</strong><span>{getTankLabel(point, tanks)}</span><em>{point.status}</em></div><button type="button" onClick={() => onEdit(pump, point)}><Pencil size={16} />Modifier</button></article>)}</div>}</section>; })}</div>}
      {!isLoading && !everyPumpHasPoint && <p className="station-wizard-requirement" role="status">Ajoutez au moins un pistolet à chaque pompe avant de continuer.</p>}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />Retour aux pompes</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={!everyPumpHasPoint || isLoading}>Continuer vers les compteurs</button></div>
    </section>
  );
}

export default DispensingPointStep;
