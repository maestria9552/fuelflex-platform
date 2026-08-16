import {
  ArrowRight,
  BarChart3,
  CheckCircle2,
  Fuel,
  Gauge,
  ShieldCheck,
  Warehouse,
} from "lucide-react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

import "./HomePage.css";
import fuelFlexLogo from "../assets/images/logofuelflex.png";
import { formatNumber } from "../i18n/formatters.js";

function HomePage() {
  const { t, i18n } = useTranslation(["home", "auth", "common"]);

  return (
    <main className="home-page">
      <header className="home-header">
        <Link to="/" className="home-brand">
          <img src={fuelFlexLogo} alt={t("common:brand.logoAlt")} />

          <div className="home-brand-name">
            <span>FUEL</span>
            <strong>FLEX</strong>
          </div>
        </Link>

        <nav className="home-navigation">
          <a href="#fonctionnalites">{t("home:navigation.features")}</a>
          <a href="#avantages">{t("home:navigation.benefits")}</a>
          <a href="#contact">{t("home:navigation.contact")}</a>
        </nav>

        <div className="home-actions">
          <Link to="/connexion" className="login-button">
            {t("auth:register.login")}
          </Link>

          <Link to="/inscription" className="signup-button">
            {t("home:navigation.createAccount")}
            <ArrowRight size={17} />
          </Link>
        </div>
      </header>

      <section className="home-hero">
        <div className="hero-content">
          <div className="hero-badge">
            <ShieldCheck size={17} />
            {t("home:hero.badge")}
          </div>

          <h1>
            {t("home:hero.title")}
            <span> {t("home:hero.highlight")}</span>
          </h1>

          <p className="hero-description">
            {t("home:hero.description")}
          </p>

          <div className="hero-actions">
            <Link to="/inscription" className="hero-primary-button">
              {t("home:hero.start")}
              <ArrowRight size={19} />
            </Link>

            <Link to="/connexion" className="hero-secondary-button">
              {t("home:hero.workspace")}
            </Link>
          </div>

          <div className="hero-benefits">
            <span>
              <CheckCircle2 size={17} />
              {t("home:hero.quickSetup")}
            </span>

            <span>
              <CheckCircle2 size={17} />
              {t("home:hero.realTime")}
            </span>

            <span>
              <CheckCircle2 size={17} />
              {t("home:hero.secureData")}
            </span>
          </div>
        </div>

        <div className="hero-dashboard">
          <div className="dashboard-glow" />

          <div className="dashboard-card">
            <div className="dashboard-header">
              <div>
                <p>{t("home:dashboard.overview")}</p>
                <h2>{t("home:dashboard.title")}</h2>
              </div>

              <div className="dashboard-status">
                <span />
                {t("home:dashboard.online")}
              </div>
            </div>

            <div className="dashboard-stats">
              <article>
                <div className="stat-icon">
                  <Fuel size={21} />
                </div>

                <div>
                  <span>{t("home:dashboard.activeStations")}</span>
                  <strong>12</strong>
                </div>
              </article>

              <article>
                <div className="stat-icon">
                  <Gauge size={21} />
                </div>

                <div>
                  <span>{t("home:dashboard.activePumps")}</span>
                  <strong>48</strong>
                </div>
              </article>

              <article>
                <div className="stat-icon">
                  <Warehouse size={21} />
                </div>

                <div>
                  <span>{t("home:dashboard.availableStock")}</span>
                  <strong>
                    {t("home:dashboard.stockValue", {
                      value: formatNumber(184500, {
                        language: i18n.resolvedLanguage,
                      }),
                    })}
                  </strong>
                </div>
              </article>
            </div>

            <div className="dashboard-chart">
              <div className="chart-header">
                <div>
                  <span>{t("home:dashboard.todaySales")}</span>
                  <strong>
                    {t("home:dashboard.salesValue", {
                      value: formatNumber(24850, {
                        language: i18n.resolvedLanguage,
                      }),
                    })}
                  </strong>
                </div>

                <BarChart3 size={24} />
              </div>

              <div className="chart-bars">
                <span style={{ height: "38%" }} />
                <span style={{ height: "56%" }} />
                <span style={{ height: "45%" }} />
                <span style={{ height: "72%" }} />
                <span style={{ height: "62%" }} />
                <span style={{ height: "88%" }} />
                <span style={{ height: "76%" }} />
              </div>

              <div className="chart-labels">
                <span>{t("home:dashboard.days.monday")}</span>
                <span>{t("home:dashboard.days.tuesday")}</span>
                <span>{t("home:dashboard.days.wednesday")}</span>
                <span>{t("home:dashboard.days.thursday")}</span>
                <span>{t("home:dashboard.days.friday")}</span>
                <span>{t("home:dashboard.days.saturday")}</span>
                <span>{t("home:dashboard.days.sunday")}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}

export default HomePage;
