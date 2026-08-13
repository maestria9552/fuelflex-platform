package com.fuelflex.platform.stationproductprice.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StationProductPriceUpdateRequest {
    @NotNull(message = "Le prix est obligatoire.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être strictement supérieur à zéro.")
    @Digits(integer = 16, fraction = 3, message = "Le prix accepte au maximum 16 chiffres entiers et 3 décimales.")
    private BigDecimal price;

    private Boolean active;
}
