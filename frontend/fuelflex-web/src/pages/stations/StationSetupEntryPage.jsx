import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation(["stationSetup", "common", "stations"]);
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
        setVerificationError(error?.message || t("entry.verificationFallback"));
        setVerificationStatus("error");
      });

    return () => controller.abort();
  }, [organizationId, t, verificationAttempt]);

  const handleStartWizard = () => {
    saveStationSetupDraft(organizationId, { activeStep: "station" });
    setIsWizardOpen(true);
  };

  const openProductCatalog = () => navigate("/superviseur/produits?returnTo=station-setup");

  const renderLoading = () => (
    <section className="station-setup-state-card">
      <LoaderCircle className="station-setup-spinner" size={36} />
      <h2>{t("entry.loadingTitle")}</h2>
      <p>{t("entry.loadingDescription")}</p>
    </section>
  );

  const renderError = (message, onRetry) => (
    <section className="station-setup-state-card station-setup-error-card">
      <span className="station-setup-state-icon"><AlertCircle size={30} /></span>
      <h2>{t("entry.verificationFailed")}</h2>
      <p>{message}</p>
      <button type="button" className="station-setup-primary" onClick={onRetry}><RefreshCw size={17} />{t("common:actions.retry")}</button>
    </section>
  );

  const renderReadyState = () => (
    <section className="station-setup-ready-card">
      <div className="station-setup-ready-icon"><ShieldCheck size={34} /></div>
      <span className="station-setup-kicker">{t("entry.readyKicker")}</span>
      <h2>{t("entry.readyTitle")}</h2>
      <p>{t("entry.readyDescription")}</p>
      <div className="station-setup-actions">
        <button type="button" className="station-setup-secondary" onClick={openProductCatalog}><PackagePlus size={17} />{t("entry.manageProducts")}</button>
        <button type="button" className="station-setup-primary" onClick={handleStartWizard}>{t("entry.start")}<ArrowRight size={17} /></button>
      </div>
    </section>
  );

  return (
    <SupervisorLayout>
      <main className="station-setup-page">
        <header className="station-setup-header">
          <div>
            <span className="station-setup-eyebrow">{t("entry.eyebrow")}</span>
            <h1>{t("entry.title")}</h1>
            <p>{t("entry.description")}</p>
          </div>
          <div className="station-setup-header-badge"><Fuel size={19} /><span>{t("entry.badge")}</span></div>
        </header>

        {verificationStatus === "loading" && renderLoading()}
        {verificationStatus === "missing-organization" && renderError(t("stations:feedback.organizationMissing"), () => navigate("/configuration-societe"))}
        {verificationStatus === "error" && renderError(verificationError, () => setVerificationAttempt((attempt) => attempt + 1))}
        {verificationStatus === "missing-products" && (
          <section className="station-setup-requirement-card">
            <span className="station-setup-requirement-icon"><PackagePlus size={31} /></span>
            <span className="station-setup-kicker">{t("entry.productKicker")}</span>
            <h2>{t("entry.productRequiredTitle")}</h2>
            <p>{t("entry.productRequiredDescription")}</p>
            <div className="station-setup-actions">
              <button type="button" className="station-setup-secondary" onClick={() => navigate("/superviseur/dashboard")}><ArrowLeft size={17} />{t("entry.backToDashboard")}</button>
              <button type="button" className="station-setup-primary" onClick={openProductCatalog}>{t("entry.manageProducts")}<ArrowRight size={17} /></button>
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
