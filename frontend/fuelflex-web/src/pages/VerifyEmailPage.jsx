import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowRight,
  CheckCircle2,
  KeyRound,
  Mail,
  RefreshCw,
} from "lucide-react";

import "./RegisterPage.css";
import fuelFlexLogo from "../assets/images/logofuelflex.png";

function VerifyEmailPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState(
    sessionStorage.getItem("fuelflex_verification_email") || ""
  );

  const [verificationCode, setVerificationCode] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const readResponse = async (response) => {
    const contentType = response.headers.get("content-type");

    if (
      contentType &&
      contentType.includes("application/json")
    ) {
      return response.json();
    }

    return response.text();
  };

  const extractErrorMessage = (result) => {
    if (typeof result === "string" && result.trim()) {
      return result;
    }

    return (
      result?.message ||
      result?.error ||
      "Une erreur est survenue."
    );
  };

  const handleVerification = async (event) => {
    event.preventDefault();

    setErrorMessage("");
    setSuccessMessage("");

    const normalizedEmail = email.trim().toLowerCase();
    const normalizedCode = verificationCode.trim();

    if (!normalizedEmail) {
      setErrorMessage("L’adresse e-mail est obligatoire.");
      return;
    }

    if (!normalizedCode) {
      setErrorMessage(
        "Veuillez saisir le code de vérification."
      );
      return;
    }

    try {
      setIsSubmitting(true);

      const response = await fetch(
        "/api/v1/auth/verify-email",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            email: normalizedEmail,
            verificationCode: normalizedCode,
          }),
        }
      );

      const result = await readResponse(response);

      if (!response.ok) {
        throw new Error(extractErrorMessage(result));
      }

      sessionStorage.removeItem(
        "fuelflex_verification_email"
      );

      setSuccessMessage(
        result?.message ||
          "Votre adresse e-mail a été vérifiée avec succès."
      );

      setTimeout(() => {
        navigate("/connexion", {
          replace: true,
          state: {
            message:
              "Compte vérifié. Vous pouvez maintenant vous connecter.",
            email: normalizedEmail,
          },
        });
      }, 1200);
    } catch (error) {
      setErrorMessage(
        error.message ||
          "Impossible de vérifier votre adresse e-mail."
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResendCode = async () => {
    setErrorMessage("");
    setSuccessMessage("");

    const normalizedEmail = email.trim().toLowerCase();

    if (!normalizedEmail) {
      setErrorMessage(
        "Saisissez votre adresse e-mail avant de demander un nouveau code."
      );
      return;
    }

    try {
      setIsResending(true);

      const response = await fetch(
        "/api/v1/auth/resend-code",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            email: normalizedEmail,
          }),
        }
      );

      const result = await readResponse(response);

      if (!response.ok) {
        throw new Error(extractErrorMessage(result));
      }

      sessionStorage.setItem(
        "fuelflex_verification_email",
        normalizedEmail
      );

      setSuccessMessage(
        result?.message ||
          "Un nouveau code de vérification a été envoyé."
      );
    } catch (error) {
      setErrorMessage(
        error.message ||
          "Impossible de renvoyer le code de vérification."
      );
    } finally {
      setIsResending(false);
    }
  };

  return (
    <main className="register-page">
      <section className="register-visual">
        <div className="brand">
          <img
            src={fuelFlexLogo}
            alt="Logo FuelFlex"
            className="brand-logo"
          />

          <div className="brand-wordmark">
            <span className="brand-fuel">FUEL</span>
            <span className="brand-flex">FLEX</span>
            <small>FUEL STATION</small>
          </div>
        </div>

        <div className="visual-content">
          <p className="visual-kicker">
            SÉCURITÉ DU COMPTE
          </p>

          <h1>
            Vérifiez votre identité
            <span> en quelques secondes.</span>
          </h1>

          <p className="visual-description">
            Un code de vérification a été envoyé à votre
            adresse e-mail afin de sécuriser l’accès à votre
            organisation FuelFlex.
          </p>
        </div>

        <div className="visual-footer">
          <span>Compte sécurisé</span>
          <span>Accès contrôlé</span>
          <span>Données protégées</span>
        </div>
      </section>

      <section className="register-panel">
        <div className="register-card">
          <div className="register-heading">
            <div className="register-icon">
              <CheckCircle2 size={24} />
            </div>

            <h2>Vérification de l’adresse e-mail</h2>

            <p>
              Saisissez le code reçu dans votre boîte
              e-mail.
            </p>
          </div>

          <form
            className="register-form"
            onSubmit={handleVerification}
          >
            <div className="form-group">
              <label htmlFor="email">
                Adresse e-mail
              </label>

              <div className="input-wrapper">
                <Mail
                  className="input-icon"
                  size={19}
                />

                <input
                  id="email"
                  type="email"
                  name="email"
                  placeholder="exemple@entreprise.com"
                  value={email}
                  onChange={(event) =>
                    setEmail(event.target.value)
                  }
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="verificationCode">
                Code de vérification
              </label>

              <div className="input-wrapper">
                <KeyRound
                  className="input-icon"
                  size={19}
                />

                <input
                  id="verificationCode"
                  type="text"
                  name="verificationCode"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  placeholder="Entrez le code reçu"
                  value={verificationCode}
                  onChange={(event) =>
                    setVerificationCode(
                      event.target.value
                        .replace(/\D/g, "")
                        .slice(0, 6)
                    )
                  }
                  maxLength={6}
                  required
                />
              </div>
            </div>

            {errorMessage && (
              <div className="form-message error">
                {errorMessage}
              </div>
            )}

            {successMessage && (
              <div className="form-message success">
                {successMessage}
              </div>
            )}

            <button
              className="register-button"
              type="submit"
              disabled={
                isSubmitting ||
                verificationCode.length !== 6
              }
            >
              {isSubmitting
                ? "Vérification en cours..."
                : "Vérifier mon compte"}

              {!isSubmitting && (
                <ArrowRight size={19} />
              )}
            </button>

            <button
              type="button"
              className="register-button"
              onClick={handleResendCode}
              disabled={isResending || isSubmitting}
            >
              {isResending
                ? "Envoi du code..."
                : "Renvoyer le code"}

              {!isResending && (
                <RefreshCw size={18} />
              )}
            </button>
          </form>

          <p className="login-link">
            Vous avez déjà vérifié votre compte ?{" "}
            <Link to="/connexion">
              Se connecter
            </Link>
          </p>
        </div>

        <p className="copyright">
          © 2026 FuelFlex Platform. Tous droits réservés.
        </p>
      </section>
    </main>
  );
}

export default VerifyEmailPage;