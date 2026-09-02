import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const read = (path) => readFile(new URL(path, import.meta.url), 'utf8');

test('Manager sidebar and routes mirror endpoint permissions', async () => {
  const app = await read('./App.jsx');
  const sidebar = await read('./components/layout/ManagerSidebar.jsx');
  for (const permission of ['order:view', 'reception:view', 'operational-day:view', 'pos-sale:view', 'pump-attendant-validation:view', 'report:view']) {
    assert.match(app, new RegExp(permission));
    assert.match(sidebar, new RegExp(permission));
  }
  assert.match(sidebar, /item\.permissions\.every\(hasPermission\)/);
});

test('Manager notifications keep purchase orders inside the Manager routes', async () => {
  const notifications = await read('./features/notification/components/NotificationsPopover.jsx');
  assert.match(notifications, /navigate\(prefix \+ "\/commandes\/" \+ notification\.resourceId\)/);
  assert.doesNotMatch(notifications, /navigate\("\/superviseur\/commandes\/"/);
});

test('the access denied redirect has a concrete route and page', async () => {
  const app = await read('./App.jsx');
  const page = await read('./pages/AccessDeniedPage.jsx');
  assert.match(app, /path="\/acces-refuse" element=\{<AccessDeniedPage/);
  assert.match(page, /protectedRoute\.forbiddenTitle/);
});
