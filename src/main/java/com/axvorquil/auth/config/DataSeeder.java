package com.axvorquil.auth.config;

import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * On startup: if no ADMIN user exists, creates a default admin account.
 * This runs once and is idempotent — safe to leave in production.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_EMAIL    = "admin@axvorquil.com";
    private static final String DEFAULT_PASSWORD = "Admin@1234";

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> "ADMIN".equals(u.getClinicRole()));

        if (!adminExists) {
            // Promote existing user if one exists, otherwise create fresh
            userRepository.findByEmail(DEFAULT_EMAIL).ifPresentOrElse(
                existing -> {
                    existing.setClinicRole("ADMIN");
                    existing.setEnabled(true);
                    existing.setEmailVerified(true);
                    userRepository.save(existing);
                    log.info("✅ Promoted existing user to ADMIN: {}", DEFAULT_EMAIL);
                },
                () -> {
                    User admin = User.builder()
                            .firstName("Admin")
                            .lastName("Axvorquil")
                            .email(DEFAULT_EMAIL)
                            .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                            .clinicRole("ADMIN")
                            .enabled(true)
                            .emailVerified(true)
                            .build();
                    userRepository.save(admin);
                    log.info("✅ Default admin created: {} / {}", DEFAULT_EMAIL, DEFAULT_PASSWORD);
                }
            );
        } else {
            log.info("✅ Admin user already exists — skipping seed.");
        }
    }
}
