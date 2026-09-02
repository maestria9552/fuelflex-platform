import assert from "node:assert/strict";
import test from "node:test";
import { formatCurrency, formatVolume } from "./formatters.js";

const visible = (value) => String(value).replace(/[\u00a0\u202f]/g, " ");
const money = (value) =>
  visible(
    formatCurrency(value, "CDF", {
      language: "fr",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }),
  );

test("formats French currency with visible thousands groups", () => {
  assert.equal(money(1500236), "1 500 236,00 CDF");
  assert.equal(money(18497150.78), "18 497 150,78 CDF");
  assert.equal(money(570400), "570 400,00 CDF");
});

test("formats French volumes with visible thousands groups", () => {
  assert.equal(
    visible(
      formatVolume(15429.53, {
        language: "fr",
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }),
    ),
    "15 429,53 L",
  );
});
