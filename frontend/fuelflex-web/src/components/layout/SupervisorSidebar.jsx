import "./SupervisorSidebar.css";
import { getSupervisorPendingOrderCount } from "../../services/purchaseOrder/supervisorPurchaseOrderService";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { NavLink, useLocation } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import {
  LayoutDashboard,
  Building2,
  Fuel,
  Droplets,
  Warehouse,
  Cylinder,
  BadgeDollarSign,
  Handshake,
  UsersRound,
  ChartNoAxesCombined,
  ClipboardCheck,
  Settings2,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  X,
} from "lucide-react";

import FuelFlexLogo from "../brand/FuelFlexLogo";

const navigationGroups = [
  {
    id: "general",
    labelKey: "groups.general",
    items: [
      {
        id: "dashboard",
        labelKey: "items.dashboard",
        path: "/superviseur/dashboard",
        icon: LayoutDashboard,
      },
      {
        id: "orders",
        labelKey: "items.orders",
        path: "/superviseur/commandes",
        icon: ClipboardCheck,
      },
      {
        id: "societe",
        labelKey: "items.company",
        path: "/superviseur/societe",
        icon: Building2,
      },
    ],
  },
  {
    id: "configuration",
    labelKey: "groups.configuration",
    items: [
      {
        id: "stations",
        labelKey: "items.stations",
        path: "/superviseur/stations",
        icon: Fuel,
      },
      {
        id: "produits",
        labelKey: "items.products",
        path: "/superviseur/produits",
        icon: Droplets,
      },
      {
        id: "depots",
        labelKey: "items.depots",
        path: "/superviseur/depots",
        icon: Warehouse,
      },
      {
        id: "citernes",
        labelKey: "items.tanks",
        path: "/superviseur/citernes",
        icon: Cylinder,
      },
      {
        id: "pompes",
        labelKey: "items.pumps",
        path: "/superviseur/pompes",
        icon: Fuel,
        children: [
          { id: "pistolets", labelKey: "items.dispensingPoints", path: "/superviseur/pistolets" },
          { id: "compteurs", labelKey: "items.fuelMeters", path: "/superviseur/compteurs" },
        ],
      },
      {
        id: "tarification",
        labelKey: "items.pricing",
        path: "/superviseur/tarification",
        icon: BadgeDollarSign,
      },
    ],
  },
  {
    id: "gestion",
    labelKey: "groups.management",
    items: [
      {
        id: "clients",
        labelKey: "items.partnerCustomers",
        path: "/superviseur/clients",
        icon: Handshake,
      },
      {
        id: "employes",
        labelKey: "items.employees",
        path: "/superviseur/employes",
        icon: UsersRound,
      },
    ],
  },
  {
    id: "analyse",
    labelKey: "groups.analytics",
    items: [
      {
        id: "rapports",
        labelKey: "items.reports",
        path: "/superviseur/rapports",
        icon: ChartNoAxesCombined,
      },
      {
        id: "parametres",
        labelKey: "items.settings",
        path: "/superviseur/parametres",
        icon: Settings2,
      },
    ],
  },
];

function SupervisorSidebar({
  isOpen = false,
  isCollapsed = false,
  onClose,
  onToggleCollapse,
}) {
  const { t } = useTranslation(["navigation", "common"]);
  const location = useLocation();
  const [pendingOrderCount, setPendingOrderCount] = useState(0);
  useEffect(() => { let active = true; const refresh = () => getSupervisorPendingOrderCount().then((count) => active && setPendingOrderCount(Number(count?.count ?? count) || 0)).catch(() => {}); refresh(); const timer = window.setInterval(refresh, 45000); window.addEventListener("fuelflex:notifications-refresh", refresh); return () => { active = false; window.clearInterval(timer); window.removeEventListener("fuelflex:notifications-refresh", refresh); }; }, []);
  const isPumpSectionActive = ["/superviseur/pompes", "/superviseur/pistolets", "/superviseur/compteurs"].includes(location.pathname);
  const isPumpChildActive = ["/superviseur/pistolets", "/superviseur/compteurs"].includes(location.pathname);
  const [isPumpsOpen, setIsPumpsOpen] = useState(isPumpSectionActive);

  const showPumpsOpen = isPumpChildActive || isPumpsOpen;

  const sidebarWidth = isCollapsed ? 88 : 286;

  return (
    <>
      <AnimatePresence>
        {isOpen && (
          <motion.button
            type="button"
            className="supervisor-sidebar-overlay"
            aria-label={t("navigation:sidebar.closeMenu")}
            onClick={onClose}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          />
        )}
      </AnimatePresence>

      <motion.aside
        className={[
          "supervisor-sidebar",
          isOpen ? "supervisor-sidebar-open" : "",
          isCollapsed ? "supervisor-sidebar-collapsed" : "",
        ]
          .filter(Boolean)
          .join(" ")}
        animate={{ width: sidebarWidth }}
        transition={{
          duration: 0.28,
          ease: [0.22, 1, 0.36, 1],
        }}
      >
        <div className="supervisor-sidebar-header">
          <div className="supervisor-sidebar-brand">
            <FuelFlexLogo
              size={isCollapsed ? 46 : 52}
              compact={isCollapsed}
            />
          </div>

          <button
            type="button"
            className="supervisor-sidebar-mobile-close"
            onClick={onClose}
            aria-label={t("navigation:sidebar.closeMenu")}
          >
            <X size={20} strokeWidth={1.8} />
          </button>
        </div>

        <nav
          className="supervisor-sidebar-navigation"
          aria-label={t("navigation:sidebar.supervisorNavigation")}
        >
          {navigationGroups.map((group) => (
            <div
              className="supervisor-sidebar-group"
              key={group.id}
            >
              {!isCollapsed && (
                <motion.p
                  className="supervisor-sidebar-group-label"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ duration: 0.2 }}
                >
                  {t(`navigation:${group.labelKey}`)}
                </motion.p>
              )}

              <div className="supervisor-sidebar-group-links">
                {group.items.map((item) => {
                  const Icon = item.icon;
                  const itemLabel = t(`navigation:${item.labelKey}`);

                  if (item.children) {
                    return (
                      <div className="supervisor-sidebar-parent" key={item.id}>
                        <div className="supervisor-sidebar-parent-row">
                          <NavLink to={item.path} onClick={onClose} title={isCollapsed ? itemLabel : undefined} className={`supervisor-sidebar-link supervisor-sidebar-parent-link ${isPumpSectionActive ? "supervisor-sidebar-link-active" : ""}`}>
                            <span className="supervisor-sidebar-link-icon"><Icon size={20} strokeWidth={1.8} aria-hidden="true" /></span>
                            {!isCollapsed && <span className="supervisor-sidebar-link-label">{itemLabel}</span>}
                          </NavLink>
                          {!isCollapsed && <button type="button" className="supervisor-sidebar-submenu-toggle" aria-label={showPumpsOpen ? t("navigation:sidebar.collapsePumpsSubmenu") : t("navigation:sidebar.expandPumpsSubmenu")} aria-expanded={showPumpsOpen} onClick={() => setIsPumpsOpen((current) => isPumpChildActive ? true : !current)}><ChevronDown size={17} aria-hidden="true" /></button>}
                        </div>
                        {!isCollapsed && showPumpsOpen && <div className="supervisor-sidebar-submenu">{item.children.map((child) => <NavLink key={child.id} to={child.path} onClick={onClose} className={({ isActive }) => `supervisor-sidebar-sublink ${isActive ? "supervisor-sidebar-sublink-active" : ""}`}><span />{t(`navigation:${child.labelKey}`)}</NavLink>)}</div>}
                      </div>
                    );
                  }

                  return (
                    <NavLink
                      key={item.id}
                      to={item.path}
                      onClick={onClose}
                      title={isCollapsed ? itemLabel : undefined}
                      className={({ isActive }) =>
                        [
                          "supervisor-sidebar-link",
                          isActive
                            ? "supervisor-sidebar-link-active"
                            : "",
                        ]
                          .filter(Boolean)
                          .join(" ")
                      }
                    >
                      <span className="supervisor-sidebar-link-icon">
                        <Icon
                          size={20}
                          strokeWidth={1.8}
                          aria-hidden="true"
                        />
                      </span>

                      {item.id === "orders" && pendingOrderCount > 0 && !isCollapsed && <b className="supervisor-sidebar-pending-badge">{pendingOrderCount > 99 ? "99+" : pendingOrderCount}</b>}
                      {!isCollapsed && (
                        <motion.span
                          className="supervisor-sidebar-link-label"
                          initial={{ opacity: 0, x: -6 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ duration: 0.2 }}
                        >
                          {itemLabel}
                        </motion.span>
                      )}
                    </NavLink>
                  );
                })}
              </div>
            </div>
          ))}
        </nav>

        <div className="supervisor-sidebar-footer">
          {!isCollapsed && (
            <motion.div
              className="supervisor-sidebar-system-status"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              <span className="supervisor-sidebar-status-dot" />

              <div>
                <strong>{t("navigation:sidebar.systemOperational")}</strong>
                <span>FuelFlex Platform v1.0</span>
              </div>
            </motion.div>
          )}

          <button
            type="button"
            className="supervisor-sidebar-collapse-button"
            onClick={onToggleCollapse}
            aria-label={
              isCollapsed
                ? t("navigation:sidebar.expandMenu")
                : t("navigation:sidebar.collapseMenu")
            }
            title={
              isCollapsed
                ? t("navigation:sidebar.expandMenu")
                : t("navigation:sidebar.collapseMenu")
            }
          >
            {isCollapsed ? (
              <ChevronRight size={20} strokeWidth={1.8} />
            ) : (
              <>
                <ChevronLeft size={20} strokeWidth={1.8} />
                <span>{t("navigation:sidebar.collapseMenu")}</span>
              </>
            )}
          </button>
        </div>
      </motion.aside>
    </>
  );
}

export default SupervisorSidebar;
