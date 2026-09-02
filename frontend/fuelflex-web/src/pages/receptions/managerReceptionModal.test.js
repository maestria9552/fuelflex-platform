import assert from "node:assert/strict";
import test from "node:test";
import fs from "node:fs";

const source = fs.readFileSync(
  new URL("./ManagerReceptionFormPage.jsx", import.meta.url),
  "utf8",
);
const has = (value) => assert.ok(source.includes(value), "missing: " + value);

test("normal reception submission uses the shared Attention modal", () => {
  has('import ConfirmationModal');
  has('title={t("common:modal.attention"');
  has('cancelLabel={t("common:actions.no"');
  has('confirmLabel={t("common:actions.ok"');
  has('onConfirm={submit}');
  has('t("receptions:modal.normalTitle")');
  has('t("receptions:modal.normalText")');
});

test("over-delivery keeps the summary and required justification", () => {
  has('modal==="over"');
  has('t("receptions:modal.summary")');
  has('t("receptions:modal.justification")');
  has('value={justification}');
  has('onChange={e=>setJustification(e.target.value)}');
  has('if(overages.length&&!justification.trim())return');
  has('submitManagerReception(reception.id,{comment:submitComment})');
});

test("closing and loading behavior remain guarded", () => {
  has('onClose={()=>!saving&&setModal(null)}');
  has('isLoading={saving}');
  has('setSaving(true)');
  has('finally{setSaving(false)}');
});
