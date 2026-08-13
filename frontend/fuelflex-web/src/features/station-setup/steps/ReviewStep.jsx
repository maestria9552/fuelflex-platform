import { AlertTriangle, ArrowLeft, CheckCircle2, LoaderCircle, RefreshCw, XCircle } from "lucide-react";

const STEP_LABELS = {
  station: "Station",
  products: "Produits utilisés",
  depots: "Dépôts",
  tanks: "Citernes",
  pumps: "Pompes",
  "dispensing-points": "Pistolets",
  "fuel-meters": "Compteurs",
};

function SectionStatus({ valid, warning = false }) {
  if (warning) return <span className="warning"><AlertTriangle size={15} />Avertissement</span>;
  return valid ? <span className="valid"><CheckCircle2 size={15} />Validé</span> : <span className="invalid"><XCircle size={15} />À corriger</span>;
}

function ReviewStep({ station, products, depots, tanksByDepot, pumps, dispensingPointsByPump, pumpMetersByPump, pointMetersByPoint, validation, isLoading, errorMessage, onBack, onRetry, onGoToStep, onContinue }) {
  const issues = validation?.issues || [];
  const hasIssue = (step) => issues.some((issue) => issue.step === step);
  const productsValid = products.length > 0;
  const configurationValid = Boolean(validation?.valid) && productsValid;

  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><CheckCircle2 size={24} /></span><div><small>ÉTAPE 8 SUR 9</small><h2>Vérification</h2><p>Contrôlez l’ensemble de la configuration avant la mise en service.</p></div></div>
      <div className="station-wizard-review-toolbar"><p>Les données affichées ont été relues depuis la configuration persistée.</p><button type="button" className="station-setup-secondary" onClick={onRetry} disabled={isLoading}><RefreshCw size={16} />Relancer la vérification</button></div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> Réessayer</button></div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />Vérification de la configuration...</div> : <>
        <div className="station-wizard-review-sections">
          <section><header><div><strong>Station</strong><small>{station?.code}</small></div><SectionStatus valid={!hasIssue("station")} /></header><p>{station?.name}</p></section>
          <section><header><div><strong>Produits utilisés</strong><small>{products.length} sélectionné{products.length > 1 ? "s" : ""}</small></div><SectionStatus valid={productsValid} /></header>{products.length ? <ul>{products.map((product) => <li key={product.id}><CheckCircle2 size={14} />{product.name} <small>{product.code}</small></li>)}</ul> : <p>Aucun produit sélectionné dans le wizard.</p>}</section>
          <section><header><div><strong>Dépôts et citernes</strong><small>{depots.length} dépôt{depots.length > 1 ? "s" : ""}</small></div><SectionStatus valid={!hasIssue("depots") && !hasIssue("tanks")} /></header><div className="station-wizard-review-tree">{depots.map((depot) => <div key={depot.id}><strong>{depot.name}</strong><small>{depot.code}</small><ul>{(tanksByDepot[depot.id] || []).map((tank) => <li key={tank.id}><CheckCircle2 size={14} /><span>{tank.name} — {tank.capacityLiters} L<small>{tank.code} · {tank.productName}</small></span></li>)}</ul></div>)}</div></section>
          <section><header><div><strong>Pompes, pistolets et compteurs</strong><small>{pumps.length} pompe{pumps.length > 1 ? "s" : ""}</small></div><SectionStatus valid={!hasIssue("pumps") && !hasIssue("dispensing-points") && !hasIssue("fuel-meters")} /></header><div className="station-wizard-review-tree">{pumps.map((pump) => { const points = dispensingPointsByPump[pump.id] || []; const globalMeter = (pumpMetersByPump[pump.id] || []).find((meter) => meter.active); return <div key={pump.id}><strong>{pump.name}</strong><small>{pump.code} · {pump.meteringLevel === "PUMP" ? "Comptage global" : "Comptage par pistolet"}</small>{pump.meteringLevel === "PUMP" && <ul><li>{globalMeter ? <CheckCircle2 size={14} /> : <XCircle size={14} />}<span>{globalMeter ? globalMeter.name : "Compteur global manquant"}</span></li></ul>}<ul>{points.map((point) => { const meter = (pointMetersByPoint[point.id] || []).find((candidate) => candidate.active); return <li key={point.id}><CheckCircle2 size={14} /><span>{point.name}<small>{point.code} · {point.tankName}{pump.meteringLevel === "DISPENSING_POINT" ? ` → ${meter?.name || "Compteur manquant"}` : ""}</small></span></li>; })}</ul></div>; })}</div></section>
        </div>
        {issues.length > 0 && <section className="station-wizard-review-issues"><header><AlertTriangle size={20} /><div><strong>Configuration à corriger</strong><span>{issues.length} anomalie{issues.length > 1 ? "s" : ""} détectée{issues.length > 1 ? "s" : ""}</span></div></header>{issues.map((issue, index) => <article key={`${issue.code}-${issue.objectId || index}`}><div><strong>{issue.objectName || issue.objectType}</strong><p>{issue.message}</p><small>{STEP_LABELS[issue.step] || issue.step}</small></div>{STEP_LABELS[issue.step] && <button type="button" onClick={() => onGoToStep(issue.step)}>Corriger</button>}</article>)}</section>}
        {!productsValid && <section className="station-wizard-review-issues"><article><div><strong>Produits utilisés</strong><p>Sélectionnez au moins un produit pour cette station.</p></div><button type="button" onClick={() => onGoToStep("products")}>Corriger</button></article></section>}
        {configurationValid && <div className="station-wizard-alert success" role="status">La configuration persistée est complète et prête pour la mise en service.</div>}
      </>}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />Retour aux compteurs</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={!configurationValid || isLoading}>Passer à la mise en service</button></div>
    </section>
  );
}

export default ReviewStep;
