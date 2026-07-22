import {
  Building2,
  CircleDollarSign,
  Droplets,
  Fuel,
  Gauge,
  TrendingUp,
  Users,
} from "lucide-react";

import SupervisorLayout from "../../components/layout/SupervisorLayout";
import "./SupervisorDashboardPage.css";

const dashboardStats = [
  {
    id: "stations",
    title: "Stations actives",
    value: "04",
    description: "Toutes les stations sont opérationnelles",
    evolution: "+1 ce mois",
    icon: Building2,
  },
  {
    id: "stock",
    title: "Stock disponible",
    value: "68 450 L",
    description: "Essence et gasoil confondus",
    evolution: "72 % de capacité",
    icon: Droplets,
  },
  {
    id: "sales",
    title: "Ventes du jour",
    value: "12 840 $",
    description: "Toutes les stations",
    evolution: "+8,4 %",
    icon: CircleDollarSign,
  },
  {
    id: "volume",
    title: "Volume vendu",
    value: "9 620 L",
    description: "Depuis l’ouverture de la journée",
    evolution: "+5,2 %",
    icon: Fuel,
  },
];

const stations = [
  {
    id: 1,
    name: "FuelFlex Gombe",
    location: "Kinshasa",
    stock: "18 400 L",
    sales: "4 280 $",
    status: "Opérationnelle",
  },
  {
    id: 2,
    name: "FuelFlex Limete",
    location: "Kinshasa",
    stock: "21 850 L",
    sales: "3 940 $",
    status: "Opérationnelle",
  },
  {
    id: 3,
    name: "FuelFlex Lubumbashi",
    location: "Haut-Katanga",
    stock: "16 700 L",
    sales: "2 870 $",
    status: "Opérationnelle",
  },
  {
    id: 4,
    name: "FuelFlex Matadi",
    location: "Kongo Central",
    stock: "11 500 L",
    sales: "1 750 $",
    status: "Surveillance",
  },
];

const activities = [
  {
    id: 1,
    title: "Réception de carburant validée",
    description: "FuelFlex Limete · 12 000 litres de gasoil",
    time: "Il y a 18 minutes",
    icon: Droplets,
  },
  {
    id: 2,
    title: "Clôture journalière terminée",
    description: "FuelFlex Gombe · Aucun écart détecté",
    time: "Il y a 42 minutes",
    icon: Gauge,
  },
  {
    id: 3,
    title: "Nouveau gestionnaire enregistré",
    description: "Affecté à la station FuelFlex Matadi",
    time: "Il y a 2 heures",
    icon: Users,
  },
];

function SupervisorDashboardPage() {
  return (
    <SupervisorLayout>
      <main className="supervisor-dashboard">
        <section className="supervisor-dashboard-header">
          <div>
            <span className="supervisor-dashboard-eyebrow">
              Vue d’ensemble
            </span>

            <h1>Tableau de bord</h1>

            <p>
              Suivez les performances, les stocks et les opérations de vos
              stations en temps réel.
            </p>
          </div>

          <div className="supervisor-dashboard-period">
            <TrendingUp size={18} />

            <div>
              <span>Performance globale</span>
              <strong>+7,8 %</strong>
            </div>
          </div>
        </section>

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
          <article className="supervisor-dashboard-panel supervisor-dashboard-chart-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>Analyse des ventes</span>
                <h2>Évolution hebdomadaire</h2>
              </div>

              <button type="button">
                Cette semaine
              </button>
            </div>

            <div className="supervisor-dashboard-chart">
              <div className="supervisor-dashboard-chart-values">
                <span>15K</span>
                <span>10K</span>
                <span>5K</span>
                <span>0</span>
              </div>

              <div className="supervisor-dashboard-chart-area">
                <div className="supervisor-dashboard-chart-grid">
                  <span />
                  <span />
                  <span />
                  <span />
                </div>

                <svg
                  viewBox="0 0 700 240"
                  role="img"
                  aria-label="Évolution des ventes de la semaine"
                  preserveAspectRatio="none"
                >
                  <defs>
                    <linearGradient
                      id="salesGradient"
                      x1="0"
                      y1="0"
                      x2="0"
                      y2="1"
                    >
                      <stop
                        offset="0%"
                        stopColor="currentColor"
                        stopOpacity="0.28"
                      />
                      <stop
                        offset="100%"
                        stopColor="currentColor"
                        stopOpacity="0"
                      />
                    </linearGradient>
                  </defs>

                  <path
                    className="supervisor-dashboard-chart-fill"
                    d="M0,190 C70,165 105,175 160,132 C220,84 270,120 325,94 C385,64 420,80 480,42 C540,10 595,62 700,24 L700,240 L0,240 Z"
                  />

                  <path
                    className="supervisor-dashboard-chart-line"
                    d="M0,190 C70,165 105,175 160,132 C220,84 270,120 325,94 C385,64 420,80 480,42 C540,10 595,62 700,24"
                  />
                </svg>

                <div className="supervisor-dashboard-chart-days">
                  <span>Lun</span>
                  <span>Mar</span>
                  <span>Mer</span>
                  <span>Jeu</span>
                  <span>Ven</span>
                  <span>Sam</span>
                  <span>Dim</span>
                </div>
              </div>
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
                  <strong>72 %</strong>
                  <span>Disponible</span>
                </div>
              </div>
            </div>

            <div className="supervisor-dashboard-stock-details">
              <div>
                <span>Essence</span>
                <strong>28 750 L</strong>
              </div>

              <div>
                <span>Gasoil</span>
                <strong>39 700 L</strong>
              </div>
            </div>
          </article>
        </section>

        <section className="supervisor-dashboard-bottom-grid">
          <article className="supervisor-dashboard-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>Réseau FuelFlex</span>
                <h2>Performance des stations</h2>
              </div>

              <button type="button">
                Voir toutes
              </button>
            </div>

            <div className="supervisor-dashboard-table-wrapper">
              <table className="supervisor-dashboard-table">
                <thead>
                  <tr>
                    <th>Station</th>
                    <th>Stock</th>
                    <th>Ventes du jour</th>
                    <th>Statut</th>
                  </tr>
                </thead>

                <tbody>
                  {stations.map((station) => (
                    <tr key={station.id}>
                      <td>
                        <div className="supervisor-dashboard-station">
                          <div className="supervisor-dashboard-station-icon">
                            <Building2 size={18} />
                          </div>

                          <div>
                            <strong>{station.name}</strong>
                            <span>{station.location}</span>
                          </div>
                        </div>
                      </td>

                      <td>{station.stock}</td>
                      <td>{station.sales}</td>

                      <td>
                        <span
                          className={[
                            "supervisor-dashboard-status",
                            station.status === "Surveillance"
                              ? "supervisor-dashboard-status-warning"
                              : "",
                          ]
                            .filter(Boolean)
                            .join(" ")}
                        >
                          {station.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </article>

          <article className="supervisor-dashboard-panel">
            <div className="supervisor-dashboard-panel-header">
              <div>
                <span>Journal système</span>
                <h2>Activités récentes</h2>
              </div>
            </div>

            <div className="supervisor-dashboard-activities">
              {activities.map((activity) => {
                const Icon = activity.icon;

                return (
                  <div
                    key={activity.id}
                    className="supervisor-dashboard-activity"
                  >
                    <div className="supervisor-dashboard-activity-icon">
                      <Icon size={18} />
                    </div>

                    <div className="supervisor-dashboard-activity-content">
                      <strong>{activity.title}</strong>
                      <p>{activity.description}</p>
                      <span>{activity.time}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </article>
        </section>
      </main>
    </SupervisorLayout>
  );
}

export default SupervisorDashboardPage;