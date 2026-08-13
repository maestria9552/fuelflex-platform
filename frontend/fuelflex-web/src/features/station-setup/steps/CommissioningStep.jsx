import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  AlertTriangle,
  ArrowLeft,
  CheckCircle2,
  CircleDollarSign,
  Gauge,
  LayoutDashboard,
  LoaderCircle,
  RefreshCw,
} from "lucide-react";

const SUMMARY_ITEMS = [
  ["depots", "dépôt"],
  ["tanks", "citerne"],
  ["pumps", "pompe"],
  ["dispensingPoints", "pistolet"],
  ["fuelMeters", "compteur"],
];

function CommissioningStep({
  station,
  selectedProductIds,
  validation,
  isLoading,
  isCompleting,
  isCompleted,
  errorMessage,
  onBack,
  onRetry,
  onFinish,
}) {
  const navigate = useNavigate();
  const [pricingMessageVisible, setPricingMessageVisible] = useState(false);
  const issues = validation?.issues || [];
  const isValid = Boolean(validation?.valid) && selectedProductIds.length > 0;

  if (isCompleted) {
    return (
      <section className="station-wizard-panel station-wizard-commissioning">
        <div className="station-wizard-completion-hero">
          <span><CheckCircle2 size={34} /></span>
          <small>CONFIGURATION TECHNIQUE TERMINÉE</small>
          <h2>La structure de votre station est prête.</h2>
          <p>
            Poursuivez avec la tarification et la configuration opérationnelle
            avant de commencer l’exploitation.
          </p>
        </div>
        {pricingMessageVisible && (
          <div className="station-wizard-alert success" role="status">
            La tarification constitue la prochaine étape de configuration. Son
            interface sera disponible dans un prochain bloc.
          </div>
        )}
        <div className="station-wizard-actions station-wizard-completion-actions">
          <button type="button" className="station-setup-secondary" onClick={() => navigate("/superviseur/dashboard")}>
            <LayoutDashboard size={17} />Retour au tableau de bord
          </button>
          <button type="button" className="station-setup-primary" onClick={() => setPricingMessageVisible(true)}>
            <CircleDollarSign size={17} />Configurer les prix
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="station-wizard-panel station-wizard-commissioning">
      <div className="station-wizard-heading">
        <span><Gauge size={24} /></span>
        <div>
          <small>ÉTAPE 9 SUR 9</small>
          <h2>Mise en service technique</h2>
          <p>La configuration technique de votre station est complète.</p>
        </div>
      </div>

      <div className="station-wizard-explanation">
        FuelFlex vérifie la structure de la station, ses dépôts, citernes,
        pompes, pistolets et compteurs. Vous pourrez ensuite poursuivre la
        configuration opérationnelle, notamment la tarification et
        l’affectation des utilisateurs.
      </div>

      {errorMessage && (
        <div className="station-wizard-alert error" role="alert">
          <span>{errorMessage}</span>
          <button type="button" onClick={onRetry} disabled={isLoading || isCompleting}>
            <RefreshCw size={15} /> Réessayer
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="station-wizard-products-loading">
          <LoaderCircle className="station-setup-spinner" size={28} />
          Validation finale de la configuration...
        </div>
      ) : (
        <>
          {validation?.valid && (
            <div className="station-wizard-commissioning-summary">
              <header>
                <CheckCircle2 size={22} />
                <div><strong>Configuration technique validée</strong><span>{station?.name}</span></div>
              </header>
              <div className="station-wizard-commissioning-counts">
                {SUMMARY_ITEMS.map(([key, label]) => {
                  const count = validation?.summary?.[key] || 0;
                  return <div key={key}><CheckCircle2 size={16} /><span><strong>{count}</strong> {label}{count > 1 ? "s" : ""}</span></div>;
                })}
              </div>
            </div>
          )}

          {(!validation?.valid || selectedProductIds.length === 0) && (
            <section className="station-wizard-review-issues">
              <header><AlertTriangle size={20} /><div><strong>Configuration à corriger</strong><span>La mise en service technique ne peut pas être terminée.</span></div></header>
              {selectedProductIds.length === 0 && <article><div><strong>Produits utilisés</strong><p>Sélectionnez au moins un produit pour cette station.</p></div></article>}
              {issues.map((issue, index) => <article key={`${issue.code}-${issue.objectId || index}`}><div><strong>{issue.objectName || issue.objectType}</strong><p>{issue.message}</p></div></article>)}
            </section>
          )}
        </>
      )}

      <div className="station-wizard-actions">
        <button type="button" className="station-setup-secondary" onClick={onBack} disabled={isCompleting}>
          <ArrowLeft size={17} />Retour à la vérification
        </button>
        <button type="button" className="station-setup-primary" onClick={onFinish} disabled={!isValid || isLoading || isCompleting}>
          {isCompleting ? <LoaderCircle className="station-setup-spinner" size={17} /> : <CheckCircle2 size={17} />}
          {isCompleting ? "Validation finale..." : "Terminer la configuration technique"}
        </button>
      </div>
    </section>
  );
}

export default CommissioningStep;
