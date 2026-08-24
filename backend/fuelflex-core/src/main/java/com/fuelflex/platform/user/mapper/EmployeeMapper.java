package com.fuelflex.platform.user.mapper;

import org.springframework.stereotype.Component;

import com.fuelflex.platform.user.dto.response.EmployeeResponse;
import com.fuelflex.platform.user.entity.User;

@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(User user) {
        return toResponse(user, false);
    }

    public EmployeeResponse toResponse(User user, boolean invitationSent) {
        String roleCode = user.getRoles().stream()
                .map(role -> role.getCode())
                .filter(code -> code != null)
                .sorted()
                .findFirst()
                .orElse(null);

        return EmployeeResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .operationalCode(user.getOperationalCode())
                .enabled(user.isEnabled())
                .invitationSent(invitationSent)
                .invitationPending(!user.isEnabled() && !user.isEmailVerified())
                .roleCode(roleCode)
                .organizationId(user.getOrganization() == null ? null : user.getOrganization().getId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
