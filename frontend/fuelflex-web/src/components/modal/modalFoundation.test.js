import assert from "node:assert/strict";
import test from "node:test";
import fs from "node:fs";

const appModal = fs.readFileSync(new URL("./AppModal.jsx", import.meta.url), "utf8");
const confirmationModal = fs.readFileSync(new URL("./ConfirmationModal.jsx", import.meta.url), "utf8");
const appModalCss = fs.readFileSync(new URL("./AppModal.css", import.meta.url), "utf8");
const confirmationCss = fs.readFileSync(new URL("./ConfirmationModal.css", import.meta.url), "utf8");

test("AppModal exposes information, attention and form-compatible contracts", () => {
  assert.match(appModal, /modalType = "form"/);
  assert.match(appModal, /app-modal-action-no/);
  assert.match(appModal, /app-modal-action-ok/);
  assert.match(appModal, /aria-modal="true"/);
  assert.match(appModalCss, /background:#0f2747|background: #0f2747/);
});

test("AppModal does not steal field focus when an inline onClose changes", () => {
  assert.match(appModal, /useEffectEvent/);
  assert.match(appModal, /closeFromEffect\(\)/);
  assert.match(
    appModal,
    /\[isOpen, closeOnEscape, isLoading\]/,
  );
  assert.doesNotMatch(
    appModal,
    /\[isOpen, closeOnEscape, isLoading, onClose\]/,
  );
});

test("ConfirmationModal keeps compatible callbacks and standard actions", () => {
  assert.match(confirmationModal, /modalType="attention"/);
  assert.match(confirmationModal, /headerIcon={Icon}/);
  assert.match(confirmationModal, /onClick={safeClose}/);
  assert.match(confirmationModal, /onClick={safeConfirm}/);
  assert.match(confirmationCss, /background:#dc2626|background: #dc2626/);
  assert.match(confirmationCss, /background:#16a34a|background: #16a34a/);
});
