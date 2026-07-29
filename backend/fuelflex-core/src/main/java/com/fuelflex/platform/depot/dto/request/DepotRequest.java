package com.fuelflex.platform.depot.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepotRequest {

    @NotBlank(message = "Le code du dépôt est obligatoire.")
    @Size(
            max = 50,
            message = "Le code du dépôt ne peut pas dépasser 50 caractères."
    )
    private String code;

    @NotBlank(message = "Le nom du dépôt est obligatoire.")
    @Size(
            max = 150,
            message = "Le nom du dépôt ne peut pas dépasser 150 caractères."
    )
    private String name;

    @Size(
            max = 255,
            message = "La description ne peut pas dépasser 255 caractères."
    )
    private String description;

    @Size(
            max = 255,
            message = "L’emplacement ne peut pas dépasser 255 caractères."
    )
    private String location;

    @Min(
            value = 1,
            message = "L’ordre d’affichage doit être supérieur ou égal à 1."
    )
    private Integer displayOrder;

    private Boolean active;
}