package com.fuelflex.platform.dispensingpoint.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.fuelflex.platform.dispensingpoint.entity.DispensingPointStatus;
import com.fuelflex.platform.dispensingpoint.entity.MeteringMode;

import jakarta.validation.constraints.DecimalMin;
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
public class DispensingPointRequest {

    @NotNull(
            message = "L’identifiant de la citerne est obligatoire."
    )
    private UUID tankId;

    @NotBlank(
            message = "Le code du point de distribution est obligatoire."
    )
    @Size(
            max = 50,
            message = "Le code du point de distribution ne peut pas dépasser 50 caractères."
    )
    private String code;

    @NotBlank(
            message = "Le nom du point de distribution est obligatoire."
    )
    @Size(
            max = 150,
            message = "Le nom du point de distribution ne peut pas dépasser 150 caractères."
    )
    private String name;

    @Positive(
            message = "Le numéro du pistolet doit être supérieur à zéro."
    )
    private Integer nozzleNumber;

    @NotNull(
            message = "Le mode de comptage est obligatoire."
    )
    private MeteringMode meteringMode;

    @NotNull(
            message = "L’index actuel est obligatoire."
    )
    @DecimalMin(
            value = "0.000",
            inclusive = true,
            message = "L’index actuel ne peut pas être négatif."
    )
    private BigDecimal currentIndex;

    private DispensingPointStatus status;

    @Positive(
            message = "L’ordre d’affichage doit être supérieur à zéro."
    )
    private Integer displayOrder;

    private Boolean active;
}