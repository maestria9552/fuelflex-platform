import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { ArrowLeft, Printer } from "lucide-react";
import QRCode from "qrcode";
import { useTranslation } from "react-i18next";
import ManagerLayout from "../../components/layout/ManagerLayout";
import SupervisorLayout from "../../components/layout/SupervisorLayout";
import { getManagerOrder } from "../../services/purchaseOrder/purchaseOrderService";
import { getSupervisorOrder } from "../../services/purchaseOrder/supervisorPurchaseOrderService";
import { formatDateTime } from "../../i18n/formatters";
import { getStoredUser } from "../../services/auth/authStorage";
import "./PurchaseOrderDocument.css";

function PurchaseOrderDocumentPage() {
  const { id } = useParams();
  const location = useLocation();
  const { t, i18n } = useTranslation(["orders", "common"]);
  const isSupervisor = location.pathname.startsWith("/superviseur/");
  const [order, setOrder] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [qrDataUrl, setQrDataUrl] = useState("");
  const storedUser = getStoredUser() || {};
  const organizationName = order?.organization?.name || storedUser.organizationName || storedUser.organization?.name || "FuelFlex";
  const Layout = isSupervisor ? SupervisorLayout : ManagerLayout;
  const backPath = isSupervisor ? `/superviseur/commandes/${id}` : `/gerant/commandes/${id}`;

  useEffect(() => {
    let active = true;
    (isSupervisor ? getSupervisorOrder(id) : getManagerOrder(id))
      .then((result) => active && setOrder(result))
      .catch((result) => active && setError(result))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [id, isSupervisor]);

  const date = (value) => value ? formatDateTime(value, { language: i18n.resolvedLanguage || i18n.language }) : "—";
  const managerName = order?.createdBy ? `${order.createdBy.firstName} ${order.createdBy.lastName}` : "—";
  const reviewerName = order?.supervisorReviewedBy ? `${order.supervisorReviewedBy.firstName} ${order.supervisorReviewedBy.lastName}` : "—";
  const isDraft = order?.status === "DRAFT" || order?.status === "PENDING_SUPERVISOR_APPROVAL";
  const isSupervisorApproved = Boolean(order?.supervisorReviewedBy && order?.supervisorReviewedAt && order.status !== "SUPERVISOR_REJECTED");

  useEffect(() => {
    if (!order?.orderNumber) return undefined;
    const baseUrl = import.meta.env.VITE_APP_BASE_URL || window.location.origin;
    const verificationRef = order.verificationToken || order.publicVerificationToken || order.orderNumber;
    let active = true;
    QRCode.toDataURL(baseUrl + "/verification/commande/" + encodeURIComponent(verificationRef), { errorCorrectionLevel: "M", margin: 1, width: 96, color: { dark: "#17263d", light: "#ffffff" } })
      .then((url) => active && setQrDataUrl(url)).catch(() => active && setQrDataUrl(""));
    return () => { active = false; };
  }, [order]);

  const quantity = (value) => value !== null && value !== undefined && value !== "" ? new Intl.NumberFormat(i18n.resolvedLanguage || i18n.language, { maximumFractionDigits: 3 }).format(Number(value)) : "—";

  return (
    <Layout>
      <main className="purchase-order-document-page">
        <div className="purchase-order-document-toolbar">
          <Link to={backPath} className="orders-back-link"><ArrowLeft size={16} />{t("orders:document.back")}</Link>
          {order && <button type="button" className="orders-primary-button document-print-button" onClick={() => window.print()}><Printer size={16} />{t("orders:document.print")}</button>}
        </div>
        {loading && <div className="orders-state">{t("orders:loading")}</div>}
        {!loading && error && <div className="orders-state orders-state-error">{t("orders:errors.load")}</div>}
        {!loading && order && (
          <article className="purchase-order-document" aria-label={t("orders:document.title")}>
            <header className="purchase-order-document-header">
              <div className="purchase-order-document-company">
                {order.organization?.logoUrl && <img className="purchase-order-document-logo" src={order.organization.logoUrl} alt="" />}
                <div className="purchase-order-document-company-info">
                  <p className="purchase-order-document-kicker">{organizationName}</p>
                  <div className="purchase-order-document-legal">
                    {order.organization?.registrationNumber && <span><strong>RCCM :</strong> {order.organization.registrationNumber}</span>}
                    {order.organization?.nationalId && <span><strong>ID NAT :</strong> {order.organization.nationalId}</span>}
                    {order.organization?.taxNumber && <span><strong>NIF :</strong> {order.organization.taxNumber}</span>}
                    {order.organization?.address && <span className="purchase-order-document-address">{order.organization.address}{order.organization.city ? " · " + order.organization.city : ""}</span>}
                  </div>
                </div>
              </div>
              <div className="purchase-order-document-reference"><span>{t("orders:document.reference")}</span><strong>{order.orderNumber}</strong><span>{t("orders:document.date")}</span><strong>{date(order.createdAt)}</strong>{isDraft && <em>{t("orders:document.draft")}</em>}</div>
            </header>
            <div className="purchase-order-document-divider" />
            <h1 className="purchase-order-document-title">{t("orders:document.title")}</h1>
            <section className="purchase-order-document-section-heading"><h2>{t("orders:document.orderInformation")}</h2></section>
            <section className="purchase-order-document-meta">
              <div className="purchase-order-document-meta-station"><span>{t("orders:document.station")}</span><strong>{order.station?.name || "—"}</strong>{order.station?.code && <small className="purchase-order-document-meta-code">{order.station.code}</small>}{(order.station?.city || order.station?.address) && <small className="purchase-order-document-meta-location">{[order.station.city, order.station.address].filter(Boolean).join(" · ")}</small>}</div>
              <div className="purchase-order-document-meta-supplier"><span>{t("orders:document.supplier")}</span><strong>{order.supplier?.displayName || t("orders:common.noSupplier")}</strong></div>
            </section>
            <section className="purchase-order-document-items">
              {order.attachments?.length > 0 && <p className="purchase-order-document-attachment-count">{t("orders:document.attachmentsCount", { count: order.attachments.length })}</p>}
              <h2>{t("orders:document.items")}</h2>
              <table><thead><tr><th>{t("orders:document.line")}</th><th>{t("orders:document.product")}</th><th>{t("orders:document.quantity")}</th><th>{t("orders:document.unit")}</th></tr></thead>
                <tbody>{(order.items || []).map((item, index) => <tr key={item.id || item.productId}><td>{index + 1}</td><td>{item.productName || item.productCode}</td><td>{quantity(item.quantity)}</td><td>{item.unit}</td></tr>)}</tbody>
              </table>
            </section>
            <section className="purchase-order-document-validation"><h2>{t("orders:document.validation")}</h2>{isSupervisorApproved ? <div><span>{t("orders:document.validatedBy")}</span><strong>{reviewerName}</strong><small>{date(order.supervisorReviewedAt)}</small></div> : <div className="purchase-order-document-validation-empty"><span>—</span></div>}</section>
            <footer className="purchase-order-document-footer">
              {qrDataUrl && <div className="purchase-order-document-qr"><img src={qrDataUrl} alt={t("orders:document.verifyAuthenticity")} /><small>{t("orders:document.verifyAuthenticity")}</small></div>}
              <div><span>{t("orders:document.created")}</span><strong>{date(order.createdAt)}</strong><small>{managerName}</small></div>
              <div><span>{t("orders:document.submitted")}</span><strong>{date(order.submittedAt)}</strong></div>
              <div><span>{t("orders:document.validated")}</span><strong>{date(order.supervisorReviewedAt)}</strong><small>{reviewerName}</small></div>
            </footer>
          </article>
        )}
      </main>
    </Layout>
  );
}

export default PurchaseOrderDocumentPage;
