package com.fuelflex.platform.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
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

import com.fuelflex.platform.auth.dto.request.EmployeeActivationSetPasswordRequest;
import com.fuelflex.platform.auth.service.OtpService;
import com.fuelflex.platform.email.service.EmailService;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.security.jwt.JwtService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplActivationTest {
    @Mock UserRepository users; @Mock RoleRepository roles; @Mock PasswordEncoder encoder;
    @Mock EmailService email; @Mock JwtService jwt;
    private AuthServiceImpl service;
    @BeforeEach void setUp() { service = new AuthServiceImpl(users, roles, encoder, new OtpService(), email, jwt); }

    @Test void validInvitationActivatesAndConsumesCode() {
        User user = pending(); when(users.findByEmailIgnoreCase("employee@test.local")).thenReturn(Optional.of(user));
        when(encoder.encode("Password123")).thenReturn("new-hash");
        EmployeeActivationSetPasswordRequest request = request("Password123", "Password123");
        service.activateEmployee(request);
        assertThat(user.isEnabled()).isTrue(); assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getPasswordHash()).isEqualTo("new-hash"); assertThat(user.getVerificationCode()).isNull();
        verify(users).save(user);
    }

    @Test void consumedInvitationCannotBeReused() {
        User user = pending(); when(users.findByEmailIgnoreCase("employee@test.local")).thenReturn(Optional.of(user));
        when(encoder.encode("Password123")).thenReturn("new-hash");
        service.activateEmployee(request("Password123", "Password123"));
        assertThatThrownBy(() -> service.activateEmployee(request("Password123", "Password123")))
                .hasMessageContaining("invalid or expired");
    }

    private User pending() { User u=new User(); u.setId(UUID.randomUUID()); u.setEmail("employee@test.local"); u.setFirstName("Test"); u.setLastName("Employee"); u.setPasswordHash("random"); u.setEnabled(false); u.setEmailVerified(false); u.setVerificationCode("123456"); u.setVerificationCodeExpiration(OffsetDateTime.now().plusMinutes(10)); Role r=new Role(); r.setCode("MANAGER"); r.setActive(true); u.setRoles(new HashSet<>(Set.of(r))); return u; }
    private EmployeeActivationSetPasswordRequest request(String p,String c) { EmployeeActivationSetPasswordRequest r=new EmployeeActivationSetPasswordRequest(); r.setEmail("employee@test.local"); r.setCode("123456"); r.setPassword(p); r.setConfirmPassword(c); return r; }
}
