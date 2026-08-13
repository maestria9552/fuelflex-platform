import { ArrowLeft, ArrowRight, MapPinned, Pencil, Plus, RefreshCw, LoaderCircle } from "lucide-react";

function DepotStep({ depots, isLoading, errorMessage, successMessage, onCreate, onEdit, onBack, onContinue, onRetry }) {
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading">
        <span><MapPinned size={24} /></span>
        <div><small>ÉTAPE 3 SUR 9</small><h2>Dépôts</h2><p>Les dépôts permettent d’organiser les citernes de la station.</p></div>
      </div>
      {errorMessage && <div className="station-wizard-alert error" role="alert"><span>{errorMessage}</span><button type="button" onClick={onRetry}><RefreshCw size={15} /> Réessayer</button></div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      {isLoading ? (
        <div className="station-wizard-products-loading"><LoaderCircle className="station-setup-spinner" size={28} />Chargement des dépôts...</div>
      ) : depots.length === 0 ? (
        <div className="station-wizard-resource-empty"><MapPinned size={30} /><strong>Aucun dépôt configuré</strong><p>Créez le premier dépôt de cette station pour pouvoir y rattacher des citernes.</p><button type="button" className="station-setup-primary" onClick={onCreate}><Plus size={17} />Créer le premier dépôt</button></div>
      ) : (
        <><div className="station-wizard-list-toolbar"><span>{depots.length} dépôt{depots.length > 1 ? "s" : ""}</span><button type="button" className="station-setup-secondary" onClick={onCreate}><Plus size={17} />Ajouter un dépôt</button></div>
        <div className="station-wizard-resource-grid">{depots.map((depot) => <article key={depot.id} className="station-wizard-resource-card"><div><small>{depot.code}</small><h3>{depot.name}</h3>{depot.description && <p>{depot.description}</p>}<span>{depot.location || "Emplacement non renseigné"}</span></div><button type="button" onClick={() => onEdit(depot)}><Pencil size={16} />Modifier</button></article>)}</div></>
      )}
      <div className="station-wizard-actions"><button type="button" className="station-setup-secondary" onClick={onBack}><ArrowLeft size={17} />Retour aux produits</button><button type="button" className="station-setup-primary" onClick={onContinue} disabled={isLoading || depots.length === 0}>Continuer vers les citernes<ArrowRight size={17} /></button></div>
    </section>
  );
}
export default DepotStep;
