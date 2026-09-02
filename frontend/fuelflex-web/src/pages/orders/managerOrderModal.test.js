import assert from "node:assert/strict";
import test from "node:test";
import fs from "node:fs";

const source = fs.readFileSync(
  new URL("./ManagerOrderDetailPage.jsx", import.meta.url),
  "utf8",
);
const has = (value) => assert.ok(source.includes(value), "missing: " + value);

test("Manager DRAFT submission uses the Attention confirmation contract", () => {
  has('order.status === "DRAFT"');
  has("onClick={() => setConfirmOpen(true)}");
  has('title={t("common:modal.attention"');
  has('cancelLabel={t("common:actions.no"');
  has('confirmLabel={t("common:actions.ok"');
  has('<p>{t("orders:detail.submitConfirm")}</p>');
});

test("submission keeps the existing loading guard and submit callback", () => {
  has("setSubmitting(true)");
  has("submitManagerOrder(id)");
  has("isLoading={submitting}");
  has("onConfirm={submit}");
});
