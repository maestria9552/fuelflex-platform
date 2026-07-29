import { useMemo, useState } from "react";
import {
  ArrowRight,
  BarChart3,
  Building2,
  Check,
  ChevronRight,
  CircleDollarSign,
  Droplets,
  Gauge,
  LayoutDashboard,
  LoaderCircle,
  LogOut,
  Menu,
  Settings,
  ShieldCheck,
  Users,
  Warehouse,
} from "lucide-react";
import { useNavigate } from "react-router-dom";

import "./OrganizationSetupPage.css";
import fuelFlexLogo from "../../assets/images/logofuelflex.png";
import { clearAuthSession } from "../../services/auth/authStorage";

function getStoredSession() {
  const localToken = localStorage.getItem(
    "fuelflex_access_token"
  );

  const sessionToken = sessionStorage.getItem(
    "fuelflex_access_token"
  );

  if (localToken) {
    return {
      accessToken: localToken,
      storage: localStorage,
    };
  }

  if (sessionToken) {
    return {
      accessToken: sessionToken,
      storage: sessionStorage,
    };
  }

  return null;
}

function OrganizationSetupPage() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: "",
    tradeName: "",
    registrationNumber: "",
    nationalId: "",
    taxNumber: "",
    email: "",
    phone: "",
    country: "République démocratique du Congo",
    province: "",
    city: "",
    address: "",
    defaultCurrency: "USD",
    timezone: "Africa/Kinshasa",
    defaultLanguage: "fr",
  });

  const [addTradeName, setAddTradeName] =
    useState(false);

  const [activeStep, setActiveStep] =
    useState(1);

  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const [errorMessage, setErrorMessage] =
    useState("");

  const completionPercentage = useMemo(() => {
    const importantFields = [
      formData.name,
      formData.registrationNumber,
      formData.nationalId,
      formData.taxNumber,
      formData.email,
      formData.phone,
      formData.city,
      formData.address,
    ];

    const completedFields = importantFields.filter(
      (value) => value && value.trim()
    ).length;

    return Math.round(
      (completedFields / importantFields.length) *
        100
    );
  }, [formData]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previousData) => ({
      ...previousData,
      [name]: value,
    }));

    if (errorMessage) {
      setErrorMessage("");
    }
  };

  const handleTradeNameOptionChange = (event) => {
    const isEnabled = event.target.checked;

    setAddTradeName(isEnabled);

    if (!isEnabled) {
      setFormData((previousData) => ({
        ...previousData,
        tradeName: "",
      }));
    }

    if (errorMessage) {
      setErrorMessage("");
    }
  };

  const handleLogout = () => {
    clearAuthSession();

    navigate("/connexion", {
      replace: true,
    });
  };

  const goToNextStep = () => {
    setErrorMessage("");

    if (!formData.name.trim()) {
      setErrorMessage(
        "Veuillez renseigner le nom officiel de votre société."
      );

      return;
    }

    setActiveStep(2);
  };

  const goToPreviousStep = () => {
    setErrorMessage("");
    setActiveStep(1);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setErrorMessage("");
    setIsSubmitting(true);

    const session = getStoredSession();

    if (!session) {
      handleLogout();
      return;
    }

    const payload = {
      name: formData.name.trim(),

      tradeName:
        addTradeName &&
        formData.tradeName.trim()
          ? formData.tradeName.trim()
          : null,

      registrationNumber:
        formData.registrationNumber.trim() ||
        null,

      nationalId:
        formData.nationalId.trim() || null,

      taxNumber:
        formData.taxNumber.trim() || null,

      email:
        formData.email.trim().toLowerCase() ||
        null,

      phone:
        formData.phone.trim() || null,

      country:
        formData.country.trim() || null,

      province:
        formData.province.trim() || null,

      city:
        formData.city.trim() || null,

      address:
        formData.address.trim() || null,

      defaultCurrency:
        formData.defaultCurrency,

      timezone:
        formData.timezone,

      defaultLanguage:
        formData.defaultLanguage,
    };

    try {
      const response = await fetch(
        "/api/v1/organizations",
        {
          method: "POST",
          headers: {
            "Content-Type":
              "application/json",
            Authorization:
              `Bearer ${session.accessToken}`,
          },
          body: JSON.stringify(payload),
        }
      );

      const contentType =
        response.headers.get("content-type");

      const result =
        contentType?.includes(
          "application/json"
        )
          ? await response.json()
          : await response.text();

      if (response.status === 401) {
        handleLogout();
        return;
      }

      if (!response.ok) {
        const message =
          typeof result === "string"
            ? result
            : result?.message ||
              result?.error ||
              "Impossible de créer la société.";

        throw new Error(message);
      }

      const storedUser =
        session.storage.getItem(
          "fuelflex_user"
        );

      const currentUser = storedUser
        ? JSON.parse(storedUser)
        : {};

      session.storage.setItem(
        "fuelflex_user",
        JSON.stringify({
          ...currentUser,
          organizationConfigured: true,
          organizationId: result.id,
          organizationCode: result.code,
          organizationName:
            result.tradeName || result.name,
          organizationOfficialName:
            result.name,
          organizationTradeName:
            result.tradeName || null,
        })
      );

      navigate(
        "/superviseur/dashboard",
        {
          replace: true,
        }
      );
    } catch (error) {
      setErrorMessage(
        error.message ||
          "Une erreur est survenue pendant la création de la société."
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="organization-overlay-page">
      <div
        className="organization-dashboard-background"
        aria-hidden="true"
      >
        <aside className="organization-preview-sidebar">
          <div className="organization-preview-brand">
            <img
              src={fuelFlexLogo}
              alt=""
            />

            <div>
              <strong>FUELFLEX</strong>
              <span>PLATFORM</span>
            </div>
          </div>

          <nav>
            <div className="active">
              <LayoutDashboard size={19} />
              Tableau de bord
            </div>

            <div>
              <Building2 size={19} />
              Stations
            </div>

            <div>
              <Warehouse size={19} />
              Stocks
            </div>

            <div>
              <Droplets size={19} />
              Produits
            </div>

            <div>
              <Users size={19} />
              Équipe
            </div>

            <div>
              <BarChart3 size={19} />
              Rapports
            </div>

            <div>
              <Settings size={19} />
              Paramètres
            </div>
          </nav>
        </aside>

        <section className="organization-preview-main">
          <header className="organization-preview-topbar">
            <button type="button">
              <Menu size={21} />
            </button>

            <div>
              <span>Vue d’ensemble</span>

              <strong>
                Tableau de bord superviseur
              </strong>
            </div>

            <div className="organization-preview-user">
              <div>GM</div>

              <span>
                <strong>Superviseur</strong>
                <small>Compte principal</small>
              </span>
            </div>
          </header>

          <div className="organization-preview-content">
            <div className="organization-preview-heading">
              <div>
                <p>APERÇU GLOBAL</p>

                <h1>
                  Bonjour, bienvenue sur FuelFlex
                </h1>

                <span>
                  Suivez les performances de votre
                  réseau de stations.
                </span>
              </div>

              <button type="button">
                Cette semaine
                <ChevronRight size={17} />
              </button>
            </div>

            <div className="organization-preview-kpis">
              <article>
                <div>
                  <Building2 size={22} />
                </div>

                <span>Stations actives</span>
                <strong>0</strong>
                <small>
                  Configuration requise
                </small>
              </article>

              <article>
                <div>
                  <CircleDollarSign size={22} />
                </div>

                <span>Ventes du jour</span>
                <strong>0 USD</strong>
                <small>
                  Aucune donnée disponible
                </small>
              </article>

              <article>
                <div>
                  <Droplets size={22} />
                </div>

                <span>Stock disponible</span>
                <strong>0 L</strong>
                <small>
                  Aucun produit configuré
                </small>
              </article>

              <article>
                <div>
                  <Gauge size={22} />
                </div>

                <span>Pompes actives</span>
                <strong>0</strong>
                <small>
                  Aucune pompe configurée
                </small>
              </article>
            </div>

            <div className="organization-preview-panels">
              <article>
                <div className="organization-preview-panel-title">
                  <div>
                    <strong>
                      Évolution des ventes
                    </strong>

                    <span>
                      Activité des sept derniers jours
                    </span>
                  </div>
                </div>

                <div className="organization-preview-chart">
                  <span />
                  <span />
                  <span />
                  <span />
                  <span />
                  <span />
                  <span />
                </div>
              </article>

              <article>
                <div className="organization-preview-panel-title">
                  <div>
                    <strong>
                      État de configuration
                    </strong>

                    <span>
                      Préparation de votre espace
                    </span>
                  </div>
                </div>

                <div className="organization-preview-status">
                  <div>
                    <Check size={17} />
                    Compte superviseur créé
                  </div>

                  <div>
                    <Building2 size={17} />
                    Société à configurer
                  </div>

                  <div>
                    <Warehouse size={17} />
                    Stations à créer
                  </div>
                </div>
              </article>
            </div>
          </div>
        </section>
      </div>

      <div className="organization-overlay-blur" />

      <section className="organization-setup-modal">
        <header className="organization-modal-header">
          <div className="organization-modal-brand">
            <img
              src={fuelFlexLogo}
              alt="Logo FuelFlex"
            />

            <div>
              <strong>FuelFlex Platform</strong>

              <span>
                Configuration de l’organisation
              </span>
            </div>
          </div>

          <button
            type="button"
            className="organization-modal-logout"
            onClick={handleLogout}
          >
            <LogOut size={18} />
            Déconnexion
          </button>
        </header>

        <div className="organization-modal-content">
          <aside className="organization-modal-summary">
            <div className="organization-summary-icon">
              <Building2 size={30} />
            </div>

            <p className="organization-summary-kicker">
              CONFIGURATION INITIALE
            </p>

            <h1>
              Créons votre espace professionnel
            </h1>

            <p className="organization-summary-description">
              Renseignez les informations essentielles
              de votre société. Les autres éléments
              pourront être complétés depuis votre
              tableau de bord.
            </p>

            <div className="organization-summary-progress">
              <div className="organization-progress-header">
                <span>
                  Progression du profil
                </span>

                <strong>
                  {completionPercentage} %
                </strong>
              </div>

              <div className="organization-progress-track">
                <span
                  style={{
                    width:
                      `${completionPercentage}%`,
                  }}
                />
              </div>
            </div>

            <div className="organization-summary-security">
              <ShieldCheck size={19} />

              <p>
                Votre code société sera généré
                automatiquement et toutes les
                relations utiliseront un UUID unique.
              </p>
            </div>
          </aside>

          <div className="organization-modal-form-area">
            <div className="organization-stepper">
              <div
                className={
                  activeStep >= 1
                    ? "active"
                    : ""
                }
              >
                <span>
                  {activeStep > 1 ? (
                    <Check size={16} />
                  ) : (
                    "1"
                  )}
                </span>

                <div>
                  <strong>
                    Identification
                  </strong>

                  <small>
                    Informations principales
                  </small>
                </div>
              </div>

              <div className="organization-step-line" />

              <div
                className={
                  activeStep >= 2
                    ? "active"
                    : ""
                }
              >
                <span>2</span>

                <div>
                  <strong>Coordonnées</strong>

                  <small>
                    Informations complémentaires
                  </small>
                </div>
              </div>
            </div>

            {errorMessage && (
              <div
                className="organization-modal-alert"
                role="alert"
              >
                {errorMessage}
              </div>
            )}

            <form
              className="organization-modal-form"
              onSubmit={handleSubmit}
            >
              {activeStep === 1 && (
                <div className="organization-form-step">
                  <div className="organization-form-heading">
                    <p>ÉTAPE 1 SUR 2</p>

                    <h2>
                      Identité de la société
                    </h2>

                    <span>
                      Seul le nom officiel de la société
                      est obligatoire pour continuer.
                    </span>
                  </div>

                  <div className="organization-form-grid">
                    <label className="organization-field-full">
                      Nom officiel de la société

                      <span className="organization-required">
                        *
                      </span>

                      <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        placeholder="Ex. MITAS HOLDING SARL"
                        autoFocus
                        required
                      />
                    </label>

                    <label className="organization-field-full organization-optional-choice">
                      <span>
                        <input
                          type="checkbox"
                          checked={addTradeName}
                          onChange={
                            handleTradeNameOptionChange
                          }
                        />

                        Ajouter un nom commercial
                      </span>

                      <small>
                        Facultatif — activez cette option
                        uniquement si votre société utilise
                        une marque ou une appellation
                        commerciale différente.
                      </small>
                    </label>

                    {addTradeName && (
                      <label className="organization-field-full">
                        Nom commercial

                        <input
                          type="text"
                          name="tradeName"
                          value={formData.tradeName}
                          onChange={handleChange}
                          placeholder="Ex. MITAS"
                        />
                      </label>
                    )}

                    <label>
                      RCCM

                      <input
                        type="text"
                        name="registrationNumber"
                        value={
                          formData.registrationNumber
                        }
                        onChange={handleChange}
                        placeholder="CD/KIN/RCCM/..."
                      />
                    </label>

                    <label>
                      ID National

                      <input
                        type="text"
                        name="nationalId"
                        value={formData.nationalId}
                        onChange={handleChange}
                        placeholder="Identifiant national"
                      />
                    </label>

                    <label className="organization-field-full">
                      Numéro fiscal

                      <input
                        type="text"
                        name="taxNumber"
                        value={formData.taxNumber}
                        onChange={handleChange}
                        placeholder="NIF de la société"
                      />
                    </label>
                  </div>

                  <div className="organization-form-actions">
                    <span>
                      Vous pourrez compléter ou modifier
                      ces informations plus tard.
                    </span>

                    <button
                      type="button"
                      onClick={goToNextStep}
                    >
                      Continuer
                      <ArrowRight size={18} />
                    </button>
                  </div>
                </div>
              )}

              {activeStep === 2 && (
                <div className="organization-form-step">
                  <div className="organization-form-heading">
                    <p>ÉTAPE 2 SUR 2</p>

                    <h2>
                      Coordonnées et paramètres
                    </h2>

                    <span>
                      Ces informations restent
                      facultatives à cette étape.
                    </span>
                  </div>

                  <div className="organization-form-grid">
                    <label>
                      Adresse e-mail

                      <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="contact@societe.com"
                      />
                    </label>

                    <label>
                      Téléphone

                      <input
                        type="tel"
                        name="phone"
                        value={formData.phone}
                        onChange={handleChange}
                        placeholder="+243..."
                      />
                    </label>

                    <label>
                      Province

                      <input
                        type="text"
                        name="province"
                        value={formData.province}
                        onChange={handleChange}
                        placeholder="Ex. Kinshasa"
                      />
                    </label>

                    <label>
                      Ville

                      <input
                        type="text"
                        name="city"
                        value={formData.city}
                        onChange={handleChange}
                        placeholder="Ex. Kinshasa"
                      />
                    </label>

                    <label className="organization-field-full">
                      Adresse physique

                      <input
                        type="text"
                        name="address"
                        value={formData.address}
                        onChange={handleChange}
                        placeholder="Commune, avenue et numéro"
                      />
                    </label>

                    <label>
                      Devise principale

                      <select
                        name="defaultCurrency"
                        value={
                          formData.defaultCurrency
                        }
                        onChange={handleChange}
                      >
                        <option value="USD">
                          Dollar américain (USD)
                        </option>

                        <option value="CDF">
                          Franc congolais (CDF)
                        </option>
                      </select>
                    </label>

                    <label>
                      Fuseau horaire

                      <select
                        name="timezone"
                        value={formData.timezone}
                        onChange={handleChange}
                      >
                        <option value="Africa/Kinshasa">
                          Kinshasa
                        </option>

                        <option value="Africa/Lubumbashi">
                          Lubumbashi
                        </option>
                      </select>
                    </label>
                  </div>

                  <div className="organization-form-actions">
                    <button
                      type="button"
                      className="organization-back-button"
                      onClick={goToPreviousStep}
                      disabled={isSubmitting}
                    >
                      Retour
                    </button>

                    <button
                      type="submit"
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? (
                        <>
                          <LoaderCircle
                            size={18}
                            className="organization-spinner"
                          />

                          Création en cours...
                        </>
                      ) : (
                        <>
                          Créer ma société
                          <ArrowRight size={18} />
                        </>
                      )}
                    </button>
                  </div>
                </div>
              )}
            </form>
          </div>
        </div>
      </section>
    </main>
  );
}

export default OrganizationSetupPage;