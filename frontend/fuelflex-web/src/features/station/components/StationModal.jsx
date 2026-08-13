import { useState } from "react";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { updateStation } from "../../../services/station/stationService";
import StationForm from "./StationForm";
import "../../product/components/ProductFormModal.css";

function StationModal({ isOpen, organizationId, station, onClose, onSaved }) {
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = async (payload) => {
    if (isSaving || !station?.id) return;
    setIsSaving(true); setErrorMessage("");
    try {
      const savedStation = await updateStation(organizationId, station.id, payload);
      onSaved?.(savedStation);
    } catch (error) {
      setErrorMessage(error?.message || "Impossible de modifier la station.");
    } finally { setIsSaving(false); }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return <AppModal isOpen={isOpen} title="Modifier la station" description="Mettez à jour les informations générales et l’état opérationnel de la station." size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>Annuler</button><button type="submit" form="station-modal-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />Enregistrement...</> : <><Save size={18} />Enregistrer</>}</button></>}>
    {errorMessage && <div className="product-form-modal-alert" role="alert">{errorMessage}</div>}
    <StationForm station={station} formId="station-modal-form" isSaving={isSaving} onSubmit={handleSubmit} />
  </AppModal>;
}

export default StationModal;
