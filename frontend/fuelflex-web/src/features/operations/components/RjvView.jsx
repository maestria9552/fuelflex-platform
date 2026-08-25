import { useTranslation } from "react-i18next";

function decimal(value, locale) {
  if (value === null || value === undefined) return "—";
  return new Intl.NumberFormat(locale, { maximumFractionDigits: 3 }).format(Number(value));
}

function AggregateTable({ title, rows, locale }) {
  const { t } = useTranslation("operations");
  return (
    <section className="operations-card">
      <h3>{title}</h3>
      {rows?.length ? (
        <div className="operations-table-wrap">
          <table className="operations-table operations-table-compact">
            <thead><tr><th>{t("rjv.name")}</th><th>{t("rjv.cashVolume")}</th><th>{t("rjv.creditVolume")}</th><th>{t("rjv.totalVolume")}</th><th>{t("rjv.totalAmount")}</th></tr></thead>
            <tbody>{rows.map((row) => <tr key={row.id}><td>{row.name}</td><td>{decimal(row.cashVolume, locale)}</td><td>{decimal(row.creditVolume, locale)}</td><td>{decimal(row.totalVolume, locale)}</td><td>{decimal(row.totalAmount, locale)}</td></tr>)}</tbody>
          </table>
        </div>
      ) : <p className="operations-muted">{t("rjv.noData")}</p>}
    </section>
  );
}

export default function RjvView({ report, locale }) {
  const { t } = useTranslation("operations");
  if (!report) return <div className="operations-empty compact"><p>{t("rjv.unavailable")}</p></div>;

  const metrics = [
    ["cashVolume", report.cashVolume, "L"],
    ["cashAmount", report.cashAmount, ""],
    ["creditVolume", report.creditVolume, "L"],
    ["creditAmount", report.creditAmount, ""],
    ["totalSoldVolume", report.totalSoldVolume, "L"],
    ["totalSalesAmount", report.totalSalesAmount, ""],
    ["meteredVolume", report.meteredVolume, "L"],
    ["volumeVariance", report.volumeVariance, "L"],
    ["expenseAmount", report.expenseAmount, ""],
    ["theoreticalStock", report.theoreticalStock, "L"],
    ["physicalStock", report.physicalStock, "L"],
    ["stockVariance", report.stockVariance, "L"],
  ];

  return (
    <div className="operations-rjv">
      <div className="operations-kpis">
        {metrics.map(([key, value, unit]) => (
          <article className="operations-kpi" key={key}>
            <span>{t(`rjv.${key}`)}</span>
            <strong>{decimal(value, locale)} {unit}</strong>
          </article>
        ))}
      </div>
      <div className="operations-grid">
        <AggregateTable title={t("rjv.byProduct")} rows={report.byProduct} locale={locale} />
        <AggregateTable title={t("rjv.byPumpAttendant")} rows={report.byPumpAttendant} locale={locale} />
      </div>
      <AggregateTable title={t("rjv.byFuelMeter")} rows={report.byFuelMeter} locale={locale} />
      <section className="operations-card">
        <h3>{t("rjv.stockDetails")}</h3>
        {report.stocks?.length ? (
          <div className="operations-table-wrap">
            <table className="operations-table operations-table-compact">
              <thead><tr><th>{t("fields.tank")}</th><th>{t("fields.product")}</th><th>{t("rjv.theoreticalStock")}</th><th>{t("rjv.physicalStock")}</th><th>{t("rjv.stockVariance")}</th></tr></thead>
              <tbody>{report.stocks.map((stock) => <tr key={stock.tankId}><td>{stock.tankName}</td><td>{stock.productName}</td><td>{decimal(stock.theoreticalStock, locale)} L</td><td>{decimal(stock.physicalStock, locale)} L</td><td>{decimal(stock.stockVariance, locale)} L</td></tr>)}</tbody>
            </table>
          </div>
        ) : <p className="operations-muted">{t("rjv.noData")}</p>}
      </section>
    </div>
  );
}
