package com.fuelflex.platform.product.dto.request;

import java.util.UUID;

import com.fuelflex.platform.product.entity.ProductUnit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

    @NotNull(message = "La catégorie est obligatoire.")
    private UUID categoryId;

    @NotBlank(message = "Le code est obligatoire.")
    @Size(max = 30, message = "Le code ne peut pas dépasser 30 caractères.")
    private String code;

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères.")
    private String name;

    @Size(max = 80, message = "Le nom court ne peut pas dépasser 80 caractères.")
    private String shortName;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères.")
    private String description;

    @NotNull(message = "L'unité est obligatoire.")
    private ProductUnit unit;

    @Size(max = 20, message = "Le code-barres ne peut pas dépasser 20 caractères.")
    private String barcode;

    @Size(max = 20, message = "La couleur ne peut pas dépasser 20 caractères.")
    private String color;

    private Integer displayOrder;

    private Boolean active;
}