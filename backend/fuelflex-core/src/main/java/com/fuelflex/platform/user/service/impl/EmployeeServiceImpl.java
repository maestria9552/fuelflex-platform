package com.fuelflex.platform.user.service.impl;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.auth.service.OtpService;
import com.fuelflex.platform.email.service.EmailService;
import com.fuelflex.platform.common.exception.ConflictException;
import com.fuelflex.platform.assignment.service.EmployeeAssignmentService;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.exception.ResourceNotFoundException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.entity.RoleType;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.user.dto.request.EmployeeCreateRequest;
import com.fuelflex.platform.user.dto.request.EmployeeStatusRequest;
import com.fuelflex.platform.user.dto.request.EmployeeUpdateRequest;
import com.fuelflex.platform.user.dto.response.AssignableEmployeeRoleResponse;
import com.fuelflex.platform.user.dto.response.EmployeePageResponse;
import com.fuelflex.platform.user.dto.response.EmployeeResponse;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.mapper.EmployeeMapper;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.user.service.EmployeeRolePolicy;
import com.fuelflex.platform.user.service.EmployeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthorizationService authorizationService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;
    private final EmployeeAssignmentService employeeAssignmentService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final com.fuelflex.platform.user.repository.PumpAttendantNumberRepository pumpAttendantNumbers;

    @Override
    @Transactional(readOnly = true)
    public EmployeePageResponse findAll(
            int page,
            int size,
            String search,
            String roleCode,
            Boolean enabled
    ) {
        Organization organization = getCurrentOrganization();
        String normalizedRoleCode = normalizeVisibleRoleFilter(roleCode);
        PageRequest pageable = PageRequest.of(
                validatePage(page),
                validateSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<EmployeeResponse> employees = userRepository.findEmployees(
                organization.getId(),
                EmployeeRolePolicy.VISIBLE_ROLE_CODES,
                normalizeNullableText(search),
                normalizedRoleCode,
                enabled,
                RoleType.EXTERNAL,
                pageable
        ).map(employeeMapper::toResponse);
        return EmployeePageResponse.from(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(UUID employeeId) {
        return employeeMapper.toResponse(getVisibleEmployee(employeeId));
    }

    @Override
    public EmployeeResponse create(EmployeeCreateRequest request) {
        Organization organization = getCurrentOrganization();
        Role role = getAssignableRole(request.getRoleCode());
        String email = normalizeEmail(request.getEmail());
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("This email address is already registered.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException("This phone number is already registered.");
        }

        User employee = new User();
        employee.setFirstName(normalizeRequiredText(request.getFirstName()));
        employee.setLastName(normalizeRequiredText(request.getLastName()));
        employee.setEmail(email);
        employee.setPhoneNumber(phoneNumber);
        employee.setOrganization(organization);
        employee.setRoles(new HashSet<>(List.of(role)));
        assignOperationalCodeIfRequired(employee, role);

        // The account cannot be used until the future invitation flow lets the
        // employee choose a password and verifies the email address.
        employee.setPasswordHash(passwordEncoder.encode(generateUnusableSecret()));
        employee.setEnabled(false);
        employee.setEmailVerified(false);
        employee.setPhoneVerified(false);
        String invitationCode = otpService.generateCode();
        OffsetDateTime invitationExpiresAt = otpService.expirationDate();
        employee.setVerificationCode(invitationCode);
        employee.setVerificationCodeExpiration(invitationExpiresAt);
        employee.setVerificationCodeAttempts(0);

        User savedEmployee = userRepository.save(employee);
        boolean invitationSent = true;
        try {
            emailService.sendEmployeeInvitation(savedEmployee.getEmail(), savedEmployee.getFirstName(),
                    savedEmployee.getLastName(), invitationCode, invitationExpiresAt);
        } catch (Exception exception) {
            invitationSent = false;
            log.error("Unable to send employee invitation to {}.", savedEmployee.getEmail(), exception);
        }
        return employeeMapper.toResponse(savedEmployee, invitationSent);
    }

    @Override
    public EmployeeResponse update(UUID employeeId, EmployeeUpdateRequest request) {
        User employee = getAdministrableEmployee(employeeId);
        Role role = getAssignableRole(request.getRoleCode());
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        if (userRepository.existsByPhoneNumberAndIdNot(phoneNumber, employee.getId())) {
            throw new BusinessException("This phone number is already registered.");
        }

        String currentRole = employee.getRoles().stream()
                .filter(Role::isActive).map(Role::getCode)
                .filter(EmployeeRolePolicy::isAssignable).findFirst().orElse("");
        if (!currentRole.equals(role.getCode())
                && employeeAssignmentService.countActiveAssignments(
                        employee.getId(), employee.getOrganization().getId()) > 0) {
            throw new ConflictException(
                    "Employee role cannot change while active station assignments exist.");
        }

        employee.setFirstName(normalizeRequiredText(request.getFirstName()));
        employee.setLastName(normalizeRequiredText(request.getLastName()));
        employee.setPhoneNumber(phoneNumber);
        employee.setRoles(new HashSet<>(List.of(role)));
        assignOperationalCodeIfRequired(employee, role);

        return employeeMapper.toResponse(userRepository.save(employee));
    }

    @Override
    public EmployeeResponse updateStatus(UUID employeeId, EmployeeStatusRequest request) {
        User employee = getAdministrableEmployee(employeeId);
        boolean enabled = Boolean.TRUE.equals(request.getEnabled());
        if (employee.isEnabled() && !enabled) {
            employeeAssignmentService.endAllForEmployee(employee,
                    authorizationService.getAuthenticatedUser(),
                    OffsetDateTime.now(), "EMPLOYEE_DISABLED");
        }
        employee.setEnabled(enabled);
        return employeeMapper.toResponse(userRepository.save(employee));
    }

    @Override
    public com.fuelflex.platform.user.dto.response.EmployeeInvitationResponse resendInvitation(UUID employeeId) {
        User employee = getAdministrableEmployee(employeeId);
        if (employee.isEnabled() || employee.isEmailVerified()) {
            throw new ConflictException("An invitation can only be resent to a pending employee.");
        }
        String code = otpService.generateCode();
        OffsetDateTime expiresAt = otpService.expirationDate();
        employee.setVerificationCode(code);
        employee.setVerificationCodeExpiration(expiresAt);
        employee.setVerificationCodeAttempts(0);
        User saved = userRepository.save(employee);
        boolean sent = true;
        try {
            emailService.sendEmployeeInvitation(saved.getEmail(), saved.getFirstName(), saved.getLastName(), code, expiresAt);
        } catch (Exception exception) {
            sent = false;
            log.error("Unable to resend employee invitation to {}.", saved.getEmail(), exception);
        }
        return new com.fuelflex.platform.user.dto.response.EmployeeInvitationResponse(saved.getId(), sent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignableEmployeeRoleResponse> findAssignableRoles() {
        getCurrentOrganization();
        return EmployeeRolePolicy.ASSIGNABLE_ROLE_CODES.stream()
                .map(roleRepository::findByCodeIgnoreCase)
                .flatMap(java.util.Optional::stream)
                .filter(Role::isActive)
                .map(role -> new AssignableEmployeeRoleResponse(role.getCode(), role.getName()))
                .toList();
    }

    private void assignOperationalCodeIfRequired(User employee, Role role) {
        if ("PUMP_ATTENDANT".equalsIgnoreCase(role.getCode()) && employee.getOperationalCode() == null) {
            employee.setOperationalCode("PMP-" + String.format("%06d", pumpAttendantNumbers.nextValue()));
        }
    }

    private Organization getCurrentOrganization() {
        User currentUser = authorizationService.getAuthenticatedUser();
        if (currentUser.getOrganization() == null
                || currentUser.getOrganization().getId() == null) {
            throw new ForbiddenException("Authenticated supervisor has no organization.");
        }
        return currentUser.getOrganization();
    }

    private User getVisibleEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new ResourceNotFoundException("Employee was not found.");
        }
        Organization organization = getCurrentOrganization();
        User employee = userRepository.findByIdAndOrganizationId(employeeId, organization.getId())
                .filter(this::isVisibleEmployee)
                .orElseThrow(() -> new ResourceNotFoundException("Employee was not found."));
        return employee;
    }

    private User getAdministrableEmployee(UUID employeeId) {
        return getVisibleEmployee(employeeId);
    }

    private boolean isVisibleEmployee(User user) {
        return hasExactlyOneAssignableRole(user);
    }

    private boolean hasExactlyOneAssignableRole(User user) {
        long assignableRoleCount = user.getRoles().stream()
                .filter(Role::isActive)
                .map(Role::getCode)
                .filter(EmployeeRolePolicy::isAssignable)
                .count();
        return assignableRoleCount == 1
                && user.getRoles().stream()
                        .filter(Role::isActive)
                        .allMatch(role -> EmployeeRolePolicy.isAssignable(role.getCode()));
    }

    private Role getAssignableRole(String requestedRoleCode) {
        String roleCode = normalizeCode(requestedRoleCode);
        if (!EmployeeRolePolicy.isAssignable(roleCode)) {
            throw new BusinessException("The requested role is not assignable to an employee.");
        }
        Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new BusinessException("The requested role does not exist."));
        if (!role.isActive()) {
            throw new BusinessException("The requested role is inactive.");
        }
        return role;
    }

    private String normalizeVisibleRoleFilter(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return null;
        }
        String normalized = normalizeCode(roleCode);
        if (!EmployeeRolePolicy.VISIBLE_ROLE_CODES.contains(normalized)) {
            throw new BusinessException("The requested employee role filter is invalid.");
        }
        return normalized;
    }

    private int validatePage(int page) {
        if (page < 0) {
            throw new BusinessException("Page must be zero or greater.");
        }
        return page;
    }

    private int validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException("Page size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }
        return size;
    }

    private String normalizeRequiredText(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return normalizeRequiredText(value);
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhoneNumber(String value) {
        return value.trim().replaceAll("[\\s()\\-]", "");
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String generateUnusableSecret() {
        byte[] secret = new byte[32];
        SECURE_RANDOM.nextBytes(secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }
}
