import { useState } from "react";
import { getStoredUser } from "../../services/auth/authStorage";
import ManagerSidebar from "./ManagerSidebar";
import ManagerTopbar from "./ManagerTopbar";
import "./ManagerLayout.css";

function ManagerLayout({ children }) {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [currentUser, setCurrentUser] = useState(() => getStoredUser());
  return <div className={`manager-layout ${isSidebarCollapsed ? "manager-layout-collapsed" : ""}`}><ManagerSidebar isOpen={isSidebarOpen} isCollapsed={isSidebarCollapsed} onClose={() => setIsSidebarOpen(false)} onToggleCollapse={() => setIsSidebarCollapsed((value) => !value)} /><div className="manager-layout-body"><ManagerTopbar onOpenSidebar={() => setIsSidebarOpen(true)} user={currentUser} onUserUpdated={setCurrentUser} /><main className="manager-layout-content">{children}</main></div></div>;
}
export default ManagerLayout;
