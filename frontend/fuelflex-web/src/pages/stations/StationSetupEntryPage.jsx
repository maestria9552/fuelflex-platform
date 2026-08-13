import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  AlertCircle,
  ArrowLeft,
  ArrowRight,
  Fuel,
  LoaderCircle,
  PackagePlus,
  RefreshCw,
  ShieldCheck,
} from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import StationWizard from "../../features/station-setup/StationWizard";
import {
  getStationSetupDraft,
  saveStationSetupDraft,
} from "../../features/station-setup/stationSetupStorage";
import { getStoredUser } from "../../services/auth/authStorage";
import { hasActiveProducts } from "../../services/product/productService";
import "./StationSetupEntryPage.css";

function StationSetupEntryPage() {
  const navigate = useNavigate();
  const organizationId = getStoredUser()?.organizationId || null;
  const [verificationStatus, setVerificationStatus] = useState(
    organizationId ? "loading" : "missing-organization"
  );
  const [verificationError, setVerificationError] = useState("");
  const [verificationAttempt, setVerificationAttempt] = useState(0);
  const [isWizardOpen, setIsWizardOpen] = useState(() =>
    Boolean(getStationSetupDraft(organizationId)?.stationId)
  );

  useEffect(() => {
    if (!organizationId) return undefined;
    const controller = new AbortController();

    Promise.resolve()
      .then(() => {
        setVerificationStatus("loading");
        setVerificationError("");
        return hasActiveProducts(organizationId, { signal: controller.signal });
      })
      .then((hasProducts) => setVerificationStatus(hasProducts ? "ready" : "missing-products"))
      .catch((error) => {
        if (error?.name === "AbortError") return;
        setVerificationError(error?.message || "Impossible de vérifier les produits actifs.");
        setVerificationStatus("error");
      });

    return () => controller.abort();
  }, [organizationId, verificationAttempt]);

  const handleStartWizard = () => {
    saveStationSetupDraft(organizationId, { activeStep: "station" });
    setIsWizardOpen(true);
  };

  const openProductCatalog = () => navigate("/superviseur/produits?returnTo=station-setup");

  const renderLoading = () => (
    <section className="station-setup-state-card">
      <LoaderCircle className="station-setup-spinner" size={36} />
      <h2>Vérification des produits actifs...</h2>
      <p>Nous vérifions que votre organisation peut commencer la configuration d’une station.</p>
    </section>
  );

  const renderError = (message, onRetry) => (
    <section className="station-setup-state-card station-setup-error-card">
      <span className="station-setup-state-icon"><AlertCircle size={30} /></span>
      <h2>La vérification n’a pas abouti</h2>
      <p>{message}</p>
      <button type="button" className="station-setup-primary" onClick={onRetry}><RefreshCw size={17} />Réessayer</button>
    </section>
  );

  const renderReadyState = () => (
    <section className="station-setup-ready-card">
      <div className="station-setup-ready-icon"><ShieldCheck size={34} /></div>
      <span className="station-setup-kicker">PRÉREQUIS VALIDÉ</span>
      <h2>Votre organisation est prête</h2>
      <p>Le catalogue contient au moins un produit actif. Sélectionnez ensuite les produits utilisés par cette station.</p>
      <div className="station-setup-actions">
        <button type="button" className="station-setup-secondary" onClick={openProductCatalog}><PackagePlus size={17} />Gérer les produits</button>
        <button type="button" className="station-setup-primary" onClick={handleStartWizard}>Commencer la configuration<ArrowRight size={17} /></button>
      </div>
    </section>
  );

  return (
    <SupervisorLayout>
      <main className="station-setup-page">
        <header className="station-setup-header">
          <div>
            <span className="station-setup-eyebrow">Configuration</span>
            <h1>Créer une station</h1>
            <p>Vérifiez les prérequis de votre organisation avant de commencer la configuration technique.</p>
          </div>
          <div className="station-setup-header-badge"><Fuel size={19} /><span>Assistant de préparation</span></div>
        </header>

        {verificationStatus === "loading" && renderLoading()}
        {verificationStatus === "missing-organization" && renderError("Aucune société n’est associée à ce compte.", () => navigate("/configuration-societe"))}
        {verificationStatus === "error" && renderError(verificationError, () => setVerificationAttempt((attempt) => attempt + 1))}
        {verificationStatus === "missing-products" && (
          <section className="station-setup-requirement-card">
            <span className="station-setup-requirement-icon"><PackagePlus size={31} /></span>
            <span className="station-setup-kicker">PRÉREQUIS PRODUIT</span>
            <h2>Vous devez configurer au moins un produit avant de créer une station.</h2>
            <p>Les catégories et produits forment le catalogue permanent de l’organisation et seront réutilisés dans toutes vos stations.</p>
            <div className="station-setup-actions">
              <button type="button" className="station-setup-secondary" onClick={() => navigate("/superviseur/dashboard")}><ArrowLeft size={17} />Retour au tableau de bord</button>
              <button type="button" className="station-setup-primary" onClick={openProductCatalog}>Gérer les produits<ArrowRight size={17} /></button>
            </div>
          </section>
        )}
        {verificationStatus === "ready" && (isWizardOpen
          ? <StationWizard organizationId={organizationId} onBackToPreparation={() => setIsWizardOpen(false)} />
          : renderReadyState())}
      </main>
    </SupervisorLayout>
  );
}

export default StationSetupEntryPage;
