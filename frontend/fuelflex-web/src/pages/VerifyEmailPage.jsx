import { useState } from "react";
import { Trans, useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, CheckCircle2, KeyRound, Mail, RefreshCw } from "lucide-react";

import "./RegisterPage.css";
import fuelFlexLogo from "../assets/images/logofuelflex.png";

function VerifyEmailPage() {
  const { t } = useTranslation(["auth", "common"]);
  const navigate = useNavigate();
  const [email, setEmail] = useState(sessionStorage.getItem("fuelflex_verification_email") || "");
  const [verificationCode, setVerificationCode] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const renderMessage = (message) => message?.key ? t(message.key) : message?.text || "";
  const readResponse = async (response) => response.headers.get("content-type")?.includes("application/json") ? response.json() : response.text();
  const extractBackendMessage = (result) => typeof result === "string" && result.trim() ? result : result?.message || result?.error || null;

  const handleVerification = async (event) => {
    event.preventDefault();
    setErrorMessage(null); setSuccessMessage(null);
    const normalizedEmail = email.trim().toLowerCase();
    const normalizedCode = verificationCode.trim();
    if (!normalizedEmail) { setErrorMessage({ key: "auth:errors.emailRequired" }); return; }
    if (!normalizedCode) { setErrorMessage({ key: "auth:errors.verificationCodeRequired" }); return; }
    try {
      setIsSubmitting(true);
      const response = await fetch("/api/v1/auth/verify-email", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email: normalizedEmail, verificationCode: normalizedCode }) });
      const result = await readResponse(response);
      if (!response.ok) { const message = extractBackendMessage(result); setErrorMessage(message ? { text: message } : { key: "auth:errors.verificationFailed" }); return; }
      sessionStorage.removeItem("fuelflex_verification_email");
      const message = typeof result === "object" ? result?.message : null;
      setSuccessMessage(message ? { text: message } : { key: "auth:success.emailVerified" });
      setTimeout(() => navigate("/connexion", { replace: true, state: { message: t("auth:success.accountVerifiedLogin"), email: normalizedEmail } }), 1200);
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: "auth:errors.verificationFailed" });
    } finally { setIsSubmitting(false); }
  };

  const handleResendCode = async () => {
    setErrorMessage(null); setSuccessMessage(null);
    const normalizedEmail = email.trim().toLowerCase();
    if (!normalizedEmail) { setErrorMessage({ key: "auth:errors.emailRequiredForResend" }); return; }
    try {
      setIsResending(true);
      const response = await fetch("/api/v1/auth/resend-code", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email: normalizedEmail }) });
      const result = await readResponse(response);
      if (!response.ok) { const message = extractBackendMessage(result); setErrorMessage(message ? { text: message } : { key: "auth:errors.resendFailed" }); return; }
      sessionStorage.setItem("fuelflex_verification_email", normalizedEmail);
      const message = typeof result === "object" ? result?.message : null;
      setSuccessMessage(message ? { text: message } : { key: "auth:success.codeResent" });
    } catch (error) {
      setErrorMessage(error?.message ? { text: error.message } : { key: "auth:errors.resendFailed" });
    } finally { setIsResending(false); }
  };

  return (
    <main className="register-page">
      <section className="register-visual">
        <div className="brand"><img src={fuelFlexLogo} alt={t("common:brand.logoAlt")} className="brand-logo" /><div className="brand-wordmark"><span className="brand-fuel">FUEL</span><span className="brand-flex">FLEX</span><small>FUEL STATION</small></div></div>
        <div className="visual-content"><p className="visual-kicker">{t("auth:verify.kicker")}</p><h1><Trans i18nKey="verify.visualTitle" ns="auth" components={{ highlight: <span /> }} /></h1><p className="visual-description">{t("auth:verify.visualDescription")}</p></div>
        <div className="visual-footer"><span>{t("auth:verify.benefits.secureAccount")}</span><span>{t("auth:verify.benefits.controlledAccess")}</span><span>{t("auth:verify.benefits.protectedData")}</span></div>
      </section>
      <section className="register-panel">
        <div className="register-card">
          <div className="register-heading"><div className="register-icon"><CheckCircle2 size={24} /></div><h2>{t("auth:verify.title")}</h2><p>{t("auth:verify.subtitle")}</p></div>
          <form className="register-form" onSubmit={handleVerification}>
            <div className="form-group"><label htmlFor="email">{t("auth:verify.emailLabel")}</label><div className="input-wrapper"><Mail className="input-icon" size={19} /><input id="email" type="email" name="email" placeholder={t("auth:verify.emailPlaceholder")} value={email} onChange={(event) => setEmail(event.target.value)} required /></div></div>
            <div className="form-group"><label htmlFor="verificationCode">{t("auth:verify.codeLabel")}</label><div className="input-wrapper"><KeyRound className="input-icon" size={19} /><input id="verificationCode" type="text" name="verificationCode" inputMode="numeric" autoComplete="one-time-code" placeholder={t("auth:verify.codePlaceholder")} value={verificationCode} onChange={(event) => setVerificationCode(event.target.value.replace(/\D/g, "").slice(0, 6))} maxLength={6} required /></div></div>
            {errorMessage && <div className="form-message error" role="alert">{renderMessage(errorMessage)}</div>}
            {successMessage && <div className="form-message success" role="status">{renderMessage(successMessage)}</div>}
            <button className="register-button" type="submit" disabled={isSubmitting || verificationCode.length !== 6}>{isSubmitting ? t("auth:verify.submitting") : t("auth:verify.submit")}{!isSubmitting && <ArrowRight size={19} />}</button>
            <button type="button" className="register-button" onClick={handleResendCode} disabled={isResending || isSubmitting}>{isResending ? t("auth:verify.resending") : t("auth:verify.resend")}{!isResending && <RefreshCw size={18} />}</button>
          </form>
          <p className="login-link">{t("auth:verify.alreadyVerified")} {" "}<Link to="/connexion">{t("auth:verify.login")}</Link></p>
        </div>
        <p className="copyright">{t("common:brand.copyright")}</p>
      </section>
    </main>
  );
}

export default VerifyEmailPage;
