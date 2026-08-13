import { useEffect, useState } from "react";
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";
import { AnimatePresence } from "framer-motion";

import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import VerifyEmailPage from "./pages/VerifyEmailPage";
import SupervisorDashboardPage from "./pages/dashboards/SupervisorDashboardPage";
import OrganizationSetupPage from "./pages/organization/OrganizationSetupPage";
import CompanyPage from "./pages/organization/CompanyPage";
import StationSetupEntryPage from "./pages/stations/StationSetupEntryPage";
import StationsPage from "./pages/stations/StationsPage";
import ProductsPage from "./pages/products/ProductsPage";
import DepotsPage from "./pages/depots/DepotsPage";
import TanksPage from "./pages/tanks/TanksPage";
import PumpsPage from "./pages/pumps/PumpsPage";
import DispensingPointsPage from "./pages/dispensing-points/DispensingPointsPage";
import FuelMetersPage from "./pages/fuel-meters/FuelMetersPage";

import ProtectedRoute from "./components/auth/ProtectedRoute";
import RoleBasedRedirect from "./components/auth/RoleBasedRedirect";
import AppSplashScreen from "./components/feedback/AppSplashScreen";

function App() {
  const [isAppLoading, setIsAppLoading] = useState(true);

  useEffect(() => {
    const loadingTimer = window.setTimeout(() => {
      setIsAppLoading(false);
    }, 1800);

    return () => {
      window.clearTimeout(loadingTimer);
    };
  }, []);

  return (
    <BrowserRouter>
      <div
        className={[
          "app-root",
          isAppLoading ? "app-root-loading" : "",
        ]
          .filter(Boolean)
          .join(" ")}
      >
        <Routes>
          <Route
            path="/"
            element={<HomePage />}
          />

          <Route
            path="/connexion"
            element={<LoginPage />}
          />

          <Route
            path="/inscription"
            element={<RegisterPage />}
          />

          <Route
            path="/verification-email"
            element={<VerifyEmailPage />}
          />
          <Route
            path="/configuration-societe"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <OrganizationSetupPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/dashboard"
            element={
              <ProtectedRoute>
                <SupervisorDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/societe"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <CompanyPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/produits"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <ProductsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/depots"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <DepotsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/citernes"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <TanksPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/pompes"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <PumpsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/pistolets"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <DispensingPointsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/compteurs"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <FuelMetersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/stations"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <StationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/superviseur/stations/nouvelle"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <StationSetupEntryPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <RoleBasedRedirect />
              </ProtectedRoute>
            }
          />
          <Route
            path="*"
            element={<Navigate to="/" replace />}
          />
        </Routes>

        <AnimatePresence>
          {isAppLoading && (
            <AppSplashScreen
              key="fuelflex-splash-screen"
              message="Initialisation de FuelFlex Platform..."
            />
          )}
        </AnimatePresence>
      </div>
    </BrowserRouter>
  );
}

export default App;
