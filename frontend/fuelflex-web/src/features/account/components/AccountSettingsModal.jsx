import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  CheckCircle2,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
  RefreshCw,
} from "lucide-react";

import AppModal from "../../../components/modal/AppModal";
import { formatDateTime } from "../../../i18n/formatters";
import {
  changeMyPassword,
  getMyAccount,
} from "../../../services/account/accountService";
import "./AccountSettingsModal.css";

const EMPTY_PASSWORDS = {
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
};

function AccountSettingsModal({ isOpen, onClose }) {
  const { t, i18n } = useTranslation(["account", "common"]);
  const [account, setAccount] = useState(null);
  const [passwords, setPasswords] = useState(EMPTY_PASSWORDS);
  const [visibleFields, setVisibleFields] = useState({});
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

    async function loadAccount() {
      setIsLoading(true);
      setErrorMessage("");
      setSuccessMessage("");

      try {
        const result = await getMyAccount({
          signal: controller.signal,
        });
        setAccount(result);
      } catch (error) {
        if (error?.name !== "AbortError") {
          setErrorMessage(error?.message || t("account:feedback.loadError"));
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    loadAccount();
    return () => controller.abort();
  }, [isOpen, loadAttempt, t]);

  const resetSensitiveState = () => {
    setPasswords(EMPTY_PASSWORDS);
    setVisibleFields({});
  };

  const handleClose = () => {
    if (isSaving) {
      return;
    }

    resetSensitiveState();
    setErrorMessage("");
    setSuccessMessage("");
    onClose?.();
  };

  const handlePasswordChange = (event) => {
    const { name, value } = event.target;

    setPasswords((currentPasswords) => ({
      ...currentPasswords,
      [name]: value,
    }));
    setErrorMessage("");
    setSuccessMessage("");
  };

  const toggleVisibility = (fieldName) => {
    setVisibleFields((currentFields) => ({
      ...currentFields,
      [fieldName]: !currentFields[fieldName],
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (
      !passwords.currentPassword
      || !passwords.newPassword
      || !passwords.confirmPassword
    ) {
      setErrorMessage(t("account:validation.requiredFields"));
      return;
    }

    if (passwords.newPassword.length < 8) {
      setErrorMessage(t("account:validation.minimumLength"));
      return;
    }

    if (passwords.newPassword !== passwords.confirmPassword) {
      setErrorMessage(t("account:validation.passwordMismatch"));
      return;
    }

    if (passwords.currentPassword === passwords.newPassword) {
      setErrorMessage(t("account:validation.passwordUnchanged"));
      return;
    }

    setIsSaving(true);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      const updatedAccount = await changeMyPassword({
        currentPassword: passwords.currentPassword,
        newPassword: passwords.newPassword,
      });

      setAccount(updatedAccount);
      resetSensitiveState();
      setSuccessMessage(t("account:feedback.passwordChanged"));
    } catch (error) {
      setErrorMessage(error?.message || t("account:feedback.saveError"));
    } finally {
      setIsSaving(false);
    }
  };

  const formatAccountDate = (value) => {
    if (!value) {
      return t("account:information.notAvailable");
    }

    return formatDateTime(value, {
      language: i18n.resolvedLanguage || i18n.language,
    });
  };

  const renderPasswordField = ({
    name,
    label,
    autoComplete,
    autoFocus = false,
  }) => {
    const isVisible = Boolean(visibleFields[name]);
    const visibilityLabel = t(
      isVisible
        ? "account:accessibility.hidePassword"
        : "account:accessibility.showPassword",
      { field: label },
    );

    return (
      <label className="account-settings-password-field">
        <span>{label}</span>
        <span className="account-settings-password-control">
          <input
            autoFocus={autoFocus}
            type={isVisible ? "text" : "password"}
            name={name}
            value={passwords[name]}
            onChange={handlePasswordChange}
            autoComplete={autoComplete}
            aria-describedby={
              name === "currentPassword"
                ? undefined
                : "account-password-help"
            }
            required
            minLength={name === "currentPassword" ? undefined : 8}
            disabled={isSaving}
          />
          <button
            type="button"
            onClick={() => toggleVisibility(name)}
            aria-label={visibilityLabel}
            title={visibilityLabel}
            aria-pressed={isVisible}
            disabled={isSaving}
          >
            {isVisible ? (
              <EyeOff size={18} aria-hidden="true" />
            ) : (
              <Eye size={18} aria-hidden="true" />
            )}
          </button>
        </span>
      </label>
    );
  };

  const footer = !isLoading && account && (
    <>
      <button
        type="button"
        className="account-settings-cancel"
        onClick={handleClose}
        disabled={isSaving}
      >
        {t("common:actions.cancel")}
      </button>
      <button
        type="submit"
        form="account-password-form"
        className="account-settings-save"
        disabled={isSaving}
      >
        {isSaving && (
          <LoaderCircle
            size={18}
            className="account-settings-spinner"
            aria-hidden="true"
          />
        )}
        {t(
          isSaving
            ? "account:actions.saving"
            : "account:actions.changePassword",
        )}
      </button>
    </>
  );

  return (
    <AppModal
      isOpen={isOpen}
      title={t("account:title")}
      description={t("account:description")}
      size="md"
      closeOnOverlay={!isSaving}
      closeOnEscape={!isSaving}
      onClose={handleClose}
      footer={footer}
    >
      {isLoading ? (
        <div className="account-settings-loading" role="status">
          <LoaderCircle
            size={24}
            className="account-settings-spinner"
            aria-hidden="true"
          />
          <span>{t("account:feedback.loading")}</span>
        </div>
      ) : errorMessage && !account ? (
        <div className="account-settings-load-error" role="alert">
          <p>{errorMessage}</p>
          <button
            type="button"
            onClick={() => setLoadAttempt((attempt) => attempt + 1)}
          >
            <RefreshCw size={17} aria-hidden="true" />
            {t("common:actions.retry")}
          </button>
        </div>
      ) : account ? (
        <div className="account-settings-content">
          <section
            className="account-settings-section"
            aria-labelledby="account-information-title"
          >
            <header>
              <Mail size={20} aria-hidden="true" />
              <div>
                <h3 id="account-information-title">
                  {t("account:information.title")}
                </h3>
                <p>{t("account:information.description")}</p>
              </div>
            </header>

            <dl className="account-settings-details">
              <div>
                <dt>{t("account:information.email")}</dt>
                <dd>{account.email}</dd>
              </div>
              <div>
                <dt>{t("account:information.emailStatus")}</dt>
                <dd>
                  <span
                    className={[
                      "account-settings-status",
                      account.emailVerified ? "verified" : "unverified",
                    ].join(" ")}
                  >
                    {account.emailVerified && (
                      <CheckCircle2 size={14} aria-hidden="true" />
                    )}
                    {t(
                      account.emailVerified
                        ? "account:information.verified"
                        : "account:information.unverified",
                    )}
                  </span>
                </dd>
              </div>
              <div>
                <dt>{t("account:information.lastLogin")}</dt>
                <dd>{formatAccountDate(account.lastLoginAt)}</dd>
              </div>
              <div>
                <dt>{t("account:information.passwordChangedAt")}</dt>
                <dd>{formatAccountDate(account.passwordChangedAt)}</dd>
              </div>
            </dl>
          </section>

          <section
            className="account-settings-section"
            aria-labelledby="account-security-title"
          >
            <header>
              <LockKeyhole size={20} aria-hidden="true" />
              <div>
                <h3 id="account-security-title">
                  {t("account:security.title")}
                </h3>
                <p>{t("account:security.description")}</p>
              </div>
            </header>

            {errorMessage && (
              <div className="account-settings-alert error" role="alert">
                {errorMessage}
              </div>
            )}
            {successMessage && (
              <div className="account-settings-alert success" role="status">
                {successMessage}
              </div>
            )}

            <form id="account-password-form" onSubmit={handleSubmit}>
              {renderPasswordField({
                name: "currentPassword",
                label: t("account:fields.currentPassword"),
                autoComplete: "current-password",
                autoFocus: true,
              })}
              {renderPasswordField({
                name: "newPassword",
                label: t("account:fields.newPassword"),
                autoComplete: "new-password",
              })}
              {renderPasswordField({
                name: "confirmPassword",
                label: t("account:fields.confirmPassword"),
                autoComplete: "new-password",
              })}
              <p id="account-password-help">
                {t("account:security.passwordRule")}
              </p>
            </form>
          </section>
        </div>
      ) : null}
    </AppModal>
  );
}

export default AccountSettingsModal;
