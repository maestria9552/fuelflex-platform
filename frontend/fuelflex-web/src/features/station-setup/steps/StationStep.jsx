import { ArrowLeft, ArrowRight, Building2, LoaderCircle } from "lucide-react";

import StationForm from "../../station/components/StationForm";

function StationStep({ station, isSaving, errorMessage, successMessage, onBack, onSubmit }) {
  return (
    <section className="station-wizard-panel">
      <div className="station-wizard-heading">
        <span><Building2 size={24} /></span>
        <div>
          <small>ÉTAPE 1 SUR 9</small>
          <h2>Informations de la station</h2>
          <p>Créez l’identité et les coordonnées principales de la station.</p>
        </div>
      </div>
      {errorMessage && <div className="station-wizard-alert error" role="alert">{errorMessage}</div>}
      {successMessage && <div className="station-wizard-alert success" role="status">{successMessage}</div>}
      <StationForm station={station} formId="station-wizard-form" isSaving={isSaving} onSubmit={onSubmit}>
        <div className="station-wizard-actions">
          <button type="button" className="station-setup-secondary" onClick={onBack} disabled={isSaving}><ArrowLeft size={17} /> Retour vers la préparation</button>
          <button type="submit" className="station-setup-primary" disabled={isSaving}>{isSaving ? <><LoaderCircle className="station-setup-spinner" size={18} />Enregistrement...</> : <>{station ? "Mettre à jour et continuer" : "Créer et continuer"}<ArrowRight size={17} /></>}</button>
        </div>
      </StationForm>
    </section>
  );
}

export default StationStep;
