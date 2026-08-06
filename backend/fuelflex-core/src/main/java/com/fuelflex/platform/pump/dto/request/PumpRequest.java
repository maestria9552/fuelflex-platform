package com.fuelflex.platform.pump.dto.request;

import com.fuelflex.platform.pump.entity.MeteringLevel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PumpRequest {

    @NotBlank(message = "Le code de la pompe est obligatoire.")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Le nom de la pompe est obligatoire.")
    @Size(max = 150)
    private String name;

    @NotNull(message = "Le numéro de la pompe est obligatoire.")
    @Min(value = 1, message = "Le numéro de la pompe doit être supérieur à zéro.")
    private Integer pumpNumber;

    @NotNull(message = "Le niveau de comptage est obligatoire.")
    private MeteringLevel meteringLevel;

    @Size(max = 100)
    private String manufacturer;

    @Size(max = 100)
    private String model;

    @Size(max = 100)
    private String serialNumber;

    @Size(max = 255)
    private String location;

    private Integer displayOrder;

    private Boolean active;
}
