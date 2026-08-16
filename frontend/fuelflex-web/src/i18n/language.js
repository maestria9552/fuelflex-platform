export const DEFAULT_LANGUAGE = "fr";
export const LANGUAGE_STORAGE_KEY = "fuelflex_language";

export const SUPPORTED_LANGUAGES = Object.freeze([
  Object.freeze({ code: "fr", label: "Français" }),
  Object.freeze({ code: "en", label: "English" }),
]);

const supportedLanguageCodes = new Set(
  SUPPORTED_LANGUAGES.map(({ code }) => code)
);

function getBaseLanguage(languageCode) {
  if (typeof languageCode !== "string") {
    return null;
  }

  return languageCode.trim().toLowerCase().split(/[-_]/)[0] || null;
}

export function resolveLanguage(languageCode) {
  const baseLanguage = getBaseLanguage(languageCode);

  return supportedLanguageCodes.has(baseLanguage)
    ? baseLanguage
    : DEFAULT_LANGUAGE;
}

function getStoredLanguage() {
  try {
    return window.localStorage.getItem(LANGUAGE_STORAGE_KEY);
  } catch {
    return null;
  }
}

function getBrowserLanguages() {
  if (typeof navigator === "undefined") {
    return [];
  }

  return Array.isArray(navigator.languages) && navigator.languages.length > 0
    ? navigator.languages
    : [navigator.language];
}

export function resolveInitialLanguage() {
  if (typeof window !== "undefined") {
    const storedLanguage = getStoredLanguage();

    if (storedLanguage) {
      return storeLanguage(storedLanguage);
    }
  }

  const supportedBrowserLanguage = getBrowserLanguages().find(
    (language) => supportedLanguageCodes.has(getBaseLanguage(language))
  );

  return supportedBrowserLanguage
    ? resolveLanguage(supportedBrowserLanguage)
    : DEFAULT_LANGUAGE;
}

export function storeLanguage(languageCode) {
  const language = resolveLanguage(languageCode);

  try {
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, language);
  } catch {
    // La langue reste active en mémoire si le stockage est indisponible.
  }

  return language;
}
