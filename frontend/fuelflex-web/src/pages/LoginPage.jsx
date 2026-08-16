import { useState } from "react";
import { Trans, useTranslation } from "react-i18next";
import {
  ArrowRight,
  Eye,
  EyeOff,
  LockKeyhole,
  Mail,
  ShieldCheck,
} from "lucide-react";
import { Link, useNavigate } from "react-router-dom";

import "./LoginPage.css";
import fuelFlexLogo from "../assets/images/logofuelflex.png";

function LoginPage() {
  const { t } = useTranslation(["auth", "common"]);
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    rememberMe: false,
  });
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const renderMessage = (message) =>
    message?.key ? t(message.key) : message?.text || "";

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((previousData) => ({
      ...previousData,
      [name]: type === "checkbox" ? checked : value,
    }));
    if (errorMessage) setErrorMessage(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    const payload = {
      email: formData.email.trim().toLowerCase(),
      password: formData.password,
    };

    try {
      const response = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const contentType = response.headers.get("content-type");
      const result = contentType?.includes("application/json")
        ? await response.json()
        : await response.text();

      if (!response.ok) {
        const backendMessage = typeof result === "string"
          ? result
          : result?.message || result?.error;
        setErrorMessage(
          backendMessage
            ? { text: backendMessage }
            : { key: "auth:errors.invalidCredentials" }
        );
        return;
      }

      const storage = formData.rememberMe ? localStorage : sessionStorage;
      localStorage.removeItem("fuelflex_access_token");
      localStorage.removeItem("fuelflex_user");
      sessionStorage.removeItem("fuelflex_access_token");
      sessionStorage.removeItem("fuelflex_user");
      storage.setItem("fuelflex_access_token", result.accessToken);
      storage.setItem("fuelflex_user", JSON.stringify({
        userId: result.userId,
        firstName: result.firstName,
        lastName: result.lastName,
        email: result.email,
        roles: result.roles || [],
        permissions: result.permissions || [],
        tokenType: result.tokenType,
        expiresIn: result.expiresIn,
        organizationConfigured: result.organizationConfigured === true,
        organizationId: result.organizationId || null,
      }));
      navigate("/dashboard", { replace: true });
    } catch (error) {
      setErrorMessage(
        error?.message
          ? { text: error.message }
          : { key: "auth:errors.serverUnavailableFuelFlex" }
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="login-page">
      <section className="login-visual">
        <div className="login-brand">
          <img src={fuelFlexLogo} alt={t("common:brand.logoAlt")} />
          <div className="login-brand-wordmark">
            <span>FUEL</span><strong>FLEX</strong><small>PLATFORM</small>
          </div>
        </div>
        <div className="login-visual-content">
          <div className="login-visual-badge">
            <ShieldCheck size={17} />{t("auth:login.visualBadge")}
          </div>
          <h1><Trans i18nKey="login.visualTitle" ns="auth" components={{ highlight: <span /> }} /></h1>
          <p>{t("auth:login.visualDescription")}</p>
        </div>
        <div className="login-visual-footer">
          <span>{t("auth:login.benefits.realtime")}</span>
          <span>{t("auth:login.benefits.centralized")}</span>
          <span>{t("auth:login.benefits.secure")}</span>
        </div>
      </section>

      <section className="login-panel">
        <div className="login-card">
          <div className="login-heading">
            <div className="login-heading-icon"><LockKeyhole size={23} /></div>
            <p className="login-kicker">{t("auth:login.kicker")}</p>
            <h2>{t("auth:login.title")}</h2>
            <p className="login-subtitle">{t("auth:login.subtitle")}</p>
          </div>
          {errorMessage && <div className="login-alert" role="alert">{renderMessage(errorMessage)}</div>}
          <form className="login-form" onSubmit={handleSubmit}>
            <div className="login-form-group">
              <label htmlFor="email">{t("auth:login.emailLabel")}</label>
              <div className="login-input-wrapper">
                <Mail className="login-input-icon" size={19} />
                <input id="email" type="email" name="email" placeholder={t("auth:login.emailPlaceholder")} value={formData.email} onChange={handleChange} autoComplete="email" required />
              </div>
            </div>
            <div className="login-form-group">
              <div className="login-label-row">
                <label htmlFor="password">{t("auth:login.passwordLabel")}</label>
                <Link to="/mot-de-passe-oublie">{t("auth:login.forgotPassword")}</Link>
              </div>
              <div className="login-input-wrapper">
                <LockKeyhole className="login-input-icon" size={19} />
                <input id="password" type={showPassword ? "text" : "password"} name="password" placeholder={t("auth:login.passwordPlaceholder")} value={formData.password} onChange={handleChange} autoComplete="current-password" minLength={8} required />
                <button type="button" className="login-password-toggle" onClick={() => setShowPassword((value) => !value)} aria-label={t(showPassword ? "auth:login.hidePassword" : "auth:login.showPassword")}>
                  {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                </button>
              </div>
            </div>
            <label className="remember-option">
              <input type="checkbox" name="rememberMe" checked={formData.rememberMe} onChange={handleChange} />
              <span className="custom-checkbox" />
              <span>{t("auth:login.rememberMe")}</span>
            </label>
            <button className="login-submit-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? <><span className="login-spinner" />{t("auth:login.submitting")}</> : <>{t("auth:login.submit")}<ArrowRight size={19} /></>}
            </button>
          </form>
          <div className="login-separator"><span /><p>{t("auth:login.newUser")}</p><span /></div>
          <Link to="/inscription" className="create-account-link">{t("auth:login.createSupervisorAccount")}<ArrowRight size={18} /></Link>
        </div>
        <p className="login-copyright">{t("common:brand.copyright")}</p>
      </section>
    </main>
  );
}

export default LoginPage;
