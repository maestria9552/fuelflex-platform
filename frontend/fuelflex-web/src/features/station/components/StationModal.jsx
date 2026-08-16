import { useState } from "react";
import { useTranslation } from "react-i18next";
import { LoaderCircle, Save } from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { updateStation } from "../../../services/station/stationService";
import StationForm from "./StationForm";
import "../../product/components/ProductFormModal.css";

function StationModal({ isOpen, organizationId, station, onClose, onSaved }) {
  const { t } = useTranslation(["stations", "common"]);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  const handleSubmit = async (payload) => {
    if (isSaving || !station?.id) return;
    setIsSaving(true);
    setErrorMessage(null);
    try {
      const savedStation = await updateStation(organizationId, station.id, payload);
      onSaved?.(savedStation);
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: "stations:feedback.updateFailed" });
    } finally { setIsSaving(false); }
  };

  const safeClose = () => { if (!isSaving) onClose?.(); };

  return <AppModal isOpen={isOpen} title={t("stations:modal.title")} description={t("stations:modal.description")} size="lg" closeOnOverlay={!isSaving} closeOnEscape={!isSaving} onClose={safeClose} footer={<><button type="button" className="product-form-modal-cancel" onClick={safeClose} disabled={isSaving}>{t("common:actions.cancel")}</button><button type="submit" form="station-modal-form" className="product-form-modal-save" disabled={isSaving}>{isSaving ? <><LoaderCircle className="product-form-modal-spinner" size={18} />{t("stations:modal.saving")}</> : <><Save size={18} />{t("common:actions.save")}</>}</button></>}>
    {errorMessage && <div className="product-form-modal-alert" role="alert">{renderMessage(errorMessage)}</div>}
    <StationForm station={station} formId="station-modal-form" isSaving={isSaving} onSubmit={handleSubmit} />
  </AppModal>;
}

export default StationModal;
