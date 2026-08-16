import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  Building2,
  CheckCircle2,
  ChevronDown,
  ChevronRight,
  Contact,
  FileBadge,
  Globe2,
  Image,
  LoaderCircle,
  MapPin,
  Save,
  Settings2,
} from "lucide-react";

import AppModal from "../../components/modal/AppModal";
import {
  getOrganizationById,
  updateOrganization,
  uploadOrganizationLogo,
} from "../../services/organization/organizationService";
import {
  getStoredUser,
  mergeStoredUser,
} from "../../services/auth/authStorage";

import "./OrganizationSettingsModal.css";

const EMPTY_FORM = {
  name: "",
  tradeName: "",
  registrationNumber: "",
  nationalId: "",
  taxNumber: "",
  email: "",
  phone: "",
  website: "",
  logoUrl: "",
  country: "République démocratique du Congo",
  province: "",
  city: "",
  address: "",
  defaultCurrency: "USD",
  timezone: "Africa/Kinshasa",
  defaultLanguage: "fr",
  primaryColor: "#059669",
  secondaryColor: "#0f172a",
};

const SECTIONS = [
  {
    id: "general",
    icon: Building2,
  },
  {
    id: "legal",
    icon: FileBadge,
  },
  {
    id: "contact",
    icon: Contact,
  },
  {
    id: "location",
    icon: MapPin,
  },
  {
    id: "regional",
    icon: Globe2,
  },
  {
    id: "appearance",
    icon: Image,
  },
];

const MAX_LOGO_SIZE = 2 * 1024 * 1024;

const ALLOWED_LOGO_TYPES = [
  "image/png",
  "image/jpeg",
  "image/webp",
  "image/svg+xml",
];

function normalizeValue(value) {
  return value ?? "";
}

function mapOrganizationToForm(organization) {
  return {
    name: normalizeValue(organization.name),
    tradeName: normalizeValue(organization.tradeName),
    registrationNumber: normalizeValue(organization.registrationNumber),
    nationalId: normalizeValue(organization.nationalId),
    taxNumber: normalizeValue(organization.taxNumber),
    email: normalizeValue(organization.email),
    phone: normalizeValue(organization.phone),
    website: normalizeValue(organization.website),
    logoUrl: normalizeValue(organization.logoUrl),
    country:
      normalizeValue(organization.country) ||
      "République démocratique du Congo",
    province: normalizeValue(organization.province),
    city: normalizeValue(organization.city),
    address: normalizeValue(organization.address),
    defaultCurrency: normalizeValue(organization.defaultCurrency) || "USD",
    timezone: normalizeValue(organization.timezone) || "Africa/Kinshasa",
    defaultLanguage: normalizeValue(organization.defaultLanguage) || "fr",
    primaryColor: normalizeValue(organization.primaryColor) || "#059669",
    secondaryColor: normalizeValue(organization.secondaryColor) || "#0f172a",
  };
}

function buildPayload(formData) {
  return {
    name: formData.name.trim(),
    tradeName: formData.tradeName.trim() || null,
    registrationNumber: formData.registrationNumber.trim() || null,
    nationalId: formData.nationalId.trim() || null,
    taxNumber: formData.taxNumber.trim() || null,
    email: formData.email.trim() || null,
    phone: formData.phone.trim() || null,
    website: formData.website.trim() || null,
    logoUrl: formData.logoUrl || null,
    country: formData.country.trim() || null,
    province: formData.province.trim() || null,
    city: formData.city.trim() || null,
    address: formData.address.trim() || null,
    defaultCurrency: formData.defaultCurrency || null,
    timezone: formData.timezone || null,
    defaultLanguage: formData.defaultLanguage || null,
    primaryColor: formData.primaryColor || null,
    secondaryColor: formData.secondaryColor || null,
  };
}

function calculateProgress(formData) {
  const fields = [
    formData.name,
    formData.registrationNumber,
    formData.nationalId,
    formData.taxNumber,
    formData.address,
    formData.phone,
    formData.email,
    formData.logoUrl,
  ];

  const completed = fields.filter((value) =>
    Boolean(String(value || "").trim()),
  ).length;

  return {
    completed,
    total: fields.length,
    percentage: Math.round((completed / fields.length) * 100),
  };
}

function OrganizationSettingsModal({
  isOpen,
  organizationId,
  onClose,
  onSaved,
}) {
  const { t } = useTranslation(["organization", "common"]);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [organization, setOrganization] = useState(null);
  const [activeSection, setActiveSection] = useState("general");

  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const [selectedLogoFile, setSelectedLogoFile] = useState(null);

  const [logoPreviewUrl, setLogoPreviewUrl] = useState("");

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const progress = useMemo(() => calculateProgress(formData), [formData]);

  useEffect(() => {
    if (!isOpen || !organizationId) {
      return undefined;
    }

    const controller = new AbortController();

    async function loadOrganization() {
      setIsLoading(true);
      setErrorMessage("");
      setSuccessMessage("");

      try {
        const result = await getOrganizationById(organizationId, {
          signal: controller.signal,
        });

        setOrganization(result);
        setFormData(mapOrganizationToForm(result));
      } catch (error) {
        if (error?.name === "AbortError") {
          return;
        }

        setErrorMessage(error?.message || t("feedback.loadError"));
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    loadOrganization();

    return () => {
      controller.abort();
    };
  }, [isOpen, organizationId, t]);

  const resetModalState = () => {
    if (logoPreviewUrl) {
      URL.revokeObjectURL(logoPreviewUrl);
    }

    setActiveSection("general");
    setErrorMessage("");
    setSuccessMessage("");
    setSelectedLogoFile(null);
    setLogoPreviewUrl("");
  };

  const handleClose = () => {
    resetModalState();
    onClose?.();
  };

  useEffect(() => {
    return () => {
      if (logoPreviewUrl) {
        URL.revokeObjectURL(logoPreviewUrl);
      }
    };
  }, [logoPreviewUrl]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }));

    setSuccessMessage("");
  };

  const handleLogoSelection = (event) => {
    const file = event.target.files?.[0];

    setErrorMessage("");
    setSuccessMessage("");

    if (!file) {
      setSelectedLogoFile(null);
      setLogoPreviewUrl("");
      return;
    }

    if (!ALLOWED_LOGO_TYPES.includes(file.type)) {
      event.target.value = "";

      setSelectedLogoFile(null);
      setLogoPreviewUrl("");

      setErrorMessage(t("feedback.invalidLogoType"));

      setActiveSection("appearance");
      return;
    }

    if (file.size > MAX_LOGO_SIZE) {
      event.target.value = "";

      setSelectedLogoFile(null);
      setLogoPreviewUrl("");

      setErrorMessage(t("feedback.logoTooLarge"));

      setActiveSection("appearance");
      return;
    }

    if (logoPreviewUrl) {
      URL.revokeObjectURL(logoPreviewUrl);
    }

    const previewUrl = URL.createObjectURL(file);

    setSelectedLogoFile(file);
    setLogoPreviewUrl(previewUrl);
  };

  const handleSectionToggle = (sectionId) => {
    setActiveSection((currentSection) =>
      currentSection === sectionId ? "" : sectionId,
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!organizationId) {
      setErrorMessage(t("feedback.missingId"));
      return;
    }

    if (!formData.name.trim()) {
      setErrorMessage(t("feedback.nameRequired"));
      setActiveSection("general");
      return;
    }

    setIsSaving(true);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      let updatedOrganization = await updateOrganization(
        organizationId,
        buildPayload(formData),
      );

      if (selectedLogoFile) {
        updatedOrganization = await uploadOrganizationLogo(
          organizationId,
          selectedLogoFile,
        );
      }

      setOrganization(updatedOrganization);
      setFormData(mapOrganizationToForm(updatedOrganization));

      setSelectedLogoFile(null);
      setLogoPreviewUrl("");

      const currentUser = getStoredUser() || {};

      const updatedUser = mergeStoredUser({
        ...currentUser,
        organizationId: updatedOrganization.id,
        organizationCode: updatedOrganization.code,
        organizationName:
          updatedOrganization.tradeName || updatedOrganization.name,
        organizationOfficialName: updatedOrganization.name,
        organizationTradeName: updatedOrganization.tradeName || null,
        registrationNumber: updatedOrganization.registrationNumber || null,
        nationalId: updatedOrganization.nationalId || null,
        taxNumber: updatedOrganization.taxNumber || null,
        organizationAddress: updatedOrganization.address || null,
        organizationPhone: updatedOrganization.phone || null,
        organizationEmail: updatedOrganization.email || null,
        organizationLogo: updatedOrganization.logoUrl || null,
      });

      setSuccessMessage(
        selectedLogoFile ? t("feedback.savedWithLogo") : t("feedback.saved"),
      );

      onSaved?.(updatedOrganization, updatedUser);
    } catch (error) {
      setErrorMessage(error?.message || t("feedback.saveError"));
    } finally {
      setIsSaving(false);
    }
  };

  const modalFooter = (
    <>
      <button
        type="button"
        className="organization-settings-cancel"
        onClick={handleClose}
        disabled={isSaving}
      >
        {t("common:actions.cancel")}
      </button>

      <button
        type="submit"
        form="organization-settings-form"
        className="organization-settings-save"
        disabled={isLoading || isSaving}
      >
        {isSaving ? (
          <>
            <LoaderCircle size={18} className="organization-settings-spinner" />
            {t("modal.saving")}
          </>
        ) : (
          <>
            <Save size={18} />
            {t("common:actions.save")}
          </>
        )}
      </button>
    </>
  );

  return (
    <AppModal
      isOpen={isOpen}
      title={t("modal.title")}
      description={t("modal.description")}
      size="lg"
      footer={modalFooter}
      closeOnOverlay={!isSaving}
      closeOnEscape={!isSaving}
      onClose={handleClose}
    >
      {isLoading ? (
        <div className="organization-settings-loading">
          <LoaderCircle size={30} className="organization-settings-spinner" />

          <strong>{t("modal.loading")}</strong>

          <span>{t("modal.loadingDescription")}</span>
        </div>
      ) : (
        <form
          id="organization-settings-form"
          className="organization-settings-form"
          onSubmit={handleSubmit}
        >
          <section className="organization-settings-summary">
            <div className="organization-settings-summary-icon">
              <Settings2 size={23} />
            </div>

            <div className="organization-settings-summary-content">
              <div>
                <span>{t("modal.configurationProgress")}</span>

                <strong>
                  {t("progress.items", {
                    completed: progress.completed,
                    count: progress.total,
                  })}
                </strong>
              </div>

              <div className="organization-settings-progress">
                <span
                  style={{
                    width: `${progress.percentage}%`,
                  }}
                />
              </div>
            </div>

            <div className="organization-settings-percentage">
              {progress.percentage} %
            </div>
          </section>

          {organization && (
            <section className="organization-settings-metadata">
              <div>
                <span>{t("modal.organizationCode")}</span>
                <strong>{organization.code}</strong>
              </div>

              <div>
                <span>{t("fields.status")}</span>
                <strong
                  className={
                    organization.active
                      ? "organization-settings-status active"
                      : "organization-settings-status"
                  }
                >
                  <CheckCircle2 size={15} />
                  {organization.active
                    ? t("modal.active")
                    : t(`status.${organization.status}`, {
                        defaultValue: t("modal.inactive"),
                      })}
                </strong>
              </div>
            </section>
          )}

          {errorMessage && (
            <div className="organization-settings-alert error" role="alert">
              {errorMessage}
            </div>
          )}

          {successMessage && (
            <div className="organization-settings-alert success" role="status">
              <CheckCircle2 size={18} />
              {successMessage}
            </div>
          )}

          <div className="organization-settings-sections">
            {SECTIONS.map((section) => {
              const Icon = section.icon;
              const isActive = activeSection === section.id;

              return (
                <section
                  key={section.id}
                  className={[
                    "organization-settings-section",
                    isActive ? "active" : "",
                  ]
                    .filter(Boolean)
                    .join(" ")}
                >
                  <button
                    type="button"
                    className="organization-settings-section-header"
                    onClick={() => handleSectionToggle(section.id)}
                    aria-expanded={isActive}
                    aria-label={t("accessibility.sectionToggle", {
                      section: t(`sections.${section.id}.label`),
                    })}
                  >
                    <span className="organization-settings-section-icon">
                      <Icon size={20} />
                    </span>

                    <span className="organization-settings-section-title">
                      <strong>{t(`sections.${section.id}.label`)}</strong>
                      <small>{t(`sections.${section.id}.description`)}</small>
                    </span>

                    {isActive ? (
                      <ChevronDown size={20} />
                    ) : (
                      <ChevronRight size={20} />
                    )}
                  </button>

                  {isActive && (
                    <div className="organization-settings-section-body">
                      {section.id === "general" && (
                        <div className="organization-settings-grid">
                          <label className="organization-settings-field-full">
                            <span>
                              {t("fields.officialName")}
                              <strong>*</strong>
                            </span>

                            <input
                              type="text"
                              name="name"
                              value={formData.name}
                              onChange={handleChange}
                              placeholder={t("placeholders.officialName")}
                              required
                            />
                          </label>

                          <label className="organization-settings-field-full">
                            <span>{t("fields.tradeName")}</span>

                            <input
                              type="text"
                              name="tradeName"
                              value={formData.tradeName}
                              onChange={handleChange}
                              placeholder={t("placeholders.tradeName")}
                            />

                            <small>{t("help.tradeName")}</small>
                          </label>
                        </div>
                      )}

                      {section.id === "legal" && (
                        <div className="organization-settings-grid">
                          <label>
                            <span>{t("fields.rccm")}</span>

                            <input
                              type="text"
                              name="registrationNumber"
                              value={formData.registrationNumber}
                              onChange={handleChange}
                              placeholder="CD/KIN/RCCM/..."
                            />
                          </label>

                          <label>
                            <span>{t("fields.nationalId")}</span>

                            <input
                              type="text"
                              name="nationalId"
                              value={formData.nationalId}
                              onChange={handleChange}
                              placeholder={t("placeholders.nationalId")}
                            />
                          </label>

                          <label className="organization-settings-field-full">
                            <span>{t("fields.taxNumber")}</span>

                            <input
                              type="text"
                              name="taxNumber"
                              value={formData.taxNumber}
                              onChange={handleChange}
                              placeholder={t("placeholders.taxNumber")}
                            />
                          </label>
                        </div>
                      )}

                      {section.id === "contact" && (
                        <div className="organization-settings-grid">
                          <label>
                            <span>{t("fields.phone")}</span>

                            <input
                              type="tel"
                              name="phone"
                              value={formData.phone}
                              onChange={handleChange}
                              placeholder="+243 ..."
                            />
                          </label>

                          <label>
                            <span>{t("fields.email")}</span>

                            <input
                              type="email"
                              name="email"
                              value={formData.email}
                              onChange={handleChange}
                              placeholder="contact@societe.com"
                            />
                          </label>

                          <label className="organization-settings-field-full">
                            <span>{t("fields.website")}</span>

                            <input
                              type="url"
                              name="website"
                              value={formData.website}
                              onChange={handleChange}
                              placeholder="https://www.societe.com"
                            />
                          </label>
                        </div>
                      )}

                      {section.id === "location" && (
                        <div className="organization-settings-grid">
                          <label>
                            <span>{t("fields.country")}</span>

                            <input
                              type="text"
                              name="country"
                              value={formData.country}
                              onChange={handleChange}
                              placeholder={t("placeholders.country")}
                            />
                          </label>

                          <label>
                            <span>{t("fields.province")}</span>

                            <input
                              type="text"
                              name="province"
                              value={formData.province}
                              onChange={handleChange}
                              placeholder={t("placeholders.province")}
                            />
                          </label>

                          <label>
                            <span>{t("fields.city")}</span>

                            <input
                              type="text"
                              name="city"
                              value={formData.city}
                              onChange={handleChange}
                              placeholder={t("placeholders.city")}
                            />
                          </label>

                          <label>
                            <span>{t("fields.address")}</span>

                            <input
                              type="text"
                              name="address"
                              value={formData.address}
                              onChange={handleChange}
                              placeholder={t("placeholders.address")}
                            />
                          </label>
                        </div>
                      )}

                      {section.id === "regional" && (
                        <div className="organization-settings-grid">
                          <label>
                            <span>{t("fields.defaultCurrency")}</span>

                            <select
                              name="defaultCurrency"
                              value={formData.defaultCurrency}
                              onChange={handleChange}
                            >
                              <option value="USD">{t("options.usd")}</option>
                              <option value="CDF">{t("options.cdf")}</option>
                            </select>
                          </label>

                          <label>
                            <span>{t("fields.defaultLanguage")}</span>

                            <select
                              name="defaultLanguage"
                              value={formData.defaultLanguage}
                              onChange={handleChange}
                            >
                              <option value="fr">{t("options.fr")}</option>
                              <option value="en">{t("options.en")}</option>
                            </select>
                          </label>

                          <label className="organization-settings-field-full">
                            <span>{t("fields.timezone")}</span>

                            <select
                              name="timezone"
                              value={formData.timezone}
                              onChange={handleChange}
                            >
                              <option value="Africa/Kinshasa">
                                {t("options.kinshasa")}
                              </option>
                              <option value="Africa/Lubumbashi">
                                {t("options.lubumbashi")}
                              </option>
                            </select>
                          </label>
                        </div>
                      )}

                      {section.id === "appearance" && (
                        <div className="organization-settings-grid">
                          <label className="organization-settings-field-full">
                            <span>{t("fields.logo")}</span>

                            <input
                              type="file"
                              accept=".png,.jpg,.jpeg,.webp,.svg,image/png,image/jpeg,image/webp,image/svg+xml"
                              onChange={handleLogoSelection}
                              disabled={isSaving}
                            />

                            <small>{t("help.logo")}</small>

                            {selectedLogoFile && (
                              <small>
                                {t("help.selectedFile", {
                                  name: selectedLogoFile.name,
                                })}
                              </small>
                            )}
                          </label>

                          <label>
                            <span>{t("fields.primaryColor")}</span>

                            <div className="organization-settings-color-field">
                              <input
                                type="color"
                                name="primaryColor"
                                value={formData.primaryColor}
                                onChange={handleChange}
                              />

                              <input
                                type="text"
                                name="primaryColor"
                                value={formData.primaryColor}
                                onChange={handleChange}
                                placeholder="#059669"
                              />
                            </div>
                          </label>

                          <label>
                            <span>{t("fields.secondaryColor")}</span>

                            <div className="organization-settings-color-field">
                              <input
                                type="color"
                                name="secondaryColor"
                                value={formData.secondaryColor}
                                onChange={handleChange}
                              />

                              <input
                                type="text"
                                name="secondaryColor"
                                value={formData.secondaryColor}
                                onChange={handleChange}
                                placeholder="#0f172a"
                              />
                            </div>
                          </label>

                          <div className="organization-settings-field-full organization-settings-preview">
                            <div
                              className="organization-settings-preview-brand"
                              style={{
                                background: formData.secondaryColor,
                              }}
                            >
                              {logoPreviewUrl || formData.logoUrl ? (
                                <img
                                  src={logoPreviewUrl || formData.logoUrl}
                                  alt={t("accessibility.previewLogoAlt")}
                                />
                              ) : (
                                <Building2 size={26} />
                              )}

                              <div>
                                <strong>
                                  {formData.tradeName ||
                                    formData.name ||
                                    t("accessibility.companyFallback")}
                                </strong>

                                <span
                                  style={{
                                    color: formData.primaryColor,
                                  }}
                                >
                                  FuelFlex Platform
                                </span>
                              </div>
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </section>
              );
            })}
          </div>
        </form>
      )}
    </AppModal>
  );
}

export default OrganizationSettingsModal;
