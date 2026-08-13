import { useEffect, useMemo, useState } from "react";
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
    label: "Informations générales",
    description: "Identité officielle et commerciale",
    icon: Building2,
  },
  {
    id: "legal",
    label: "Informations juridiques",
    description: "RCCM, ID National et NIF",
    icon: FileBadge,
  },
  {
    id: "contact",
    label: "Coordonnées",
    description: "Téléphone, e-mail et site web",
    icon: Contact,
  },
  {
    id: "location",
    label: "Localisation",
    description: "Pays, province, ville et adresse",
    icon: MapPin,
  },
  {
    id: "regional",
    label: "Paramètres régionaux",
    description: "Devise, langue et fuseau horaire",
    icon: Globe2,
  },
  {
    id: "appearance",
    label: "Identité visuelle",
    description: "Logo et couleurs de la société",
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
    registrationNumber: normalizeValue(
      organization.registrationNumber
    ),
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
    defaultCurrency:
      normalizeValue(organization.defaultCurrency) || "USD",
    timezone:
      normalizeValue(organization.timezone) ||
      "Africa/Kinshasa",
    defaultLanguage:
      normalizeValue(organization.defaultLanguage) || "fr",
    primaryColor:
      normalizeValue(organization.primaryColor) || "#059669",
    secondaryColor:
      normalizeValue(organization.secondaryColor) || "#0f172a",
  };
}

function buildPayload(formData) {
  return {
    name: formData.name.trim(),
    tradeName: formData.tradeName.trim() || null,
    registrationNumber:
      formData.registrationNumber.trim() || null,
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
    Boolean(String(value || "").trim())
  ).length;

  return {
    completed,
    total: fields.length,
    percentage: Math.round(
      (completed / fields.length) * 100
    ),
  };
}

function OrganizationSettingsModal({
  isOpen,
  organizationId,
  onClose,
  onSaved,
}) {
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [organization, setOrganization] = useState(null);
  const [activeSection, setActiveSection] =
    useState("general");

  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const [selectedLogoFile, setSelectedLogoFile] =
  useState(null);

  const [logoPreviewUrl, setLogoPreviewUrl] =
  useState("");

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] =
    useState("");

  const progress = useMemo(
    () => calculateProgress(formData),
    [formData]
  );

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
        const result = await getOrganizationById(
          organizationId,
          {
            signal: controller.signal,
          }
        );

        setOrganization(result);
        setFormData(mapOrganizationToForm(result));
      } catch (error) {
        if (error?.name === "AbortError") {
          return;
        }

        setErrorMessage(
          error?.message ||
            "Impossible de charger les informations de la société."
        );
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
  }, [isOpen, organizationId]);

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

    setErrorMessage(
      "Format non autorisé. Sélectionnez un fichier PNG, JPG, WEBP ou SVG."
    );

    setActiveSection("appearance");
    return;
  }

  if (file.size > MAX_LOGO_SIZE) {
    event.target.value = "";

    setSelectedLogoFile(null);
    setLogoPreviewUrl("");

    setErrorMessage(
      "Le logo ne doit pas dépasser 2 Mo."
    );

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
      currentSection === sectionId
        ? ""
        : sectionId
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!organizationId) {
      setErrorMessage(
        "L’identifiant de l’organisation est indisponible."
      );
      return;
    }

    if (!formData.name.trim()) {
      setErrorMessage(
        "Le nom officiel de la société est obligatoire."
      );
      setActiveSection("general");
      return;
    }

    setIsSaving(true);
    setErrorMessage("");
    setSuccessMessage("");

    try {
      let updatedOrganization = await updateOrganization(
      organizationId,
      buildPayload(formData)
    );

    if (selectedLogoFile) {
      updatedOrganization =
        await uploadOrganizationLogo(
          organizationId,
          selectedLogoFile
        );
    }

    setOrganization(updatedOrganization);
    setFormData(
      mapOrganizationToForm(updatedOrganization)
    );

    setSelectedLogoFile(null);
    setLogoPreviewUrl("");

      const currentUser = getStoredUser() || {};

      const updatedUser = mergeStoredUser({
        ...currentUser,
        organizationId: updatedOrganization.id,
        organizationCode: updatedOrganization.code,
        organizationName:
          updatedOrganization.tradeName ||
          updatedOrganization.name,
        organizationOfficialName:
          updatedOrganization.name,
        organizationTradeName:
          updatedOrganization.tradeName || null,
        registrationNumber:
          updatedOrganization.registrationNumber || null,
        nationalId:
          updatedOrganization.nationalId || null,
        taxNumber:
          updatedOrganization.taxNumber || null,
        organizationAddress:
          updatedOrganization.address || null,
        organizationPhone:
          updatedOrganization.phone || null,
        organizationEmail:
          updatedOrganization.email || null,
        organizationLogo:
          updatedOrganization.logoUrl || null,
      });

      setSuccessMessage(
      selectedLogoFile
        ? "Les informations et le logo de la société ont été enregistrés."
        : "Les informations de la société ont été enregistrées."
    );

      onSaved?.(updatedOrganization, updatedUser);
    } catch (error) {
      setErrorMessage(
        error?.message ||
          "Impossible d’enregistrer les informations."
      );
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
        Annuler
      </button>

      <button
        type="submit"
        form="organization-settings-form"
        className="organization-settings-save"
        disabled={isLoading || isSaving}
      >
        {isSaving ? (
          <>
            <LoaderCircle
              size={18}
              className="organization-settings-spinner"
            />
            Enregistrement...
          </>
        ) : (
          <>
            <Save size={18} />
            Enregistrer
          </>
        )}
      </button>
    </>
  );

  return (
    <AppModal
      isOpen={isOpen}
      title="Paramètres de l’organisation"
      description="Complétez et actualisez les informations administratives de votre société."
      size="lg"
      footer={modalFooter}
      closeOnOverlay={!isSaving}
      closeOnEscape={!isSaving}
      onClose={handleClose}
    >
      {isLoading ? (
        <div className="organization-settings-loading">
          <LoaderCircle
            size={30}
            className="organization-settings-spinner"
          />

          <strong>Chargement de la société...</strong>

          <span>
            Nous récupérons les informations enregistrées.
          </span>
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
                <span>Progression de la configuration</span>

                <strong>
                  {progress.completed} sur {progress.total} éléments
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
                <span>Code organisation</span>
                <strong>{organization.code}</strong>
              </div>

              <div>
                <span>Statut</span>
                <strong
                  className={
                    organization.active
                      ? "organization-settings-status active"
                      : "organization-settings-status"
                  }
                >
                  <CheckCircle2 size={15} />
                  {organization.active
                    ? "Organisation active"
                    : organization.status || "Inactive"}
                </strong>
              </div>
            </section>
          )}

          {errorMessage && (
            <div
              className="organization-settings-alert error"
              role="alert"
            >
              {errorMessage}
            </div>
          )}

          {successMessage && (
            <div
              className="organization-settings-alert success"
              role="status"
            >
              <CheckCircle2 size={18} />
              {successMessage}
            </div>
          )}

          <div className="organization-settings-sections">
            {SECTIONS.map((section) => {
              const Icon = section.icon;
              const isActive =
                activeSection === section.id;

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
                    onClick={() =>
                      handleSectionToggle(section.id)
                    }
                    aria-expanded={isActive}
                  >
                    <span className="organization-settings-section-icon">
                      <Icon size={20} />
                    </span>

                    <span className="organization-settings-section-title">
                      <strong>{section.label}</strong>
                      <small>{section.description}</small>
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
                              Nom officiel
                              <strong>*</strong>
                            </span>

                            <input
                              type="text"
                              name="name"
                              value={formData.name}
                              onChange={handleChange}
                              placeholder="Nom légal de la société"
                              required
                            />
                          </label>

                          <label className="organization-settings-field-full">
                            <span>Nom commercial</span>

                            <input
                              type="text"
                              name="tradeName"
                              value={formData.tradeName}
                              onChange={handleChange}
                              placeholder="Marque ou enseigne commerciale"
                            />

                            <small>
                              Facultatif. Ce nom sera utilisé dans
                              l’interface lorsque renseigné.
                            </small>
                          </label>
                        </div>
                      )}

                      {section.id === "legal" && (
                        <div className="organization-settings-grid">
                          <label>
                            <span>RCCM</span>

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
                            <span>ID National</span>

                            <input
                              type="text"
                              name="nationalId"
                              value={formData.nationalId}
                              onChange={handleChange}
                              placeholder="Numéro d’identification nationale"
                            />
                          </label>

                          <label className="organization-settings-field-full">
                            <span>Numéro fiscal — NIF</span>

                            <input
                              type="text"
                              name="taxNumber"
                              value={formData.taxNumber}
                              onChange={handleChange}
                              placeholder="Numéro d’identification fiscale"
                            />
                          </label>
                        </div>
                      )}

                      {section.id === "contact" && (
                        <div className="organization-settings-grid">
                          <label>
                            <span>Téléphone</span>

                            <input
                              type="tel"
                              name="phone"
                              value={formData.phone}
                              onChange={handleChange}
                              placeholder="+243 ..."
                            />
                          </label>

                          <label>
                            <span>Adresse e-mail</span>

                            <input
                              type="email"
                              name="email"
                              value={formData.email}
                              onChange={handleChange}
                              placeholder="contact@societe.com"
                            />
                          </label>

                          <label className="organization-settings-field-full">
                            <span>Site web</span>

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
                            <span>Pays</span>

                            <input
                              type="text"
                              name="country"
                              value={formData.country}
                              onChange={handleChange}
                              placeholder="Pays"
                            />
                          </label>

                          <label>
                            <span>Province</span>

                            <input
                              type="text"
                              name="province"
                              value={formData.province}
                              onChange={handleChange}
                              placeholder="Province"
                            />
                          </label>

                          <label>
                            <span>Ville</span>

                            <input
                              type="text"
                              name="city"
                              value={formData.city}
                              onChange={handleChange}
                              placeholder="Ville"
                            />
                          </label>

                          <label>
                            <span>Adresse physique</span>

                            <input
                              type="text"
                              name="address"
                              value={formData.address}
                              onChange={handleChange}
                              placeholder="Avenue, numéro, commune"
                            />
                          </label>
                        </div>
                      )}

                      {section.id === "regional" && (
                        <div className="organization-settings-grid">
                          <label>
                            <span>Devise par défaut</span>

                            <select
                              name="defaultCurrency"
                              value={formData.defaultCurrency}
                              onChange={handleChange}
                            >
                              <option value="USD">
                                Dollar américain — USD
                              </option>
                              <option value="CDF">
                                Franc congolais — CDF
                              </option>
                            </select>
                          </label>

                          <label>
                            <span>Langue par défaut</span>

                            <select
                              name="defaultLanguage"
                              value={formData.defaultLanguage}
                              onChange={handleChange}
                            >
                              <option value="fr">
                                Français
                              </option>
                              <option value="en">
                                Anglais
                              </option>
                            </select>
                          </label>

                          <label className="organization-settings-field-full">
                            <span>Fuseau horaire</span>

                            <select
                              name="timezone"
                              value={formData.timezone}
                              onChange={handleChange}
                            >
                              <option value="Africa/Kinshasa">
                                Kinshasa — UTC+1
                              </option>
                              <option value="Africa/Lubumbashi">
                                Lubumbashi — UTC+2
                              </option>
                            </select>
                          </label>
                        </div>
                      )}

                      {section.id === "appearance" && (
                        <div className="organization-settings-grid">
                          <label className="organization-settings-field-full">
                            <span>Logo de la société</span>

                            <input
                              type="file"
                              accept=".png,.jpg,.jpeg,.webp,.svg,image/png,image/jpeg,image/webp,image/svg+xml"
                              onChange={handleLogoSelection}
                              disabled={isSaving}
                            />

                            <small>
                              Formats acceptés : PNG, JPG, WEBP et SVG.
                              Taille maximale : 2 Mo.
                            </small>

                            {selectedLogoFile && (
                              <small>
                                Fichier sélectionné :{" "}
                                <strong>{selectedLogoFile.name}</strong>
                              </small>
                            )}
                          </label>

                          <label>
                            <span>Couleur principale</span>

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
                            <span>Couleur secondaire</span>

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
                                  alt="Aperçu du logo"
                                />
                              ) : (
                                <Building2 size={26} />
                              )}

                              <div>
                                <strong>
                                  {formData.tradeName ||
                                    formData.name ||
                                    "Votre société"}
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