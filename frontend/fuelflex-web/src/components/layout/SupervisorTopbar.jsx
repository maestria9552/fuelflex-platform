import React, { useState } from "react";
import {
  Bell,
  ChevronDown,
  Menu,
  Search,
  Settings,
  UserRound,
} from "lucide-react";

import "./SupervisorTopbar.css";

function SupervisorTopbar({
  onOpenSidebar,
  user = {
    name: "Superviseur FuelFlex",
    role: "Superviseur principal",
  },
}) {
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const initials = user.name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");

  return (
    <header className="supervisor-topbar">
      <div className="supervisor-topbar-left">
        <button
          type="button"
          className="supervisor-topbar-menu-button"
          onClick={onOpenSidebar}
          aria-label="Ouvrir le menu"
        >
          <Menu size={21} strokeWidth={1.8} />
        </button>

        <div className="supervisor-topbar-search">
          <Search
            size={18}
            strokeWidth={1.8}
            aria-hidden="true"
          />

          <input
            type="search"
            placeholder="Rechercher dans FuelFlex..."
            aria-label="Rechercher dans FuelFlex"
          />

          <span className="supervisor-topbar-search-shortcut">
            Ctrl K
          </span>
        </div>
      </div>

      <div className="supervisor-topbar-actions">
        <button
          type="button"
          className="supervisor-topbar-action-button"
          aria-label="Paramètres"
          title="Paramètres"
        >
          <Settings size={19} strokeWidth={1.8} />
        </button>

        <button
          type="button"
          className="supervisor-topbar-action-button supervisor-topbar-notification-button"
          aria-label="Notifications"
          title="Notifications"
        >
          <Bell size={19} strokeWidth={1.8} />

          <span className="supervisor-topbar-notification-badge">
            3
          </span>
        </button>

        <div className="supervisor-topbar-profile">
          <button
            type="button"
            className="supervisor-topbar-profile-button"
            onClick={() =>
              setIsProfileOpen((currentValue) => !currentValue)
            }
            aria-expanded={isProfileOpen}
            aria-haspopup="menu"
          >
            <span className="supervisor-topbar-avatar">
              {initials || <UserRound size={18} />}
            </span>

            <span className="supervisor-topbar-profile-text">
              <strong>{user.name}</strong>
              <small>{user.role}</small>
            </span>

            <ChevronDown
              size={17}
              strokeWidth={1.8}
              className={[
                "supervisor-topbar-profile-chevron",
                isProfileOpen
                  ? "supervisor-topbar-profile-chevron-open"
                  : "",
              ]
                .filter(Boolean)
                .join(" ")}
            />
          </button>

          {isProfileOpen && (
            <div
              className="supervisor-topbar-profile-menu"
              role="menu"
            >
              <button type="button" role="menuitem">
                Mon profil
              </button>

              <button type="button" role="menuitem">
                Paramètres du compte
              </button>

              <div className="supervisor-topbar-profile-menu-divider" />

              <button
                type="button"
                role="menuitem"
                className="supervisor-topbar-profile-menu-danger"
              >
                Se déconnecter
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export default SupervisorTopbar;