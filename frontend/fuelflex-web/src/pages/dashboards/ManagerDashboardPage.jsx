import { BriefcaseBusiness, CheckCircle2 } from "lucide-react";
import { useTranslation } from "react-i18next";

import ManagerLayout from "../../components/layout/ManagerLayout";
import "./ManagerDashboardPage.css";

function ManagerDashboardPage() {
  const { t } = useTranslation("managerDashboard");
  return <ManagerLayout><section className="manager-dashboard"><div className="manager-dashboard-kicker"><BriefcaseBusiness size={17} />{t("eyebrow")}</div><h1>{t("title")}</h1><p className="manager-dashboard-welcome">{t("welcome")}</p><article className="manager-dashboard-card"><CheckCircle2 size={24} /><div><h2>{t("readyTitle")}</h2><p>{t("readyDescription")}</p></div></article></section></ManagerLayout>;
}

export default ManagerDashboardPage;
