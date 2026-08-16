import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  LoaderCircle,
  RefreshCw,
  Save,
  UserRound,
} from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { mergeStoredUser } from "../../../services/auth/authStorage";
import {
  getMyProfile,
  updateMyProfile,
} from "../../../services/profile/profileService";
import "./ProfileModal.css";

const EMPTY_FORM = {
  firstName: "",
  lastName: "",
  email: "",
  phoneNumber: "",
};

function getInitials(firstName, lastName, email) {
  const nameInitials = [firstName, lastName]
    .map((value) => String(value || "").trim())
    .filter(Boolean)
    .map((value) => value.charAt(0).toUpperCase())
    .join("");

  if (nameInitials) {
    return nameInitials;
  }

  return String(email || "")
    .split("@")[0]
    .split(/[._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((value) => value.charAt(0).toUpperCase())
    .join("");
}

function mapProfileToForm(profile) {
  return {
    firstName: profile?.firstName || "",
    lastName: profile?.lastName || "",
    email: profile?.email || "",
    phoneNumber: profile?.phoneNumber || "",
  };
}

function ProfileModal({ isOpen, onClose, onSaved }) {
  const { t } = useTranslation(["profile", "common"]);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [loadAttempt, setLoadAttempt] = useState(0);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    const controller = new AbortController();

    async function loadProfile() {
      setIsLoading(true);
      setErrorMessage("");
      setSuccessMessage("");

      try {
        const profile = await getMyProfile({
          signal: controller.signal,
        });

        setFormData(mapProfileToForm(profile));
      } catch (error) {
        if (error?.name !== "AbortError") {
          setErrorMessage(error?.message || t("profile:feedback.loadError"));
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    loadProfile();

    return () => controller.abort();
  }, [isOpen, loadAttempt, t]);

  const handleClose = () => {
    if (isSaving) {
      return;
    }

    setErrorMessage("");
    setSuccessMessage("");
    onClose?.();
  };

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }));
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const payload = {
      firstName: formData.firstName.trim(),
      lastName: formData.lastName.trim(),
      phoneNumber: formData.phoneNumber.trim(),
    };

    if (!payload.firstName || !payload.lastName || !payload.phoneNumber) {
      setErrorMessage(t("profile:validation.requiredFields"));
      return;
    }

    setIsSaving(true);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      const profile = await updateMyProfile(payload);
      const updatedUser = mergeStoredUser({
        firstName: profile.firstName,
        lastName: profile.lastName,
        email: profile.email,
        phoneNumber: profile.phoneNumber,
      });

      setFormData(mapProfileToForm(profile));
      setSuccessMessage(t("profile:feedback.saved"));
      onSaved?.(profile, updatedUser);
    } catch (error) {
      setErrorMessage(error?.message || t("profile:feedback.saveError"));
    } finally {
      setIsSaving(false);
    }
  };

  const initials = getInitials(
    formData.firstName,
    formData.lastName,
    formData.email,
  );

  const footer = !isLoading && (
    <>
      <button
        type="button"
        className="profile-modal-cancel"
        onClick={handleClose}
        disabled={isSaving}
      >
        {t("common:actions.cancel")}
      </button>
      <button
        type="submit"
        form="profile-form"
        className="profile-modal-save"
        disabled={isSaving || Boolean(errorMessage && !formData.email)}
      >
        {isSaving ? (
          <>
            <LoaderCircle
              size={18}
              className="profile-modal-spinner"
              aria-hidden="true"
            />
            {t("profile:actions.saving")}
          </>
        ) : (
          <>
            <Save size={18} aria-hidden="true" />
            {t("common:actions.save")}
          </>
        )}
      </button>
    </>
  );

  return (
    <AppModal
      isOpen={isOpen}
      title={t("profile:title")}
      description={t("profile:description")}
      size="md"
      closeOnOverlay={!isSaving}
      closeOnEscape={!isSaving}
      onClose={handleClose}
      footer={footer}
    >
      {isLoading ? (
        <div className="profile-modal-loading" role="status">
          <LoaderCircle
            size={24}
            className="profile-modal-spinner"
            aria-hidden="true"
          />
          <span>{t("profile:feedback.loading")}</span>
        </div>
      ) : errorMessage && !formData.email ? (
        <div className="profile-modal-load-error" role="alert">
          <p>{errorMessage}</p>
          <button
            type="button"
            onClick={() => setLoadAttempt((attempt) => attempt + 1)}
          >
            <RefreshCw size={17} aria-hidden="true" />
            {t("common:actions.retry")}
          </button>
        </div>
      ) : (
        <form id="profile-form" onSubmit={handleSubmit}>
          <div className="profile-modal-identity">
            <span className="profile-modal-avatar" aria-hidden="true">
              {initials || <UserRound size={24} />}
            </span>
            <div>
              <strong>
                {[formData.firstName, formData.lastName]
                  .filter(Boolean)
                  .join(" ") || formData.email}
              </strong>
              <span>{formData.email}</span>
            </div>
          </div>

          {errorMessage && (
            <div className="profile-modal-alert error" role="alert">
              {errorMessage}
            </div>
          )}
          {successMessage && (
            <div className="profile-modal-alert success" role="status">
              {successMessage}
            </div>
          )}

          <div className="profile-modal-grid">
            <label>
              <span>{t("profile:fields.firstName")}</span>
              <input
                autoFocus
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                maxLength={100}
                required
                disabled={isSaving}
              />
            </label>

            <label>
              <span>{t("profile:fields.lastName")}</span>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                maxLength={100}
                required
                disabled={isSaving}
              />
            </label>

            <label className="profile-modal-field-full">
              <span>{t("profile:fields.email")}</span>
              <input
                type="email"
                value={formData.email}
                readOnly
                aria-describedby="profile-email-help"
              />
              <small id="profile-email-help">
                {t("profile:fields.emailReadOnly")}
              </small>
            </label>

            <label className="profile-modal-field-full">
              <span>{t("profile:fields.phoneNumber")}</span>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                maxLength={30}
                required
                disabled={isSaving}
                placeholder={t("profile:fields.phonePlaceholder")}
              />
            </label>
          </div>
        </form>
      )}
    </AppModal>
  );
}

export default ProfileModal;
