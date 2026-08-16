import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { X } from "lucide-react";

import "./AppModal.css";

function AppModal({
  isOpen,
  title,
  description,
  size = "lg",
  children,
  footer,
  closeOnOverlay = true,
  closeOnEscape = true,
  onClose,
}) {
  const { t } = useTranslation("common");

  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    const handleKeyDown = (event) => {
      if (closeOnEscape && event.key === "Escape") {
        onClose?.();
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, closeOnEscape, onClose]);

  if (!isOpen) {
    return null;
  }

  const handleOverlayClick = (event) => {
    if (
      closeOnOverlay &&
      event.target === event.currentTarget
    ) {
      onClose?.();
    }
  };

  return (
    <div
      className="app-modal-overlay"
      role="presentation"
      onMouseDown={handleOverlayClick}
    >
      <section
        className={`app-modal app-modal-${size}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="app-modal-title"
        aria-describedby={
          description ? "app-modal-description" : undefined
        }
      >
        <header className="app-modal-header">
          <div className="app-modal-heading">
            <h2 id="app-modal-title">{title}</h2>

            {description && (
              <p id="app-modal-description">
                {description}
              </p>
            )}
          </div>

          <button
            type="button"
            className="app-modal-close"
            aria-label={t("modal.closeWindow")}
            onClick={onClose}
          >
            <X size={20} />
          </button>
        </header>

        <div className="app-modal-body">
          {children}
        </div>

        {footer && (
          <footer className="app-modal-footer">
            {footer}
          </footer>
        )}
      </section>
    </div>
  );
}

export default AppModal;