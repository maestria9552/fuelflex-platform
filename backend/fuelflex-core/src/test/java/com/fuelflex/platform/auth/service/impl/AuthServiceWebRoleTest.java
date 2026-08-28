package com.fuelflex.platform.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fuelflex.platform.auth.dto.request.LoginRequest;
import com.fuelflex.platform.auth.dto.request.ResendVerificationCodeRequest;
import com.fuelflex.platform.auth.service.OtpService;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.email.service.EmailService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.security.jwt.JwtService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceWebRoleTest {

    @Mock UserRepository users;
    @Mock RoleRepository roles;
    @Mock PasswordEncoder passwords;
    @Mock OtpService otp;
    @Mock EmailService email;
    @Mock JwtService jwt;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(users, roles, passwords, otp, email, jwt);
    }

    @Test
    void explicitlyRefusesPumpAttendantWebLoginBeforePasswordAuthentication() {
        User attendant = user("PUMP_ATTENDANT");
        when(users.findByEmailIgnoreCase(attendant.getEmail()))
                .thenReturn(Optional.of(attendant));

        assertThatThrownBy(() -> service.login(login(attendant)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot sign in to the Web portal");
        verify(passwords, never()).matches(any(), any());
        verify(jwt, never()).generateAccessToken(any());
    }

    @Test
    void refusesToCreateWebOtpForPumpAttendant() {
        User attendant = user("PUMP_ATTENDANT");
        attendant.setEmailVerified(false);
        when(users.findByEmailIgnoreCase(attendant.getEmail()))
                .thenReturn(Optional.of(attendant));
        ResendVerificationCodeRequest request =
                new ResendVerificationCodeRequest();
        request.setEmail(attendant.getEmail());

        assertThatThrownBy(() -> service.resendVerificationCode(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("do not use Web verification codes");
        verify(otp, never()).generateCode();
        verify(users, never()).save(attendant);
    }

    @Test
    void managerAndSupervisorKeepWebAuthentication() {
        for (String role : Set.of("MANAGER", "SUPERVISOR")) {
            User user = user(role);
            when(users.findByEmailIgnoreCase(user.getEmail()))
                    .thenReturn(Optional.of(user));
            when(passwords.matches("valid-password", "web-hash"))
                    .thenReturn(true);
            when(users.save(user)).thenReturn(user);
            when(jwt.generateAccessToken(user)).thenReturn("jwt-" + role);
            when(jwt.getAccessTokenExpiration()).thenReturn(3_600_000L);

            var response = service.login(login(user));

            assertThat(response.getAccessToken()).isEqualTo("jwt-" + role);
            assertThat(response.getRoles()).contains(role);
        }
    }

    private LoginRequest login(User user) {
        LoginRequest request = new LoginRequest();
        request.setEmail(user.getEmail());
        request.setPassword("valid-password");
        return request;
    }

    private User user(String roleCode) {
        Role role = new Role();
        role.setCode(roleCode);
        role.setActive(true);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Web");
        user.setLastName(roleCode);
        user.setEmail(roleCode.toLowerCase() + "@fuelflex.test");
        user.setPasswordHash("web-hash");
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRoles(new HashSet<>(Set.of(role)));
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        user.setOrganization(organization);
        return user;
    }
}
