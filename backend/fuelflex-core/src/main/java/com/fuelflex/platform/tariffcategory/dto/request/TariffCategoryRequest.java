package com.fuelflex.platform.tariffcategory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TariffCategoryRequest {
    @NotBlank(message = "Le code de la catégorie tarifaire est obligatoire.")
    @Size(max = 50, message = "Le code ne peut pas dépasser 50 caractères.")
    private String code;

    @NotBlank(message = "Le nom de la catégorie tarifaire est obligatoire.")
    @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères.")
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères.")
    private String description;

    @Min(value = 1, message = "L’ordre d’affichage doit être supérieur ou égal à 1.")
    private Integer displayOrder;

    private Boolean active;
}
