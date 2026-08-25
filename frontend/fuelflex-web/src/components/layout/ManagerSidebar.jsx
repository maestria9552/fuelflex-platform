import { BarChart3, CalendarDays, ClipboardCheck, LayoutDashboard, PackageCheck, ShoppingCart, UsersRound, X } from "lucide-react";
import { useTranslation } from "react-i18next";
import { NavLink } from "react-router-dom";
import { hasPermission } from "../../services/auth/permissionService";
import FuelFlexLogo from "../brand/FuelFlexLogo";
import "./ManagerSidebar.css";

const groups = [
  { id: "general", label: "groups.general", items: [{ id: "dashboard", label: "items.dashboard", path: "/gerant/dashboard", icon: LayoutDashboard }] },
  { id: "operations", label: "groups.operations", items: [
    { id: "orders", label: "items.orders", path: "/gerant/commandes", icon: ClipboardCheck },
    { id: "receipts", label: "items.receipts", path: "/gerant/receptions", icon: PackageCheck },
    { id: "dailyOperations", label: "items.dailyOperations", path: "/gerant/operations", icon: CalendarDays, permission: "operational-day:view" },
    { id: "sales", label: "items.sales", path: "/gerant/ventes", icon: ShoppingCart, permission: "pos-sale:view" },
    { id: "pumpAttendants", label: "items.pumpAttendants", path: "/gerant/pompistes", icon: UsersRound, permission: "pump-attendant-validation:view" },
  ] },
  { id: "analytics", label: "groups.analytics", items: [{ id: "reports", label: "items.reports", icon: BarChart3, soon: true }] },
];

function ManagerSidebar({ isOpen = false, isCollapsed = false, pendingCount = 0, onClose, onToggleCollapse }) {
  const { t } = useTranslation(["navigation", "common"]);
  return <>
    {isOpen && <button className="manager-sidebar-overlay" type="button" aria-label={t("navigation:sidebar.closeMenu")} onClick={onClose} />}
    <aside className={`manager-sidebar ${isOpen ? "manager-sidebar-open" : ""} ${isCollapsed ? "manager-sidebar-collapsed" : ""}`}>
      <div className="manager-sidebar-header"><FuelFlexLogo size={isCollapsed ? 46 : 52} compact={isCollapsed} /><button type="button" className="manager-sidebar-close" aria-label={t("navigation:sidebar.closeMenu")} onClick={onClose}><X size={20} /></button></div>
      <nav className="manager-sidebar-navigation" aria-label={t("navigation:sidebar.managerNavigation")}>
        {groups.map((group) => <section className="manager-sidebar-group" key={group.id}>{!isCollapsed && <p>{t(`navigation:${group.label}`)}</p>}<div>{group.items.filter((item) => !item.permission || hasPermission(item.permission)).map((item) => { const Icon = item.icon; if (item.soon) return <span className="manager-sidebar-link manager-sidebar-link-disabled" key={item.id}><span className="manager-sidebar-icon"><Icon size={20} /></span>{!isCollapsed && <><span>{t(`navigation:${item.label}`)}</span><small>{t("navigation:sidebar.comingSoon")}</small></>}</span>; return <NavLink key={item.id} to={item.path} onClick={onClose} className={({ isActive }) => `manager-sidebar-link ${isActive ? "manager-sidebar-link-active" : ""}`} title={isCollapsed ? t(`navigation:${item.label}`) : undefined}><span className="manager-sidebar-icon"><Icon size={20} /></span>{!isCollapsed && <span>{t(`navigation:${item.label}`)}</span>}{item.id === "orders" && pendingCount > 0 && <b className="manager-sidebar-badge">{pendingCount > 99 ? "99+" : pendingCount}</b>}</NavLink>; })}</div></section>)}
      </nav>
      <div className="manager-sidebar-footer"><span className="manager-status-dot" />{!isCollapsed && <span>{t("navigation:sidebar.systemOperational")}</span>}<button type="button" onClick={onToggleCollapse} aria-label={isCollapsed ? t("navigation:sidebar.expandMenu") : t("navigation:sidebar.collapseMenu")}><span aria-hidden="true">‹</span></button></div>
    </aside>
  </>;
}
export default ManagerSidebar;
