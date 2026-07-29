package com.fuelflex.platform.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductCategoryRequest {

    @NotBlank(message = "Le code de la catégorie est obligatoire.")
    @Size(
            max = 50,
            message = "Le code de la catégorie ne peut pas dépasser 50 caractères."
    )
    private String code;

    @NotBlank(message = "Le nom de la catégorie est obligatoire.")
    @Size(
            max = 120,
            message = "Le nom de la catégorie ne peut pas dépasser 120 caractères."
    )
    private String name;

    @Size(
            max = 500,
            message = "La description ne peut pas dépasser 500 caractères."
    )
    private String description;

    private Boolean active;
}