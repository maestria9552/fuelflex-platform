import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
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
import { formatNumber } from "../../i18n/formatters";
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
      labelKey: "organizationName",
      completed: Boolean(user.organizationName),
    },
    {
      id: "registrationNumber",
      labelKey: "registrationNumber",
      completed: Boolean(user.registrationNumber),
    },
    {
      id: "nationalId",
      labelKey: "nationalId",
      completed: Boolean(user.nationalId),
    },
    {
      id: "taxNumber",
      labelKey: "taxNumber",
      completed: Boolean(user.taxNumber),
    },
    {
      id: "organizationAddress",
      labelKey: "organizationAddress",
      completed: Boolean(user.organizationAddress),
    },
    {
      id: "organizationPhone",
      labelKey: "organizationPhone",
      completed: Boolean(user.organizationPhone),
    },
    {
      id: "organizationEmail",
      labelKey: "organizationEmail",
      completed: Boolean(user.organizationEmail),
    },
    {
      id: "organizationLogo",
      labelKey: "organizationLogo",
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
  const { t, i18n } = useTranslation("dashboard");
  const navigate = useNavigate();
  const formatDashboardNumber = (value) => formatNumber(value, { language: i18n.resolvedLanguage });

  const [user, setUser] = useState(() => getStoredUser() || {});
  const [isOrganizationModalOpen, setIsOrganizationModalOpen] =
    useState(false);

  const organizationId = user.organizationId || null;
  const organizationName =
    user.organizationName || t("fallback.organization");

  const organizationCode =
    user.organizationCode || t("fallback.organizationCode");

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

      setUser((currentUser) => {
        const refreshedUser =
          buildUserOrganizationData(
            currentUser,
            organization
          );

        return mergeStoredUser(refreshedUser);
      });
    } catch (error) {
      console.error(t("errors.loadOrganization"), error);
    }
  };

  loadOrganization();

  return () => {
    isCancelled = true;
  };
}, [organizationId, t]);

  const dashboardStats = [
    {
      id: "stations",
      title: t("kpi.stations.title"),
      value: formatDashboardNumber(0),
      description: t("kpi.stations.description"),
      evolution: t("kpi.stations.evolution"),
      icon: Building2,
    },
    {
      id: "stock",
      title: t("kpi.stock.title"),
      value: t("kpi.liters", { value: formatDashboardNumber(0) }),
      description: t("kpi.stock.description"),
      evolution: t("kpi.stock.evolution", { percentage: formatDashboardNumber(0) }),
      icon: Droplets,
    },
    {
      id: "sales",
      title: t("kpi.sales.title"),
      value: t("kpi.currencyAmount", { value: formatDashboardNumber(0) }),
      description: t("kpi.sales.description"),
      evolution: t("kpi.sales.evolution", { percentage: formatDashboardNumber(0) }),
      icon: CircleDollarSign,
    },
    {
      id: "volume",
      title: t("kpi.volume.title"),
      value: t("kpi.liters", { value: formatDashboardNumber(0) }),
      description: t("kpi.volume.description"),
      evolution: t("kpi.volume.evolution", { percentage: formatDashboardNumber(0) }),
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
              {t("header.eyebrow")}
            </span>

            <h1>{organizationName}</h1>

            <p>
              {t("header.description")}
            </p>
          </div>

          <div className="supervisor-dashboard-period">
            <ShieldCheck size={18} />

            <div>
              <span>{t("header.organizationCode")}</span>
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
                  {t("configuration.eyebrow")}
                </span>

                <h2>{t("configuration.title")}</h2>

                <p>
                  {t("configuration.description")}
                </p>

                <div className="supervisor-configuration-progress">
                  <div className="supervisor-configuration-progress-header">
                    <span>
                      {t("configuration.progress", {
                        count: configuration.completedCount,
                        completed: formatDashboardNumber(configuration.completedCount),
                        total: formatDashboardNumber(configuration.totalCount),
                      })}
                    </span>

                    <strong>{formatDashboardNumber(configuration.percentage)} %</strong>
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

                    {t(`configuration.fields.${field.labelKey}`)}
                  </div>
                ))}
              </div>

              <button
                type="button"
                onClick={handleCompleteConfiguration}
              >
                {t("configuration.complete")}
                <ArrowRight size={18} />
              </button>
            </div>
          </section>
        )}

        <section
          className="supervisor-dashboard-stats"
          aria-label={t("kpi.ariaLabel")}
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
                <span>{t("salesAnalysis.eyebrow")}</span>
                <h2>{t("salesAnalysis.title")}</h2>
              </div>

              <button type="button" disabled>
                {t("salesAnalysis.period")}
              </button>
            </div>

            <div className="supervisor-dashboard-empty-state">
              <div className="supervisor-dashboard-empty-icon">
                <TrendingUp size={30} />
              </div>

              <h3>{t("salesAnalysis.emptyTitle")}</h3>

              <p>
                {t("salesAnalysis.emptyDescription")}
              </p>
            </div>
          </article>

          <article className="supervisor-dashboard-panel supervisor-dashboard-stock-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>{t("stock.eyebrow")}</span>
                <h2>{t("stock.title")}</h2>
              </div>

              <Gauge size={20} />
            </div>

            <div className="supervisor-dashboard-stock-gauge">
              <div className="supervisor-dashboard-stock-ring">
                <div>
                  <strong>{formatDashboardNumber(0)} %</strong>
                  <span>{t("stock.available")}</span>
                </div>
              </div>
            </div>

            <div className="supervisor-dashboard-stock-details">
              <div>
                <span>{t("stock.gasoline")}</span>
                <strong>{t("kpi.liters", { value: formatDashboardNumber(0) })}</strong>
              </div>

              <div>
                <span>{t("stock.diesel")}</span>
                <strong>{t("kpi.liters", { value: formatDashboardNumber(0) })}</strong>
              </div>
            </div>
          </article>
        </section>

        <section className="supervisor-dashboard-bottom-grid">
          <article className="supervisor-dashboard-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>{t("stations.eyebrow")}</span>
                <h2>{t("stations.title")}</h2>
              </div>
            </div>

            <div className="supervisor-dashboard-empty-state supervisor-dashboard-stations-empty">
              <div className="supervisor-dashboard-empty-icon">
                <Building2 size={30} />
              </div>

              <h3>{t("stations.emptyTitle")}</h3>

              <p>
                {t("stations.emptyDescription")}
              </p>

              <button
                type="button"
                onClick={handleCreateStation}
              >
                {t("stations.create")}
                <ArrowRight size={17} />
              </button>
            </div>
          </article>

          <article className="supervisor-dashboard-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>{t("onboarding.eyebrow")}</span>
                <h2>{t("onboarding.title")}</h2>
              </div>
            </div>

            <div className="supervisor-dashboard-onboarding">
              <div className="supervisor-dashboard-onboarding-item completed">
                <span>
                  <Check size={17} />
                </span>

                <div>
                  <strong>{t("onboarding.accountTitle")}</strong>
                  <p>{t("onboarding.accountDescription")}</p>
                </div>
              </div>

              <div className="supervisor-dashboard-onboarding-item completed">
                <span>
                  <Check size={17} />
                </span>

                <div>
                  <strong>{t("onboarding.organizationTitle")}</strong>
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
                    {t("onboarding.informationTitle")}
                  </strong>

                  <p>
                    {isOrganizationInformationComplete
                      ? t("onboarding.informationComplete")
                      : t("onboarding.informationMissing")}
                  </p>
                </div>
              </div>

              <div className="supervisor-dashboard-onboarding-item">
                <span>
                  <MapPin size={17} />
                </span>

                <div>
                  <strong>{t("onboarding.stationTitle")}</strong>
                  <p>{t("onboarding.stationDescription")}</p>
                </div>
              </div>

              <div className="supervisor-dashboard-onboarding-item">
                <span>
                  <Users size={17} />
                </span>

                <div>
                  <strong>{t("onboarding.teamTitle")}</strong>
                  <p>{t("onboarding.teamDescription")}</p>
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