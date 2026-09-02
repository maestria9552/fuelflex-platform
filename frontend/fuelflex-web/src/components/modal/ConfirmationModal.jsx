import {
  AlertTriangle,
  CircleAlert,
  HelpCircle,
  LoaderCircle,
} from "lucide-react";
import { useTranslation } from "react-i18next";
import AppModal from "./AppModal";
import "./ConfirmationModal.css";
const variantIcons = {
  default: HelpCircle,
  warning: AlertTriangle,
  danger: CircleAlert,
};
function ConfirmationModal({
  isOpen,
  title,
  description,
  confirmLabel,
  loadingLabel,
  cancelLabel,
  variant = "default",
  isLoading = false,
  confirmDisabled = false,
  errorMessage = "",
  children,
  onConfirm,
  onClose,
}) {
  const { t } = useTranslation("common");
  const normalizedVariant = variantIcons[variant] ? variant : "default";
  const Icon = variantIcons[normalizedVariant];
  const safeClose = () => {
    if (!isLoading) onClose?.();
  };
  const safeConfirm = () => {
    if (!isLoading) onConfirm?.();
  };
  return (
    <AppModal
      isOpen={isOpen}
      title={title}
      description={description}
      size="sm"
      modalType="attention"
      headerIcon={Icon}
      isLoading={isLoading}
      closeOnOverlay={!isLoading}
      closeOnEscape={!isLoading}
      onClose={safeClose}
      footer={
        <>
          <button
            type="button"
            className="confirmation-modal-cancel"
            onClick={safeClose}
            disabled={isLoading || confirmDisabled}
          >
            {cancelLabel ?? t("actions.no", { defaultValue: "No" })}
          </button>
          <button
            type="button"
            className={
              "confirmation-modal-confirm confirmation-modal-confirm-" +
              normalizedVariant
            }
            onClick={safeConfirm}
            disabled={isLoading}
          >
            {isLoading && (
              <LoaderCircle className="confirmation-modal-spinner" size={18} />
            )}
            {isLoading
              ? (loadingLabel ?? t("actions.confirming"))
              : (confirmLabel ?? t("actions.ok", { defaultValue: "OK" }))}
          </button>
        </>
      }
    >
      {children}
      {errorMessage && (
        <div className="confirmation-modal-error" role="alert">
          <CircleAlert size={18} />
          <span>{errorMessage}</span>
        </div>
      )}
    </AppModal>
  );
}
export default ConfirmationModal;
