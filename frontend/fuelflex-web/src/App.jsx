import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import VerifyEmailPage from "./pages/VerifyEmailPage";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import RoleBasedRedirect from "./components/auth/RoleBasedRedirect";
import SupervisorDashboardPage from "./pages/dashboards/SupervisorDashboardPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
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

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;