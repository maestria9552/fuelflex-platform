package com.fuelflex.platform.tank.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.fuelflex.platform.tank.entity.TankStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TankRequest {

    @NotNull(message = "Le produit de la citerne est obligatoire.")
    private UUID productId;

    @NotBlank(message = "Le code de la citerne est obligatoire.")
    @Size(
            max = 50,
            message = "Le code de la citerne ne peut pas dépasser 50 caractères."
    )
    private String code;

    @NotBlank(message = "Le nom de la citerne est obligatoire.")
    @Size(
            max = 150,
            message = "Le nom de la citerne ne peut pas dépasser 150 caractères."
    )
    private String name;

    @NotNull(message = "La capacité de la citerne est obligatoire.")
    @DecimalMin(
            value = "0.001",
            message = "La capacité de la citerne doit être supérieure à zéro."
    )
    @Digits(
            integer = 16,
            fraction = 3,
            message = "La capacité doit contenir au maximum 16 chiffres entiers et 3 décimales."
    )
    private BigDecimal capacityLiters;

    @DecimalMin(
            value = "0.000",
            inclusive = true,
            message = "Le niveau minimal ne peut pas être négatif."
    )
    @Digits(
            integer = 16,
            fraction = 3,
            message = "Le niveau minimal doit contenir au maximum 16 chiffres entiers et 3 décimales."
    )
    private BigDecimal minimumLevelLiters;

    @DecimalMin(
            value = "0.001",
            message = "Le niveau maximal doit être supérieur à zéro."
    )
    @Digits(
            integer = 16,
            fraction = 3,
            message = "Le niveau maximal doit contenir au maximum 16 chiffres entiers et 3 décimales."
    )
    private BigDecimal maximumLevelLiters;

    private TankStatus status;

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