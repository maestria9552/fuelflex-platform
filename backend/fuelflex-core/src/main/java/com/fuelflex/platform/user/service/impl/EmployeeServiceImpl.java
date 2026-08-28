package com.fuelflex.platform.user.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
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
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.user.dto.request.EmployeeCreateRequest;
import com.fuelflex.platform.user.dto.request.ManagerPumpAttendantRequest;
import com.fuelflex.platform.user.dto.request.EmployeeStatusRequest;
import com.fuelflex.platform.user.dto.request.EmployeeUpdateRequest;
import com.fuelflex.platform.user.dto.response.AssignableEmployeeRoleResponse;
import com.fuelflex.platform.user.dto.response.EmployeePageResponse;
import com.fuelflex.platform.user.dto.response.EmployeeResponse;
import com.fuelflex.platform.user.dto.response.PumpAttendantCreationResponse;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.model.Gender;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;
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
    private final StationAccessService stationAccessService;
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
        getCurrentOrganization();
        Role role = getAssignableRole(request.getRoleCode());
        if (isPumpAttendantRole(role)) {
            throw new BusinessException(
                    "Use the dedicated pump-attendant creation operation.");
        }
        return createEmployee(request, role);
    }

    @Override
    public PumpAttendantCreationResponse createPumpAttendant(
            EmployeeCreateRequest request) {
        getCurrentOrganization();
        Role role = getAssignableRole(request.getRoleCode());
        if (!isPumpAttendantRole(role)) {
            throw new BusinessException(
                    "The dedicated operation only creates pump attendants.");
        }
        User saved = createUser(request, role);
        String credential = issuePosCredential(saved);
        return new PumpAttendantCreationResponse(
                employeeMapper.toResponse(saved), credential);
    }

    private EmployeeResponse createEmployee(
            EmployeeCreateRequest request, Role role) {
        User savedEmployee = createUser(request, role);
        String invitationCode = otpService.generateCode();
        OffsetDateTime invitationExpiresAt = otpService.expirationDate();
        savedEmployee.setVerificationCode(invitationCode);
        savedEmployee.setVerificationCodeExpiration(invitationExpiresAt);
        savedEmployee.setVerificationCodeAttempts(0);
        savedEmployee = userRepository.save(savedEmployee);
        boolean invitationSent = true;
        try {
            emailService.sendEmployeeInvitation(
                    savedEmployee.getEmail(), savedEmployee.getFirstName(),
                    savedEmployee.getLastName(), invitationCode,
                    invitationExpiresAt);
        } catch (Exception exception) {
            invitationSent = false;
            log.error("Unable to send employee invitation to {}.",
                    savedEmployee.getEmail(), exception);
        }
        return employeeMapper.toResponse(savedEmployee, invitationSent);
    }

    private User createUser(EmployeeCreateRequest request, Role role) {
        User supervisor = authorizationService.getAuthenticatedUser();
        Organization organization = getCurrentOrganization();
        validateStationForRole(role, request.getStationId(), true);
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
        applyIdentityProfile(employee, role, request.getPostName(),
                request.getGender(), request.getBirthPlace(),
                request.getBirthDate(), request.getAddress());
        employee.setOrganization(organization);
        employee.setRoles(new HashSet<>(List.of(role)));
        assignOperationalCodeIfRequired(employee, role);
        applyDirectValidationStatus(employee, role);

        employee.setPasswordHash(passwordEncoder.encode(generateUnusableSecret()));
        employee.setEnabled(isPumpAttendantRole(role));
        employee.setEmailVerified(false);
        employee.setPhoneVerified(false);
        employee.setVerificationCode(null);
        employee.setVerificationCodeExpiration(null);
        employee.setVerificationCodeAttempts(0);

        User savedEmployee = userRepository.save(employee);
        if (isPumpAttendantRole(role)) {
            stationAccessService.checkStationAccess(
                    supervisor, request.getStationId());
            employeeAssignmentService.assignForPumpAttendantOnboarding(
                    savedEmployee, request.getStationId(), supervisor,
                    "PUMP_ATTENDANT_DIRECT_CREATION");
        }
        return savedEmployee;
    }

    @Override
    public EmployeeResponse update(UUID employeeId, EmployeeUpdateRequest request) {
        User employee = getAdministrableEmployee(employeeId);
        requireValidatedForSupervisorMutation(employee);
        Role role = getAssignableRole(request.getRoleCode());
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        if (userRepository.existsByPhoneNumberAndIdNot(phoneNumber, employee.getId())) {
            throw new BusinessException("This phone number is already registered.");
        }

        String currentRole = employee.getRoles().stream()
                .filter(Role::isActive).map(Role::getCode)
                .filter(EmployeeRolePolicy::isAssignable).findFirst().orElse("");
        boolean changingToPumpAttendant = isPumpAttendantRole(role)
                && !"PUMP_ATTENDANT".equalsIgnoreCase(currentRole);
        validateStationForRole(
                role, request.getStationId(), changingToPumpAttendant);
        if (!currentRole.equals(role.getCode())
                && employeeAssignmentService.countActiveAssignments(
                        employee.getId(), employee.getOrganization().getId()) > 0) {
            throw new ConflictException(
                    "Employee role cannot change while active station assignments exist.");
        }

        employee.setFirstName(normalizeRequiredText(request.getFirstName()));
        employee.setLastName(normalizeRequiredText(request.getLastName()));
        employee.setPhoneNumber(phoneNumber);
        applyIdentityProfile(employee, role, request.getPostName(),
                request.getGender(), request.getBirthPlace(),
                request.getBirthDate(), request.getAddress());
        employee.setRoles(new HashSet<>(List.of(role)));
        assignOperationalCodeIfRequired(employee, role);
        applyDirectValidationStatus(employee, role);

        if (isPumpAttendantRole(role)) {
            employee.setEnabled(true);
            employee.setEmailVerified(false);
            employee.setVerificationCode(null);
            employee.setVerificationCodeExpiration(null);
        }

        User saved = userRepository.save(employee);
        if (isPumpAttendantRole(role) && request.getStationId() != null) {
            User supervisor = authorizationService.getAuthenticatedUser();
            stationAccessService.checkStationAccess(
                    supervisor, request.getStationId());
            employeeAssignmentService.assignForPumpAttendantOnboarding(
                    saved, request.getStationId(), supervisor,
                    "PUMP_ATTENDANT_SUPERVISOR_UPDATE");
        }
        return employeeMapper.toResponse(saved);
    }

    @Override
    public EmployeeResponse updateStatus(UUID employeeId, EmployeeStatusRequest request) {
        User employee = getAdministrableEmployee(employeeId);
        boolean enabled = Boolean.TRUE.equals(request.getEnabled());
        if (enabled) {
            requireValidatedPumpAttendant(employee);
        }
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
        if (isPumpAttendant(employee)) {
            throw new ConflictException(
                    "Pump attendants do not use Web invitations.");
        }
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
    public EmployeeResponse createPumpAttendantDraft(ManagerPumpAttendantRequest request) {
        User manager = getCurrentUserWithRole("MANAGER");
        if (stationAccessService.getAccessibleStationIds(manager).isEmpty()) {
            throw new ForbiddenException("Manager has no accessible station.");
        }
        Organization organization = manager.getOrganization();
        Role role = getAssignableRole("PUMP_ATTENDANT");
        stationAccessService.checkStationAccess(manager, request.getStationId());
        String email = normalizeEmail(request.getEmail());
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        rejectDuplicateIdentity(email, phoneNumber, null);

        User employee = new User();
        employee.setFirstName(normalizeRequiredText(request.getFirstName()));
        employee.setLastName(normalizeRequiredText(request.getLastName()));
        employee.setEmail(email);
        employee.setPhoneNumber(phoneNumber);
        applyIdentityProfile(employee, role, request.getPostName(),
                request.getGender(), request.getBirthPlace(),
                request.getBirthDate(), request.getAddress());
        employee.setOrganization(organization);
        employee.setRoles(new HashSet<>(List.of(role)));
        employee.setPreparedBy(manager);
        employee.setPumpAttendantValidationStatus(PumpAttendantValidationStatus.PREPARATION);
        assignOperationalCodeIfRequired(employee, role);
        employee.setPasswordHash(passwordEncoder.encode(generateUnusableSecret()));
        employee.setEnabled(false);
        employee.setEmailVerified(false);
        employee.setPhoneVerified(false);
        employee.setVerificationCode(null);
        employee.setVerificationCodeExpiration(null);
        employee.setVerificationCodeAttempts(0);
        User saved = userRepository.save(employee);
        employeeAssignmentService.assignForPumpAttendantOnboarding(
                saved, request.getStationId(), manager,
                "PUMP_ATTENDANT_MANAGER_PREPARATION");
        return employeeMapper.toResponse(saved);
    }

    @Override
    public EmployeeResponse updatePumpAttendantDraft(
            UUID employeeId, ManagerPumpAttendantRequest request) {
        User manager = getCurrentUserWithRole("MANAGER");
        User employee = userRepository.lockByIdAndOrganizationId(
                        employeeId, manager.getOrganization().getId())
                .filter(this::isPumpAttendant)
                .filter(value -> value.getPreparedBy() != null
                        && manager.getId().equals(value.getPreparedBy().getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prepared pump attendant was not found."));
        if (employee.getPumpAttendantValidationStatus()
                != PumpAttendantValidationStatus.PREPARATION
                && employee.getPumpAttendantValidationStatus()
                != PumpAttendantValidationStatus.RETURNED_FOR_CORRECTION) {
            throw new ConflictException(
                    "Pump attendant cannot be changed in the current validation status.");
        }
        stationAccessService.checkStationAccess(manager, request.getStationId());
        String email = normalizeEmail(request.getEmail());
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        rejectDuplicateIdentity(email, phoneNumber, employee.getId());
        employee.setFirstName(normalizeRequiredText(request.getFirstName()));
        employee.setLastName(normalizeRequiredText(request.getLastName()));
        employee.setEmail(email);
        employee.setPhoneNumber(phoneNumber);
        Role pumpAttendantRole = getAssignableRole("PUMP_ATTENDANT");
        applyIdentityProfile(employee, pumpAttendantRole, request.getPostName(),
                request.getGender(), request.getBirthPlace(),
                request.getBirthDate(), request.getAddress());
        User saved = userRepository.save(employee);
        employeeAssignmentService.assignForPumpAttendantOnboarding(
                saved, request.getStationId(), manager,
                "PUMP_ATTENDANT_MANAGER_CORRECTION");
        return employeeMapper.toResponse(saved);
    }

    @Override
    public boolean validatePreparedPumpAttendant(User pumpAttendant) {
        if (pumpAttendant == null || !isPumpAttendant(pumpAttendant)
                || pumpAttendant.getPumpAttendantValidationStatus()
                        != PumpAttendantValidationStatus.PENDING_SUPERVISOR_APPROVAL) {
            throw new ConflictException(
                    "Pump attendant is not awaiting supervisor validation.");
        }
        pumpAttendant.setPumpAttendantValidationStatus(
                PumpAttendantValidationStatus.VALIDATED);
        pumpAttendant.setEnabled(true);
        pumpAttendant.setEmailVerified(false);
        pumpAttendant.setVerificationCode(null);
        pumpAttendant.setVerificationCodeExpiration(null);
        pumpAttendant.setVerificationCodeAttempts(0);
        userRepository.save(pumpAttendant);
        return true;
    }

    @Override
    public String issuePosCredential(User pumpAttendant) {
        if (pumpAttendant == null || !isPumpAttendant(pumpAttendant)
                || pumpAttendant.getPumpAttendantValidationStatus()
                        != PumpAttendantValidationStatus.VALIDATED) {
            throw new ConflictException(
                    "Only a validated pump attendant can receive a POS credential.");
        }
        if (pumpAttendant.getPosCredentialHash() != null) {
            throw new ConflictException(
                    "A POS credential has already been issued for this pump attendant.");
        }
        String credential = generatePosCredential();
        pumpAttendant.setPosCredentialHash(passwordEncoder.encode(credential));
        userRepository.save(pumpAttendant);
        return credential;
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

    private void rejectDuplicateIdentity(String email, String phoneNumber, UUID employeeId) {
        boolean duplicateEmail = employeeId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, employeeId);
        if (duplicateEmail) {
            throw new BusinessException("This email address is already registered.");
        }
        boolean duplicatePhone = employeeId == null
                ? userRepository.existsByPhoneNumber(phoneNumber)
                : userRepository.existsByPhoneNumberAndIdNot(phoneNumber, employeeId);
        if (duplicatePhone) {
            throw new BusinessException("This phone number is already registered.");
        }
    }

    private void applyDirectValidationStatus(User employee, Role role) {
        if ("PUMP_ATTENDANT".equalsIgnoreCase(role.getCode())) {
            if (employee.getPumpAttendantValidationStatus() == null) {
                employee.setPumpAttendantValidationStatus(
                        PumpAttendantValidationStatus.VALIDATED);
            }
            return;
        }
        employee.setPumpAttendantValidationStatus(null);
        employee.setPreparedBy(null);
    }

    private void validateStationForRole(
            Role role, UUID stationId, boolean requiredForPumpAttendant) {
        if (isPumpAttendantRole(role)) {
            if (requiredForPumpAttendant && stationId == null) {
                throw new BusinessException(
                        "A station is required for a pump attendant.");
            }
            return;
        }
        if (stationId != null) {
            throw new BusinessException(
                    "A station can only be selected for a pump attendant.");
        }
    }

    private void applyIdentityProfile(
            User employee,
            Role role,
            String postName,
            Gender gender,
            String birthPlace,
            LocalDate birthDate,
            String address
    ) {
        if (!isPumpAttendantRole(role)) {
            employee.setPostName(null);
            employee.setGender(null);
            employee.setBirthPlace(null);
            employee.setBirthDate(null);
            employee.setAddress(null);
            return;
        }
        if (postName == null || postName.isBlank()
                || gender == null
                || birthPlace == null || birthPlace.isBlank()
                || birthDate == null || !birthDate.isBefore(LocalDate.now())
                || address == null || address.isBlank()) {
            throw new BusinessException(
                    "Pump attendant identity information is incomplete.");
        }
        employee.setPostName(normalizeRequiredText(postName));
        employee.setGender(gender);
        employee.setBirthPlace(normalizeRequiredText(birthPlace));
        employee.setBirthDate(birthDate);
        employee.setAddress(normalizeRequiredText(address));
    }

    private boolean isPumpAttendantRole(Role role) {
        return role != null
                && "PUMP_ATTENDANT".equalsIgnoreCase(role.getCode());
    }

    private void requireValidatedForSupervisorMutation(User employee) {
        if (isPumpAttendant(employee)
                && employee.getPumpAttendantValidationStatus()
                        != PumpAttendantValidationStatus.VALIDATED) {
            throw new ConflictException(
                    "Pump attendant must be changed through its validation request.");
        }
    }

    private void requireValidatedPumpAttendant(User employee) {
        if (isPumpAttendant(employee)
                && employee.getPumpAttendantValidationStatus()
                        != PumpAttendantValidationStatus.VALIDATED) {
            throw new ConflictException(
                    "Pump attendant is not validated by a supervisor.");
        }
    }

    private boolean isPumpAttendant(User employee) {
        return employee.getRoles().stream()
                .filter(Role::isActive)
                .map(Role::getCode)
                .anyMatch("PUMP_ATTENDANT"::equalsIgnoreCase);
    }

    private void assignOperationalCodeIfRequired(User employee, Role role) {
        if ("PUMP_ATTENDANT".equalsIgnoreCase(role.getCode()) && employee.getOperationalCode() == null) {
            employee.setOperationalCode("PMP-" + String.format("%06d", pumpAttendantNumbers.nextValue()));
        }
    }

    private User getCurrentUserWithRole(String roleCode) {
        User currentUser = authorizationService.getAuthenticatedUser();
        if (currentUser == null || !currentUser.isEnabled()
                || currentUser.getRoles().stream()
                        .filter(Role::isActive)
                        .map(Role::getCode)
                        .noneMatch(roleCode::equalsIgnoreCase)) {
            throw new ForbiddenException("Role required: " + roleCode + ".");
        }
        if (currentUser.getOrganization() == null
                || currentUser.getOrganization().getId() == null) {
            throw new ForbiddenException("Authenticated user has no organization.");
        }
        return currentUser;
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

    private String generatePosCredential() {
        byte[] credential = new byte[24];
        SECURE_RANDOM.nextBytes(credential);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(credential);
    }
}
