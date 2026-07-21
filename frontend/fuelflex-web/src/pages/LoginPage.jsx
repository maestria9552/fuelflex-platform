import { useState } from "react";
import {
  ArrowLeft,
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
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
    rememberMe: false,
  });

  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;

    setFormData((previousData) => ({
      ...previousData,
      [name]: type === "checkbox" ? checked : value,
    }));

    if (errorMessage) {
      setErrorMessage("");
    }
  };

    const handleSubmit = async (event) => {
    event.preventDefault();

    setErrorMessage("");
    setIsSubmitting(true);

    const payload = {
      email: formData.email.trim().toLowerCase(),
      password: formData.password,
    };

    try {
      const response = await fetch("/api/v1/auth/login", {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify(payload),
      });

      const contentType = response.headers.get("content-type");

      const result =
        contentType &&
        contentType.includes("application/json")
          ? await response.json()
          : await response.text();

      if (!response.ok) {
        const message =
          typeof result === "string"
            ? result
            : result?.message ||
              result?.error ||
              "Adresse e-mail ou mot de passe incorrect.";

        throw new Error(message);
      }

      const storage = formData.rememberMe
        ? localStorage
        : sessionStorage;

      localStorage.removeItem("fuelflex_access_token");
      localStorage.removeItem("fuelflex_user");
      sessionStorage.removeItem("fuelflex_access_token");
      sessionStorage.removeItem("fuelflex_user");

      storage.setItem(
        "fuelflex_access_token",
        result.accessToken
      );

      storage.setItem(
        "fuelflex_user",
        JSON.stringify({
          userId: result.userId,
          firstName: result.firstName,
          lastName: result.lastName,
          email: result.email,
          roles: result.roles || [],
          permissions: result.permissions || [],
          tokenType: result.tokenType,
          expiresIn: result.expiresIn,
        })
      );

      navigate("/dashboard", {
        replace: true,
      });
    } catch (error) {
      setErrorMessage(
        error.message ||
          "Impossible de communiquer avec le serveur FuelFlex."
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="login-page">
      <section className="login-visual">
        <div className="login-brand">
          <img src={fuelFlexLogo} alt="Logo FuelFlex" />

          <div className="login-brand-wordmark">
            <span>FUEL</span>
            <strong>FLEX</strong>
            <small>PLATFORM</small>
          </div>
        </div>

        <div className="login-visual-content">
          <div className="login-visual-badge">
            <ShieldCheck size={17} />
            Accès sécurisé à votre espace
          </div>

          <h1>
            Retrouvez le contrôle complet de
            <span> vos stations.</span>
          </h1>

          <p>
            Connectez-vous pour suivre vos opérations, vos stocks, vos ventes,
            vos équipes et les performances de toutes vos stations.
          </p>
        </div>

        <div className="login-visual-footer">
          <span>Suivi en temps réel</span>
          <span>Données centralisées</span>
          <span>Accès sécurisé</span>
        </div>
      </section>

      <section className="login-panel">

        <div className="login-card">
          <div className="login-heading">
            <div className="login-heading-icon">
              <LockKeyhole size={23} />
            </div>

            <p className="login-kicker">ESPACE SÉCURISÉ</p>

            <h2>Bienvenue sur FuelFlex</h2>

            <p className="login-subtitle">
              Connectez-vous avec votre adresse e-mail professionnelle.
            </p>
          </div>

          {errorMessage && (
            <div className="login-alert" role="alert">
              {errorMessage}
            </div>
          )}

          <form className="login-form" onSubmit={handleSubmit}>
            <div className="login-form-group">
              <label htmlFor="email">Adresse e-mail</label>

              <div className="login-input-wrapper">
                <Mail className="login-input-icon" size={19} />

                <input
                  id="email"
                  type="email"
                  name="email"
                  placeholder="exemple@entreprise.com"
                  value={formData.email}
                  onChange={handleChange}
                  autoComplete="email"
                  required
                />
              </div>
            </div>

            <div className="login-form-group">
              <div className="login-label-row">
                <label htmlFor="password">Mot de passe</label>

                <Link to="/mot-de-passe-oublie">
                  Mot de passe oublié ?
                </Link>
              </div>

              <div className="login-input-wrapper">
                <LockKeyhole className="login-input-icon" size={19} />

                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  name="password"
                  placeholder="Votre mot de passe"
                  value={formData.password}
                  onChange={handleChange}
                  autoComplete="current-password"
                  minLength={8}
                  required
                />

                <button
                  type="button"
                  className="login-password-toggle"
                  onClick={() => setShowPassword((value) => !value)}
                  aria-label={
                    showPassword
                      ? "Masquer le mot de passe"
                      : "Afficher le mot de passe"
                  }
                >
                  {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                </button>
              </div>
            </div>

            <label className="remember-option">
              <input
                type="checkbox"
                name="rememberMe"
                checked={formData.rememberMe}
                onChange={handleChange}
              />

              <span className="custom-checkbox" />

              <span>Maintenir ma session active sur cet appareil</span>
            </label>

            <button
              className="login-submit-button"
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <span className="login-spinner" />
                  Connexion en cours...
                </>
              ) : (
                <>
                  Se connecter
                  <ArrowRight size={19} />
                </>
              )}
            </button>
          </form>

          <div className="login-separator">
            <span />
            <p>Nouveau sur FuelFlex ?</p>
            <span />
          </div>

          <Link to="/inscription" className="create-account-link">
            Créer un compte superviseur
            <ArrowRight size={18} />
          </Link>
        </div>

        <p className="login-copyright">
          © 2026 FuelFlex Platform. Tous droits réservés.
        </p>
      </section>
    </main>
  );
}

export default LoginPage;