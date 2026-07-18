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

import "./HomePage.css";
import fuelFlexLogo from "../assets/images/logofuelflex.png";

function HomePage() {
  return (
    <main className="home-page">
      <header className="home-header">
        <Link to="/" className="home-brand">
          <img src={fuelFlexLogo} alt="FuelFlex" />

          <div className="home-brand-name">
            <span>FUEL</span>
            <strong>FLEX</strong>
          </div>
        </Link>

        <nav className="home-navigation">
          <a href="#fonctionnalites">Fonctionnalités</a>
          <a href="#avantages">Avantages</a>
          <a href="#contact">Contact</a>
        </nav>

        <div className="home-actions">
          <Link to="/connexion" className="login-button">
            Se connecter
          </Link>

          <Link to="/inscription" className="signup-button">
            Créer un compte
            <ArrowRight size={17} />
          </Link>
        </div>
      </header>

      <section className="home-hero">
        <div className="hero-content">
          <div className="hero-badge">
            <ShieldCheck size={17} />
            Gestion intelligente des stations-service
          </div>

          <h1>
            Pilotez toutes vos stations depuis
            <span> une plateforme unique.</span>
          </h1>

          <p className="hero-description">
            FuelFlex centralise vos stocks, vos ventes, vos pompes, vos
            citernes, vos équipes et vos rapports pour vous offrir une vision
            complète de votre activité.
          </p>

          <div className="hero-actions">
            <Link to="/inscription" className="hero-primary-button">
              Commencer maintenant
              <ArrowRight size={19} />
            </Link>

            <Link to="/connexion" className="hero-secondary-button">
              Accéder à mon espace
            </Link>
          </div>

          <div className="hero-benefits">
            <span>
              <CheckCircle2 size={17} />
              Configuration rapide
            </span>

            <span>
              <CheckCircle2 size={17} />
              Suivi en temps réel
            </span>

            <span>
              <CheckCircle2 size={17} />
              Données sécurisées
            </span>
          </div>
        </div>

        <div className="hero-dashboard">
          <div className="dashboard-glow" />

          <div className="dashboard-card">
            <div className="dashboard-header">
              <div>
                <p>Vue d’ensemble</p>
                <h2>Tableau de bord</h2>
              </div>

              <div className="dashboard-status">
                <span />
                En ligne
              </div>
            </div>

            <div className="dashboard-stats">
              <article>
                <div className="stat-icon">
                  <Fuel size={21} />
                </div>

                <div>
                  <span>Stations actives</span>
                  <strong>12</strong>
                </div>
              </article>

              <article>
                <div className="stat-icon">
                  <Gauge size={21} />
                </div>

                <div>
                  <span>Pompes actives</span>
                  <strong>48</strong>
                </div>
              </article>

              <article>
                <div className="stat-icon">
                  <Warehouse size={21} />
                </div>

                <div>
                  <span>Stock disponible</span>
                  <strong>184 500 L</strong>
                </div>
              </article>
            </div>

            <div className="dashboard-chart">
              <div className="chart-header">
                <div>
                  <span>Ventes du jour</span>
                  <strong>24 850 USD</strong>
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
        </div>
      </section>
    </main>
  );
}

export default HomePage;