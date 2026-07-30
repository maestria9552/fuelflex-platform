package com.fuelflex.platform.common.util;

public final class TextNormalizer {

    private TextNormalizer() {
        throw new IllegalStateException(
                "Cette classe utilitaire ne peut pas être instanciée."
        );
    }

    public static String normalizeCode(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    public static String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim()
                .replaceAll("\\s+", " ");
    }

    public static String normalizeNullableText(
            String value
    ) {
        String normalizedValue = normalizeText(value);

        if (
                normalizedValue == null
                        || normalizedValue.isBlank()
        ) {
            return null;
        }

        return normalizedValue;
    }
}