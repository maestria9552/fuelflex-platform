package com.fuelflex.platform.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.user.dto.request.ChangeMyPasswordRequest;
import com.fuelflex.platform.user.dto.response.MyAccountResponse;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    private static final String AUTHENTICATED_EMAIL = "user@fuelflex.test";
    private static final String CURRENT_HASH = "current-bcrypt-hash";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserAccountServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserAccountServiceImpl(userRepository, passwordEncoder);

        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());

        Role role = new Role();
        role.setCode("SUPERVISOR");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Current");
        user.setLastName("User");
        user.setEmail(AUTHENTICATED_EMAIL);
        user.setPhoneNumber("+243800000001");
        user.setPasswordHash(CURRENT_HASH);
        user.setPasswordChangedAt(OffsetDateTime.now().minusDays(30));
        user.setLastLoginAt(OffsetDateTime.now().minusHours(2));
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setPhoneVerified(false);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(2);
        user.setOrganization(organization);
        user.getRoles().add(role);
    }

    @Test
    void getMyAccountReturnsOnlyRelevantAccountInformation() {
        arrangeCurrentUser();

        MyAccountResponse response = service.getMyAccount(AUTHENTICATED_EMAIL);

        assertThat(response.getEmail()).isEqualTo(AUTHENTICATED_EMAIL);
        assertThat(response.isEmailVerified()).isTrue();
        assertThat(response.getLastLoginAt()).isEqualTo(user.getLastLoginAt());
        assertThat(response.getPasswordChangedAt())
                .isEqualTo(user.getPasswordChangedAt());
    }

    @Test
    void changePasswordVerifiesCurrentPasswordBeforeEncodingNewPassword() {
        arrangeCurrentUser();
        when(passwordEncoder.matches("current-password", CURRENT_HASH))
                .thenReturn(true);
        when(passwordEncoder.matches("new-password", CURRENT_HASH))
                .thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);

        service.changeMyPassword(
                AUTHENTICATED_EMAIL,
                request("current-password", "new-password")
        );

        InOrder order = Mockito.inOrder(passwordEncoder, userRepository);
        order.verify(passwordEncoder).matches("current-password", CURRENT_HASH);
        order.verify(passwordEncoder).matches("new-password", CURRENT_HASH);
        order.verify(passwordEncoder).encode("new-password");
        order.verify(userRepository).save(user);
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPasswordWithoutMutation() {
        arrangeCurrentUser();
        OffsetDateTime previousChangedAt = user.getPasswordChangedAt();
        when(passwordEncoder.matches("wrong-password", CURRENT_HASH))
                .thenReturn(false);

        assertThatThrownBy(() ->
                service.changeMyPassword(
                        AUTHENTICATED_EMAIL,
                        request("wrong-password", "new-password")
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Current password is incorrect.");

        assertThat(user.getPasswordHash()).isEqualTo(CURRENT_HASH);
        assertThat(user.getPasswordChangedAt()).isEqualTo(previousChangedAt);
        verify(passwordEncoder, never()).encode("new-password");
        verify(userRepository, never()).save(user);
    }

    @Test
    void changePasswordRejectsNewPasswordIdenticalToCurrentPassword() {
        arrangeCurrentUser();
        when(passwordEncoder.matches("current-password", CURRENT_HASH))
                .thenReturn(true);
        when(passwordEncoder.matches("same-password", CURRENT_HASH))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.changeMyPassword(
                        AUTHENTICATED_EMAIL,
                        request("current-password", "same-password")
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(
                        "New password must be different from the current password."
                );

        verify(passwordEncoder, never()).encode("same-password");
        verify(userRepository, never()).save(user);
    }

    @Test
    void changePasswordReplacesHashAndUpdatesPasswordChangedAt() {
        arrangeCurrentUser();
        OffsetDateTime previousChangedAt = user.getPasswordChangedAt();
        when(passwordEncoder.matches("current-password", CURRENT_HASH))
                .thenReturn(true);
        when(passwordEncoder.matches("new-password", CURRENT_HASH))
                .thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);

        MyAccountResponse response = service.changeMyPassword(
                AUTHENTICATED_EMAIL,
                request("current-password", "new-password")
        );

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getPasswordChangedAt()).isAfter(previousChangedAt);
        assertThat(response.getPasswordChangedAt())
                .isEqualTo(user.getPasswordChangedAt());
    }

    @Test
    void changePasswordPreservesIdentityAuthorizationAndSecurityState() {
        arrangeCurrentUser();
        Set<Role> roles = user.getRoles();
        Organization organization = user.getOrganization();
        when(passwordEncoder.matches("current-password", CURRENT_HASH))
                .thenReturn(true);
        when(passwordEncoder.matches("new-password", CURRENT_HASH))
                .thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);

        service.changeMyPassword(
                AUTHENTICATED_EMAIL,
                request("current-password", "new-password")
        );

        assertThat(user.getEmail()).isEqualTo(AUTHENTICATED_EMAIL);
        assertThat(user.getRoles()).isSameAs(roles);
        assertThat(user.getOrganization()).isSameAs(organization);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.isPhoneVerified()).isFalse();
        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
    }

    @Test
    void accountAlwaysTargetsAuthenticatedPrincipal() {
        arrangeCurrentUser();

        service.getMyAccount(AUTHENTICATED_EMAIL);

        verify(userRepository).findByEmailIgnoreCase(AUTHENTICATED_EMAIL);
    }

    @Test
    void invalidOrUnknownPrincipalIsRejected() {
        assertThatThrownBy(() -> service.getMyAccount(" "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Authenticated user is unavailable.");
        verifyNoInteractions(userRepository, passwordEncoder);

        when(userRepository.findByEmailIgnoreCase("missing@fuelflex.test"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getMyAccount("missing@fuelflex.test")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Authenticated user was not found.");
    }

    @Test
    void changePasswordRequestUsesRegistrationPasswordPolicy() {
        Validator validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();

        assertThat(validator.validate(request(null, null))).hasSize(2);
        assertThat(validator.validate(request(
                "current-password",
                "short"
        ))).hasSize(1);
        assertThat(validator.validate(request(
                "current-password",
                "12345678"
        ))).isEmpty();
    }

    @Test
    void accountContractsExcludeClientIdentityAndSensitiveFields() {
        Set<String> requestFields = fieldNames(ChangeMyPasswordRequest.class);
        Set<String> responseFields = fieldNames(MyAccountResponse.class);

        assertThat(requestFields).containsExactlyInAnyOrder(
                "currentPassword",
                "newPassword"
        );
        assertThat(responseFields).containsExactlyInAnyOrder(
                "email",
                "emailVerified",
                "lastLoginAt",
                "passwordChangedAt"
        );
        assertThat(requestFields).doesNotContain(
                "userId",
                "email",
                "roles",
                "permissions",
                "organizationId",
                "passwordHash",
                "enabled",
                "accountLocked",
                "emailVerified",
                "phoneVerified"
        );
        assertThat(responseFields).doesNotContain(
                "password",
                "passwordHash",
                "failedLoginAttempts",
                "lockedUntil"
        );
    }

    private void arrangeCurrentUser() {
        when(userRepository.findByEmailIgnoreCase(AUTHENTICATED_EMAIL))
                .thenReturn(Optional.of(user));
    }

    private ChangeMyPasswordRequest request(
            String currentPassword,
            String newPassword
    ) {
        return ChangeMyPasswordRequest.builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .build();
    }

    private Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }
}
