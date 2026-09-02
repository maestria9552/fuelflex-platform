import { DEFAULT_LANGUAGE, resolveLanguage } from "./language.js";

const LOCALES_BY_LANGUAGE = Object.freeze({
  fr: "fr-CD",
  en: "en",
});

// Normalize French grouping characters so separators remain visible in the UI.
function normalizeFrenchSpacing(value) {
  return String(value).replace(/[\u00a0\u202f]/g, " ");
}

function isLocaleSupported(locale) {
  try {
    return Intl.NumberFormat.supportedLocalesOf([locale]).length > 0;
  } catch {
    return false;
  }
}

export function getLocaleForLanguage(language) {
  const requestedLocale = LOCALES_BY_LANGUAGE[resolveLanguage(language)];
  const fallbackLocale = LOCALES_BY_LANGUAGE[DEFAULT_LANGUAGE];

  if (isLocaleSupported(requestedLocale)) {
    return requestedLocale;
  }

  return isLocaleSupported(fallbackLocale) ? fallbackLocale : DEFAULT_LANGUAGE;
}

export function formatNumber(value, options = {}) {
  const { language = DEFAULT_LANGUAGE, ...formatOptions } = options;

  return new Intl.NumberFormat(
    getLocaleForLanguage(language),
    formatOptions,
  ).format(value);
}

export function formatCurrency(value, currency, options = {}) {
  const {
    language = DEFAULT_LANGUAGE,
    minimumFractionDigits = 0,
    maximumFractionDigits = 3,
    ...formatOptions
  } = options;

  if (resolveLanguage(language) === "fr") {
    const amount = formatNumber(value, {
      language,
      minimumFractionDigits,
      maximumFractionDigits,
      ...formatOptions,
      useGrouping: true,
    });
    return (
      normalizeFrenchSpacing(amount) +
      " " +
      String(currency || "").toUpperCase()
    ).trim();
  }

  return formatNumber(value, {
    language,
    style: "currency",
    currency,
    currencyDisplay: "code",
    minimumFractionDigits,
    maximumFractionDigits,
    ...formatOptions,
    useGrouping: true,
  });
}

export function formatVolume(value, options = {}) {
  const {
    language = DEFAULT_LANGUAGE,
    unit = "L",
    minimumFractionDigits = 0,
    maximumFractionDigits = 2,
    ...formatOptions
  } = options;
  const amount = formatNumber(value, {
    language,
    minimumFractionDigits,
    maximumFractionDigits,
    ...formatOptions,
    useGrouping: true,
  });
  return normalizeFrenchSpacing(amount) + " " + unit;
}

export function formatDate(value, options = {}) {
  const { language = DEFAULT_LANGUAGE, ...formatOptions } = options;

  return new Intl.DateTimeFormat(
    getLocaleForLanguage(language),
    formatOptions,
  ).format(new Date(value));
}

export function formatDateTime(value, options = {}) {
  const { language = DEFAULT_LANGUAGE, ...formatOptions } = options;

  return new Intl.DateTimeFormat(getLocaleForLanguage(language), {
    dateStyle: "medium",
    timeStyle: "short",
    ...formatOptions,
  }).format(new Date(value));
}
