import { ShieldX } from "lucide-react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";

export default function AccessDeniedPage() {
  const { t } = useTranslation("auth");
  return (
    <main className="access-denied-page">
      <section>
        <ShieldX size={38} />
        <h1>{t("protectedRoute.forbiddenTitle")}</h1>
        <p>{t("protectedRoute.forbiddenDescription")}</p>
        <Link to="/dashboard">{t("protectedRoute.backToDashboard")}</Link>
      </section>
    </main>
  );
}
