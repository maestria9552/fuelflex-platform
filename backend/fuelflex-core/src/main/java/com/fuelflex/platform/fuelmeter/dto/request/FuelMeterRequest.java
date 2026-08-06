package com.fuelflex.platform.fuelmeter.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.fuelflex.platform.fuelmeter.entity.FuelMeterStatus;
import com.fuelflex.platform.fuelmeter.entity.MeterTechnology;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuelMeterRequest {

    private UUID pumpId;

    private UUID dispensingPointId;

    @NotBlank(message = "Le code du compteur est obligatoire.")
    @Size(
            max = 50,
            message = "Le code du compteur ne peut pas dépasser 50 caractères."
    )
    private String code;

    @NotBlank(message = "Le nom du compteur est obligatoire.")
    @Size(
            max = 150,
            message = "Le nom du compteur ne peut pas dépasser 150 caractères."
    )
    private String name;

    @NotNull(message = "La technologie du compteur est obligatoire.")
    private MeterTechnology technology;

    @NotNull(message = "L’index actuel est obligatoire.")
    @DecimalMin(
            value = "0.000",
            inclusive = true,
            message = "L’index actuel ne peut pas être négatif."
    )
    @Digits(
            integer = 16,
            fraction = 3,
            message = "L’index actuel doit contenir au maximum 16 chiffres entiers et 3 décimales."
    )
    private BigDecimal currentIndex;

    private FuelMeterStatus status;

    @Positive(
            message = "L’ordre d’affichage doit être supérieur à zéro."
    )
    private Integer displayOrder;

    private Boolean active;
}
