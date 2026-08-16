import { useRef, useState } from "react";
import { ArrowRight, CheckCircle2, Eye, EyeOff, LockKeyhole, Mail } from "lucide-react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import FuelFlexLogo from "../components/brand/FuelFlexLogo";
import "./EmployeeActivationPage.css";

function EmployeeActivationPage() {
  const { t } = useTranslation(["auth", "common"]);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState(() => ({ email: searchParams.get("email")?.trim() || "", code: "", password: "", confirmPassword: "" }));
  const [otpValues, setOtpValues] = useState(["", "", "", "", "", ""]);
  const otpRefs = useRef([]);
  const [verified, setVerified] = useState(false);
  const [activated, setActivated] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState(null);

  const update = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
    if (message) setMessage(null);
  };

  const setOtp = (nextValues, focusIndex = null) => {
    setOtpValues(nextValues);
    setForm((current) => ({ ...current, code: nextValues.join("") }));
    if (focusIndex !== null) {
      window.requestAnimationFrame(() => otpRefs.current[focusIndex]?.focus());
    }
  };

  const handleOtpChange = (index, value) => {
    const digits = value.replace(/\D/g, "");
    const nextValues = [...otpValues];
    if (!digits) {
      nextValues[index] = "";
      setOtp(nextValues);
      return;
    }
    digits.slice(0, 6 - index).split("").forEach((digit, offset) => {
      nextValues[index + offset] = digit;
    });
    setOtp(nextValues, Math.min(index + digits.length, 5));
  };

  const handleOtpPaste = (event) => {
    event.preventDefault();
    const digits = event.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
    if (!digits) return;
    const nextValues = ["", "", "", "", "", ""];
    digits.split("").forEach((digit, offset) => {
      nextValues[offset] = digit;
    });
    setOtp(nextValues, Math.min(digits.length, 5));
  };

  const handleOtpKeyDown = (index, event) => {
    if (event.key === "Backspace" && !otpValues[index] && index > 0) {
      event.preventDefault();
      otpRefs.current[index - 1]?.focus();
    }
    if (event.key === "ArrowLeft" && index > 0) {
      event.preventDefault();
      otpRefs.current[index - 1]?.focus();
    }
    if (event.key === "ArrowRight" && index < 5) {
      event.preventDefault();
      otpRefs.current[index + 1]?.focus();
    }
  };

  const request = async (url, body) => {
    const response = await fetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
    const type = response.headers.get("content-type") || "";
    const data = type.includes("json") ? await response.json() : await response.text();
    if (!response.ok) throw new Error(typeof data === "string" ? data : data?.message || data?.error || t("auth:errors.activationFailed"));
    return data;
  };

  const submit = async (event) => {
    event.preventDefault();
    setMessage(null);
    setBusy(true);
    try {
      const email = form.email.trim().toLowerCase();
      const code = form.code.trim();
      if (!verified) {
        await request("/api/v1/auth/employee-activation/verify", { email, code });
        setVerified(true);
        setMessage({ type: "success", text: t("auth:activation.invitationVerified") });
      } else {
        if (form.password !== form.confirmPassword) throw new Error(t("auth:errors.passwordsMismatch"));
        await request("/api/v1/auth/employee-activation/set-password", { email, code, password: form.password, confirmPassword: form.confirmPassword });
        setActivated(true);
      }
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="employee-activation-page">
      <div className="employee-activation-orbit employee-activation-orbit-one" />
      <div className="employee-activation-orbit employee-activation-orbit-two" />
      <section className="employee-activation-card" aria-labelledby="employee-activation-title">
        <FuelFlexLogo size={52} className="employee-activation-brand" />

        {activated ? (
          <div className="employee-activation-success" role="status">
            <div className="employee-activation-success-icon"><CheckCircle2 size={30} /></div>
            <h1 id="employee-activation-title">{t("auth:activation.successTitle")}</h1>
            <p>{t("auth:activation.successDescription")}</p>
            <button className="employee-activation-button" type="button" onClick={() => navigate("/connexion", { replace: true })}>
              {t("auth:activation.login")}<ArrowRight size={18} />
            </button>
          </div>
        ) : (
          <>
            <header className="employee-activation-heading">
              <p className="employee-activation-kicker">{t("auth:activation.kicker")}</p>
              <h1 id="employee-activation-title">{t("auth:activation.pageTitle")}</h1>
              <p>{t("auth:activation.pageDescription")}</p>
            </header>

            {message && <div className={`employee-activation-message ${message.type}`} role={message.type === "error" ? "alert" : "status"}>{message.text}</div>}

            <form className="employee-activation-form" onSubmit={submit} noValidate>
              <div className="employee-activation-field">
                <label htmlFor="activation-email">{t("auth:activation.email")}</label>
                <div className="employee-activation-input-wrap">
                  <Mail className="employee-activation-input-icon" size={18} />
                  <input id="activation-email" name="email" type="email" autoComplete="email" placeholder={t("auth:activation.emailPlaceholder")} value={form.email} onChange={update} disabled={verified || busy} autoFocus={!form.email} required />
                </div>
              </div>

              <div className="employee-activation-field">
                <span className="employee-activation-field-label" id="activation-code-label">{t("auth:activation.code")}</span>
                <div className="employee-activation-otp" role="group" aria-labelledby="activation-code-label">
                  {otpValues.map((value, index) => (
                    <input
                      key={`activation-digit-${index}`}
                      ref={(element) => { otpRefs.current[index] = element; }}
                      className="employee-activation-otp-input"
                      type="text"
                      inputMode="numeric"
                      autoComplete={index === 0 ? "one-time-code" : "off"}
                      pattern="[0-9]*"
                      maxLength={1}
                      value={value}
                      onChange={(event) => handleOtpChange(index, event.target.value)}
                      onPaste={handleOtpPaste}
                      onKeyDown={(event) => handleOtpKeyDown(index, event)}
                      disabled={verified || busy}
                      aria-label={t("auth:activation.digitLabel", { current: index + 1 })}
                      required
                    />
                  ))}
                </div>
              </div>

              {verified && <div className="employee-activation-passwords">
                <div className="employee-activation-field">
                  <label htmlFor="activation-password">{t("auth:activation.password")}</label>
                  <div className="employee-activation-input-wrap">
                    <LockKeyhole className="employee-activation-input-icon" size={18} />
                    <input id="activation-password" name="password" type={showPassword ? "text" : "password"} autoComplete="new-password" minLength={8} placeholder={t("auth:activation.passwordPlaceholder")} value={form.password} onChange={update} disabled={busy} required />
                    <button type="button" className="employee-activation-password-toggle" onClick={() => setShowPassword((value) => !value)} aria-label={t(showPassword ? "auth:login.hidePassword" : "auth:login.showPassword")}>
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>
                <div className="employee-activation-field">
                  <label htmlFor="activation-confirm">{t("auth:activation.confirmPassword")}</label>
                  <div className="employee-activation-input-wrap">
                    <LockKeyhole className="employee-activation-input-icon" size={18} />
                    <input id="activation-confirm" name="confirmPassword" type={showConfirm ? "text" : "password"} autoComplete="new-password" minLength={8} placeholder={t("auth:activation.confirmPasswordPlaceholder")} value={form.confirmPassword} onChange={update} disabled={busy} required />
                    <button type="button" className="employee-activation-password-toggle" onClick={() => setShowConfirm((value) => !value)} aria-label={t(showConfirm ? "auth:login.hidePassword" : "auth:login.showPassword")}>
                      {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>
              </div>}

              <button className="employee-activation-button" type="submit" disabled={busy || (!verified && form.code.length !== 6)}>
                {busy ? <><span className="employee-activation-spinner" />{t("auth:activation.submitting")}</> : <>{t(verified ? "auth:activation.activate" : "auth:activation.verify")}<ArrowRight size={18} /></>}
              </button>
            </form>
          </>
        )}

        {!activated && <Link className="employee-activation-login-link" to="/connexion">{t("auth:activation.backToLogin")}</Link>}
      </section>
    </main>
  );
}

export default EmployeeActivationPage;
