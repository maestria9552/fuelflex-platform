import { ArrowLeft, ArrowRight, Building2, LoaderCircle } from "lucide-react";
import { useTranslation } from "react-i18next";

import StationForm from "../../station/components/StationForm";

function StationStep({ station, isSaving, errorMessage, successMessage, onBack, onSubmit }) {
  const { t } = useTranslation("stationSetup");
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading">
        <span><Building2 size={24} /></span>
        <div>
          <small>{t("steps.progress", { current: 1, total: 9 })}</small>
          <h2>{t("steps.station.title")}</h2>
          <p>{t("steps.station.description")}</p>
        </div>
      </div>
      {errorMessage && <div className="station-wizard-alert error" role="alert">{errorMessage}</div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      <StationForm station={station} formId="station-wizard-form" isSaving={isSaving} onSubmit={onSubmit}>
        <div className="station-wizard-actions">
          <button type="button" className="station-setup-secondary" onClick={onBack} disabled={isSaving}><ArrowLeft size={17} /> {t("steps.station.back")}</button>
          <button type="submit" className="station-setup-primary" disabled={isSaving}>{isSaving ? <><LoaderCircle className="station-setup-spinner" size={18} />{t("steps.station.saving")}</> : <>{t(station ? "steps.station.updateContinue" : "steps.station.createContinue")}<ArrowRight size={17} /></>}</button>
        </div>
      </StationForm>
    </section>
  );
}

export default StationStep;
