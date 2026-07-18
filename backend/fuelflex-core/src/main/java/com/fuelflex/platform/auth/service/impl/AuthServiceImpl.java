package com.fuelflex.platform.auth.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fuelflex.platform.auth.dto.RegisterRequest;
import com.fuelflex.platform.auth.dto.RegisterResponse;
import com.fuelflex.platform.auth.service.AuthService;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.repository.RoleRepository;
import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;
import com.fuelflex.platform.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match.");
        }

        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email already exists.");
        }

        if (userRepository.existsByPhoneNumber(phone)) {
            throw new BusinessException("Phone number already exists.");
        }

        Role supervisorRole = roleRepository
                .findByCodeIgnoreCase("SUPERVISOR")
                .orElseThrow(() ->
                        new IllegalStateException("SUPERVISOR role not found.")
                );

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(supervisorRole);

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhoneNumber())
                .message("Account created successfully.")
                .build();
    }
}