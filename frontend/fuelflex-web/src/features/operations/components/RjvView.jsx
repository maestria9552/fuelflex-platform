import { useTranslation } from "react-i18next";

import { formatCurrency } from "../../../i18n/formatters";

const decimal = (value, locale) =>
  value === null || value === undefined
    ? "—"
    : new Intl.NumberFormat(locale, { maximumFractionDigits: 3 }).format(
        Number(value),
      );
function Metrics({ title, items, locale, money }) {
  return (
    <section className="operations-card operations-rjv-section">
      <h3>{title}</h3>
      <div className="operations-rjv-metrics">
        {items.map(([label, value, unit]) => (
          <div key={label}>
            <span>{label}</span>
            <strong>
              {unit === "money"
                ? money(value)
                : `${decimal(value, locale)} ${unit}`}
            </strong>
          </div>
        ))}
      </div>
    </section>
  );
}
function AggregateTable({ title, rows, locale, money }) {
  const { t } = useTranslation("operations");
  return (
    <section className="operations-card">
      <h3>{title}</h3>
      {rows?.length ? (
        <div className="operations-table-wrap">
          <table className="operations-table operations-table-compact">
            <thead>
              <tr>
                <th>{t("rjv.name")}</th>
                <th>{t("rjv.cashVolume")}</th>
                <th>{t("rjv.creditVolume")}</th>
                <th>{t("rjv.totalVolume")}</th>
                <th>{t("rjv.cashAmount")}</th>
                <th>{t("rjv.creditAmount")}</th>
                <th>{t("rjv.totalAmount")}</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.name}</td>
                  <td>{decimal(row.cashVolume, locale)} L</td>
                  <td>{decimal(row.creditVolume, locale)} L</td>
                  <td>{decimal(row.totalVolume, locale)} L</td>
                  <td>{money(row.cashAmount)}</td>
                  <td>{money(row.creditAmount)}</td>
                  <td>{money(row.totalAmount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="operations-muted">{t("rjv.noData")}</p>
      )}
    </section>
  );
}

export default function RjvView({ report, locale, language, currency }) {
  const { t } = useTranslation("operations");
  const money = (value) =>
    value === null || value === undefined
      ? "—"
      : formatCurrency(Number(value), currency, { language });
  if (!report)
    return (
      <div className="operations-empty compact">
        <p>{t("rjv.unavailable")}</p>
      </div>
    );
  return (
    <div className="operations-rjv">
      <section className="operations-card operations-rjv-section">
        <h3>{t("rjv.daySection")}</h3>
        <div className="operations-rjv-metrics">
          <div>
            <span>{t("fields.station")}</span>
            <strong>{report.stationName || "—"}</strong>
          </div>
          <div>
            <span>{t("fields.businessDate")}</span>
            <strong>{report.businessDate || "—"}</strong>
          </div>
          <div>
            <span>{t("fields.status")}</span>
            <strong>{t(`statuses.${report.status}`)}</strong>
          </div>
        </div>
      </section>
      <Metrics
        title={t("rjv.activitySection")}
        locale={locale}
        money={money}
        items={[
          [t("rjv.meteredVolume"), report.meteredVolume, "L"],
          [t("rjv.tankReturnVolume"), report.tankReturnVolume, "L"],
          [t("rjv.totalSoldVolume"), report.totalSoldVolume, "L"],
          [t("rjv.cashVolume"), report.cashVolume, "L"],
          [t("rjv.creditVolume"), report.creditVolume, "L"],
          [t("rjv.volumeVariance"), report.volumeVariance, "L"],
        ]}
      />
      <div className="operations-grid">
        <Metrics
          title={t("rjv.turnoverSection")}
          locale={locale}
          money={money}
          items={[
            [t("rjv.cashAmount"), report.cashAmount, "money"],
            [t("rjv.creditAmount"), report.creditAmount, "money"],
            [t("rjv.totalSalesAmount"), report.totalSalesAmount, "money"],
          ]}
        />
        <Metrics
          title={t("rjv.cashSection")}
          locale={locale}
          money={money}
          items={[
            [t("rjv.expectedCash"), report.expectedCash, "money"],
            [
              t("rjv.expensesAmount"),
              report.expensesAmount ?? report.expenseAmount,
              "money",
            ],
            [t("rjv.expectedNetCash"), report.expectedNetCash, "money"],
          ]}
        />
      </div>
      {report.cashReconciliationAvailable && <section className="operations-card operations-rjv-section"><h3>{t("closeDay.cash.title")}</h3><div className="operations-rjv-metrics"><div><span>{t("closeDay.cash.referenceCurrency")}</span><strong>{report.referenceCurrency}</strong></div><div><span>{t("closeDay.cash.grossExpected")}</span><strong>{money(report.cashGrossExpected)}</strong></div><div><span>{t("closeDay.cash.disbursedExpenses")}</span><strong>{money(report.disbursedExpenseAmount)}</strong></div><div><span>{t("closeDay.cash.netExpected")}</span><strong>{money(report.cashNetExpected)}</strong></div><div><span>{t("closeDay.cash.referenceCash", { currency: report.referenceCurrency })}</span><strong>{money(report.physicalReferenceAmount)}</strong></div><div><span>{t("closeDay.cash.usdCash")}</span><strong>{decimal(report.physicalUsdAmount, locale)} USD</strong></div><div><span>{t("closeDay.cash.rate", { currency: report.referenceCurrency })}</span><strong>1 USD = {decimal(report.usdExchangeRate, locale)} {report.referenceCurrency}</strong></div><div><span>{t("closeDay.cash.convertedUsd")}</span><strong>{money(report.convertedUsdAmount)}</strong></div><div><span>{t("closeDay.cash.observed")}</span><strong>{money(report.observedCashAmount)}</strong></div><div><span>{t("closeDay.cash.variance")}</span><strong>{money(report.cashVariance)}</strong></div></div><p className={`cash-status cash-status-${report.cashStatus}`}>{t(`closeDay.cash.statuses.${report.cashStatus}`)}</p></section>}
      {report.status === "CLOSED" && !report.cashReconciliationAvailable && <section className="operations-card operations-rjv-section"><h3>{t("closeDay.cash.title")}</h3><p className="operations-muted">{t("closeDay.cash.historicalUnavailable")}</p></section>}
      <Metrics
        title={t("rjv.stockSection")}
        locale={locale}
        money={money}
        items={[
          [t("rjv.theoreticalStock"), report.theoreticalStock, "L"],
          [t("rjv.physicalStock"), report.physicalStock, "L"],
          [t("rjv.stockVariance"), report.stockVariance, "L"],
        ]}
      />
      <section className="operations-rjv-analysis">
        <h3>{t("rjv.analysisSection")}</h3>
        <div className="operations-grid">
          <AggregateTable
            title={t("rjv.byProduct")}
            rows={report.byProduct}
            locale={locale}
            money={money}
          />
          <AggregateTable
            title={t("rjv.byPumpAttendant")}
            rows={report.byPumpAttendant}
            locale={locale}
            money={money}
          />
        </div>
        <AggregateTable
          title={t("rjv.byFuelMeter")}
          rows={report.byFuelMeter}
          locale={locale}
          money={money}
        />
      </section>
      <section className="operations-card operations-rjv-section">
        <h3>{t("rjv.internalSection")}</h3>
        <div className="operations-rjv-metrics"><div><span>{t("rjv.internalVolume")}</span><strong>{decimal(report.internalConsumptionVolume, locale)} L</strong></div><div><span>{t("rjv.internalAmount")}</span><strong>{money(report.internalConsumptionAmount)}</strong></div></div>
        {report.internalConsumptionsByProduct?.length ? <div className="operations-table-wrap"><table className="operations-table operations-table-compact"><thead><tr><th>{t("rjv.name")}</th><th>{t("rjv.internalVolume")}</th><th>{t("rjv.internalAmount")}</th></tr></thead><tbody>{report.internalConsumptionsByProduct.map((row) => <tr key={row.productId}><td>{row.productName}</td><td>{decimal(row.quantity, locale)} L</td><td>{money(row.amount)}</td></tr>)}</tbody></table></div> : <p className="operations-muted">{t("rjv.noData")}</p>}
        {report.internalConsumptions?.length ? <ul className="operations-data-list">{report.internalConsumptions.map((item) => <li key={item.id}><span><strong>{item.usageBeneficiary}</strong><small>{item.productName} · {item.recordedAt ? new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(item.recordedAt)) : "—"}{item.observation ? ` · ${item.observation}` : ""}</small></span><b>{decimal(item.quantity, locale)} L · {money(item.totalAmount)}</b></li>)}</ul> : null}
      </section>
      <section className="operations-card">
        <h3>{t("rjv.reconciliationSection")}</h3>
        {report.reconciliations?.length ? (
          <div className="operations-reconciliation-list">
            {report.reconciliations.map((item) => (
              <article key={item.id}>
                <header>
                  <strong>{item.pumpAttendantName}</strong>
                  <span>
                    {item.pumpName} · {item.fuelMeterName}
                  </span>
                </header>
                <dl>
                  <div>
                    <dt>{t("fields.openingIndex")}</dt>
                    <dd>{decimal(item.openingIndex, locale)}</dd>
                  </div>
                  <div>
                    <dt>{t("fields.closingIndex")}</dt>
                    <dd>{decimal(item.closingIndex, locale)}</dd>
                  </div>
                  <div>
                    <dt>{t("rjv.meteredVolume")}</dt>
                    <dd>{decimal(item.meteredVolume, locale)} L</dd>
                  </div>
                  <div>
                    <dt>{t("rjv.tankReturnVolume")}</dt>
                    <dd>{decimal(item.tankReturnVolume, locale)} L</dd>
                  </div>
                  <div>
                    <dt>{t("rjv.cashVolume")}</dt>
                    <dd>{decimal(item.cashVolume, locale)} L</dd>
                  </div>
                  <div>
                    <dt>{t("rjv.creditVolume")}</dt>
                    <dd>{decimal(item.creditVolume, locale)} L</dd>
                  </div>
                  <div>
                    <dt>{t("rjv.totalSalesAmount")}</dt>
                    <dd>{money(item.turnover)}</dd>
                  </div>
                  <div>
                    <dt>{t("rjv.volumeVariance")}</dt>
                    <dd>{decimal(item.volumeVariance, locale)} L</dd>
                  </div>
                </dl>
              </article>
            ))}
          </div>
        ) : (
          <p className="operations-muted">{t("rjv.noData")}</p>
        )}
      </section>
      <section className="operations-card">
        <h3>{t("rjv.stockDetails")}</h3>
        {report.stocks?.length ? (
          <div className="operations-stock-grid">
            {report.stocks.map((stock) => (
              <article key={stock.tankId}>
                <small>{stock.productName}</small>
                <strong>{stock.tankName}</strong>
                <span>
                  {t("rjv.theoreticalStock")}:{" "}
                  {decimal(stock.theoreticalStock, locale)} L
                </span>
                <span>
                  {t("rjv.physicalStock")}:{" "}
                  {decimal(stock.physicalStock, locale)} L
                </span>
                <b>
                  {t("rjv.stockVariance")}:{" "}
                  {decimal(stock.stockVariance, locale)} L
                </b>
              </article>
            ))}
          </div>
        ) : (
          <p className="operations-muted">{t("rjv.noData")}</p>
        )}
      </section>
      <section className="operations-card">
        <h3>{t("rjv.expensesSection")}</h3>
        {report.expenses?.length ? (
          <ul className="operations-data-list">
            {report.expenses.map((item) => (
              <li key={item.id}>
                <span>
                  <strong>{item.label}</strong>
                  <small>{item.reference || item.comment || "—"}</small>
                </span>
                <b>{money(item.amount)}</b>
              </li>
            ))}
          </ul>
        ) : (
          <p className="operations-muted">{t("expenses.empty")}</p>
        )}
      </section>
    </div>
  );
}
