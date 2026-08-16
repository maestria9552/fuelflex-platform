import { useState } from "react";
import { Trans, useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, Building2, Check, Eye, EyeOff, LockKeyhole, Mail, Phone, User } from "lucide-react";

import "./RegisterPage.css";
import fuelFlexLogo from "../assets/images/logofuelflex.png";

function RegisterPage() {
  const { t } = useTranslation(["auth", "common"]);
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [formData, setFormData] = useState({ firstName: "", lastName: "", email: "", phone: "", password: "", confirmPassword: "" });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const passwordsMatch = formData.password.length > 0 && formData.confirmPassword.length > 0 && formData.password === formData.confirmPassword;
  const confirmationStarted = formData.confirmPassword.length > 0;
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((previousData) => ({ ...previousData, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);
    if (!passwordsMatch) {
      setErrorMessage({ key: "auth:errors.passwordsMismatch" });
      return;
    }
    const payload = {
      firstName: formData.firstName.trim(), lastName: formData.lastName.trim(),
      email: formData.email.trim(), phone: formData.phone.trim(),
      password: formData.password, confirmPassword: formData.confirmPassword,
    };
    try {
      setIsSubmitting(true);
      const response = await fetch("/api/v1/auth/register", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
      const contentType = response.headers.get("content-type");
      const result = contentType?.includes("application/json") ? await response.json() : await response.text();
      if (!response.ok) {
        const backendMessage = typeof result === "string" ? result : result?.message || result?.error;
        setErrorMessage(backendMessage ? { text: backendMessage } : { key: "auth:errors.registrationFailed" });
        return;
      }
      const backendMessage = typeof result === "object" ? result?.message : null;
      setSuccessMessage(backendMessage ? { text: backendMessage } : { key: "auth:success.accountCreated" });
      sessionStorage.setItem("fuelflex_verification_email", payload.email);
      setTimeout(() => navigate("/verification-email"), 1200);
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: "auth:errors.serverUnavailable" });
    } finally {
      setIsSubmitting(false);
    }
  };

  const passwordStatus = !confirmationStarted ? t("auth:register.confirmPasswordPrompt") : passwordsMatch ? t("auth:register.passwordsMatch") : t("auth:register.passwordsMismatch");

  return (
    <main className="register-page">
      <section className="register-visual">
        <div className="brand"><img src={fuelFlexLogo} alt={t("common:brand.logoAlt")} className="brand-logo" /><div className="brand-wordmark"><span className="brand-fuel">FUEL</span><span className="brand-flex">FLEX</span><small>FUEL STATION</small></div></div>
        <div className="visual-content"><p className="visual-kicker">{t("auth:register.kicker")}</p><h1><Trans i18nKey="register.visualTitle" ns="auth" components={{ highlight: <span /> }} /></h1><p className="visual-description">{t("auth:register.visualDescription")}</p></div>
        <div className="visual-footer"><span>{t("auth:register.benefits.centralized")}</span><span>{t("auth:register.benefits.realtime")}</span><span>{t("auth:register.benefits.security")}</span></div>
      </section>
      <section className="register-panel">
        <div className="register-card">
          <div className="register-heading"><div className="register-icon"><Building2 size={24} /></div><h2>{t("auth:register.title")}</h2><p>{t("auth:register.subtitle")}</p></div>
          <form className="register-form" onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group"><label htmlFor="firstName">{t("auth:register.firstNameLabel")}</label><div className="input-wrapper"><User className="input-icon" size={19} /><input id="firstName" type="text" name="firstName" placeholder={t("auth:register.firstNamePlaceholder")} value={formData.firstName} onChange={handleChange} required /></div></div>
              <div className="form-group"><label htmlFor="lastName">{t("auth:register.lastNameLabel")}</label><div className="input-wrapper"><User className="input-icon" size={19} /><input id="lastName" type="text" name="lastName" placeholder={t("auth:register.lastNamePlaceholder")} value={formData.lastName} onChange={handleChange} required /></div></div>
            </div>
            <div className="form-group"><label htmlFor="email">{t("auth:register.emailLabel")}</label><div className="input-wrapper"><Mail className="input-icon" size={19} /><input id="email" type="email" name="email" placeholder={t("auth:register.emailPlaceholder")} value={formData.email} onChange={handleChange} required /></div></div>
            <div className="form-group"><label htmlFor="phone">{t("auth:register.phoneLabel")}</label><div className="input-wrapper"><Phone className="input-icon" size={19} /><input id="phone" type="tel" name="phone" placeholder={t("auth:register.phonePlaceholder")} value={formData.phone} onChange={handleChange} required /></div></div>
            <div className="form-row">
              <div className="form-group"><label htmlFor="password">{t("auth:register.passwordLabel")}</label><div className="input-wrapper"><LockKeyhole className="input-icon" size={19} /><input id="password" type={showPassword ? "text" : "password"} name="password" placeholder={t("auth:register.passwordPlaceholder")} value={formData.password} onChange={handleChange} minLength={8} required /><button type="button" className="password-toggle" onClick={() => setShowPassword((value) => !value)} aria-label={t("auth:register.togglePassword")}>{showPassword ? <EyeOff size={19} /> : <Eye size={19} />}</button></div></div>
              <div className="form-group"><label htmlFor="confirmPassword">{t("auth:register.confirmationLabel")}</label><div className="input-wrapper"><LockKeyhole className="input-icon" size={19} /><input id="confirmPassword" type={showConfirmation ? "text" : "password"} name="confirmPassword" placeholder={t("auth:register.confirmationPlaceholder")} value={formData.confirmPassword} onChange={handleChange} minLength={8} required /><button type="button" className="password-toggle" onClick={() => setShowConfirmation((value) => !value)} aria-label={t("auth:register.toggleConfirmation")}>{showConfirmation ? <EyeOff size={19} /> : <Eye size={19} />}</button></div></div>
            </div>
            <div className={`password-match ${!confirmationStarted ? "waiting" : passwordsMatch ? "success" : "error"}`}><div className="match-bar"><span /></div><p>{passwordsMatch && <Check size={16} />}{passwordStatus}</p></div>
            {errorMessage && <div className="form-message error" role="alert">{renderMessage(errorMessage)}</div>}
            {successMessage && <div className="form-message success" role="status">{renderMessage(successMessage)}</div>}
            <button className="register-button" type="submit" disabled={!passwordsMatch || isSubmitting}>{isSubmitting ? t("auth:register.submitting") : t("auth:register.submit")}{!isSubmitting && <ArrowRight size={19} />}</button>
          </form>
          <p className="login-link">{t("auth:register.alreadyRegistered")} {" "}<Link to="/connexion">{t("auth:register.login")}</Link></p>
        </div>
        <p className="copyright">{t("common:brand.copyright")}</p>
      </section>
    </main>
  );
}

export default RegisterPage;
