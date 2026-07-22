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
            path="/superviseur/dashboard"
            element={
              <ProtectedRoute>
                <SupervisorDashboardPage />
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