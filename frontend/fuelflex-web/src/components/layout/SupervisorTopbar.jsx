import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
  Check,
  ChevronDown,
  Menu,
  Search,
  Settings,
  UserRound,
} from "lucide-react";

import { changeLanguage } from "../../i18n/index.js";
import {
  resolveLanguage,
  SUPPORTED_LANGUAGES,
} from "../../i18n/language.js";
import {
  clearAuthSession,
  getStoredUser,
} from "../../services/auth/authStorage";
import ProfileModal from "../../features/profile/components/ProfileModal";
import AccountSettingsModal from "../../features/account/components/AccountSettingsModal";
import NotificationsPopover from "../../features/notification/components/NotificationsPopover";
import "./SupervisorTopbar.css";

const ROLE_PRIORITY = [
  "SUPER_ADMIN",
  "SUPERVISOR",
  "MANAGER",
  "PUMP_ATTENDANT",
  "ACCOUNTANT",
  "AUDITOR",
  "SUPPLIER_USER",
  "CREDIT_CUSTOMER_USER",
];

function normalizeText(value) {
  return typeof value === "string" ? value.trim() : "";
}

function normalizeRole(role) {
  if (typeof role === "string") {
    return role.trim().toUpperCase();
  }

  if (role && typeof role === "object") {
    return normalizeText(role.code || role.name).toUpperCase();
  }

  return "";
}

function getPrimaryRole(roles) {
  const normalizedRoles = Array.isArray(roles)
    ? roles.map(normalizeRole).filter(Boolean)
    : [];

  return (
    ROLE_PRIORITY.find((role) => normalizedRoles.includes(role)) ||
    normalizedRoles[0] ||
    ""
  );
}

function getInitials(firstName, lastName, email) {
  const nameInitials = [firstName, lastName]
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");

  if (nameInitials) {
    return nameInitials;
  }

  const emailName = email.split("@")[0];
  return emailName
    .split(/[._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

function SupervisorTopbar({
  onOpenSidebar,
  user = null,
  onUserUpdated,
}) {
  const { t, i18n } = useTranslation(["navigation", "common"]);
  const [activePopover, setActivePopover] = useState(null);
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
  const [isAccountModalOpen, setIsAccountModalOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const settingsContainerRef = useRef(null);
  const profileContainerRef = useRef(null);
  const settingsButtonRef = useRef(null);
  const profileButtonRef = useRef(null);
  const navigate = useNavigate();

  const currentUser = user || getStoredUser() || {};
  const firstName = normalizeText(currentUser.firstName);
  const lastName = normalizeText(currentUser.lastName);
  const email = normalizeText(currentUser.email);
  const providedName = normalizeText(currentUser.name);
  const providedRole = normalizeText(currentUser.role);
  const fullName = [firstName, lastName].filter(Boolean).join(" ");
  const displayedName =
    fullName || providedName || email || t("navigation:topbar.defaultUserName");
  const primaryRole = getPrimaryRole(currentUser.roles);
  const displayedRole = primaryRole
    ? t(`navigation:topbar.roles.${primaryRole}`)
    : providedRole || t("navigation:topbar.defaultUserRole");
  const initials = getInitials(firstName, lastName, email);
  const isSettingsOpen = activePopover === "settings";
  const isProfileOpen = activePopover === "profile";
  const activeLanguage = resolveLanguage(
    i18n.resolvedLanguage || i18n.language
  );

  useEffect(() => {
    if (!activePopover) {
      return undefined;
    }

    const handlePointerDown = (event) => {
      const activeContainer = activePopover === "settings"
        ? settingsContainerRef.current
        : profileContainerRef.current;

      if (activeContainer && !activeContainer.contains(event.target)) {
        setActivePopover(null);
      }
    };

    const handleKeyDown = (event) => {
      if (event.key !== "Escape") {
        return;
      }

      const trigger = activePopover === "settings"
        ? settingsButtonRef.current
        : profileButtonRef.current;

      setActivePopover(null);
      window.requestAnimationFrame(() => trigger?.focus());
    };

    document.addEventListener("pointerdown", handlePointerDown);
    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("pointerdown", handlePointerDown);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [activePopover]);

  const handleLogout = () => {
    clearAuthSession();
    setActivePopover(null);
    navigate("/connexion", { replace: true });
  };

  const handleLanguageChange = async (languageCode) => {
    await changeLanguage(languageCode);
    setActivePopover(null);
  };

  const handleOpenProfile = () => {
    setActivePopover(null);
    setIsProfileModalOpen(true);
  };

  const handleCloseProfile = () => {
    setIsProfileModalOpen(false);
    window.requestAnimationFrame(() => profileButtonRef.current?.focus());
  };

  const handleProfileSaved = (_profile, updatedUser) => {
    if (updatedUser) {
      onUserUpdated?.(updatedUser);
    }
  };

  const handleOpenAccountSettings = () => {
    setActivePopover(null);
    setIsAccountModalOpen(true);
  };

  const handleCloseAccountSettings = () => {
    setIsAccountModalOpen(false);
    window.requestAnimationFrame(() => profileButtonRef.current?.focus());
  };

  return (
    <>
      <header className="supervisor-topbar">
      <div className="supervisor-topbar-left">
        <button
          type="button"
          className="supervisor-topbar-menu-button"
          onClick={onOpenSidebar}
          aria-label={t("navigation:topbar.openMenu")}
        >
          <Menu size={21} strokeWidth={1.8} />
        </button>

        <div className="supervisor-topbar-search">
          <Search size={18} strokeWidth={1.8} aria-hidden="true" />
          <input
            type="search"
            placeholder={t("navigation:topbar.searchPlaceholder")}
            aria-label={t("navigation:topbar.searchLabel")}
          />
        </div>
      </div>

      <div className="supervisor-topbar-actions">
        <div className="supervisor-topbar-settings" ref={settingsContainerRef}>
          <button
            ref={settingsButtonRef}
            type="button"
            className="supervisor-topbar-action-button"
            aria-label={t("navigation:topbar.settings")}
            title={t("navigation:topbar.settings")}
            aria-expanded={isSettingsOpen}
            aria-haspopup="true"
            aria-controls="supervisor-settings-popover"
            onClick={() => setActivePopover(isSettingsOpen ? null : "settings")}
          >
            <Settings size={19} strokeWidth={1.8} aria-hidden="true" />
          </button>

          {isSettingsOpen && (
            <section
              id="supervisor-settings-popover"
              className="supervisor-topbar-settings-menu"
              aria-label={t("navigation:topbar.settings")}
            >
              <header>
                <strong>{t("navigation:topbar.language")}</strong>
                <span>{t("navigation:topbar.languageDescription")}</span>
              </header>
              <div
                className="supervisor-topbar-language-list"
                role="radiogroup"
                aria-label={t("navigation:topbar.language")}
              >
                {SUPPORTED_LANGUAGES.map((language) => {
                  const isActive = activeLanguage === language.code;
                  return (
                    <button
                      key={language.code}
                      type="button"
                      role="radio"
                      aria-checked={isActive}
                      className={isActive ? "active" : ""}
                      onClick={() => handleLanguageChange(language.code)}
                    >
                      <span>{language.label}</span>
                      {isActive && <Check size={17} aria-hidden="true" />}
                    </button>
                  );
                })}
              </div>
              <p>{t("navigation:topbar.morePreferencesSoon")}</p>
            </section>
          )}
        </div>

        <NotificationsPopover
          isOpen={isNotificationsOpen}
          onOpen={() => {
            setActivePopover(null);
            setIsNotificationsOpen(true);
          }}
          onClose={() => setIsNotificationsOpen(false)}
        />

        <div className="supervisor-topbar-profile" ref={profileContainerRef}>
          <button
            ref={profileButtonRef}
            type="button"
            className="supervisor-topbar-profile-button"
            onClick={() => setActivePopover(isProfileOpen ? null : "profile")}
            aria-label={t("navigation:topbar.userMenu")}
            aria-expanded={isProfileOpen}
            aria-haspopup="true"
            aria-controls="supervisor-user-popover"
          >
            <span className="supervisor-topbar-avatar" aria-hidden="true">
              {initials || <UserRound size={18} />}
            </span>
            <span className="supervisor-topbar-profile-text">
              <strong>{displayedName}</strong>
              <small>{displayedRole}</small>
            </span>
            <ChevronDown
              size={17}
              strokeWidth={1.8}
              aria-hidden="true"
              className={[
                "supervisor-topbar-profile-chevron",
                isProfileOpen ? "supervisor-topbar-profile-chevron-open" : "",
              ].filter(Boolean).join(" ")}
            />
          </button>

          {isProfileOpen && (
            <section
              id="supervisor-user-popover"
              className="supervisor-topbar-profile-menu"
              aria-label={t("navigation:topbar.userMenu")}
            >
              <header className="supervisor-topbar-profile-summary">
                <strong>{displayedName}</strong>
                {email && <span>{email}</span>}
                <small>{displayedRole}</small>
              </header>

              <button type="button" onClick={handleOpenProfile}>
                {t("navigation:topbar.profile")}
              </button>
              <button type="button" onClick={handleOpenAccountSettings}>
                {t("navigation:topbar.accountSettings")}
              </button>
              <div
                className="supervisor-topbar-profile-menu-divider"
                role="separator"
              />
              <button
                type="button"
                className="supervisor-topbar-profile-menu-danger"
                onClick={handleLogout}
              >
                {t("common:actions.logout")}
              </button>
            </section>
          )}
        </div>
      </div>
      </header>
      <ProfileModal
        isOpen={isProfileModalOpen}
        onClose={handleCloseProfile}
        onSaved={handleProfileSaved}
      />
      <AccountSettingsModal
        isOpen={isAccountModalOpen}
        onClose={handleCloseAccountSettings}
      />
    </>
  );
}

export default SupervisorTopbar;
