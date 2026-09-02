import { useEffect, useState } from "react";
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import { useTranslation } from "react-i18next";

import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import VerifyEmailPage from "./pages/VerifyEmailPage";
import EmployeeActivationPage from "./pages/EmployeeActivationPage";
import SupervisorDashboardPage from "./pages/dashboards/SupervisorDashboardPage";
import ManagerDashboardPage from "./pages/dashboards/ManagerDashboardPage";
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
import PricingPage from "./pages/pricing/PricingPage";
import EmployeesPage from "./pages/employees/EmployeesPage";
import EmployeeDetailPage from "./pages/employees/EmployeeDetailPage";
import SupplierPortalPage from "./pages/SupplierPortalPage";
import ManagerOrdersPage from "./pages/orders/ManagerOrdersPage";
import ManagerNewOrderPage from "./pages/orders/ManagerNewOrderPage";
import ManagerOrderDetailPage from "./pages/orders/ManagerOrderDetailPage";
import SupervisorOrdersPage from "./pages/orders/SupervisorOrdersPage";
import SupervisorOrderDetailPage from "./pages/orders/SupervisorOrderDetailPage";
import PurchaseOrderDocumentPage from "./pages/orders/PurchaseOrderDocumentPage";
import ManagerReceptionsPage from "./pages/receptions/ManagerReceptionsPage";
import ManagerReceptionFormPage from "./pages/receptions/ManagerReceptionFormPage";
import ManagerReceptionDetailPage from "./pages/receptions/ManagerReceptionDetailPage";
import SupervisorReceptionsPage from "./pages/receptions/SupervisorReceptionsPage";
import SupervisorReceptionDetailPage from "./pages/receptions/SupervisorReceptionDetailPage";
import ManagerOperationalDaysPage from "./pages/operations/ManagerOperationalDaysPage";
import ManagerOperationalDayDetailPage from "./pages/operations/ManagerOperationalDayDetailPage";
import SupervisorOperationalDaysPage from "./pages/operations/SupervisorOperationalDaysPage";
import SupervisorOperationalDayDetailPage from "./pages/operations/SupervisorOperationalDayDetailPage";
import RjvPrintPage from "./features/operations/components/RjvPrintPage";
import PumpAttendantPrintPage from "./features/operations/components/PumpAttendantPrintPage";
import ManagerSalesPage from "./pages/sales/ManagerSalesPage";
import ManagerSaleDetailPage from "./pages/sales/ManagerSaleDetailPage";
import SupervisorSalesPage from "./pages/sales/SupervisorSalesPage";
import SupervisorSaleDetailPage from "./pages/sales/SupervisorSaleDetailPage";
import ManagerPumpAttendantsPage from "./pages/employee-validation/ManagerPumpAttendantsPage";
import PumpAttendantValidationDetailPage from "./pages/employee-validation/PumpAttendantValidationDetailPage";
import SupervisorPumpAttendantValidationsPage from "./pages/employee-validation/SupervisorPumpAttendantValidationsPage";
import ReportsPage from "./pages/reports/ReportsPage";
import AccessDeniedPage from "./pages/AccessDeniedPage";

import ProtectedRoute from "./components/auth/ProtectedRoute";
import RoleBasedRedirect from "./components/auth/RoleBasedRedirect";
import AppSplashScreen from "./components/feedback/AppSplashScreen";

function App() {
  const { t } = useTranslation("common");
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
            path="/activation-employe"
            element={<EmployeeActivationPage />}
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
          <Route path="/fournisseur/dashboard" element={<ProtectedRoute allowedRoles={["SUPPLIER_USER"]}><SupplierPortalPage /></ProtectedRoute>} />
          <Route
            path="/gerant/dashboard"
            element={
              <ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["operational-day:view"]}>
                <ManagerDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route path="/gerant/commandes" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["order:view"]}><ManagerOrdersPage /></ProtectedRoute>} />
          <Route path="/gerant/receptions" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["reception:view"]}><ManagerReceptionsPage /></ProtectedRoute>} />
          <Route path="/gerant/receptions/nouvelle" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["reception:view", "reception:create"]}><ManagerReceptionFormPage /></ProtectedRoute>} />
          <Route path="/gerant/receptions/:id" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["reception:view"]}><ManagerReceptionDetailPage /></ProtectedRoute>} />
          <Route path="/gerant/operations" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["operational-day:view"]}><ManagerOperationalDaysPage /></ProtectedRoute>} />
          <Route path="/gerant/rapports" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["report:view", "operational-day:view"]}><ReportsPage role="manager" /></ProtectedRoute>} />
          <Route path="/gerant/operations/:id/rjv/print" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["report:view", "rjv:view"]}><RjvPrintPage role="manager" /></ProtectedRoute>} />
          <Route path="/gerant/operations/:dayId/assignments/:assignmentId/print" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["report:view", "reconciliation:view"]}><PumpAttendantPrintPage role="manager" /></ProtectedRoute>} />
          <Route path="/gerant/operations/:id" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["operational-day:view"]}><ManagerOperationalDayDetailPage /></ProtectedRoute>} />
          <Route path="/gerant/ventes" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["pos-sale:view"]}><ManagerSalesPage /></ProtectedRoute>} />
          <Route path="/gerant/ventes/:id" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["pos-sale:view"]}><ManagerSaleDetailPage /></ProtectedRoute>} />
          <Route path="/gerant/pompistes" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["pump-attendant-validation:view", "pump-attendant:prepare"]}><ManagerPumpAttendantsPage /></ProtectedRoute>} />
          <Route path="/gerant/validations-pompistes/:id" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["pump-attendant-validation:view"]}><PumpAttendantValidationDetailPage role="manager" /></ProtectedRoute>} />
          <Route path="/gerant/commandes/nouvelle" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["order:create"]}><ManagerNewOrderPage /></ProtectedRoute>} />
          <Route path="/gerant/commandes/:id/bon-de-commande" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["order:view"]}><PurchaseOrderDocumentPage /></ProtectedRoute>} />
          <Route path="/gerant/commandes/:id" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["order:view"]}><ManagerOrderDetailPage /></ProtectedRoute>} />
          <Route path="/gerant/commandes/:id/modifier" element={<ProtectedRoute allowedRoles={["MANAGER"]} requiredPermissions={["order:update"]}><ManagerNewOrderPage /></ProtectedRoute>} />
          <Route path="/superviseur/commandes" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]}><SupervisorOrdersPage /></ProtectedRoute>} />
          <Route path="/superviseur/receptions" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]}><SupervisorReceptionsPage /></ProtectedRoute>} />
          <Route path="/superviseur/receptions/:id" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]}><SupervisorReceptionDetailPage /></ProtectedRoute>} />
          <Route path="/superviseur/operations" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["operational-day:view"]}><SupervisorOperationalDaysPage /></ProtectedRoute>} />
          <Route path="/superviseur/rapports" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["report:view", "operational-day:view"]}><ReportsPage role="supervisor" /></ProtectedRoute>} />
          <Route path="/superviseur/operations/:id/rjv/print" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["report:view", "rjv:view"]}><RjvPrintPage role="supervisor" /></ProtectedRoute>} />
          <Route path="/superviseur/operations/:dayId/assignments/:assignmentId/print" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["report:view", "reconciliation:view"]}><PumpAttendantPrintPage role="supervisor" /></ProtectedRoute>} />
          <Route path="/superviseur/operations/:id" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["operational-day:view"]}><SupervisorOperationalDayDetailPage /></ProtectedRoute>} />
          <Route path="/superviseur/ventes" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["pos-sale:view"]}><SupervisorSalesPage /></ProtectedRoute>} />
          <Route path="/superviseur/ventes/:id" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["pos-sale:view"]}><SupervisorSaleDetailPage /></ProtectedRoute>} />
          <Route path="/superviseur/validations-pompistes" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["pump-attendant-validation:view"]}><SupervisorPumpAttendantValidationsPage /></ProtectedRoute>} />
          <Route path="/superviseur/validations-pompistes/:id" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]} requiredPermissions={["pump-attendant-validation:view"]}><PumpAttendantValidationDetailPage role="supervisor" /></ProtectedRoute>} />
          <Route path="/superviseur/commandes/:id/bon-de-commande" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]}><PurchaseOrderDocumentPage /></ProtectedRoute>} />
          <Route path="/superviseur/commandes/:id" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]}><SupervisorOrderDetailPage /></ProtectedRoute>} />
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
            path="/superviseur/tarification"
            element={
              <ProtectedRoute allowedRoles={["SUPERVISOR"]}>
                <PricingPage />
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
          <Route path="/superviseur/employes" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]}><EmployeesPage /></ProtectedRoute>} />
          <Route path="/superviseur/employes/:employeeId" element={<ProtectedRoute allowedRoles={["SUPERVISOR"]}><EmployeeDetailPage /></ProtectedRoute>} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <RoleBasedRedirect />
              </ProtectedRoute>
            }
          />
          <Route path="/acces-refuse" element={<AccessDeniedPage />} />
          <Route
            path="*"
            element={<Navigate to="/" replace />}
          />
        </Routes>

        <AnimatePresence>
          {isAppLoading && (
            <AppSplashScreen
              key="fuelflex-splash-screen"
              message={t("feedback.initializing")}
            />
          )}
        </AnimatePresence>
      </div>
    </BrowserRouter>
  );
}

export default App;
