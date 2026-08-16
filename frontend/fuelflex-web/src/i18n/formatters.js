import { DEFAULT_LANGUAGE, resolveLanguage } from "./language.js";

const LOCALES_BY_LANGUAGE = Object.freeze({
  fr: "fr-CD",
  en: "en",
});

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

  return isLocaleSupported(fallbackLocale)
    ? fallbackLocale
    : DEFAULT_LANGUAGE;
}

export function formatNumber(value, options = {}) {
  const { language = DEFAULT_LANGUAGE, ...formatOptions } = options;

  return new Intl.NumberFormat(
    getLocaleForLanguage(language),
    formatOptions
  ).format(value);
}

export function formatDate(value, options = {}) {
  const { language = DEFAULT_LANGUAGE, ...formatOptions } = options;

  return new Intl.DateTimeFormat(
    getLocaleForLanguage(language),
    formatOptions
  ).format(new Date(value));
}

export function formatDateTime(value, options = {}) {
  const { language = DEFAULT_LANGUAGE, ...formatOptions } = options;

  return new Intl.DateTimeFormat(
    getLocaleForLanguage(language),
    {
      dateStyle: "medium",
      timeStyle: "short",
      ...formatOptions,
    }
  ).format(new Date(value));
}
