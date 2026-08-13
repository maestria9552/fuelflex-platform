import { ArrowLeft, CircleGauge, LoaderCircle, Pencil, Plus, RefreshCw } from "lucide-react";

function TankStep({ depots, tanksByDepot, isLoading, errorMessage, successMessage, onCreate, onEdit, onBack, onContinue, onRetry }) {
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading"><span><CircleGauge size={24} /></span><div><small>ÉTAPE 4 SUR 9</small><h2>Citernes</h2><p>Configurez les citernes de chaque dépôt et leur produit associé.</p></div></div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> Réessayer</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />Chargement des citernes...</div> : (
        <><div className="station-wizard-list-toolbar"><span>Citernes organisées par dépôt</span><button type="button" className="station-setup-secondary" onClick={() => onCreate()}><Plus size={17} />Ajouter une citerne</button></div>
        <div className="station-wizard-depot-groups">{depots.map((depot) => { const tanks = tanksByDepot[depot.id] || []; return <section key={depot.id} className="station-wizard-depot-group"><header><div><small>{depot.code}</small><h3>{depot.name}</h3></div><button type="button" onClick={() => onCreate(depot.id)}><Plus size={16} />Ajouter</button></header>{tanks.length === 0 ? <p className="station-wizard-group-empty">Aucune citerne dans ce dépôt.</p> : <div className="station-wizard-tank-list">{tanks.map((tank) => <article key={tank.id}><div><small>{tank.code}</small><strong>{tank.name}</strong><span>{tank.productName} · {tank.capacityLiters} L</span></div><button type="button" onClick={() => onEdit(tank)}><Pencil size={16} />Modifier</button></article>)}</div>}</section>; })}</div></>
      )}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />Retour aux dépôts</button><button type="button" className="station-setup-primary" onClick={onContinue}>Continuer vers les pompes</button></div>
    </section>
  );
}
export default TankStep;
