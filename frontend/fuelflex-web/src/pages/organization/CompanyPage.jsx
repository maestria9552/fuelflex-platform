import { useEffect, useState } from "react";
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

    organizationName:
      organization.tradeName ||
      organization.name,

    organizationOfficialName:
      organization.name,

    organizationTradeName:
      organization.tradeName || null,

    registrationNumber:
      organization.registrationNumber || null,

    nationalId:
      organization.nationalId || null,

    taxNumber:
      organization.taxNumber || null,

    organizationAddress:
      organization.address || null,

    organizationPhone:
      organization.phone || null,

    organizationEmail:
      organization.email || null,

    organizationLogo:
      organization.logoUrl || null,
  };
}

function getDisplayValue(value) {
  if (
    value === null ||
    value === undefined ||
    String(value).trim() === ""
  ) {
    return "Non renseigné";
  }

  return value;
}

function CompanyPage() {
  const [user, setUser] = useState(
    () => getStoredUser() || {}
  );

  const [organization, setOrganization] =
    useState(null);

  const organizationId =
    user.organizationId || null;

  const [isLoading, setIsLoading] =
    useState(Boolean(organizationId));

  const [errorMessage, setErrorMessage] =
    useState(
      organizationId
        ? ""
        : "Aucune société n’est associée à ce compte."
    );

  const [
    isOrganizationModalOpen,
    setIsOrganizationModalOpen,
  ] = useState(false);

  useEffect(() => {
    if (!organizationId) {
      return undefined;
    }

    let isCancelled = false;

    const loadOrganization = async () => {
      setIsLoading(true);
      setErrorMessage("");

      try {
        const result =
          await getOrganizationById(
            organizationId
          );

        if (isCancelled) {
          return;
        }

        setOrganization(result);

        setUser((currentUser) => {
          const refreshedUser =
            buildUserOrganizationData(
              currentUser,
              result
            );

          return mergeStoredUser(refreshedUser);
        });
      } catch (error) {
        if (isCancelled) {
          return;
        }

        console.error(
          "Impossible de charger la société :",
          error
        );

        setErrorMessage(
          error?.response?.data?.message ||
            error?.message ||
            "Impossible de charger les informations de la société."
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
  }, [organizationId]);

  const handleOpenOrganizationModal = () => {
    setIsOrganizationModalOpen(true);
  };

  const handleCloseOrganizationModal = () => {
    setIsOrganizationModalOpen(false);
  };

  const handleOrganizationSaved = (
    updatedOrganization,
    updatedUser
  ) => {
    setOrganization(updatedOrganization);

    if (updatedUser) {
      const storedUser =
        mergeStoredUser(updatedUser);

      setUser(storedUser);
      setIsOrganizationModalOpen(false);
      return;
    }

    setUser((currentUser) => {
      const refreshedUser =
        buildUserOrganizationData(
          currentUser,
          updatedOrganization
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
    (completedAdministrativeFields /
      administrativeFields.length) *
      100
  );

  const isComplete =
    completionPercentage === 100;

  if (isLoading) {
    return (
      <SupervisorLayout>
        <main className="company-page">
          <section className="company-page-loading">
            <LoaderCircle
              size={34}
              className="company-page-spinner"
            />

            <p>
              Chargement des informations de la
              société...
            </p>
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
              <h1>Société</h1>
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
            <span className="company-page-eyebrow">
              Administration
            </span>

            <h1>Société</h1>

            <p>
              Consultez et gérez les informations
              administratives et professionnelles de
              votre organisation.
            </p>
          </div>

          <button
            type="button"
            className="company-page-edit-button"
            onClick={handleOpenOrganizationModal}
          >
            <Pencil size={18} />
            Modifier les informations
          </button>
        </section>

        <section className="company-page-identity-card">
          <div className="company-page-logo">
            {organization?.logoUrl ? (
              <img
                src={organization.logoUrl}
                alt={`Logo ${
                  organization.tradeName ||
                  organization.name
                }`}
              />
            ) : (
              <Building2 size={40} />
            )}
          </div>

          <div className="company-page-identity">
            <span>Société enregistrée</span>

            <h2>
              {getDisplayValue(
                organization?.tradeName ||
                  organization?.name
              )}
            </h2>

            {organization?.tradeName &&
              organization?.name && (
                <p>{organization.name}</p>
              )}

            <div className="company-page-code">
              <ShieldCheck size={17} />

              <span>Code FuelFlex</span>

              <strong>
                {getDisplayValue(
                  organization?.code
                )}
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
              <span>
                Profil administratif
              </span>

              <strong>
                {completionPercentage} %
              </strong>
            </div>
          </div>
        </section>

        <section className="company-page-grid">
          <article className="company-page-card">
            <header>
              <div>
                <FileText size={20} />

                <span>
                  Informations administratives
                </span>
              </div>
            </header>

            <div className="company-page-information-list">
              <div>
                <span>
                  Dénomination officielle
                </span>

                <strong>
                  {getDisplayValue(
                    organization?.name
                  )}
                </strong>
              </div>

              <div>
                <span>Nom commercial</span>

                <strong>
                  {getDisplayValue(
                    organization?.tradeName
                  )}
                </strong>
              </div>

              <div>
                <span>RCCM</span>

                <strong>
                  {getDisplayValue(
                    organization?.registrationNumber
                  )}
                </strong>
              </div>

              <div>
                <span>ID National</span>

                <strong>
                  {getDisplayValue(
                    organization?.nationalId
                  )}
                </strong>
              </div>

              <div>
                <span>NIF</span>

                <strong>
                  {getDisplayValue(
                    organization?.taxNumber
                  )}
                </strong>
              </div>
            </div>
          </article>

          <article className="company-page-card">
            <header>
              <div>
                <MapPin size={20} />

                <span>
                  Coordonnées et localisation
                </span>
              </div>
            </header>

            <div className="company-page-contact-list">
              <div>
                <span className="company-page-contact-icon">
                  <MapPin size={18} />
                </span>

                <div>
                  <span>Adresse</span>

                  <strong>
                    {getDisplayValue(
                      organization?.address
                    )}
                  </strong>
                </div>
              </div>

              <div>
                <span className="company-page-contact-icon">
                  <Phone size={18} />
                </span>

                <div>
                  <span>Téléphone</span>

                  <strong>
                    {getDisplayValue(
                      organization?.phone
                    )}
                  </strong>
                </div>
              </div>

              <div>
                <span className="company-page-contact-icon">
                  <Mail size={18} />
                </span>

                <div>
                  <span>Adresse e-mail</span>

                  <strong>
                    {getDisplayValue(
                      organization?.email
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

                <span>
                  Paramètres régionaux
                </span>
              </div>
            </header>

            <div className="company-page-information-list">
              <div>
                <span>Pays</span>

                <strong>
                  {getDisplayValue(
                    organization?.country
                  )}
                </strong>
              </div>

              <div>
                <span>Devise</span>

                <strong>
                  {getDisplayValue(
                    organization?.defaultCurrency
                  )}
                </strong>
              </div>

              <div>
                <span>Fuseau horaire</span>

                <strong>
                  {getDisplayValue(
                    organization?.timezone
                  )}
                </strong>
              </div>
            </div>
          </article>

          <article className="company-page-card">
            <header>
              <div>
                <ShieldCheck size={20} />

                <span>
                  État de la configuration
                </span>
              </div>
            </header>

            <div className="company-page-progress">
              <div>
                <span>
                  Informations essentielles
                </span>

                <strong>
                  {completedAdministrativeFields} sur{" "}
                  {administrativeFields.length}
                </strong>
              </div>

              <div className="company-page-progress-track">
                <span
                  style={{
                    width: `${completionPercentage}%`,
                  }}
                />
              </div>

              <p>
                {isComplete
                  ? "Les informations administratives essentielles de votre société sont complètes."
                  : "Complétez le RCCM, l’ID National, le NIF, l’adresse et le logo de votre société."}
              </p>
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