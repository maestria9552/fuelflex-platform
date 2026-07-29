import { useEffect, useState } from "react";
import {
  ArrowRight,
  Building2,
  Check,
  CircleDollarSign,
  Droplets,
  FileText,
  Fuel,
  Gauge,
  MapPin,
  Settings2,
  ShieldCheck,
  TrendingUp,
  Users,
} from "lucide-react";
import { useNavigate } from "react-router-dom";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import OrganizationSettingsModal from "../../features/organization/OrganizationSettingsModal";
import {
  getStoredUser,
  mergeStoredUser,
} from "../../services/auth/authStorage";

import { getOrganizationById } from "../../services/organization/organizationService";
import "./SupervisorDashboardPage.css";

function calculateOrganizationProgress(user) {
  const configurationFields = [
    {
      id: "organizationName",
      label: "Nom de l’organisation",
      completed: Boolean(user.organizationName),
    },
    {
      id: "registrationNumber",
      label: "RCCM",
      completed: Boolean(user.registrationNumber),
    },
    {
      id: "nationalId",
      label: "ID National",
      completed: Boolean(user.nationalId),
    },
    {
      id: "taxNumber",
      label: "Numéro fiscal",
      completed: Boolean(user.taxNumber),
    },
    {
      id: "organizationAddress",
      label: "Adresse",
      completed: Boolean(user.organizationAddress),
    },
    {
      id: "organizationPhone",
      label: "Téléphone",
      completed: Boolean(user.organizationPhone),
    },
    {
      id: "organizationEmail",
      label: "Adresse e-mail",
      completed: Boolean(user.organizationEmail),
    },
    {
      id: "organizationLogo",
      label: "Logo",
      completed: Boolean(user.organizationLogo),
    },
  ];

  const completedCount = configurationFields.filter(
    (field) => field.completed
  ).length;

  const percentage = Math.round(
    (completedCount / configurationFields.length) * 100
  );

  return {
    fields: configurationFields,
    completedCount,
    totalCount: configurationFields.length,
    percentage,
  };
}

function buildUserOrganizationData(
  currentUser,
  organization
) {
  return {
    ...currentUser,

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

function SupervisorDashboardPage() {
  const navigate = useNavigate();

  const [user, setUser] = useState(() => getStoredUser() || {});
  const [isOrganizationModalOpen, setIsOrganizationModalOpen] =
    useState(false);

  const organizationId = user.organizationId || null;
  const organizationName =
    user.organizationName || "Votre organisation";

  const organizationCode =
    user.organizationCode || "Code non disponible";

  const configuration = calculateOrganizationProgress(user);

  const isOrganizationInformationComplete =
  Boolean(user.registrationNumber) &&
  Boolean(user.nationalId) &&
  Boolean(user.taxNumber) &&
  Boolean(user.organizationAddress) &&
  Boolean(user.organizationLogo);

  useEffect(() => {
  if (!organizationId) {
    return undefined;
  }

  let isCancelled = false;

  const loadOrganization = async () => {
    try {
      const organization =
        await getOrganizationById(
          organizationId
        );

      if (isCancelled) {
        return;
      }

      const refreshedUser =
        buildUserOrganizationData(
          user,
          organization
        );

      const storedUser =
        mergeStoredUser(refreshedUser);

      setUser(storedUser);
    } catch (error) {
      console.error(
        "Impossible de charger l’organisation :",
        error
      );
    }
  };

  loadOrganization();

  return () => {
    isCancelled = true;
  };
}, [organizationId]);

  const dashboardStats = [
    {
      id: "stations",
      title: "Stations actives",
      value: "0",
      description: "Aucune station configurée",
      evolution: "À configurer",
      icon: Building2,
    },
    {
      id: "stock",
      title: "Stock disponible",
      value: "0 L",
      description: "Aucun stock enregistré",
      evolution: "0 % de capacité",
      icon: Droplets,
    },
    {
      id: "sales",
      title: "Ventes du jour",
      value: "0 $",
      description: "Aucune vente enregistrée",
      evolution: "0 %",
      icon: CircleDollarSign,
    },
    {
      id: "volume",
      title: "Volume vendu",
      value: "0 L",
      description: "Aucune opération aujourd’hui",
      evolution: "0 %",
      icon: Fuel,
    },
  ];

    const handleCompleteConfiguration = () => {
    if (!organizationId) {
      navigate("/configuration-societe");
      return;
    }

    setIsOrganizationModalOpen(true);
  };

  const handleCloseOrganizationModal = () => {
    setIsOrganizationModalOpen(false);
  };

  const handleOrganizationSaved = (
  updatedOrganization,
  updatedUser
) => {
  if (updatedUser) {
    setUser(updatedUser);
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
};

  const handleCreateStation = () => {
    navigate("/superviseur/stations/nouvelle");
  };

  return (
    <SupervisorLayout>
      <main className="supervisor-dashboard">
        <section className="supervisor-dashboard-header">
          <div>
            <span className="supervisor-dashboard-eyebrow">
              Vue d’ensemble
            </span>

            <h1>{organizationName}</h1>

            <p>
              Configurez votre organisation, créez vos stations et suivez
              progressivement toutes les opérations de votre réseau.
            </p>
          </div>

          <div className="supervisor-dashboard-period">
            <ShieldCheck size={18} />

            <div>
              <span>Code organisation</span>
              <strong>{organizationCode}</strong>
            </div>
          </div>
        </section>

        {configuration.percentage < 100 && (
          <section className="supervisor-configuration-card">
            <div className="supervisor-configuration-main">
              <div className="supervisor-configuration-icon">
                <Settings2 size={25} />
              </div>

              <div className="supervisor-configuration-content">
                <span className="supervisor-configuration-eyebrow">
                  CONFIGURATION DE L’ORGANISATION
                </span>

                <h2>Complétez votre profil professionnel</h2>

                <p>
                  Certaines informations administratives et opérationnelles
                  sont encore manquantes. Complétez-les pour profiter pleinement
                  de FuelFlex Platform.
                </p>

                <div className="supervisor-configuration-progress">
                  <div className="supervisor-configuration-progress-header">
                    <span>
                      {configuration.completedCount} éléments complétés sur{" "}
                      {configuration.totalCount}
                    </span>

                    <strong>{configuration.percentage} %</strong>
                  </div>

                  <div className="supervisor-configuration-progress-track">
                    <span
                      style={{
                        width: `${configuration.percentage}%`,
                      }}
                    />
                  </div>
                </div>
              </div>
            </div>

            <div className="supervisor-configuration-actions">
              <div className="supervisor-configuration-checklist">
                {configuration.fields.slice(0, 4).map((field) => (
                  <div
                    key={field.id}
                    className={
                      field.completed
                        ? "supervisor-configuration-item completed"
                        : "supervisor-configuration-item"
                    }
                  >
                    <span>
                      {field.completed ? <Check size={14} /> : null}
                    </span>

                    {field.label}
                  </div>
                ))}
              </div>

              <button
                type="button"
                onClick={handleCompleteConfiguration}
              >
                Compléter la configuration
                <ArrowRight size={18} />
              </button>
            </div>
          </section>
        )}

        <section
          className="supervisor-dashboard-stats"
          aria-label="Indicateurs principaux"
        >
          {dashboardStats.map((stat) => {
            const Icon = stat.icon;

            return (
              <article
                key={stat.id}
                className="supervisor-dashboard-stat-card"
              >
                <div className="supervisor-dashboard-stat-top">
                  <div className="supervisor-dashboard-stat-icon">
                    <Icon size={22} />
                  </div>

                  <span className="supervisor-dashboard-stat-evolution">
                    {stat.evolution}
                  </span>
                </div>

                <div className="supervisor-dashboard-stat-content">
                  <span>{stat.title}</span>
                  <strong>{stat.value}</strong>
                  <p>{stat.description}</p>
                </div>
              </article>
            );
          })}
        </section>

        <section className="supervisor-dashboard-grid">
          <article className="supervisor-dashboard-panel supervisor-dashboard-empty-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>Analyse des ventes</span>
                <h2>Évolution hebdomadaire</h2>
              </div>

              <button type="button" disabled>
                Cette semaine
              </button>
            </div>

            <div className="supervisor-dashboard-empty-state">
              <div className="supervisor-dashboard-empty-icon">
                <TrendingUp size={30} />
              </div>

              <h3>Aucune donnée de vente</h3>

              <p>
                Le graphique des ventes apparaîtra après la création d’une
                station et l’enregistrement des premières opérations.
              </p>
            </div>
          </article>

          <article className="supervisor-dashboard-panel supervisor-dashboard-stock-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>Capacité globale</span>
                <h2>Niveau des stocks</h2>
              </div>

              <Gauge size={20} />
            </div>

            <div className="supervisor-dashboard-stock-gauge">
              <div className="supervisor-dashboard-stock-ring">
                <div>
                  <strong>0 %</strong>
                  <span>Disponible</span>
                </div>
              </div>
            </div>

            <div className="supervisor-dashboard-stock-details">
              <div>
                <span>Essence</span>
                <strong>0 L</strong>
              </div>

              <div>
                <span>Gasoil</span>
                <strong>0 L</strong>
              </div>
            </div>
          </article>
        </section>

        <section className="supervisor-dashboard-bottom-grid">
          <article className="supervisor-dashboard-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>Réseau FuelFlex</span>
                <h2>Vos stations</h2>
              </div>
            </div>

            <div className="supervisor-dashboard-empty-state supervisor-dashboard-stations-empty">
              <div className="supervisor-dashboard-empty-icon">
                <Building2 size={30} />
              </div>

              <h3>Aucune station enregistrée</h3>

              <p>
                Créez votre première station pour commencer à gérer les
                produits, citernes, pompes et équipes.
              </p>

              <button
                type="button"
                onClick={handleCreateStation}
              >
                Créer une station
                <ArrowRight size={17} />
              </button>
            </div>
          </article>

          <article className="supervisor-dashboard-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>Démarrage</span>
                <h2>Prochaines étapes</h2>
              </div>
            </div>

            <div className="supervisor-dashboard-onboarding">
              <div className="supervisor-dashboard-onboarding-item completed">
                <span>
                  <Check size={17} />
                </span>

                <div>
                  <strong>Compte superviseur créé</strong>
                  <p>Votre accès principal est opérationnel.</p>
                </div>
              </div>

              <div className="supervisor-dashboard-onboarding-item completed">
                <span>
                  <Check size={17} />
                </span>

                <div>
                  <strong>Organisation créée</strong>
                  <p>{organizationName}</p>
                </div>
              </div>

              <div
                className={[
                  "supervisor-dashboard-onboarding-item",
                  isOrganizationInformationComplete
                    ? "completed"
                    : "",
                ]
                  .filter(Boolean)
                  .join(" ")}
              >
                <span>
                  {isOrganizationInformationComplete ? (
                    <Check size={17} />
                  ) : (
                    <FileText size={17} />
                  )}
                </span>

                <div>
                  <strong>
                    Compléter les informations
                  </strong>

                  <p>
                    {isOrganizationInformationComplete
                      ? "Informations administratives complètes."
                      : "RCCM, ID National, NIF, adresse et logo."}
                  </p>
                </div>
              </div>

              <div className="supervisor-dashboard-onboarding-item">
                <span>
                  <MapPin size={17} />
                </span>

                <div>
                  <strong>Créer votre première station</strong>
                  <p>Configurez son emplacement et ses paramètres.</p>
                </div>
              </div>

              <div className="supervisor-dashboard-onboarding-item">
                <span>
                  <Users size={17} />
                </span>

                <div>
                  <strong>Ajouter votre équipe</strong>
                  <p>Créez les gestionnaires et les pompistes.</p>
                </div>
              </div>
            </div>
          </article>
        </section>
          </main>

      <OrganizationSettingsModal
        isOpen={isOrganizationModalOpen}
        organizationId={organizationId}
        onClose={handleCloseOrganizationModal}
        onSaved={handleOrganizationSaved}
      />
    </SupervisorLayout>
  );
}

export default SupervisorDashboardPage;