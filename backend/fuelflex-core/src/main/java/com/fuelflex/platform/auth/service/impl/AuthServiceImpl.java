package com.fuelflex.platform.auth.service.impl;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fuelflex.platform.auth.dto.LoginRequest;
import com.fuelflex.platform.auth.dto.LoginResponse;
import com.fuelflex.platform.auth.dto.RegisterRequest;
import com.fuelflex.platform.auth.dto.RegisterResponse;
import com.fuelflex.platform.auth.dto.ResendVerificationCodeRequest;
import com.fuelflex.platform.auth.dto.VerifyEmailRequest;
import com.fuelflex.platform.auth.service.AuthService;
import com.fuelflex.platform.auth.service.OtpService;
import com.fuelflex.platform.common.exception.ApiErrorResponse;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.email.service.EmailService;
import com.fuelflex.platform.permission.entity.Permission;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.security.jwt.JwtService;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final int MAX_VERIFICATION_ATTEMPTS = 5;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_MINUTES = 15;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(
                    "Passwords do not match."
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String phone = normalizePhoneNumber(
                request.getPhone()
        );

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(
                    "This email address is already registered. "
                            + "Use resend code if the account is not verified."
            );
        }

        if (userRepository.existsByPhoneNumber(phone)) {
            throw new BusinessException(
                    "This phone number is already registered."
            );
        }

        Role supervisorRole = roleRepository
                .findByCodeIgnoreCase("SUPERVISOR")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "SUPERVISOR role not found."
                        )
                );

        User user = new User();

        user.setFirstName(
                request.getFirstName().trim()
        );

        user.setLastName(
                request.getLastName().trim()
        );

        user.setEmail(email);
        user.setPhoneNumber(phone);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        user.setVerificationCode(
                otpService.generateCode()
        );

        user.setVerificationCodeExpiration(
                otpService.expirationDate()
        );

        user.setVerificationCodeAttempts(0);

        user.getRoles().add(supervisorRole);

        User savedUser = userRepository.save(user);

        boolean verificationEmailSent = true;

        try {
           emailService.sendVerificationCode(
                user.getEmail(),
                user.getFirstName(),
                user.getVerificationCode()
        );

        } catch (Exception exception) {

        log.error(
                "Unable to resend verification email to {}.",
                user.getEmail(),
                exception
        );

        throw new BusinessException(
                "The verification code was generated, "
                        + "but the email could not be sent. "
                        + "Please check the email configuration."
        );
        }

        String message = verificationEmailSent
                ? "Account created successfully. "
                        + "A verification code has been sent "
                        + "to your email address."
                : "Account created successfully, but the verification email "
                        + "could not be sent. Use resend code from the "
                        + "verification page.";

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhoneNumber())
                .message(message)
                .build();
    }

    @Override
    public void verifyEmail(
            VerifyEmailRequest request
    ) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String verificationCode = request
                .getVerificationCode()
                .trim();

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new BusinessException(
                                "Account not found."
                        )
                );

        if (user.isEmailVerified()) {
            throw new BusinessException(
                    "This email address is already verified."
            );
        }

        if (user.getVerificationCodeAttempts()
                >= MAX_VERIFICATION_ATTEMPTS) {

            throw new BusinessException(
                    "Maximum verification attempts reached. "
                            + "Request a new verification code."
            );
        }

        if (otpService.isExpired(
                user.getVerificationCodeExpiration()
        )) {

            throw new BusinessException(
                    "The verification code has expired. "
                            + "Request a new verification code."
            );
        }

        if (user.getVerificationCode() == null
                || !user.getVerificationCode()
                        .equals(verificationCode)) {

            user.setVerificationCodeAttempts(
                    user.getVerificationCodeAttempts() + 1
            );

            userRepository.save(user);

            int remainingAttempts =
                    MAX_VERIFICATION_ATTEMPTS
                            - user.getVerificationCodeAttempts();

            throw new BusinessException(
                    "Invalid verification code. "
                            + "Remaining attempts: "
                            + remainingAttempts
                            + "."
            );
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        user.setVerificationCodeAttempts(0);

        userRepository.save(user);
    }

    @Override
    public void resendVerificationCode(
            ResendVerificationCodeRequest request
    ) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new BusinessException(
                                "Account not found."
                        )
                );

        if (user.isEmailVerified()) {
            throw new BusinessException(
                    "This email address is already verified."
            );
        }

        user.setVerificationCode(
                otpService.generateCode()
        );

        user.setVerificationCodeExpiration(
                otpService.expirationDate()
        );

        user.setVerificationCodeAttempts(0);

        userRepository.save(user);

        emailService.sendVerificationCode(
                user.getEmail(),
                user.getFirstName(),
                user.getVerificationCode()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new BusinessException(
                                "Invalid email address or password."
                        )
                );

        OffsetDateTime now = OffsetDateTime.now();

        if (user.isAccountLocked()
                && user.getLockedUntil() != null
                && !user.getLockedUntil().isAfter(now)) {

            user.setAccountLocked(false);
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);

            userRepository.save(user);
        }

        if (user.isAccountLocked()) {

            if (user.getLockedUntil() != null) {
                throw new BusinessException(
                        "Account temporarily locked until "
                                + user.getLockedUntil()
                                + "."
                );
            }

            throw new BusinessException(
                    "This account is locked. Contact support."
            );
        }

        if (!user.isEnabled()) {
            throw new BusinessException(
                    "This account is disabled."
            );
        }

        if (user.isAccountExpired()) {
            throw new BusinessException(
                    "This account has expired."
            );
        }

        if (user.isCredentialsExpired()) {
            throw new BusinessException(
                    "Your credentials have expired. "
                            + "Reset your password."
            );
        }

        if (!user.isEmailVerified()) {
            throw new BusinessException(
                    "Verify your email address before signing in."
            );
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {

            int failedAttempts =
                    user.getFailedLoginAttempts() + 1;

            user.setFailedLoginAttempts(
                    failedAttempts
            );

            if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {

                user.setAccountLocked(true);

                user.setLockedUntil(
                        now.plusMinutes(
                                ACCOUNT_LOCK_MINUTES
                        )
                );

                userRepository.save(user);

                throw new BusinessException(
                        "Maximum login attempts reached. "
                                + "Account locked for "
                                + ACCOUNT_LOCK_MINUTES
                                + " minutes."
                );
            }

            userRepository.save(user);

            int remainingAttempts =
                    MAX_LOGIN_ATTEMPTS
                            - failedAttempts;

            throw new BusinessException(
                    "Invalid email address or password. "
                            + "Remaining attempts: "
                            + remainingAttempts
                            + "."
            );
        }

        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);

        userRepository.save(user);

        String accessToken =
                jwtService.generateAccessToken(user);

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles()
                .stream()
                .flatMap(role ->
                        role.getPermissions().stream()
                )
                .filter(Permission::isActive)
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(
                        jwtService
                                .getAccessTokenExpiration()
                                / 1000
                )
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private String normalizePhoneNumber(
            String phone
    ) {

        if (phone == null) {
            return null;
        }

        return phone
                .trim()
                .replaceAll("[\\s()\\-]", "");
    }
        
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneralException(
                Exception ex,
                HttpServletRequest request
        ) {

        ex.printStackTrace();

        ApiErrorResponse response = ApiErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(
                    HttpStatus.INTERNAL_SERVER_ERROR
                            .getReasonPhrase()
            )
            .message(
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "An internal server error occurred."
            )
            .path(request.getRequestURI())
            .build();

    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
}
}