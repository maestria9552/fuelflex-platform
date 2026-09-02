import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const internalSource = fs.readFileSync(
  new URL("./InternalConsumptionModal.jsx", import.meta.url),
  "utf8",
);
const returnSource = fs.readFileSync(
  new URL("./TankReturnModal.jsx", import.meta.url),
  "utf8",
);
const detailSource = fs.readFileSync(
  new URL("./OperationalDayDetail.jsx", import.meta.url),
  "utf8",
);

const includesAll = (source, values) => {
  values.forEach((value) =>
    assert.ok(source.includes(value), `missing: ${value}`),
  );
};

test("INTERNAL keeps assignment context and existing fields", () => {
  includesAll(internalSource, [
    "headerIcon={Fuel}",
    'isOpen={Boolean(shift)}',
    'operations:fields.pumpAttendant',
    'operations:fields.pump',
    'operations:fields.fuelMeter',
    'operations:fields.product',
    'operations:fields.sourceTank',
    'operations:fields.businessDate',
    'value={form.quantity}',
    'value={form.usageBeneficiary}',
    'value={form.observation}',
  ]);
});

test("INTERNAL preserves validation, callback and exact payload", () => {
  includesAll(internalSource, [
    "Number(form.quantity) > 0",
    "form.usageBeneficiary.trim()",
    "valid && !isLoading",
    "onSubmit({ quantity: form.quantity",
    "usageBeneficiary: form.usageBeneficiary.trim()",
    "observation: form.observation.trim() || null",
  ]);
  includesAll(detailSource, [
    "createInternalConsumption(selectedShift.id, payload)",
    '"operations:feedback.internalAdded"',
  ]);
});

test("tank return derives source/product and only selects compatible destinations", () => {
  includesAll(returnSource, [
    "headerIcon={Droplets}",
    'isOpen={Boolean(shift)}',
    "tank.productId === shift?.productId",
    'operations:fields.product',
    'operations:fields.sourceTank',
    'operations:fields.destinationTank',
    "compatible.map((tank)",
  ]);
});

test("tank return preserves fields, validation and exact payload conversion", () => {
  includesAll(returnSource, [
    'value={form.tankId}',
    'value={form.quantity}',
    'value={form.occurredAt}',
    'value={form.reason}',
    "form.tankId && Number(form.quantity) > 0 && form.occurredAt",
    "if (!valid || isLoading) return;",
    "onSubmit({ ...form, reason: form.reason.trim() || null",
    "occurredAt: new Date(form.occurredAt).toISOString()",
  ]);
  includesAll(detailSource, [
    "createTankReturn(selectedShift.id, payload)",
    '"operations:feedback.tankReturnAdded"',
  ]);
});

test("both business forms use guarded AppModal and red/blue actions", () => {
  for (const source of [internalSource, returnSource]) {
    includesAll(source, [
      "isLoading={isLoading}",
      "closeOnEscape={!isLoading}",
      "closeOnOverlay={!isLoading}",
      "app-modal-action-no",
      "operations-manager-form-action",
      "disabled={isLoading || !valid}",
      'onClick={onClose}',
    ]);
    assert.doesNotMatch(source, /app-modal-action-ok|ops-button-primary/);
  }
});
