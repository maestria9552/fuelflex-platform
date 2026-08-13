import { ArrowLeft, Gauge, LoaderCircle, Pencil, Plus, RefreshCw } from "lucide-react";

const TECHNOLOGY_LABELS = { MECHANICAL: "Mécanique", ELECTRONIC: "Électronique", MANUAL: "Manuel" };

function MeterSummary({ meter, onEdit }) {
  return <article className="station-wizard-meter-card"><div><small>{meter.code} · {TECHNOLOGY_LABELS[meter.technology] || meter.technology}</small><strong>{meter.name}</strong><span>Index actuel : {meter.currentIndex}</span><em>{meter.status}</em></div><button type="button" onClick={onEdit}><Pencil size={16} />Modifier</button></article>;
}

function FuelMeterStep({ pumps, dispensingPointsByPump, pumpMetersByPump, pointMetersByPoint, isLoading, errorMessage, successMessage, onConfigurePumpMeter, onConfigurePointMeter, onBack, onContinue, onRetry }) {
  const incompleteMessages = [];
  pumps.forEach((pump) => {
    if (pump.meteringLevel === "PUMP") {
      const activeMeters = (pumpMetersByPump[pump.id] || []).filter((meter) => meter.active);
      if (activeMeters.length !== 1) incompleteMessages.push(`Configurez le compteur global de la pompe ${pump.name}.`);
      return;
    }
    const pumpPoints = dispensingPointsByPump[pump.id] || [];
    if (pumpPoints.length === 0) incompleteMessages.push("Ajoutez au moins un pistolet à la pompe " + pump.name + ".");
    pumpPoints.forEach((point) => {
      const activeMeters = (pointMetersByPoint[point.id] || []).filter((meter) => meter.active);
      if (activeMeters.length !== 1) incompleteMessages.push(`Configurez le compteur du pistolet ${point.name}.`);
    });
  });
  const isComplete = pumps.length > 0 && incompleteMessages.length === 0;

  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><Gauge size={24} /></span><div><small>ÉTAPE 7 SUR 9</small><h2>Compteurs</h2><p>Configurez les compteurs utilisés pour suivre les index de distribution.</p></div></div>
      <div className="station-wizard-explanation">Le niveau de comptage défini sur chaque pompe détermine si le compteur est global à la pompe ou individuel à chaque pistolet.</div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> Réessayer</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />Chargement des compteurs...</div> : <div className="station-wizard-meter-groups">{pumps.map((pump) => {
        const points = dispensingPointsByPump[pump.id] || [];
        const pumpMeters = pumpMetersByPump[pump.id] || [];
        return <section key={pump.id} className="station-wizard-meter-group"><header><div><small>{pump.code}{pump.pumpNumber ? ` · Pompe n° ${pump.pumpNumber}` : ""}</small><h3>{pump.name}</h3><span>{pump.meteringLevel === "PUMP" ? "Comptage global à la pompe" : "Comptage par pistolet"}</span></div></header>
          {pump.meteringLevel === "PUMP" ? <div className="station-wizard-meter-parent"><div className="station-wizard-meter-parent-title"><strong>Compteur global</strong>{pumpMeters.length === 0 && <button type="button" className="station-setup-secondary" onClick={() => onConfigurePumpMeter(pump, null)}><Plus size={16} />Configurer le compteur global</button>}</div>{pumpMeters.length === 0 ? <p>Aucun compteur global configuré.</p> : pumpMeters.map((meter) => <MeterSummary key={meter.id} meter={meter} onEdit={() => onConfigurePumpMeter(pump, meter)} />)}</div> : <div className="station-wizard-meter-points">{points.map((point) => { const meters = pointMetersByPoint[point.id] || []; return <section key={point.id}><div className="station-wizard-meter-parent-title"><div><small>{point.code}{point.nozzleNumber ? ` · Pistolet n° ${point.nozzleNumber}` : ""}</small><strong>{point.name}</strong></div>{meters.length === 0 && <button type="button" className="station-setup-secondary" onClick={() => onConfigurePointMeter(pump, point, null)}><Plus size={16} />Configurer le compteur</button>}</div>{meters.length === 0 ? <p>Aucun compteur individuel configuré.</p> : meters.map((meter) => <MeterSummary key={meter.id} meter={meter} onEdit={() => onConfigurePointMeter(pump, point, meter)} />)}</section>; })}</div>}
        </section>;
      })}</div>}
      {!isLoading && !isComplete && <div className="station-wizard-requirement" role="status">{incompleteMessages.map((message) => <span key={message}>{message}</span>)}</div>}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />Retour aux pistolets</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={!isComplete || isLoading}>Continuer vers la vérification</button></div>
    </section>
  );
}

export default FuelMeterStep;
