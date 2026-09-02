import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";
import { fileURLToPath } from "node:url";
import React, { useState } from "react";
import { act, create } from "react-test-renderer";
import reactPlugin from "@vitejs/plugin-react";
import { createServer } from "vite";

const listSource = fs.readFileSync(
  new URL("./OperationalDaysList.jsx", import.meta.url),
  "utf8",
);
const detailSource = fs.readFileSync(
  new URL("./OperationalDayDetail.jsx", import.meta.url),
  "utf8",
);
const operationsCss = fs.readFileSync(
  new URL("../../../pages/operations/Operations.css", import.meta.url),
  "utf8",
);

const includesAll = (source, values) => {
  values.forEach((value) =>
    assert.ok(source.includes(value), `missing: ${value}`),
  );
};

test("opening a day keeps its form, callback and guarded standard actions", () => {
  includesAll(listSource, [
    'isOpen={openModal}',
    'headerIcon={CalendarDays}',
    'form="open-operational-day-form"',
    'openOperationalDay({ stationId, businessDate })',
    'if (submitting) return;',
    'isLoading={submitting}',
    'closeOnEscape={!submitting}',
    'closeOnOverlay={!submitting}',
    'app-modal-action-no',
    'operations-manager-form-action',
  ]);
});

test("pump-attendant assignment keeps both selections and its exact payload", () => {
  includesAll(detailSource, [
    'isOpen={modal === "assignment"}',
    'headerIcon={UserPlus}',
    'value={assignmentForm.pumpAttendantId}',
    'value={assignmentForm.fuelMeterId}',
    'decimal(item.currentIndex, locale)',
    'createShiftAssignment(id, assignmentForm)',
    'form="assignment-form"',
    'isLoading={submitting}',
  ]);
});

test("expense keeps its fields, validation and nullable payload fields", () => {
  includesAll(detailSource, [
    'isOpen={modal === "expense"}',
    'headerIcon={ReceiptText}',
    'value={expenseForm.label}',
    'value={expenseForm.amount}',
    'value={expenseForm.reference}',
    'value={expenseForm.comment}',
    '!expenseForm.label || !expenseForm.amount',
    'createDailyExpense(id, {',
    'reference: expenseForm.reference || null',
    'comment: expenseForm.comment || null',
  ]);
});

test("expense fields retain complete values during real controlled input", async () => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  const server = await createServer({
    configFile: false,
    root: fileURLToPath(new URL("../../../../", import.meta.url)),
    plugins: [reactPlugin()],
    server: { middlewareMode: true, hmr: false },
    appType: "custom",
  });
  try {
    const { ExpenseFormFields } = await server.ssrLoadModule(
      "/src/features/operations/components/OperationalDayDetail.jsx",
    );
    let currentForm;
    function Harness() {
      const [form, setForm] = useState({
        label: "",
        amount: "",
        reference: "",
        comment: "",
      });
      currentForm = form;
      return React.createElement(ExpenseFormFields, {
        expenseForm: form,
        setExpenseForm: setForm,
        currency: "USD",
        formError: "",
        t: (key) => key,
      });
    }

    let renderer;
    await act(async () => {
      renderer = create(React.createElement(Harness));
    });
    const inputs = renderer.root.findAllByType("input");
    const comment = renderer.root.findByType("textarea");
    await act(async () => {
      inputs[0].props.onChange({
        target: { value: "Transport technicien" },
      });
      inputs[1].props.onChange({ target: { value: "25" } });
      inputs[2].props.onChange({ target: { value: "REF-001" } });
      comment.props.onChange({
        target: { value: "Déplacement terrain" },
      });
    });

    assert.deepEqual(currentForm, {
      label: "Transport technicien",
      amount: "25",
      reference: "REF-001",
      comment: "Déplacement terrain",
    });
    includesAll(detailSource, [
      "createDailyExpense(id, {",
      "...expenseForm",
      "reference: expenseForm.reference || null",
      "comment: expenseForm.comment || null",
    ]);
    await act(async () => renderer.unmount());
  } finally {
    await server.close();
  }
});

test("gauge keeps theoretical stock read-only, physical input and variance formula", () => {
  includesAll(detailSource, [
    'isOpen={modal === "gauge"}',
    'headerIcon={Gauge}',
    'selectedGaugeStock.currentStock',
    'value={gaugeForm.physicalStock}',
    'Number(gaugeForm.physicalStock) -',
    'Number(selectedGaugeStock.currentStock || 0)',
    'value={gaugeForm.comment}',
    'createTankGaugeReading(id, {',
    'tankId: gaugeForm.tankId',
    'physicalStock: gaugeForm.physicalStock',
    'comment: gaugeForm.comment || null',
  ]);
  assert.doesNotMatch(detailSource, /currentStock\s*:/);
});

test("the four forms use FuelFlex focus, red cancel and blue business actions", () => {
  assert.match(operationsCss, /border-color:\s*var\(--ff-primary\)/);
  assert.match(operationsCss, /outline:\s*3px solid var\(--ff-primary-soft\)/);
  assert.match(operationsCss, /background:\s*var\(--ff-primary\)/);
  assert.match(operationsCss, /background:\s*var\(--ff-primary-hover\)/);
  assert.equal(
    (listSource.match(/app-modal-action-no/g) || []).length +
      (detailSource.match(/app-modal-action-no/g) || []).length,
    4,
  );
  assert.equal(
    (listSource.match(/operations-manager-form-action/g) || []).length +
      (detailSource.match(/operations-manager-form-action/g) || []).length,
    8,
  );
  assert.doesNotMatch(listSource, /app-modal-action-ok/);
  assert.doesNotMatch(detailSource, /app-modal-action-ok/);
});

test("Operations standard identity accents no longer use the legacy orange", () => {
  includesAll(operationsCss, [
    ".operations-eyebrow",
    ".operations-row-link:hover",
    ".operations-current-day:hover",
    ".operations-empty svg",
    "color: var(--ff-primary)",
    "color: var(--ff-primary-hover)",
  ]);
  assert.doesNotMatch(operationsCss, /#de761c|#c96513|#b8580a|#91490f/i);
});
