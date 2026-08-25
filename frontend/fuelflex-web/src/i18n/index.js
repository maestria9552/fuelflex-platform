import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import commonEn from "./locales/en/common.json";
import commonFr from "./locales/fr/common.json";
import accountEn from "./locales/en/account.json";
import accountFr from "./locales/fr/account.json";
import notificationsEn from "./locales/en/notifications.json";
import notificationsFr from "./locales/fr/notifications.json";
import dashboardEn from "./locales/en/dashboard.json";
import dashboardFr from "./locales/fr/dashboard.json";
import authEn from "./locales/en/auth.json";
import authFr from "./locales/fr/auth.json";
import navigationEn from "./locales/en/navigation.json";
import navigationFr from "./locales/fr/navigation.json";
import stationsEn from "./locales/en/stations.json";
import stationsFr from "./locales/fr/stations.json";
import productsEn from "./locales/en/products.json";
import productsFr from "./locales/fr/products.json";
import depotsEn from "./locales/en/depots.json";
import depotsFr from "./locales/fr/depots.json";
import tanksEn from "./locales/en/tanks.json";
import tanksFr from "./locales/fr/tanks.json";
import pumpsEn from "./locales/en/pumps.json";
import pumpsFr from "./locales/fr/pumps.json";
import dispensingPointsEn from "./locales/en/dispensingPoints.json";
import dispensingPointsFr from "./locales/fr/dispensingPoints.json";
import fuelMetersEn from "./locales/en/fuelMeters.json";
import fuelMetersFr from "./locales/fr/fuelMeters.json";
import homeEn from "./locales/en/home.json";
import homeFr from "./locales/fr/home.json";
import pricingEn from "./locales/en/pricing.json";
import pricingFr from "./locales/fr/pricing.json";
import profileEn from "./locales/en/profile.json";
import profileFr from "./locales/fr/profile.json";
import organizationEn from "./locales/en/organization.json";
import organizationFr from "./locales/fr/organization.json";
import stationSetupEn from "./locales/en/stationSetup.json";
import stationSetupFr from "./locales/fr/stationSetup.json";
import employeesEn from "./locales/en/employees.json";
import employeesFr from "./locales/fr/employees.json";
import managerDashboardEn from "./locales/en/managerDashboard.json";
import managerDashboardFr from "./locales/fr/managerDashboard.json";
import ordersEn from "./locales/en/orders.json";
import ordersFr from "./locales/fr/orders.json";
import receptionsEn from "./locales/en/receptions.json";
import receptionsFr from "./locales/fr/receptions.json";
import operationsEn from "./locales/en/operations.json";
import operationsFr from "./locales/fr/operations.json";
import salesEn from "./locales/en/sales.json";
import salesFr from "./locales/fr/sales.json";
import pumpAttendantValidationEn from "./locales/en/pumpAttendantValidation.json";
import pumpAttendantValidationFr from "./locales/fr/pumpAttendantValidation.json";
import {
  DEFAULT_LANGUAGE,
  resolveInitialLanguage,
  resolveLanguage,
  storeLanguage,
} from "./language.js";

const resources = {
  fr: {
    account: accountFr,
    auth: authFr,
    common: commonFr,
    dashboard: dashboardFr,
    depots: depotsFr,
    dispensingPoints: dispensingPointsFr,
    fuelMeters: fuelMetersFr,
    home: homeFr,
    navigation: navigationFr,
    notifications: notificationsFr,
    organization: organizationFr,
    pricing: pricingFr,
    profile: profileFr,
    products: productsFr,
    stations: stationsFr,
    stationSetup: stationSetupFr,
    employees: employeesFr,
    managerDashboard: managerDashboardFr,
    orders: ordersFr,
    receptions: receptionsFr,
    operations: operationsFr,
    sales: salesFr,
    pumpAttendantValidation: pumpAttendantValidationFr,
    pumps: pumpsFr,
    tanks: tanksFr,
  },
  en: {
    account: accountEn,
    auth: authEn,
    common: commonEn,
    dashboard: dashboardEn,
    depots: depotsEn,
    dispensingPoints: dispensingPointsEn,
    fuelMeters: fuelMetersEn,
    home: homeEn,
    navigation: navigationEn,
    notifications: notificationsEn,
    organization: organizationEn,
    pricing: pricingEn,
    profile: profileEn,
    products: productsEn,
    stations: stationsEn,
    stationSetup: stationSetupEn,
    employees: employeesEn,
    managerDashboard: managerDashboardEn,
    orders: ordersEn,
    receptions: receptionsEn,
    operations: operationsEn,
    sales: salesEn,
    pumpAttendantValidation: pumpAttendantValidationEn,
    pumps: pumpsEn,
    tanks: tanksEn,
  },
};

function synchronizeDocumentLanguage(language) {
  if (typeof document !== "undefined") {
    document.documentElement.lang = resolveLanguage(language);
  }
}

i18n.use(initReactI18next).init({
  resources,
  lng: resolveInitialLanguage(),
  fallbackLng: DEFAULT_LANGUAGE,
  defaultNS: "common",
  supportedLngs: Object.keys(resources),
  load: "languageOnly",
  interpolation: {
    escapeValue: false,
  },
});

i18n.on("languageChanged", synchronizeDocumentLanguage);
synchronizeDocumentLanguage(i18n.language);

export async function changeLanguage(languageCode) {
  const language = resolveLanguage(languageCode);

  await i18n.changeLanguage(language);
  storeLanguage(language);
  synchronizeDocumentLanguage(language);

  return language;
}

export default i18n;
