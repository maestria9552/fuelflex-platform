import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const readStyle = (relativePath) => readFile(new URL(relativePath, import.meta.url), 'utf8');

test('the global FuelFlex primary palette matches the Orders reference', async () => {
  const css = await readStyle('./index.css');
  assert.match(css, /--ff-primary:\s*#2563eb\s*;/i);
  assert.match(css, /--ff-primary-hover:\s*#1d4ed8\s*;/i);
  assert.match(css, /--ff-primary-soft:\s*#eff6ff\s*;/i);
  assert.match(css, /--ff-primary-border:\s*#dbeafe\s*;/i);
});

test('Manager modules consume the same FuelFlex primary token', async () => {
  const styles = await Promise.all([
    './pages/dashboards/ManagerDashboardPage.css',
    './pages/orders/ManagerOrders.css',
    './pages/receptions/ManagerReceptions.css',
    './pages/receptions/ManagerReceptionForm.css',
    './pages/operations/Operations.css',
    './pages/sales/Sales.css',
    './pages/employee-validation/PumpAttendantValidation.css',
    './pages/reports/Reports.css',
  ].map(readStyle));
  styles.forEach((css) => assert.match(css, /var\(--ff-primary\)/));
});

test('legacy module theme colors no longer drive standard Manager actions', async () => {
  const receptions = await readStyle('./pages/receptions/ManagerReceptions.css');
  const receptionForm = await readStyle('./pages/receptions/ManagerReceptionForm.css');
  const sales = await readStyle('./pages/sales/Sales.css');
  const attendants = await readStyle('./pages/employee-validation/PumpAttendantValidation.css');
  const reports = await readStyle('./pages/reports/Reports.css');

  assert.doesNotMatch(`${receptions}\n${receptionForm}`, /#(?:0891b2|0f4c81|67d5df|e6f7fb)\b/i);
  assert.doesNotMatch(sales, /#(?:de761c|a9510b)\b/i);
  assert.doesNotMatch(attendants, /#(?:087c5b|056d50|138a64|168362|187458)\b/i);
  assert.doesNotMatch(reports, /#0c7894\b/i);
});

test('standard Manager form focus and modal actions use the primary palette', async () => {
  const styles = await Promise.all([
    './pages/operations/Operations.css',
    './pages/sales/Sales.css',
    './pages/employee-validation/PumpAttendantValidation.css',
    './pages/reports/Reports.css',
  ].map(readStyle));
  assert.match(styles[0], /\.operations-manager-form-action[\s\S]*?var\(--ff-primary\)/);
  styles.forEach((css) => assert.match(css, /:focus[\s\S]*?var\(--ff-primary\)/));
});
