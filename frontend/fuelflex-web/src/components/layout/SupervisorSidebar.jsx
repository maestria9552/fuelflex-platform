import React from "react";
import "./SupervisorSidebar.css";
import { NavLink } from "react-router-dom";
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
  BriefcaseBusiness,
  UserRoundCog,
  ChartNoAxesCombined,
  Settings2,
  ChevronLeft,
  ChevronRight,
  X,
} from "lucide-react";

import FuelFlexLogo from "../brand/FuelFlexLogo";

const navigationGroups = [
  {
    id: "general",
    label: "Général",
    items: [
      {
        id: "dashboard",
        label: "Tableau de bord",
        path: "/superviseur/dashboard",
        icon: LayoutDashboard,
      },
      {
        id: "societe",
        label: "Société",
        path: "/superviseur/societe",
        icon: Building2,
      },
    ],
  },
  {
    id: "configuration",
    label: "Configuration",
    items: [
      {
        id: "stations",
        label: "Stations",
        path: "/superviseur/stations",
        icon: Fuel,
      },
      {
        id: "produits",
        label: "Produits",
        path: "/superviseur/produits",
        icon: Droplets,
      },
      {
        id: "depots",
        label: "Dépôts",
        path: "/superviseur/depots",
        icon: Warehouse,
      },
      {
        id: "citernes",
        label: "Citernes",
        path: "/superviseur/citernes",
        icon: Cylinder,
      },
      {
        id: "pompes",
        label: "Pompes",
        path: "/superviseur/pompes",
        icon: Fuel,
      },
      {
        id: "tarification",
        label: "Tarification",
        path: "/superviseur/tarification",
        icon: BadgeDollarSign,
      },
    ],
  },
  {
    id: "gestion",
    label: "Gestion",
    items: [
      {
        id: "clients",
        label: "Clients partenaires",
        path: "/superviseur/clients",
        icon: Handshake,
      },
      {
        id: "gestionnaires",
        label: "Gestionnaires",
        path: "/superviseur/gestionnaires",
        icon: BriefcaseBusiness,
      },
      {
        id: "pompistes",
        label: "Pompistes",
        path: "/superviseur/pompistes",
        icon: UserRoundCog,
      },
    ],
  },
  {
    id: "analyse",
    label: "Analyse",
    items: [
      {
        id: "rapports",
        label: "Rapports",
        path: "/superviseur/rapports",
        icon: ChartNoAxesCombined,
      },
      {
        id: "parametres",
        label: "Paramètres",
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
  const sidebarWidth = isCollapsed ? 88 : 286;

  return (
    <>
      <AnimatePresence>
        {isOpen && (
          <motion.button
            type="button"
            className="supervisor-sidebar-overlay"
            aria-label="Fermer le menu"
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
            aria-label="Fermer le menu"
          >
            <X size={20} strokeWidth={1.8} />
          </button>
        </div>

        <nav
          className="supervisor-sidebar-navigation"
          aria-label="Navigation superviseur"
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
                  {group.label}
                </motion.p>
              )}

              <div className="supervisor-sidebar-group-links">
                {group.items.map((item) => {
                  const Icon = item.icon;

                  return (
                    <NavLink
                      key={item.id}
                      to={item.path}
                      onClick={onClose}
                      title={isCollapsed ? item.label : undefined}
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

                      {!isCollapsed && (
                        <motion.span
                          className="supervisor-sidebar-link-label"
                          initial={{ opacity: 0, x: -6 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ duration: 0.2 }}
                        >
                          {item.label}
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
                <strong>Système opérationnel</strong>
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
                ? "Développer le menu"
                : "Réduire le menu"
            }
            title={
              isCollapsed
                ? "Développer le menu"
                : "Réduire le menu"
            }
          >
            {isCollapsed ? (
              <ChevronRight size={20} strokeWidth={1.8} />
            ) : (
              <>
                <ChevronLeft size={20} strokeWidth={1.8} />
                <span>Réduire le menu</span>
              </>
            )}
          </button>
        </div>
      </motion.aside>
    </>
  );
}

export default SupervisorSidebar;