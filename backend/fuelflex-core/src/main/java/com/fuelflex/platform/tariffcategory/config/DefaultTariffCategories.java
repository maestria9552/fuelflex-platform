package com.fuelflex.platform.tariffcategory.config;

import java.util.List;

public final class DefaultTariffCategories {
    public static final String CASH_CODE = "CASH";
    public static final String CREDIT_CODE = "CREDIT";
    public static final String INTERNAL_CODE = "INTERNAL";

    public static final List<Definition> ALL = List.of(
            new Definition(CASH_CODE, "Prix officiel / Vente cash",
                    "Prix de vente appliqué aux ventes ordinaires au comptant.", 1),
            new Definition(CREDIT_CODE, "Prix vente à crédit / Partenaires",
                    "Prix de vente appliqué aux ventes à crédit et aux partenaires.", 2),
            new Definition(INTERNAL_CODE, "Prix interne station",
                    "Prix de vente appliqué aux consommations internes de la station.", 3));

    private DefaultTariffCategories() {
        throw new IllegalStateException("Cette classe ne peut pas être instanciée.");
    }

    public record Definition(String code, String name, String description, int displayOrder) {}
}
