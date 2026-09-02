import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";

import {
  isOperationalDayActivity,
  notificationActivityTimestamp,
  notificationTarget,
  operationalDayActivityTitleKey,
  operationalDayActivityTranslationKey,
} from "./operationalDayNotification.js";

const activity = {
  eventType: "OPERATIONAL_DAY_ACTIVITY",
  resourceType: "OPERATIONAL_DAY",
  resourceId: "day-123",
  activityCount: 8,
  lastActivityType: "TANK_RETURN_RECORDED",
  createdAt: "2026-09-02T08:00:00Z",
  updatedAt: "2026-09-02T09:00:00Z",
};

test("recognizes an operational-day aggregate and navigates to Supervisor detail", () => {
  assert.equal(isOperationalDayActivity(activity), true);
  assert.equal(notificationTarget(activity, true), "/superviseur/operations/day-123");
  assert.equal(notificationTarget(activity, false), "/gerant/operations/day-123");
  assert.equal(notificationActivityTimestamp(activity), activity.updatedAt);
});

test("uses the closing title and keeps ordinary notifications compatible", () => {
  assert.equal(
    operationalDayActivityTitleKey({ ...activity, lastActivityType: "OPERATIONAL_DAY_CLOSED" }),
    "notifications:operationalDayActivity.closedTitle",
  );
  assert.equal(isOperationalDayActivity({ eventType: "POS_SALE_REVERSED" }), false);
  assert.equal(notificationTarget({ eventType: "POS_SALE_REVERSED" }, true), null);
});

test("exposes every activity translation in French and English with plural forms", () => {
  const expected = [
    "OPERATIONAL_DAY_OPENED", "SHIFT_ASSIGNMENT_OPENED", "DAILY_EXPENSE_RECORDED",
    "TANK_GAUGE_RECORDED", "INTERNAL_CONSUMPTION_RECORDED", "TANK_RETURN_RECORDED",
    "SHIFT_ASSIGNMENT_CLOSED", "OPERATIONAL_DAY_CLOSED",
  ];
  for (const locale of ["fr", "en"]) {
    const translations = JSON.parse(readFileSync(
      new URL(`../../../i18n/locales/${locale}/notifications.json`, import.meta.url),
      "utf8",
    )).operationalDayActivity;
    assert.ok(translations.activityCount_one);
    assert.ok(translations.activityCount_other);
    expected.forEach((type) => assert.ok(translations.activities[type], `${locale}: ${type}`));
  }
  assert.equal(
    operationalDayActivityTranslationKey(activity),
    "notifications:operationalDayActivity.activities.TANK_RETURN_RECORDED",
  );
});
