package com.fuelflex.platform.dispensingpoint.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fuelflex.platform.dispensingpoint.entity.DispensingPointStatus;
import com.fuelflex.platform.dispensingpoint.entity.MeteringMode;

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
public class DispensingPointResponse {

    private UUID id;

    private UUID pumpId;

    private String pumpCode;

    private String pumpName;

    private UUID tankId;

    private String tankCode;

    private String tankName;

    private String code;

    private String name;

    private Integer nozzleNumber;

    private MeteringMode meteringMode;

    private BigDecimal currentIndex;

    private DispensingPointStatus status;

    private Integer displayOrder;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}