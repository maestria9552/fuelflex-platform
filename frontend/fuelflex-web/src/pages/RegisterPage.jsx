import { useState } from "react";
import {
  ArrowRight,
  Building2,
  Check,
  Eye,
  EyeOff,
  LockKeyhole,
  Mail,
  Phone,
  User,
} from "lucide-react";

import "./RegisterPage.css";
import fuelFlexLogo from "../assets/images/logofuelflex.png";

function RegisterPage() {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    password: "",
    confirmPassword: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const passwordsMatch =
    formData.password.length > 0 &&
    formData.confirmPassword.length > 0 &&
    formData.password === formData.confirmPassword;

  const confirmationStarted = formData.confirmPassword.length > 0;

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previousData) => ({
      ...previousData,
      [name]: value,
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!passwordsMatch) {
      return;
    }

    console.log(formData);
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
          <p className="visual-kicker">PLATEFORME DE GESTION</p>

          <h1>
            Pilotez vos stations
            <span> en toute simplicité.</span>
          </h1>

          <p className="visual-description">
            Centralisez vos opérations, vos stocks, vos pompes et vos équipes
            depuis une seule plateforme.
          </p>
        </div>

        <div className="visual-footer">
          <span>Gestion centralisée</span>
          <span>Suivi en temps réel</span>
          <span>Sécurité renforcée</span>
        </div>
      </section>

      <section className="register-panel">
        <div className="register-card">
          <div className="register-heading">
            <div className="register-icon">
              <Building2 size={24} />
            </div>

            <h2>Créer votre compte superviseur</h2>
            <p>Configurez votre organisation et vos stations.</p>
          </div>

          <form className="register-form" onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="firstName">Prénom</label>

                <div className="input-wrapper">
                  <User className="input-icon" size={19} />

                  <input
                    id="firstName"
                    type="text"
                    name="firstName"
                    placeholder="Votre prénom"
                    value={formData.firstName}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="lastName">Nom</label>

                <div className="input-wrapper">
                  <User className="input-icon" size={19} />

                  <input
                    id="lastName"
                    type="text"
                    name="lastName"
                    placeholder="Votre nom"
                    value={formData.lastName}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="email">Adresse e-mail</label>

              <div className="input-wrapper">
                <Mail className="input-icon" size={19} />

                <input
                  id="email"
                  type="email"
                  name="email"
                  placeholder="exemple@entreprise.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="phoneNumber">Numéro de téléphone</label>

              <div className="input-wrapper">
                <Phone className="input-icon" size={19} />

                <input
                  id="phoneNumber"
                  type="tel"
                  name="phoneNumber"
                  placeholder="+243 81 234 5678"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="password">Mot de passe</label>

                <div className="input-wrapper">
                  <LockKeyhole className="input-icon" size={19} />

                  <input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    name="password"
                    placeholder="Minimum 8 caractères"
                    value={formData.password}
                    onChange={handleChange}
                    minLength={8}
                    required
                  />

                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowPassword((value) => !value)}
                    aria-label="Afficher ou masquer le mot de passe"
                  >
                    {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                  </button>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="confirmPassword">Confirmation</label>

                <div className="input-wrapper">
                  <LockKeyhole className="input-icon" size={19} />

                  <input
                    id="confirmPassword"
                    type={showConfirmation ? "text" : "password"}
                    name="confirmPassword"
                    placeholder="Répétez le mot de passe"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    minLength={8}
                    required
                  />

                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowConfirmation((value) => !value)}
                    aria-label="Afficher ou masquer la confirmation"
                  >
                    {showConfirmation ? (
                      <EyeOff size={19} />
                    ) : (
                      <Eye size={19} />
                    )}
                  </button>
                </div>
              </div>
            </div>

            <div
              className={`password-match ${
                !confirmationStarted
                  ? "waiting"
                  : passwordsMatch
                    ? "success"
                    : "error"
              }`}
            >
              <div className="match-bar">
                <span />
              </div>

              <p>
                {passwordsMatch && <Check size={16} />}

                {!confirmationStarted
                  ? "Confirmez votre mot de passe."
                  : passwordsMatch
                    ? "Les deux mots de passe sont identiques."
                    : "Les deux mots de passe ne correspondent pas."}
              </p>
            </div>

            <button
              className="register-button"
              type="submit"
              disabled={!passwordsMatch}
            >
              Créer mon compte
              <ArrowRight size={19} />
            </button>
          </form>

          <p className="login-link">
            Vous avez déjà un compte ?
            <a href="#">Se connecter</a>
          </p>
        </div>

        <p className="copyright">
          © 2026 FuelFlex Platform. Tous droits réservés.
        </p>
      </section>
    </main>
  );
}

export default RegisterPage;