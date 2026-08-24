package com.fuelflex.platform.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.user.dto.request.EmployeeCreateRequest;
import com.fuelflex.platform.user.dto.request.EmployeeStatusRequest;
import com.fuelflex.platform.user.dto.request.EmployeeUpdateRequest;
import com.fuelflex.platform.user.dto.response.EmployeePageResponse;
import com.fuelflex.platform.user.dto.response.EmployeeResponse;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.mapper.EmployeeMapper;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentService;
import com.fuelflex.platform.auth.service.OtpService;
import com.fuelflex.platform.email.service.EmailService;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmployeeAssignmentService employeeAssignmentService;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;
    @Mock
    private com.fuelflex.platform.user.repository.PumpAttendantNumberRepository pumpAttendantNumbers;

    private EmployeeServiceImpl service;
    private Organization organization;
    private User supervisor;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(
                userRepository,
                roleRepository,
                authorizationService,
                passwordEncoder,
                new EmployeeMapper(),
                employeeAssignmentService,
                otpService,
                emailService,
                pumpAttendantNumbers
        );
        organization = organization();
        supervisor = user(organization, "SUPERVISOR");
    }

    @ParameterizedTest
    @ValueSource(strings = {"MANAGER", "PUMP_ATTENDANT", "ACCOUNTANT", "AUDITOR"})
    void createAcceptsEachWhitelistedRoleAndImposesTenantAndSingleRole(String roleCode) {
        arrangeCurrentSupervisor();
        Role role = role(roleCode, true);
        when(roleRepository.findByCodeIgnoreCase(roleCode)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-unusable-secret");
        org.mockito.Mockito.lenient().when(pumpAttendantNumbers.nextValue()).thenReturn(1L);
        when(otpService.generateCode()).thenReturn("123456");
        when(otpService.expirationDate()).thenReturn(java.time.OffsetDateTime.now().plusMinutes(30));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User employee = invocation.getArgument(0);
            employee.setId(UUID.randomUUID());
            return employee;
        });

        EmployeeResponse response = service.create(createRequest(roleCode));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getOrganization()).isSameAs(organization);
        assertThat(saved.getRoles()).containsExactly(role);
        assertThat(saved.isEnabled()).isFalse();
        assertThat(saved.isEmailVerified()).isFalse();
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-unusable-secret");
        assertThat(response.getOrganizationId()).isEqualTo(organization.getId());
        assertThat(response.getRoleCode()).isEqualTo(roleCode);
        if ("PUMP_ATTENDANT".equals(roleCode)) assertThat(response.getOperationalCode()).isEqualTo("PMP-000001");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN", "SUPERVISOR", "SUPPLIER_USER", "CREDIT_CUSTOMER_USER", "UNKNOWN"
    })
    void createRejectsForbiddenAndUnknownRoles(String roleCode) {
        arrangeCurrentSupervisor();

        assertThatThrownBy(() -> service.create(createRequest(roleCode)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not assignable");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createRejectsInactiveRole() {
        arrangeCurrentSupervisor();
        Role role = role("MANAGER", false);
        when(roleRepository.findByCodeIgnoreCase("MANAGER")).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> service.create(createRequest("MANAGER")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void createRejectsDuplicateEmailAndPhone() {
        arrangeCurrentSupervisor();
        when(roleRepository.findByCodeIgnoreCase("MANAGER"))
                .thenReturn(Optional.of(role("MANAGER", true)));
        EmployeeCreateRequest request = createRequest("MANAGER");
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(true);
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("phone");
    }

    @Test
    void creationWithoutOrganizationIsForbidden() {
        supervisor.setOrganization(null);
        when(authorizationService.getAuthenticatedUser()).thenReturn(supervisor);

        assertThatThrownBy(() -> service.create(createRequest("MANAGER")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("no organization");
    }

    @ParameterizedTest
    @ValueSource(strings = {"MANAGER", "PUMP_ATTENDANT", "ACCOUNTANT", "AUDITOR"})
    void detailReturnsEachEmployeeV1Role(String roleCode) {
        arrangeCurrentSupervisor();
        User employee = user(organization, roleCode);
        when(userRepository.findByIdAndOrganizationId(employee.getId(), organization.getId()))
                .thenReturn(Optional.of(employee));

        assertThat(service.findById(employee.getId()).getRoleCode()).isEqualTo(roleCode);
    }

    @Test
    void supervisorAndExternalUserAreOutsideEmployeeDetailScope() {
        arrangeCurrentSupervisor();
        when(userRepository.findByIdAndOrganizationId(supervisor.getId(), organization.getId()))
                .thenReturn(Optional.of(supervisor));
        assertThatThrownBy(() -> service.findById(supervisor.getId()))
                .isInstanceOf(ResourceNotFoundException.class);

        User external = user(organization, "SUPPLIER_USER");
        when(userRepository.findByIdAndOrganizationId(external.getId(), organization.getId()))
                .thenReturn(Optional.of(external));
        assertThatThrownBy(() -> service.findById(external.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void tenantScopedDetailDoesNotRevealCrossTenantUser() {
        arrangeCurrentSupervisor();
        UUID employeeId = UUID.randomUUID();
        when(userRepository.findByIdAndOrganizationId(employeeId, organization.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(employeeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee was not found.");
    }

    @Test
    void updateChangesOnlyAllowedFieldsAndReplacesRole() {
        arrangeCurrentSupervisor();
        User employee = user(organization, "MANAGER");
        String email = employee.getEmail();
        Organization originalOrganization = employee.getOrganization();
        Role accountant = role("ACCOUNTANT", true);
        when(userRepository.findByIdAndOrganizationId(employee.getId(), organization.getId()))
                .thenReturn(Optional.of(employee));
        when(roleRepository.findByCodeIgnoreCase("ACCOUNTANT")).thenReturn(Optional.of(accountant));
        when(userRepository.save(employee)).thenReturn(employee);

        EmployeeUpdateRequest request = updateRequest("ACCOUNTANT");
        EmployeeResponse response = service.update(employee.getId(), request);

        assertThat(employee.getFirstName()).isEqualTo("Updated");
        assertThat(employee.getLastName()).isEqualTo("Employee");
        assertThat(employee.getPhoneNumber()).isEqualTo("+243811234567");
        assertThat(employee.getRoles()).containsExactly(accountant);
        assertThat(employee.getEmail()).isEqualTo(email);
        assertThat(employee.getOrganization()).isSameAs(originalOrganization);
        assertThat(response.getRoleCode()).isEqualTo("ACCOUNTANT");
    }

    @Test
    void updateRejectsSupervisorAndExternalUser() {
        arrangeCurrentSupervisor();
        when(userRepository.findByIdAndOrganizationId(supervisor.getId(), organization.getId()))
                .thenReturn(Optional.of(supervisor));

        assertThatThrownBy(() -> service.update(supervisor.getId(), updateRequest("MANAGER")))
                .isInstanceOf(ResourceNotFoundException.class);

        User external = user(organization, "SUPPLIER_USER");
        when(userRepository.findByIdAndOrganizationId(external.getId(), organization.getId()))
                .thenReturn(Optional.of(external));
        assertThatThrownBy(() -> service.update(external.getId(), updateRequest("MANAGER")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void statusSupportsDisableAndReactivateWithoutDeleting() {
        arrangeCurrentSupervisor();
        User employee = user(organization, "MANAGER");
        when(userRepository.findByIdAndOrganizationId(employee.getId(), organization.getId()))
                .thenReturn(Optional.of(employee));
        when(userRepository.save(employee)).thenReturn(employee);

        EmployeeStatusRequest disabled = status(false);
        service.updateStatus(employee.getId(), disabled);
        assertThat(employee.isEnabled()).isFalse();

        EmployeeStatusRequest enabled = status(true);
        service.updateStatus(employee.getId(), enabled);
        assertThat(employee.isEnabled()).isTrue();
        verify(userRepository, org.mockito.Mockito.times(2)).save(employee);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void statusRejectsSupervisorAndCrossTenantTarget() {
        arrangeCurrentSupervisor();
        when(userRepository.findByIdAndOrganizationId(supervisor.getId(), organization.getId()))
                .thenReturn(Optional.of(supervisor));
        assertThatThrownBy(() -> service.updateStatus(supervisor.getId(), status(false)))
                .isInstanceOf(ResourceNotFoundException.class);

        UUID otherTenantId = UUID.randomUUID();
        when(userRepository.findByIdAndOrganizationId(otherTenantId, organization.getId()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateStatus(otherTenantId, status(false)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listUsesTenantFiltersPaginationAndStableNewestFirstSort() {
        arrangeCurrentSupervisor();
        User manager = user(organization, "MANAGER");
        when(userRepository.findEmployees(
                eq(organization.getId()), anyCollection(), eq("jean"), eq("MANAGER"), eq(true), eq(com.fuelflex.platform.role.entity.RoleType.EXTERNAL), any(Pageable.class)
        )).thenAnswer(invocation -> {
            java.util.Collection<String> visibleRoleCodes = invocation.getArgument(1);
            assertThat(visibleRoleCodes)
                    .containsExactlyInAnyOrder("MANAGER", "PUMP_ATTENDANT", "ACCOUNTANT", "AUDITOR")
                    .doesNotContain("SUPERVISOR", "SUPPLIER_USER", "CREDIT_CUSTOMER_USER");
            Pageable pageable = invocation.getArgument(6);
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(25);
            assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
            assertThat(pageable.getSort().getOrderFor("id").isDescending()).isTrue();
            return new PageImpl<>(List.of(manager), pageable, 26);
        });

        EmployeePageResponse response = service.findAll(1, 25, " jean ", "manager", true);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(26);
    }

    @Test
    void listRejectsOversizedPage() {
        arrangeCurrentSupervisor();
        assertThatThrownBy(() -> service.findAll(0, 101, null, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("between 1 and 100");
        verify(userRepository, never()).findEmployees(any(), anyCollection(), any(), any(), any(), any(), any());
    }

    @Test
    void responseContractContainsNoSensitiveFields() {
        assertThat(EmployeeResponse.class.getDeclaredFields())
                .extracting(field -> field.getName())
                .doesNotContain(
                        "passwordHash", "verificationCode", "failedLoginAttempts", "lockedUntil"
                );
    }

    private void arrangeCurrentSupervisor() {
        when(authorizationService.getAuthenticatedUser()).thenReturn(supervisor);
    }

    private Organization organization() {
        Organization value = new Organization();
        value.setId(UUID.randomUUID());
        return value;
    }

    private User user(Organization tenant, String roleCode) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Current");
        user.setLastName("Employee");
        user.setEmail(UUID.randomUUID() + "@fuelflex.test");
        user.setPhoneNumber("+243810000001");
        user.setPasswordHash("hash");
        user.setOrganization(tenant);
        user.setEnabled(true);
        user.setRoles(new java.util.HashSet<>(Set.of(role(roleCode, true))));
        return user;
    }

    private Role role(String code, boolean active) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(code);
        role.setName(code);
        role.setActive(active);
        return role;
    }

    private EmployeeCreateRequest createRequest(String roleCode) {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setFirstName("Jean");
        request.setLastName("Mukendi");
        request.setEmail("employee@fuelflex.test");
        request.setPhoneNumber("+243811234567");
        request.setRoleCode(roleCode);
        return request;
    }

    private EmployeeUpdateRequest updateRequest(String roleCode) {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setFirstName(" Updated ");
        request.setLastName(" Employee ");
        request.setPhoneNumber("+243 (81) 123-4567");
        request.setRoleCode(roleCode);
        return request;
    }

    private EmployeeStatusRequest status(boolean enabled) {
        EmployeeStatusRequest request = new EmployeeStatusRequest();
        request.setEnabled(enabled);
        return request;
    }
}
