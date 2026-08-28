package com.fuelflex.platform.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fuelflex.platform.assignment.service.EmployeeAssignmentService;
import com.fuelflex.platform.auth.service.OtpService;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.email.service.EmailService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.dto.request.EmployeeCreateRequest;
import com.fuelflex.platform.user.dto.request.ManagerPumpAttendantRequest;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.mapper.EmployeeMapper;
import com.fuelflex.platform.user.model.Gender;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;
import com.fuelflex.platform.user.repository.PumpAttendantNumberRepository;
import com.fuelflex.platform.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmployeePumpAttendantPreparationTest {

    @Mock
    private UserRepository users;
    @Mock
    private RoleRepository roles;
    @Mock
    private AuthorizationService authorization;
    @Mock
    private StationAccessService stationAccess;
    @Mock
    private PasswordEncoder passwords;
    @Mock
    private EmployeeAssignmentService assignments;
    @Mock
    private OtpService otp;
    @Mock
    private EmailService email;
    @Mock
    private PumpAttendantNumberRepository numbers;

    private EmployeeServiceImpl service;
    private Organization organization;
    private Role pumpRole;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(
                users, roles, authorization, stationAccess, passwords,
                new EmployeeMapper(), assignments, otp, email, numbers);
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        pumpRole = role("PUMP_ATTENDANT");
    }

    @Test
    void managerCreatesPreparationWithoutSubmissionOrInvitation() {
        User manager = user("MANAGER");
        when(authorization.getAuthenticatedUser()).thenReturn(manager);
        when(stationAccess.getAccessibleStationIds(manager))
                .thenReturn(Set.of(UUID.randomUUID()));
        when(roles.findByCodeIgnoreCase("PUMP_ATTENDANT"))
                .thenReturn(Optional.of(pumpRole));
        when(numbers.nextValue()).thenReturn(12L);
        when(passwords.encode(any())).thenReturn("unusable");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        var response = service.createPumpAttendantDraft(managerRequest());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getPreparedBy()).isSameAs(manager);
        assertThat(saved.getValue().getPumpAttendantValidationStatus())
                .isEqualTo(PumpAttendantValidationStatus.PREPARATION);
        assertThat(saved.getValue().getPostName()).isEqualTo("Kabeya");
        assertThat(saved.getValue().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(saved.getValue().getBirthPlace()).isEqualTo("Lubumbashi");
        assertThat(saved.getValue().getBirthDate()).isEqualTo(LocalDate.of(1996, 2, 20));
        assertThat(saved.getValue().getAddress()).isEqualTo("Avenue Centrale 20");
        verify(assignments).assignForPumpAttendantOnboarding(
                eq(saved.getValue()), any(UUID.class), eq(manager),
                eq("PUMP_ATTENDANT_MANAGER_PREPARATION"));
        assertThat(saved.getValue().getOperationalCode())
                .isEqualTo("PMP-000012");
        assertThat(saved.getValue().getVerificationCode()).isNull();
        assertThat(saved.getValue().getPosCredentialHash()).isNull();
        assertThat(saved.getValue().isEnabled()).isFalse();
        assertThat(response.isInvitationPending()).isFalse();
        verify(otp, never()).generateCode();
        verify(email, never()).sendEmployeeInvitation(
                any(), any(), any(), any(), any());
    }

    @Test
    void supervisorCreatesValidatedPumpAttendantWithoutSelfApproval() {
        User supervisor = user("SUPERVISOR");
        when(authorization.getAuthenticatedUser()).thenReturn(supervisor);
        when(roles.findByCodeIgnoreCase("PUMP_ATTENDANT"))
                .thenReturn(Optional.of(pumpRole));
        when(numbers.nextValue()).thenReturn(3L);
        when(passwords.encode(any())).thenReturn("unusable");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setFirstName("Direct");
        request.setLastName("Supervisor");
        request.setEmail("direct@fuelflex.test");
        request.setPhoneNumber("+243810000010");
        request.setRoleCode("PUMP_ATTENDANT");
        request.setPostName("Kabeya");
        request.setGender(Gender.MALE);
        request.setBirthPlace("Kinshasa");
        request.setBirthDate(LocalDate.of(1994, 5, 12));
        request.setAddress("Avenue des Stations 10");
        request.setStationId(UUID.randomUUID());
        var response = service.createPumpAttendant(request);
        var employee = response.employee();

        assertThat(employee.getPostName()).isEqualTo("Kabeya");
        assertThat(employee.getGender()).isEqualTo(Gender.MALE);
        assertThat(employee.getBirthPlace()).isEqualTo("Kinshasa");
        assertThat(employee.getBirthDate()).isEqualTo(LocalDate.of(1994, 5, 12));
        assertThat(employee.getAddress()).isEqualTo("Avenue des Stations 10");
        assertThat(employee.getOperationalCode()).isEqualTo("PMP-000003");
        assertThat(response.posCredential())
                .isNotBlank()
                .isNotEqualTo(employee.getOperationalCode());
        verify(assignments).assignForPumpAttendantOnboarding(
                any(User.class), eq(request.getStationId()), eq(supervisor),
                eq("PUMP_ATTENDANT_DIRECT_CREATION"));
        assertThat(employee.getPumpAttendantValidationStatus())
                .isEqualTo(PumpAttendantValidationStatus.VALIDATED);
        assertThat(employee.getPreparedById()).isNull();
        verify(otp, never()).generateCode();
        verify(email, never()).sendEmployeeInvitation(
                any(), any(), any(), any(), any());
    }

    @Test
    void pendingPumpAttendantCannotBeEditedThroughSupervisorEndpoint() {
        User supervisor = user("SUPERVISOR");
        User attendant = user("PUMP_ATTENDANT");
        attendant.setPumpAttendantValidationStatus(
                PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL);
        when(authorization.getAuthenticatedUser()).thenReturn(supervisor);
        when(users.findByIdAndOrganizationId(
                attendant.getId(), organization.getId()))
                .thenReturn(Optional.of(attendant));

        assertThatThrownBy(() -> service.update(
                attendant.getId(), new com.fuelflex.platform.user.dto.request.EmployeeUpdateRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("validation request");
    }

    @Test
    void validationMakesPumpAttendantOperationalWithoutWebInvitation() {
        User attendant = user("PUMP_ATTENDANT");
        attendant.setEnabled(false);
        attendant.setEmailVerified(false);
        attendant.setPumpAttendantValidationStatus(
                PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL);
        when(users.save(attendant)).thenReturn(attendant);

        boolean sent = service.validatePreparedPumpAttendant(attendant);

        assertThat(sent).isTrue();
        assertThat(attendant.getPumpAttendantValidationStatus())
                .isEqualTo(PumpAttendantValidationStatus.VALIDATED);
        assertThat(attendant.getVerificationCode()).isNull();
        assertThat(attendant.isEnabled()).isTrue();
        verify(otp, never()).generateCode();
        verify(email, never()).sendEmployeeInvitation(
                any(), any(), any(), any(), any());
    }

    @Test
    void posCredentialsAreRandomIndependentAndOnlyHashesAreStored() {
        User first = user("PUMP_ATTENDANT");
        first.setOperationalCode("PMP-000100");
        first.setPumpAttendantValidationStatus(
                PumpAttendantValidationStatus.VALIDATED);
        User second = user("PUMP_ATTENDANT");
        second.setOperationalCode("PMP-000101");
        second.setPumpAttendantValidationStatus(
                PumpAttendantValidationStatus.VALIDATED);
        when(passwords.encode(any(String.class)))
                .thenAnswer(invocation -> "HASH:" + invocation.getArgument(0));
        when(users.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String firstCredential = service.issuePosCredential(first);
        String secondCredential = service.issuePosCredential(second);

        assertThat(firstCredential).hasSize(32)
                .isNotEqualTo(first.getOperationalCode());
        assertThat(secondCredential).hasSize(32)
                .isNotEqualTo(second.getOperationalCode())
                .isNotEqualTo(firstCredential);
        assertThat(firstCredential).doesNotContain("000100");
        assertThat(secondCredential).doesNotContain("000101");
        assertThat(first.getPosCredentialHash())
                .isEqualTo("HASH:" + firstCredential)
                .isNotEqualTo(firstCredential);
        assertThat(second.getPosCredentialHash())
                .isEqualTo("HASH:" + secondCredential)
                .isNotEqualTo(secondCredential);
    }

    private ManagerPumpAttendantRequest managerRequest() {
        ManagerPumpAttendantRequest request =
                new ManagerPumpAttendantRequest();
        request.setFirstName("  Jean  ");
        request.setLastName(" Pompiste ");
        request.setPostName(" Kabeya ");
        request.setGender(Gender.FEMALE);
        request.setBirthPlace(" Lubumbashi ");
        request.setBirthDate(LocalDate.of(1996, 2, 20));
        request.setAddress(" Avenue Centrale 20 ");
        request.setEmail("JEAN@FUELFLEX.TEST");
        request.setPhoneNumber("+243 810 000 009");
        request.setStationId(UUID.randomUUID());
        return request;
    }

    private User user(String roleCode) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("User");
        user.setLastName(roleCode);
        user.setEmail(UUID.randomUUID() + "@fuelflex.test");
        user.setPhoneNumber("+243" + Math.abs(user.getId().hashCode()));
        user.setPasswordHash("hash");
        user.setOrganization(organization);
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(role(roleCode))));
        return user;
    }

    private Role role(String code) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(code);
        role.setName(code);
        role.setActive(true);
        return role;
    }
}
