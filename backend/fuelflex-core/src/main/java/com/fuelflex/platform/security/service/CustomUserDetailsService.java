package com.fuelflex.platform.security.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));

        boolean pumpAttendant = user.getRoles().stream()
                .filter(role -> role.isActive())
                .map(role -> role.getCode())
                .anyMatch("PUMP_ATTENDANT"::equalsIgnoreCase);
        if (pumpAttendant) {
            throw new UsernameNotFoundException(
                    "This identity is not a Web portal account.");
        }

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().stream()
                .filter(role -> role.isActive())
                .forEach(role -> {
                    authorities.add(new SimpleGrantedAuthority(role.getCode()));
                    role.getPermissions().stream()
                            .filter(permission -> permission.isActive())
                            .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                            .forEach(authorities::add);
                });

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .accountLocked(user.isAccountLocked())
                .accountExpired(user.isAccountExpired())
                .credentialsExpired(user.isCredentialsExpired())
                .authorities(authorities)
                .build();
    }
}
