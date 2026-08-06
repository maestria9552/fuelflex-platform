package com.fuelflex.platform.dispensingpoint.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.dispensingpoint.dto.request.DispensingPointRequest;
import com.fuelflex.platform.dispensingpoint.dto.response.DispensingPointResponse;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint;

@Component
public class DispensingPointMapper {

    public DispensingPoint toEntity(
            DispensingPointRequest request
    ) {
        if (request == null) {
            return null;
        }

        return DispensingPoint.builder()
                .code(request.getCode())
                .name(request.getName())
                .nozzleNumber(request.getNozzleNumber())
                .status(request.getStatus())
                .displayOrder(request.getDisplayOrder())
                .active(
                        request.getActive() != null
                                && request.getActive()
                )
                .build();
    }

    public void updateEntity(
            DispensingPoint dispensingPoint,
            DispensingPointRequest request
    ) {
        if (
                dispensingPoint == null
                        || request == null
        ) {
            return;
        }

        dispensingPoint.setCode(
                request.getCode()
        );

        dispensingPoint.setName(
                request.getName()
        );

        dispensingPoint.setNozzleNumber(
                request.getNozzleNumber()
        );

        dispensingPoint.setStatus(
                request.getStatus()
        );

        dispensingPoint.setDisplayOrder(
                request.getDisplayOrder()
        );

        if (request.getActive() != null) {
            dispensingPoint.setActive(
                    request.getActive()
            );
        }
    }

    public DispensingPointResponse toResponse(
            DispensingPoint dispensingPoint
    ) {
        if (dispensingPoint == null) {
            return null;
        }

        return DispensingPointResponse.builder()
                .id(dispensingPoint.getId())
                .pumpId(
                        dispensingPoint.getPump() != null
                                ? dispensingPoint.getPump().getId()
                                : null
                )
                .pumpCode(
                        dispensingPoint.getPump() != null
                                ? dispensingPoint.getPump().getCode()
                                : null
                )
                .pumpName(
                        dispensingPoint.getPump() != null
                                ? dispensingPoint.getPump().getName()
                                : null
                )
                .tankId(
                        dispensingPoint.getTank() != null
                                ? dispensingPoint.getTank().getId()
                                : null
                )
                .tankCode(
                        dispensingPoint.getTank() != null
                                ? dispensingPoint.getTank().getCode()
                                : null
                )
                .tankName(
                        dispensingPoint.getTank() != null
                                ? dispensingPoint.getTank().getName()
                                : null
                )
                .code(dispensingPoint.getCode())
                .name(dispensingPoint.getName())
                .nozzleNumber(
                        dispensingPoint.getNozzleNumber()
                )
                .status(
                        dispensingPoint.getStatus()
                )
                .displayOrder(
                        dispensingPoint.getDisplayOrder()
                )
                .active(
                        dispensingPoint.isActive()
                )
                .createdAt(
                        dispensingPoint.getCreatedAt()
                )
                .updatedAt(
                        dispensingPoint.getUpdatedAt()
                )
                .build();
    }
}