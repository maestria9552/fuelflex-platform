import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import {
  isPreparablePumpAttendant,
  onlyPreparablePumpAttendants,
  PREPARABLE_PUMP_ATTENDANT_STATUS,
} from './pumpAttendantPreparation.js';

const read = (path) => readFile(new URL(path, import.meta.url), 'utf8');

test('Manager Pump Attendants exposes compact cards and visible row selection', async () => {
  const page = await read('./ManagerPumpAttendantsPage.jsx');
  assert.match(page, /validation-page validation-manager-page/);
  assert.match(page, /validation-candidates-panel/);
  assert.match(page, /validation-requests-panel/);
  assert.match(page, /validation-count/);
  assert.match(page, /className=\{selectedIds\.includes\(candidate\.id\) \? "is-selected"/);
  assert.doesNotMatch(page, /manager\.eyebrow/);
});

test('Manager Pump Attendants uses FuelFlex fields, actions and responsive layout', async () => {
  const css = await read('./PumpAttendantValidation.css');
  assert.match(css, /\.validation-manager-page \.validation-search input[\s\S]*?background:\s*#fff/);
  assert.match(css, /\.validation-manager-page \.validation-primary[\s\S]*?var\(--ff-primary\)/);
  assert.match(css, /\.validation-manager-page \.validation-document-builder[\s\S]*?var\(--ff-primary-soft\)/);
  assert.match(css, /\.validation-manager-page \.validation-table-wrap tbody tr\.is-selected/);
  assert.match(css, /@media \(max-width: 760px\)[\s\S]*?\.validation-manager-page \.validation-toolbar/);
});

test('the preparation list keeps PREPARATION and excludes validated attendants', () => {
  const prepared = { id: 'prepared', validationStatus: 'PREPARATION' };
  const validated = { id: 'validated', validationStatus: 'VALIDATED' };
  assert.equal(PREPARABLE_PUMP_ATTENDANT_STATUS, 'PREPARATION');
  assert.equal(isPreparablePumpAttendant(prepared), true);
  assert.equal(isPreparablePumpAttendant(validated), false);
  assert.deepEqual(
    onlyPreparablePumpAttendants({ content: [prepared, validated], totalElements: 2 }).content,
    [prepared],
  );
  assert.deepEqual(
    onlyPreparablePumpAttendants({ content: [validated], totalElements: 1 }).content,
    [],
  );
});

test('selection and checkbox styles remain immediately visible and accessible', async () => {
  const page = await read('./ManagerPumpAttendantsPage.jsx');
  const css = await read('./PumpAttendantValidation.css');
  assert.match(page, /selectedCount/);
  assert.match(page, /validation-state empty[\s\S]*?noCandidatesDescription/);
  assert.match(css, /input\[type="checkbox"\][\s\S]*?appearance:\s*none/);
  assert.match(css, /input\[type="checkbox"\]:checked[\s\S]*?var\(--ff-primary\)/);
  assert.match(css, /input\[type="checkbox"\]:focus-visible/);
  assert.match(css, /tbody tr\.is-selected td[\s\S]*?var\(--ff-primary-soft\)/);
});
