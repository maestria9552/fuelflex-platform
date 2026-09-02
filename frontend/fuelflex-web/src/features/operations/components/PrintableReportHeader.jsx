import { useTranslation } from "react-i18next";
import { formatDateTime } from "../../../i18n/formatters";

export default function PrintableReportHeader({ organization, day, title, reference, status, extraMeta = [], language }) {
  const { t } = useTranslation("reports");
  const legal = [
    organization?.registrationNumber && ["RCCM", organization.registrationNumber],
    organization?.nationalId && ["ID NAT", organization.nationalId],
    organization?.taxNumber && ["NIF", organization.taxNumber],
  ].filter(Boolean);
  const station = day?.station || {};

  return <>
    <header className="print-header">
      <div className="print-company">
        {organization?.logoUrl && <img className="print-logo" src={organization.logoUrl} alt="" />}
        <div><p className="print-kicker">{organization?.name || "—"}</p><div className="print-legal">{legal.map(([label, value]) => <span key={label}><b>{label} :</b> {value}</span>)}{(organization?.address || organization?.city) && <span>{[organization.address, organization.city].filter(Boolean).join(" · ")}</span>}</div></div>
      </div>
      <div className="print-document-ref">
        <span>{title}</span>
        {reference && <b>{reference}</b>}
        <span>{t("print.date")} : {day?.businessDate || "—"}</span>
        <span>{t("print.status")} : {status || day?.status || "—"}</span>
        <span>{t("print.editedAt")} : {formatDateTime(new Date(), { language })}</span>
        {extraMeta.map(([label, value]) => value && <span key={label}>{label} : {value}</span>)}
      </div>
    </header>
    <div className="print-divider" />
    <div className="print-station"><b>{t("print.station")} : {station.name || day?.stationName || "—"}</b>{(station.city || station.address) && <span>{[station.city, station.address].filter(Boolean).join(" · ")}</span>}</div>
  </>;
}
