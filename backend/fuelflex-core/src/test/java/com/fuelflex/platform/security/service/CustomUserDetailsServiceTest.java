package com.fuelflex.platform.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fuelflex.platform.permission.entity.Permission;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadsActiveRoleAndPermissionAuthoritiesWithoutRenamingExistingRole() {
        Permission activePermission = permission("user:view", true);
        Permission inactivePermission = permission("user:update", false);
        Role supervisor = role("SUPERVISOR", true);
        supervisor.getPermissions().add(activePermission);
        supervisor.getPermissions().add(inactivePermission);
        Role inactiveRole = role("AUDITOR", false);
        inactiveRole.getPermissions().add(permission("report:view", true));

        User user = user();
        user.getRoles().add(supervisor);
        user.getRoles().add(inactiveRole);
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails details = new CustomUserDetailsService(userRepository)
                .loadUserByUsername(user.getEmail());

        assertThat(details.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .contains("SUPERVISOR", "user:view")
                .doesNotContain("ROLE_SUPERVISOR", "user:update", "AUDITOR", "report:view");
    }

    @Test
    void refusesPumpAttendantEvenWhenOperationalAndEnabled() {
        User user = user();
        user.getRoles().add(role("PUMP_ATTENDANT", true));
        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> new CustomUserDetailsService(userRepository)
                .loadUserByUsername(user.getEmail()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("not a Web portal account");
    }

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("supervisor@fuelflex.test");
        user.setPasswordHash("hash");
        user.setEnabled(true);
        user.setEmailVerified(true);
        return user;
    }

    private Role role(String code, boolean active) {
        Role role = new Role();
        role.setCode(code);
        role.setActive(active);
        return role;
    }

    private Permission permission(String code, boolean active) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setActive(active);
        return permission;
    }
}
