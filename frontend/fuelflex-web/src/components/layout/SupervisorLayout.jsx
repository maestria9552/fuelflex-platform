import { useState } from "react";

import { getStoredUser } from "../../services/auth/authStorage";
import SupervisorSidebar from "./SupervisorSidebar";
import SupervisorTopbar from "./SupervisorTopbar";
import "./SupervisorLayout.css";

function SupervisorLayout({ children }) {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [currentUser, setCurrentUser] = useState(() => getStoredUser());

  const handleOpenSidebar = () => {
    setIsSidebarOpen(true);
  };

  const handleCloseSidebar = () => {
    setIsSidebarOpen(false);
  };

  const handleToggleSidebar = () => {
    setIsSidebarCollapsed((currentState) => !currentState);
  };

  return (
    <div
      className={[
        "supervisor-layout",
        isSidebarCollapsed
          ? "supervisor-layout-sidebar-collapsed"
          : "",
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <SupervisorSidebar
        isOpen={isSidebarOpen}
        isCollapsed={isSidebarCollapsed}
        onClose={handleCloseSidebar}
        onToggleCollapse={handleToggleSidebar}
      />

      <div className="supervisor-layout-body">
        <SupervisorTopbar
          onOpenSidebar={handleOpenSidebar}
          user={currentUser}
          onUserUpdated={setCurrentUser}
        />

        <div className="supervisor-layout-content">
          {children}
        </div>
      </div>
    </div>
  );
}

export default SupervisorLayout;
