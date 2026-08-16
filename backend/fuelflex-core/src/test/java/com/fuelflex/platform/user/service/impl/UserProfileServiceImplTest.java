package com.fuelflex.platform.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.user.dto.request.UpdateMyProfileRequest;
import com.fuelflex.platform.user.dto.response.MyProfileResponse;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    private static final String AUTHENTICATED_EMAIL = "user@fuelflex.test";

    @Mock
    private UserRepository userRepository;

    private UserProfileServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserProfileServiceImpl(userRepository);

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
        user.setPasswordHash("unchanged-hash");
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setPhoneVerified(false);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(2);
        user.setOrganization(organization);
        user.getRoles().add(role);
    }

    @Test
    void getMyProfileReturnsOnlyCurrentUserProfileFields() {
        when(userRepository.findByEmailIgnoreCase(AUTHENTICATED_EMAIL))
                .thenReturn(Optional.of(user));

        MyProfileResponse response = service.getMyProfile(AUTHENTICATED_EMAIL);

        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getFirstName()).isEqualTo("Current");
        assertThat(response.getLastName()).isEqualTo("User");
        assertThat(response.getEmail()).isEqualTo(AUTHENTICATED_EMAIL);
        assertThat(response.getPhoneNumber()).isEqualTo("+243800000001");
    }

    @Test
    void updateMyProfileChangesAllowedFieldsAndNormalizesPhone() {
        arrangeCurrentUser();
        UpdateMyProfileRequest request = request(
                "  Updated  ",
                "  Person  ",
                " +243 (81) 234-5678 "
        );

        MyProfileResponse response =
                service.updateMyProfile(AUTHENTICATED_EMAIL, request);

        assertThat(response.getFirstName()).isEqualTo("Updated");
        assertThat(response.getLastName()).isEqualTo("Person");
        assertThat(response.getPhoneNumber()).isEqualTo("+243812345678");
        verify(userRepository).save(user);
    }

    @Test
    void updateMyProfilePreservesEmailRolesOrganizationAndSecurityFields() {
        arrangeCurrentUser();
        String passwordHash = user.getPasswordHash();
        Set<Role> roles = user.getRoles();
        Organization organization = user.getOrganization();

        service.updateMyProfile(
                AUTHENTICATED_EMAIL,
                request("New", "Name", "+243810000002")
        );

        assertThat(user.getEmail()).isEqualTo(AUTHENTICATED_EMAIL);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(user.getRoles()).isSameAs(roles);
        assertThat(user.getOrganization()).isSameAs(organization);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.isPhoneVerified()).isFalse();
        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
    }

    @Test
    void updateMyProfileAllowsKeepingOwnPhoneNumber() {
        arrangeCurrentUser();
        when(userRepository.findByPhoneNumber("+243800000001"))
                .thenReturn(Optional.of(user));

        service.updateMyProfile(
                AUTHENTICATED_EMAIL,
                request("Current", "User", "+243800000001")
        );

        verify(userRepository).save(user);
    }

    @Test
    void updateMyProfileRejectsPhoneNumberOwnedByAnotherUser() {
        arrangeCurrentUser();
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        when(userRepository.findByPhoneNumber("+243899999999"))
                .thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() ->
                service.updateMyProfile(
                        AUTHENTICATED_EMAIL,
                        request("Current", "User", "+243899999999")
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("This phone number is already registered.");

        verify(userRepository, never()).save(user);
    }

    @Test
    void profileAlwaysTargetsAuthenticatedPrincipalRatherThanAClientId() {
        arrangeCurrentUser();

        service.updateMyProfile(
                AUTHENTICATED_EMAIL,
                request("Current", "User", "+243800000001")
        );

        verify(userRepository).findByEmailIgnoreCase(AUTHENTICATED_EMAIL);
    }

    @Test
    void invalidOrUnknownPrincipalIsRejected() {
        assertThatThrownBy(() -> service.getMyProfile(" "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Authenticated user is unavailable.");

        when(userRepository.findByEmailIgnoreCase("missing@fuelflex.test"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getMyProfile("missing@fuelflex.test")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Authenticated user was not found.");
    }

    @Test
    void updateRequestValidationMatchesUserColumnConstraints() {
        Validator validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();

        assertThat(validator.validate(request("", "", ""))).hasSize(3);
        assertThat(validator.validate(request(
                "a".repeat(101),
                "b".repeat(101),
                "1".repeat(31)
        ))).hasSize(3);
        assertThat(validator.validate(request(
                "Valid",
                "User",
                "+243810000002"
        ))).isEmpty();
    }

    @Test
    void dtoContractsExcludeIdentityAuthorizationAndSecurityFields() {
        Set<String> requestFields = fieldNames(UpdateMyProfileRequest.class);
        Set<String> responseFields = fieldNames(MyProfileResponse.class);

        assertThat(requestFields).containsExactlyInAnyOrder(
                "firstName",
                "lastName",
                "phoneNumber"
        );
        assertThat(responseFields).containsExactlyInAnyOrder(
                "id",
                "firstName",
                "lastName",
                "email",
                "phoneNumber"
        );
        assertThat(requestFields).doesNotContain(
                "id",
                "email",
                "password",
                "passwordHash",
                "roles",
                "permissions",
                "organizationId",
                "enabled",
                "emailVerified",
                "phoneVerified",
                "accountLocked",
                "failedLoginAttempts"
        );
    }

    private void arrangeCurrentUser() {
        when(userRepository.findByEmailIgnoreCase(AUTHENTICATED_EMAIL))
                .thenReturn(Optional.of(user));
        lenient().when(userRepository.save(user)).thenReturn(user);
    }

    private UpdateMyProfileRequest request(
            String firstName,
            String lastName,
            String phoneNumber
    ) {
        return UpdateMyProfileRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .build();
    }

    private Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }
}
