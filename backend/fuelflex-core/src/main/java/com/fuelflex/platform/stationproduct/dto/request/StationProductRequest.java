package com.fuelflex.platform.stationproduct.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StationProductRequest {
    @NotNull(message = "L’identifiant du produit est obligatoire.")
    private UUID productId;

    @Min(value = 1, message = "L’ordre d’affichage doit être supérieur ou égal à 1.")
    private Integer displayOrder;

    private Boolean active;
}
