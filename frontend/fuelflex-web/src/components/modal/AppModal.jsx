import { useEffect, useEffectEvent, useId, useRef } from "react";
import { useTranslation } from "react-i18next";
import { LoaderCircle, X } from "lucide-react";
import "./AppModal.css";

function AppModal({
  isOpen,
  title,
  description,
  size = "lg",
  children,
  footer,
  modalType = "form",
  headerIcon: HeaderIcon,
  confirmLabel,
  loadingLabel,
  cancelLabel,
  isLoading = false,
  confirmDisabled = false,
  onConfirm,
  closeOnOverlay = true,
  closeOnEscape = true,
  onClose,
}) {
  const { t } = useTranslation("common");
  const titleId = useId();
  const descriptionId = useId();
  const closeButtonRef = useRef(null);
  const closeFromEffect = useEffectEvent(() => onClose?.());
  const isInformation = modalType === "information";
  const isAttention = modalType === "attention";
  const resolvedTitle =
    title ||
    t(isInformation ? "modal.information" : "modal.attention", {
      defaultValue: isInformation ? "Information" : "Attention",
    });
  const resolvedConfirmLabel =
    confirmLabel || t("actions.ok", { defaultValue: "OK" });
  const resolvedCancelLabel =
    cancelLabel || t("actions.no", { defaultValue: "No" });
  const resolvedLoadingLabel =
    loadingLabel || t("actions.confirming", { defaultValue: "Confirming…" });
  useEffect(() => {
    if (!isOpen) return undefined;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    closeButtonRef.current?.focus();
    const handleKeyDown = (event) => {
      if (closeOnEscape && event.key === "Escape" && !isLoading)
        closeFromEffect();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, closeOnEscape, isLoading]);
  if (!isOpen) return null;
  const handleOverlayClick = (event) => {
    if (closeOnOverlay && event.target === event.currentTarget && !isLoading)
      onClose?.();
  };
  const generatedFooter =
    (isInformation || isAttention) && !footer ? (
      <>
        {isAttention && (
          <button
            type="button"
            className="app-modal-action app-modal-action-no"
            onClick={onClose}
            disabled={isLoading}
          >
            {resolvedCancelLabel}
          </button>
        )}
        <button
          type="button"
          className="app-modal-action app-modal-action-ok"
          onClick={onConfirm || onClose}
          disabled={isLoading || confirmDisabled}
        >
          {isLoading && (
            <LoaderCircle className="app-modal-action-spinner" size={17} />
          )}
          {isLoading ? resolvedLoadingLabel : resolvedConfirmLabel}
        </button>
      </>
    ) : (
      footer
    );
  return (
    <div
      className="app-modal-overlay"
      role="presentation"
      onMouseDown={handleOverlayClick}
    >
      <section
        className={
          "app-modal app-modal-" + size + " app-modal-type-" + modalType
        }
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={description ? descriptionId : undefined}
      >
        <header className="app-modal-header">
          <div className="app-modal-heading">
            {HeaderIcon && (
              <span className="app-modal-header-icon" aria-hidden="true">
                <HeaderIcon size={19} />
              </span>
            )}
            <div>
              <h2 id={titleId}>{resolvedTitle}</h2>
              {description && <p id={descriptionId}>{description}</p>}
            </div>
          </div>
          <button
            ref={closeButtonRef}
            type="button"
            className="app-modal-close"
            aria-label={t("modal.closeWindow")}
            onClick={onClose}
            disabled={isLoading}
          >
            <X size={20} />
          </button>
        </header>
        <div className="app-modal-body">{children}</div>
        {generatedFooter && (
          <footer className="app-modal-footer">{generatedFooter}</footer>
        )}
      </section>
    </div>
  );
}
export default AppModal;
