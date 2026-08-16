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
import { useTranslation } from "react-i18next";

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
  const { t } = useTranslation(["organization", "common"]);

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
        t("organization:setup.nameRequired")
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
              t("organization:setup.createFailed");

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
          t("organization:setup.unexpectedError")
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
              {t("organization:setup.preview.dashboard")}
            </div>

            <div>
              <Building2 size={19} />
              {t("organization:setup.preview.stations")}
            </div>

            <div>
              <Warehouse size={19} />
              {t("organization:setup.preview.stocks")}
            </div>

            <div>
              <Droplets size={19} />
              {t("organization:setup.preview.products")}
            </div>

            <div>
              <Users size={19} />
              {t("organization:setup.preview.team")}
            </div>

            <div>
              <BarChart3 size={19} />
              {t("organization:setup.preview.reports")}
            </div>

            <div>
              <Settings size={19} />
              {t("organization:setup.preview.settings")}
            </div>
          </nav>
        </aside>

        <section className="organization-preview-main">
          <header className="organization-preview-topbar">
            <button type="button">
              <Menu size={21} />
            </button>

            <div>
              <span>{t("organization:setup.preview.overview")}</span>

              <strong>
                {t("organization:setup.preview.supervisorDashboard")}
              </strong>
            </div>

            <div className="organization-preview-user">
              <div>GM</div>

              <span>
                <strong>{t("organization:setup.preview.supervisor")}</strong>
                <small>{t("organization:setup.preview.mainAccount")}</small>
              </span>
            </div>
          </header>

          <div className="organization-preview-content">
            <div className="organization-preview-heading">
              <div>
                <p>{t("organization:setup.preview.globalOverview")}</p>

                <h1>
                  {t("organization:setup.preview.welcome")}
                </h1>

                <span>
                  {t("organization:setup.preview.network")}
                </span>
              </div>

              <button type="button">
                {t("organization:setup.preview.thisWeek")}
                <ChevronRight size={17} />
              </button>
            </div>

            <div className="organization-preview-kpis">
              <article>
                <div>
                  <Building2 size={22} />
                </div>

                <span>{t("organization:setup.preview.activeStations")}</span>
                <strong>0</strong>
                <small>
                  {t("organization:setup.preview.configurationRequired")}
                </small>
              </article>

              <article>
                <div>
                  <CircleDollarSign size={22} />
                </div>

                <span>{t("organization:setup.preview.todaySales")}</span>
                <strong>0 USD</strong>
                <small>
                  {t("organization:setup.preview.noData")}
                </small>
              </article>

              <article>
                <div>
                  <Droplets size={22} />
                </div>

                <span>{t("organization:setup.preview.availableStock")}</span>
                <strong>0 L</strong>
                <small>
                  {t("organization:setup.preview.noProduct")}
                </small>
              </article>

              <article>
                <div>
                  <Gauge size={22} />
                </div>

                <span>{t("organization:setup.preview.activePumps")}</span>
                <strong>0</strong>
                <small>
                  {t("organization:setup.preview.noPump")}
                </small>
              </article>
            </div>

            <div className="organization-preview-panels">
              <article>
                <div className="organization-preview-panel-title">
                  <div>
                    <strong>
                      {t("organization:setup.preview.salesTrend")}
                    </strong>

                    <span>
                      {t("organization:setup.preview.lastSevenDays")}
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
                      {t("organization:setup.preview.configurationStatus")}
                    </strong>

                    <span>
                      {t("organization:setup.preview.preparing")}
                    </span>
                  </div>
                </div>

                <div className="organization-preview-status">
                  <div>
                    <Check size={17} />
                    {t("organization:setup.preview.accountCreated")}
                  </div>

                  <div>
                    <Building2 size={17} />
                    {t("organization:setup.preview.companyPending")}
                  </div>

                  <div>
                    <Warehouse size={17} />
                    {t("organization:setup.preview.stationsPending")}
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
              alt={t("common:brand.logoAlt")}
            />

            <div>
              <strong>FuelFlex Platform</strong>

              <span>
                {t("organization:setup.header")}
              </span>
            </div>
          </div>

          <button
            type="button"
            className="organization-modal-logout"
            onClick={handleLogout}
          >
            <LogOut size={18} />
            {t("organization:setup.logout")}
          </button>
        </header>

        <div className="organization-modal-content">
          <aside className="organization-modal-summary">
            <div className="organization-summary-icon">
              <Building2 size={30} />
            </div>

            <p className="organization-summary-kicker">
              {t("organization:setup.kicker")}
            </p>

            <h1>
              {t("organization:setup.title")}
            </h1>

            <p className="organization-summary-description">
              {t("organization:setup.description")}
            </p>

            <div className="organization-summary-progress">
              <div className="organization-progress-header">
                <span>
                  {t("organization:setup.progress")}
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
                {t("organization:setup.security")}
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
                    {t("organization:setup.step1")}
                  </strong>

                  <small>
                    {t("organization:setup.step1Description")}
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
                  <strong>{t("organization:setup.step2")}</strong>

                  <small>
                    {t("organization:setup.step2Description")}
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
                    <p>{t("organization:setup.stepProgress", { current: 1, total: 2 })}</p>

                    <h2>
                      {t("organization:setup.identityTitle")}
                    </h2>

                    <span>
                      {t("organization:setup.identityDescription")}
                    </span>
                  </div>

                  <div className="organization-form-grid">
                    <label className="organization-field-full">
                      {t("organization:setup.officialName")}

                      <span className="organization-required">
                        *
                      </span>

                      <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        placeholder={t("organization:setup.officialNamePlaceholder")}
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

                        {t("organization:setup.addTradeName")}
                      </span>

                      <small>
                        {t("organization:setup.tradeNameHelp")}
                      </small>
                    </label>

                    {addTradeName && (
                      <label className="organization-field-full">
                        {t("organization:fields.tradeName")}

                        <input
                          type="text"
                          name="tradeName"
                          value={formData.tradeName}
                          onChange={handleChange}
                          placeholder={t("organization:setup.tradeNamePlaceholder")}
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
                      {t("organization:fields.nationalId")}

                      <input
                        type="text"
                        name="nationalId"
                        value={formData.nationalId}
                        onChange={handleChange}
                        placeholder={t("organization:setup.nationalIdPlaceholder")}
                      />
                    </label>

                    <label className="organization-field-full">
                      {t("organization:setup.taxNumberLabel")}

                      <input
                        type="text"
                        name="taxNumber"
                        value={formData.taxNumber}
                        onChange={handleChange}
                        placeholder={t("organization:setup.taxNumberPlaceholder")}
                      />
                    </label>
                  </div>

                  <div className="organization-form-actions">
                    <span>
                      {t("organization:setup.later")}
                    </span>

                    <button
                      type="button"
                      onClick={goToNextStep}
                    >
                      {t("organization:setup.continue")}
                      <ArrowRight size={18} />
                    </button>
                  </div>
                </div>
              )}

              {activeStep === 2 && (
                <div className="organization-form-step">
                  <div className="organization-form-heading">
                    <p>{t("organization:setup.stepProgress", { current: 2, total: 2 })}</p>

                    <h2>
                      {t("organization:setup.contactTitle")}
                    </h2>

                    <span>
                      {t("organization:setup.contactDescription")}
                    </span>
                  </div>

                  <div className="organization-form-grid">
                    <label>
                      {t("organization:fields.email")}

                      <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder={t("organization:setup.emailPlaceholder")}
                      />
                    </label>

                    <label>
                      {t("organization:fields.phone")}

                      <input
                        type="tel"
                        name="phone"
                        value={formData.phone}
                        onChange={handleChange}
                        placeholder={t("organization:setup.phonePlaceholder")}
                      />
                    </label>

                    <label>
                      {t("organization:fields.province")}

                      <input
                        type="text"
                        name="province"
                        value={formData.province}
                        onChange={handleChange}
                        placeholder={t("organization:setup.provincePlaceholder")}
                      />
                    </label>

                    <label>
                      {t("organization:fields.city")}

                      <input
                        type="text"
                        name="city"
                        value={formData.city}
                        onChange={handleChange}
                        placeholder={t("organization:setup.cityPlaceholder")}
                      />
                    </label>

                    <label className="organization-field-full">
                      {t("organization:fields.address")}

                      <input
                        type="text"
                        name="address"
                        value={formData.address}
                        onChange={handleChange}
                        placeholder={t("organization:setup.addressPlaceholder")}
                      />
                    </label>

                    <label>
                      {t("organization:setup.currency")}

                      <select
                        name="defaultCurrency"
                        value={
                          formData.defaultCurrency
                        }
                        onChange={handleChange}
                      >
                        <option value="USD">
                          {t("organization:options.usd")}
                        </option>

                        <option value="CDF">
                          {t("organization:options.cdf")}
                        </option>
                      </select>
                    </label>

                    <label>
                      {t("organization:setup.timezone")}

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
                      {t("organization:setup.back")}
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

                          {t("organization:setup.creating")}
                        </>
                      ) : (
                        <>
                          {t("organization:setup.create")}
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
