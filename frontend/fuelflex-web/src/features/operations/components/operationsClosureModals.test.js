import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";
import { fileURLToPath } from "node:url";
import React from "react";
import { act, create } from "react-test-renderer";
import reactPlugin from "@vitejs/plugin-react";
import { createServer } from "vite";

const shiftSource = fs.readFileSync(new URL("./ShiftClosureModal.jsx", import.meta.url), "utf8");
const daySource = fs.readFileSync(new URL("./DayClosureModal.jsx", import.meta.url), "utf8");
const detailSource = fs.readFileSync(new URL("./OperationalDayDetail.jsx", import.meta.url), "utf8");
const operationsCss = fs.readFileSync(new URL("../../../pages/operations/Operations.css", import.meta.url), "utf8");
const includesAll = (source, values) => values.forEach((value) => assert.ok(source.includes(value), `missing: ${value}`));

test("shift closure preserves its form and separate attention confirmation", () => {
  includesAll(shiftSource, ["headerIcon={Gauge}", 'value={closingIndex}', 'value={creditQuantity}', "returns.reduce", "internals.reduce", "setConfirming(true)", "<ConfirmationModal", 'variant="warning"', "onConfirm({ closingIndex, creditQuantity })"]);
});

test("shift volumetric formulas and incoherence validations remain unchanged", () => {
  includesAll(shiftSource, ["closing - opening", "metered - returned - internal", "sold - credit", "returned + internal > metered", "credit > sold", "cash < 0", '"meteredVolume"', '"tankReturnVolume"', '"internalConsumptionVolume"', '"cashVolume"', '"creditVolume"']);
});

test("shift form is blue/red while final confirmation stays green/red", () => {
  includesAll(shiftSource, ["isLoading={isLoading}", "closeOnEscape={!isLoading}", "closeOnOverlay={!isLoading}", "app-modal-action-no", "operations-manager-form-action", "!validation && !isLoading"]);
  assert.doesNotMatch(shiftSource, /ops-button-danger/);
});

test("day closure preserves blocking, dynamic currency and physical cash inputs", () => {
  includesAll(daySource, ["openAssignments.length > 0", "blocked || invalid || isLoading", "organization?.defaultCurrency", 'value={physicalReferenceAmount}', 'value={physicalUsdAmount}', 'value={usdExchangeRate}', "isReferenceUsd"]);
});

test("day cash formulas and semantic statuses remain unchanged", () => {
  includesAll(daySource, ["cashGross - expenses", "numeric(physicalUsdAmount) * numeric(usdExchangeRate)", "numeric(physicalReferenceAmount) + convertedUsd", "observed - cashNet", 'variance > 0 ? "EXCEDENT" : "DEFICIT"', 'Math.abs(variance) < 0.0005 ? "CONFORME"', "cash-status-${preview.status}"]);
  assert.doesNotMatch(daySource, /credit|internal/i);
});

test("day closure keeps its exact payload, callback and blue guarded form action", () => {
  includesAll(daySource, ["physicalReferenceAmount: numeric(physicalReferenceAmount).toFixed(3)", "physicalUsdAmount: numeric(physicalUsdAmount).toFixed(3)", "usdExchangeRate: numeric(usdExchangeRate).toFixed(6)", "isLoading={isLoading}", "closeOnEscape={!isLoading}", "closeOnOverlay={!isLoading}", "app-modal-action-no", "operations-manager-form-action"]);
  includesAll(detailSource, ["closeOperationalDay(id, payload)", '"operations:feedback.dayClosed"']);
  assert.match(operationsCss, /cash-closure-inputs input:focus[^}]*var\(--ff-primary\)/);
});

test("day closure physical cash inputs retain actual edited values", async () => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  const previousDocument = globalThis.document;
  const previousWindow = globalThis.window;
  globalThis.document = { body: { style: { overflow: "" } } };
  globalThis.window = {
    addEventListener() {},
    removeEventListener() {},
  };
  const server = await createServer({
    configFile: false,
    root: fileURLToPath(new URL("../../../../", import.meta.url)),
    plugins: [reactPlugin()],
    server: { middlewareMode: true, hmr: false },
    appType: "custom",
  });
  try {
    const { default: DayClosureModal } = await server.ssrLoadModule(
      "/src/features/operations/components/DayClosureModal.jsx",
    );
    let renderer;
    await act(async () => {
      renderer = create(
        React.createElement(DayClosureModal, {
          organization: { defaultCurrency: "CDF" },
          report: {
            cashGrossExpected: 100,
            disbursedExpenseAmount: 25,
            cashNetExpected: 75,
          },
          openAssignments: [],
          reconciliationsCount: 1,
          gaugesCount: 1,
          language: "fr",
          isLoading: false,
          errorMessage: "",
          onClose() {},
          onConfirm() {},
        }),
      );
    });
    let inputs = renderer.root.findAllByType("input");
    await act(async () => {
      inputs[0].props.onChange({ target: { value: "50000" } });
      inputs[1].props.onChange({ target: { value: "25" } });
      inputs[2].props.onChange({ target: { value: "2800" } });
    });
    inputs = renderer.root.findAllByType("input");
    assert.deepEqual(
      inputs.map((input) => input.props.value),
      ["50000", "25", "2800"],
    );
    await act(async () => renderer.unmount());
  } finally {
    await server.close();
    if (previousDocument === undefined) delete globalThis.document;
    else globalThis.document = previousDocument;
    if (previousWindow === undefined) delete globalThis.window;
    else globalThis.window = previousWindow;
  }
});

test("Operations fields force light editable, focus, disabled and readonly styles", () => {
  assert.match(
    operationsCss,
    /\.operations-page :is\(input, select, textarea\)[^{]*\{[^}]*background-color:\s*#fff[^}]*color-scheme:\s*light/s,
  );
  assert.match(
    operationsCss,
    /:is\(input, select, textarea\):disabled[^}]*background-color:\s*#f1f5f9/s,
  );
  assert.match(
    operationsCss,
    /:is\(input, select, textarea\)\[readonly\][^}]*background-color:\s*#f8fafc/s,
  );
});
