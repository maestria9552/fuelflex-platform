package com.fuelflex.platform.station.dto.request;

import com.fuelflex.platform.station.entity.StationStatus;
import com.fuelflex.platform.station.entity.StationType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StationRequest {

    @NotBlank(message = "Le code de la station est obligatoire.")
    @Size(
            max = 50,
            message = "Le code de la station ne peut pas dépasser 50 caractères."
    )
    private String code;

    @NotBlank(message = "Le nom de la station est obligatoire.")
    @Size(
            max = 150,
            message = "Le nom de la station ne peut pas dépasser 150 caractères."
    )
    private String name;

    @Size(
            max = 100,
            message = "Le nom court ne peut pas dépasser 100 caractères."
    )
    private String shortName;

    @NotNull(message = "Le type de station est obligatoire.")
    private StationType type;

    private StationStatus status;

    @Size(
            max = 255,
            message = "L’adresse ne peut pas dépasser 255 caractères."
    )
    private String address;

    @Size(
            max = 100,
            message = "La ville ne peut pas dépasser 100 caractères."
    )
    private String city;

    @Size(
            max = 100,
            message = "La province ne peut pas dépasser 100 caractères."
    )
    private String province;

    @Size(
            max = 100,
            message = "Le pays ne peut pas dépasser 100 caractères."
    )
    private String country;

    @Size(
            max = 30,
            message = "Le numéro de téléphone ne peut pas dépasser 30 caractères."
    )
    private String phoneNumber;

    @Email(message = "L’adresse e-mail de la station est invalide.")
    @Size(
            max = 150,
            message = "L’adresse e-mail ne peut pas dépasser 150 caractères."
    )
    private String email;

    @Size(
            max = 30,
            message = "La latitude ne peut pas dépasser 30 caractères."
    )
    private String latitude;

    @Size(
            max = 30,
            message = "La longitude ne peut pas dépasser 30 caractères."
    )
    private String longitude;

    @Min(
            value = 1,
            message = "L’ordre d’affichage doit être supérieur ou égal à 1."
    )
    private Integer displayOrder;

    private Boolean active;
}