import { AlertCircle, LoaderCircle } from "lucide-react";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import AppModal from "../../../components/modal/AppModal";

function ValidationCommentModal({
  isOpen,
  title,
  description,
  confirmLabel,
  variant = "warning",
  isLoading = false,
  errorMessage = "",
  onClose,
  onConfirm,
}) {
  const { t } = useTranslation(["pumpAttendantValidation", "common"]);
  const [comment, setComment] = useState("");

  const submit = (event) => {
    event.preventDefault();
    const normalized = comment.trim().replace(/\s+/g, " ");
    if (normalized) onConfirm(normalized);
  };

  return (
    <AppModal
      isOpen={isOpen}
      title={title}
      description={description}
      size="sm"
      closeOnEscape={!isLoading}
      closeOnOverlay={!isLoading}
      onClose={onClose}
      footer={(
        <>
          <button
            type="button"
            className="validation-secondary"
            onClick={onClose}
            disabled={isLoading}
          >
            {t("common:actions.cancel")}
          </button>
          <button
            type="submit"
            form="validation-comment-form"
            className={`validation-primary ${variant}`}
            disabled={isLoading || !comment.trim()}
          >
            {isLoading && <LoaderCircle className="validation-spin" size={17} />}
            {confirmLabel}
          </button>
        </>
      )}
    >
      <form
        id="validation-comment-form"
        className="validation-form"
        onSubmit={submit}
      >
        <label>
          <span>{t("pumpAttendantValidation:fields.reason")}</span>
          <textarea
            value={comment}
            onChange={(event) => setComment(event.target.value)}
            maxLength={1000}
            rows={5}
            required
            autoFocus
          />
        </label>
        {errorMessage && (
          <div className="validation-alert error" role="alert">
            <AlertCircle size={17} />
            {errorMessage}
          </div>
        )}
      </form>
    </AppModal>
  );
}

export default ValidationCommentModal;
