package com.axvorquil.auth.service;

import com.axvorquil.auth.dto.UserDto;
import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> listAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return toDto(user);
    }

    public UserDto updateRole(String id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setClinicRole(newRole);
        User saved = userRepository.save(user);
        log.info("Role updated for {} → {}", user.getEmail(), newRole);
        return toDto(saved);
    }

    public UserDto toggleStatus(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setEnabled(!user.isEnabled());
        return toDto(userRepository.save(user));
    }

    public void forcePasswordReset(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        log.info("Force password reset triggered for user {}", user.getEmail());
    }

    private UserDto toDto(User u) {
        return UserDto.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .clinicRole(u.getClinicRole() != null ? u.getClinicRole() : "RECEPTIONIST")
                .enabled(u.isEnabled())
                .createdAt(u.getCreatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .lastLoginIp(u.getLastLoginIp())
                .lockedUntil(u.getLockedUntil())
                .failedLoginAttempts(u.getFailedLoginAttempts())
                .build();
    }
}
