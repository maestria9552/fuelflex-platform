import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  AlertCircle,
  Building2,
  CheckCircle2,
  FileText,
  Globe2,
  LoaderCircle,
  Mail,
  MapPin,
  Pencil,
  Phone,
  ShieldCheck,
} from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import OrganizationSettingsModal from "../../features/organization/OrganizationSettingsModal";
import "./CompanyPage.css";
import {
  getStoredUser,
  mergeStoredUser,
} from "../../services/auth/authStorage";
import { getOrganizationById } from "../../services/organization/organizationService";

function buildUserOrganizationData(currentUser, organization) {
  return {
    ...currentUser,

    organizationConfigured: true,
    organizationId: organization.id,
    organizationCode: organization.code,

    organizationName: organization.tradeName || organization.name,

    organizationOfficialName: organization.name,

    organizationTradeName: organization.tradeName || null,

    registrationNumber: organization.registrationNumber || null,

    nationalId: organization.nationalId || null,

    taxNumber: organization.taxNumber || null,

    organizationAddress: organization.address || null,

    organizationPhone: organization.phone || null,

    organizationEmail: organization.email || null,

    organizationLogo: organization.logoUrl || null,
  };
}

function getDisplayValue(value, fallback) {
  if (value === null || value === undefined || String(value).trim() === "") {
    return fallback;
  }

  return value;
}

function CompanyPage() {
  const { t } = useTranslation("organization");
  const [user, setUser] = useState(() => getStoredUser() || {});

  const [organization, setOrganization] = useState(null);

  const organizationId = user.organizationId || null;

  const [isLoading, setIsLoading] = useState(Boolean(organizationId));

  const [errorMessage, setErrorMessage] = useState(
    organizationId ? "" : t("feedback.noOrganization"),
  );

  const [isOrganizationModalOpen, setIsOrganizationModalOpen] = useState(false);

  useEffect(() => {
    if (!organizationId) {
      return undefined;
    }

    let isCancelled = false;

    const loadOrganization = async () => {
      setIsLoading(true);
      setErrorMessage("");

      try {
        const result = await getOrganizationById(organizationId);

        if (isCancelled) {
          return;
        }

        setOrganization(result);

        setUser((currentUser) => {
          const refreshedUser = buildUserOrganizationData(currentUser, result);

          return mergeStoredUser(refreshedUser);
        });
      } catch (error) {
        if (isCancelled) {
          return;
        }

        console.error("Impossible de charger la société :", error);

        setErrorMessage(
          error?.response?.data?.message ||
            error?.message ||
            t("feedback.loadError"),
        );
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    };

    loadOrganization();

    return () => {
      isCancelled = true;
    };
  }, [organizationId, t]);

  const handleOpenOrganizationModal = () => {
    setIsOrganizationModalOpen(true);
  };

  const handleCloseOrganizationModal = () => {
    setIsOrganizationModalOpen(false);
  };

  const handleOrganizationSaved = (updatedOrganization, updatedUser) => {
    setOrganization(updatedOrganization);

    if (updatedUser) {
      const storedUser = mergeStoredUser(updatedUser);

      setUser(storedUser);
      setIsOrganizationModalOpen(false);
      return;
    }

    setUser((currentUser) => {
      const refreshedUser = buildUserOrganizationData(
        currentUser,
        updatedOrganization,
      );

      mergeStoredUser(refreshedUser);

      return refreshedUser;
    });

    setIsOrganizationModalOpen(false);
  };

  const administrativeFields = [
    organization?.registrationNumber,
    organization?.nationalId,
    organization?.taxNumber,
    organization?.address,
    organization?.logoUrl,
  ];

  const completedAdministrativeFields =
    administrativeFields.filter(Boolean).length;

  const completionPercentage = Math.round(
    (completedAdministrativeFields / administrativeFields.length) * 100,
  );

  const isComplete = completionPercentage === 100;

  if (isLoading) {
    return (
      <SupervisorLayout>
        <main className="company-page">
          <section className="company-page-loading">
            <LoaderCircle size={34} className="company-page-spinner" />

            <p>{t("feedback.loadingPage")}</p>
          </section>
        </main>
      </SupervisorLayout>
    );
  }

  if (errorMessage) {
    return (
      <SupervisorLayout>
        <main className="company-page">
          <section className="company-page-error">
            <AlertCircle size={34} />

            <div>
              <h1>{t("page.title")}</h1>
              <p>{errorMessage}</p>
            </div>
          </section>
        </main>
      </SupervisorLayout>
    );
  }

  return (
    <SupervisorLayout>
      <main className="company-page">
        <section className="company-page-header">
          <div>
            <span className="company-page-eyebrow">{t("page.eyebrow")}</span>

            <h1>{t("page.title")}</h1>

            <p>{t("page.description")}</p>
          </div>

          <button
            type="button"
            className="company-page-edit-button"
            onClick={handleOpenOrganizationModal}
          >
            <Pencil size={18} />
            {t("page.edit")}
          </button>
        </section>

        <section className="company-page-identity-card">
          <div className="company-page-logo">
            {organization?.logoUrl ? (
              <img
                src={organization.logoUrl}
                alt={t("accessibility.logoAlt", {
                  name: organization.tradeName || organization.name,
                })}
              />
            ) : (
              <Building2 size={40} />
            )}
          </div>

          <div className="company-page-identity">
            <span>{t("page.registered")}</span>

            <h2>
              {getDisplayValue(
                organization?.tradeName || organization?.name,
                t("feedback.noValue"),
              )}
            </h2>

            {organization?.tradeName && organization?.name && (
              <p>{organization.name}</p>
            )}

            <div className="company-page-code">
              <ShieldCheck size={17} />

              <span>{t("page.fuelFlexCode")}</span>

              <strong>
                {getDisplayValue(organization?.code, t("feedback.noValue"))}
              </strong>
            </div>
          </div>

          <div
            className={[
              "company-page-status",
              isComplete
                ? "company-page-status-complete"
                : "company-page-status-incomplete",
            ].join(" ")}
          >
            {isComplete ? (
              <CheckCircle2 size={20} />
            ) : (
              <AlertCircle size={20} />
            )}

            <div>
              <span>{t("page.administrativeProfile")}</span>

              <strong>{completionPercentage} %</strong>
            </div>
          </div>
        </section>

        <section className="company-page-grid">
          <article className="company-page-card">
            <header>
              <div>
                <FileText size={20} />

                <span>{t("page.administrativeInformation")}</span>
              </div>
            </header>

            <div className="company-page-information-list">
              <div>
                <span>{t("page.officialName")}</span>

                <strong>
                  {getDisplayValue(organization?.name, t("feedback.noValue"))}
                </strong>
              </div>

              <div>
                <span>{t("fields.tradeName")}</span>

                <strong>
                  {getDisplayValue(
                    organization?.tradeName,
                    t("feedback.noValue"),
                  )}
                </strong>
              </div>

              <div>
                <span>{t("fields.rccm")}</span>

                <strong>
                  {getDisplayValue(
                    organization?.registrationNumber,
                    t("feedback.noValue"),
                  )}
                </strong>
              </div>

              <div>
                <span>{t("fields.nationalId")}</span>

                <strong>
                  {getDisplayValue(
                    organization?.nationalId,
                    t("feedback.noValue"),
                  )}
                </strong>
              </div>

              <div>
                <span>{t("fields.nif")}</span>

                <strong>
                  {getDisplayValue(
                    organization?.taxNumber,
                    t("feedback.noValue"),
                  )}
                </strong>
              </div>
            </div>
          </article>

          <article className="company-page-card">
            <header>
              <div>
                <MapPin size={20} />

                <span>{t("page.contactAndLocation")}</span>
              </div>
            </header>

            <div className="company-page-contact-list">
              <div>
                <span className="company-page-contact-icon">
                  <MapPin size={18} />
                </span>

                <div>
                  <span>{t("fields.addressShort")}</span>

                  <strong>
                    {getDisplayValue(
                      organization?.address,
                      t("feedback.noValue"),
                    )}
                  </strong>
                </div>
              </div>

              <div>
                <span className="company-page-contact-icon">
                  <Phone size={18} />
                </span>

                <div>
                  <span>{t("fields.phone")}</span>

                  <strong>
                    {getDisplayValue(
                      organization?.phone,
                      t("feedback.noValue"),
                    )}
                  </strong>
                </div>
              </div>

              <div>
                <span className="company-page-contact-icon">
                  <Mail size={18} />
                </span>

                <div>
                  <span>{t("fields.email")}</span>

                  <strong>
                    {getDisplayValue(
                      organization?.email,
                      t("feedback.noValue"),
                    )}
                  </strong>
                </div>
              </div>
            </div>
          </article>

          <article className="company-page-card">
            <header>
              <div>
                <Globe2 size={20} />

                <span>{t("sections.regional.label")}</span>
              </div>
            </header>

            <div className="company-page-information-list">
              <div>
                <span>{t("fields.country")}</span>

                <strong>
                  {getDisplayValue(
                    organization?.country,
                    t("feedback.noValue"),
                  )}
                </strong>
              </div>

              <div>
                <span>{t("fields.currency")}</span>

                <strong>
                  {getDisplayValue(
                    organization?.defaultCurrency,
                    t("feedback.noValue"),
                  )}
                </strong>
              </div>

              <div>
                <span>{t("fields.timezone")}</span>

                <strong>
                  {getDisplayValue(
                    organization?.timezone,
                    t("feedback.noValue"),
                  )}
                </strong>
              </div>
            </div>
          </article>

          <article className="company-page-card">
            <header>
              <div>
                <ShieldCheck size={20} />

                <span>{t("page.configurationStatus")}</span>
              </div>
            </header>

            <div className="company-page-progress">
              <div>
                <span>{t("page.essentialInformation")}</span>

                <strong>
                  {t("progress.items", {
                    completed: completedAdministrativeFields,
                    count: administrativeFields.length,
                  })}
                </strong>
              </div>

              <div className="company-page-progress-track">
                <span
                  style={{
                    width: `${completionPercentage}%`,
                  }}
                />
              </div>

              <p>{isComplete ? t("page.complete") : t("page.incomplete")}</p>
            </div>
          </article>
        </section>
      </main>

      <OrganizationSettingsModal
        key={isOrganizationModalOpen ? "open" : "closed"}
        isOpen={isOrganizationModalOpen}
        organizationId={organizationId}
        onClose={handleCloseOrganizationModal}
        onSaved={handleOrganizationSaved}
      />
    </SupervisorLayout>
  );
}

export default CompanyPage;
